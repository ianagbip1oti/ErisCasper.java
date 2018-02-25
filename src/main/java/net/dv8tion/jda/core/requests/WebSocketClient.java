/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.core.requests;

import com.neovisionaries.ws.client.*;
import net.dv8tion.jda.client.entities.impl.JDAClientImpl;
import net.dv8tion.jda.client.handle.*;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.WebSocketCode;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.events.*;
import net.dv8tion.jda.core.handle.*;
import net.dv8tion.jda.core.managers.impl.PresenceImpl;
import net.dv8tion.jda.core.utils.JDALogger;
import net.dv8tion.jda.core.utils.SessionController;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.MDC;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import java.util.zip.InflaterOutputStream;

public class WebSocketClient extends WebSocketAdapter implements WebSocketListener
{
    public static final Logger LOG = JDALogger.getLog(WebSocketClient.class);
    public static final int DISCORD_GATEWAY_VERSION = 6;
    public static final int IDENTIFY_DELAY = 5;
    public static final int ZLIB_SUFFIX = 0x0000FFFF;

    private static final String INVALIDATE_REASON = "INVALIDATE_SESSION";

    protected final JDAImpl api;
    protected final JDA.ShardInfo shardInfo;
    protected final Map<String, SocketHandler> handlers = new HashMap<>();
    protected final Set<String> cfRays = new HashSet<>();
    protected final Set<String> traces = new HashSet<>();

    public WebSocket socket;
    protected String sessionId = null;
    protected Inflater zlibContext = new Inflater();
    protected ByteArrayOutputStream readBuffer;

    protected volatile Thread keepAliveThread;
    protected boolean initiating;             //cache all events?
    protected final List<JSONObject> cachedEvents = new LinkedList<>();

    protected int reconnectTimeoutS = 2;
    protected long heartbeatStartTime;

    protected final ReentrantLock audioQueueLock = new ReentrantLock();

    protected final LinkedList<String> chunkSyncQueue = new LinkedList<>();
    protected final LinkedList<String> ratelimitQueue = new LinkedList<>();
    protected volatile Thread ratelimitThread = null;
    protected volatile long ratelimitResetTime;
    protected volatile int messagesSent;

    protected volatile boolean shutdown = false;
    protected boolean shouldReconnect = true;
    protected boolean handleIdentifyRateLimit = false;
    protected boolean connected = false;

    protected volatile boolean chunkingAndSyncing = false;
    protected volatile boolean printedRateLimitMessage = false;
    protected boolean sentAuthInfo = false;
    protected boolean firstInit = true;
    protected boolean processingReady = true;

    protected volatile ConnectNode connectNode = null;

    public WebSocketClient(JDAImpl api)
    {
        this.api = api;
        this.shardInfo = api.getShardInfo();
        this.shouldReconnect = api.isAutoReconnect();
        this.connectNode = new StartingNode();
        try
        {
            api.getSessionController().appendSession(connectNode);
        }
        catch (RuntimeException | Error e)
        {
            LOG.error("Failed to append new session to session controller queue. Shutting down!", e);
            this.api.setStatus(JDA.Status.SHUTDOWN);
            this.api.getEventManager().handle(
                new ShutdownEvent(api, OffsetDateTime.now(), 1006));
            if (e instanceof RuntimeException)
                throw (RuntimeException) e;
            else
                throw (Error) e;
        }
    }

    public JDA getJDA()
    {
        return api;
    }

    public Set<String> getCfRays()
    {
        return cfRays;
    }

    public Set<String> getTraces()
    {
        return traces;
    }

    protected void updateTraces(JSONArray arr, String type, int opCode)
    {
        WebSocketClient.LOG.debug("Received a _trace for {} (OP: {}) with {}", type, opCode, arr);
        traces.clear();
        for (Object o : arr)
            traces.add(String.valueOf(o));
    }

    protected void allocateBuffer(byte[] binary) throws IOException
    {
        this.readBuffer = new ByteArrayOutputStream(binary.length * 2);
        this.readBuffer.write(binary);
    }

    protected void extendBuffer(byte[] binary) throws IOException
    {
        if (this.readBuffer != null)
            this.readBuffer.write(binary);
    }

    public void setAutoReconnect(boolean reconnect)
    {
        this.shouldReconnect = reconnect;
    }

    public boolean isConnected()
    {
        return connected;
    }

    public void ready()
    {
        if (initiating)
        {
            initiating = false;
            processingReady = false;
            if (firstInit)
            {
                firstInit = false;
                if (api.getGuilds().size() >= 2000) //Show large warning when connected to >=2000 guilds
                {
                    JDAImpl.LOG.warn(" __      __ _    ___  _  _  ___  _  _   ___  _ ");
                    JDAImpl.LOG.warn(" \\ \\    / //_\\  | _ \\| \\| ||_ _|| \\| | / __|| |");
                    JDAImpl.LOG.warn("  \\ \\/\\/ // _ \\ |   /| .` | | | | .` || (_ ||_|");
                    JDAImpl.LOG.warn("   \\_/\\_//_/ \\_\\|_|_\\|_|\\_||___||_|\\_| \\___|(_)");
                    JDAImpl.LOG.warn("You're running a session with over 2000 connected");
                    JDAImpl.LOG.warn("guilds. You should shard the connection in order");
                    JDAImpl.LOG.warn("to split the load or things like resuming");
                    JDAImpl.LOG.warn("connection might not work as expected.");
                    JDAImpl.LOG.warn("For more info see https://git.io/vrFWP");
                }
                JDAImpl.LOG.info("Finished Loading!");
                api.getEventManager().handle(new ReadyEvent(api, api.getResponseTotal()));
            }
            else
            {
                JDAImpl.LOG.info("Finished (Re)Loading!");
                api.getEventManager().handle(new ReconnectedEvent(api, api.getResponseTotal()));
            }
        }
        else
        {
            JDAImpl.LOG.info("Successfully resumed Session!");
            api.getEventManager().handle(new ResumedEvent(api, api.getResponseTotal()));
        }
        api.setStatus(JDA.Status.CONNECTED);
        LOG.debug("Resending {} cached events...", cachedEvents.size());
        handle(cachedEvents);
        LOG.debug("Sending of cached events finished.");
        cachedEvents.clear();
    }

    public boolean isReady()
    {
        return !initiating;
    }

    public void handle(List<JSONObject> events)
    {
        events.forEach(this::handleEvent);
    }

    public void send(String message)
    {
        ratelimitQueue.addLast(message);
    }

    public void chunkOrSyncRequest(JSONObject request)
    {
        chunkSyncQueue.addLast(request.toString());
    }

    private boolean send(String message, boolean skipQueue)
    {
        if (!connected)
            return false;

        long now = System.currentTimeMillis();

        if (this.ratelimitResetTime <= now)
        {
            this.messagesSent = 0;
            this.ratelimitResetTime = now + 60000;//60 seconds
            this.printedRateLimitMessage = false;
        }

        //Allows 115 messages to be sent before limiting.
        if (this.messagesSent <= 115 || (skipQueue && this.messagesSent <= 119))   //technically we could go to 120, but we aren't going to chance it
        {
            LOG.trace("<- {}", message);
            socket.sendText(message);
            this.messagesSent++;
            return true;
        }
        else
        {
            if (!printedRateLimitMessage)
            {
                LOG.warn("Hit the WebSocket RateLimit! If you see this message a lot then you might need to talk to DV8FromTheWorld.");
                printedRateLimitMessage = true;
            }
            return false;
        }
    }

    private void setupSendingThread()
    {
        ratelimitThread = new Thread(() ->
        {
            if (api.getContextMap() != null)
                MDC.setContextMap(api.getContextMap());
            boolean needRatelimit;
            boolean attemptedToSend;
            while (!Thread.currentThread().isInterrupted())
            {
                try
                {
                    //Make sure that we don't send any packets before sending auth info.
                    if (!sentAuthInfo)
                    {
                        Thread.sleep(500);
                        continue;
                    }
                    attemptedToSend = false;
                    needRatelimit = false;
                    audioQueueLock.lockInterruptibly();

                    String chunkOrSyncRequest = chunkSyncQueue.peekFirst();

                    //if lock isn't needed we already unlock here
                    if (chunkOrSyncRequest != null)
                        maybeUnlock();
                    if (chunkOrSyncRequest != null)
                    {
                        needRatelimit = !send(chunkOrSyncRequest, false);
                        if (!needRatelimit)
                            chunkSyncQueue.removeFirst();

                        attemptedToSend = true;
                    }
                    else
                    {
                        String message = ratelimitQueue.peekFirst();
                        if (message != null)
                        {
                            needRatelimit = !send(message, false);
                            if (!needRatelimit)
                                ratelimitQueue.removeFirst();
                            attemptedToSend = true;
                        }
                    }

                    if (needRatelimit || !attemptedToSend)
                        Thread.sleep(1000);
                }
                catch (InterruptedException ignored)
                {
                    LOG.debug("Main WS send thread interrupted. Most likely JDA is disconnecting the websocket.");
                    break;
                }
                finally
                {
                    // on any exception that might cause this lock to not release
                    maybeUnlock();
                }
            }
        });
        ratelimitThread.setUncaughtExceptionHandler((thread, throwable) ->
        {
            handleCallbackError(socket, throwable);
            setupSendingThread();
        });
        ratelimitThread.setName(api.getIdentifierString() + " MainWS-Sending Thread");
        ratelimitThread.start();
    }

    public void close()
    {
        if (socket != null)
            socket.sendClose(1000);
    }

    public void close(int code)
    {
        if (socket != null)
            socket.sendClose(code);
    }

    public void close(int code, String reason)
    {
        if (socket != null)
            socket.sendClose(code, reason);
    }

    public synchronized void shutdown()
    {
        shutdown = true;
        shouldReconnect = false;
        if (connectNode != null)
            api.getSessionController().removeSession(connectNode);
        close(1000, "Shutting down");
    }

    /*
        ### Start Internal methods ###
     */

    protected synchronized void connect()
    {
        if (api.getStatus() != JDA.Status.ATTEMPTING_TO_RECONNECT)
            api.setStatus(JDA.Status.CONNECTING_TO_WEBSOCKET);
        if (shutdown)
            throw new RejectedExecutionException("JDA is shutdown!");
        initiating = true;

        String url = api.getGatewayUrl() + "?encoding=json&compress=zlib-stream&v=" + DISCORD_GATEWAY_VERSION;

        try
        {
            socket = api.getWebSocketFactory()
                    .createSocket(url)
                    .addHeader("Accept-Encoding", "gzip")
                    .addListener(this);
            socket.connect();
        }
        catch (IOException | WebSocketException e)
        {
            //Completely fail here. We couldn't make the connection.
            throw new IllegalStateException(e);
        }
    }

    protected String getGateway()
    {
        try
        {
            return api.getSessionController().getGateway(api)
                + "?encoding=json&compress=zlib-stream&v=" + DISCORD_GATEWAY_VERSION;
        }
        catch (Exception ex)
        {
            return null;
        }
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers)
    {
        //writing thread
        if (api.getContextMap() != null)
            MDC.setContextMap(api.getContextMap());
        api.setStatus(JDA.Status.IDENTIFYING_SESSION);
        LOG.info("Connected to WebSocket");
        if (headers.containsKey("cf-ray"))
        {
            List<String> values = headers.get("cf-ray");
            if (!values.isEmpty())
            {
                String ray = values.get(0);
                cfRays.add(ray);
                LOG.debug("Received new CF-RAY: {}", ray);
            }
        }
        connected = true;
        reconnectTimeoutS = 2;
        messagesSent = 0;
        ratelimitResetTime = System.currentTimeMillis() + 60000;
        if (sessionId == null)
            sendIdentify();
        else
            sendResume();
    }

    @Override
    public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer)
    {
        sentAuthInfo = false;
        connected = false;
        api.setStatus(JDA.Status.DISCONNECTED);

        CloseCode closeCode = null;
        int rawCloseCode = 1000;
        //When we get 1000 from remote close we will try to resume
        // as apparently discord doesn't understand what "graceful disconnect" means
        boolean isInvalidate = false;

        if (keepAliveThread != null)
        {
            keepAliveThread.interrupt();
            keepAliveThread = null;
        }
        if (serverCloseFrame != null)
        {
            rawCloseCode = serverCloseFrame.getCloseCode();
            closeCode = CloseCode.from(rawCloseCode);
            if (closeCode == CloseCode.RATE_LIMITED)
                LOG.error("WebSocket connection closed due to ratelimit! Sent more than 120 websocket messages in under 60 seconds!");
            else if (closeCode != null)
                LOG.debug("WebSocket connection closed with code {}", closeCode);
            else
                LOG.warn("WebSocket connection closed with unknown meaning for close-code {}", rawCloseCode);
        }
        if (clientCloseFrame != null
            && clientCloseFrame.getCloseCode() == 1000
            && Objects.equals(clientCloseFrame.getCloseReason(), INVALIDATE_REASON))
        {
            //When we close with 1000 we properly dropped our session due to invalidation
            // in that case we can be sure that resume will not work and instead we invalidate and reconnect here
            isInvalidate = true;
        }

        // null is considered -reconnectable- as we do not know the close-code meaning
        boolean closeCodeIsReconnect = closeCode == null || closeCode.isReconnect();
        if (!shouldReconnect || !closeCodeIsReconnect) //we should not reconnect
        {
            if (ratelimitThread != null)
                ratelimitThread.interrupt();

            if (!closeCodeIsReconnect)
            {
                //it is possible that a token can be invalidated due to too many reconnect attempts
                //or that a bot reached a new shard minimum and cannot connect with the current settings
                //if that is the case we have to drop our connection and inform the user with a fatal error message
                LOG.error("WebSocket connection was closed and cannot be recovered due to identification issues\n{}", closeCode);
            }

            api.setStatus(JDA.Status.SHUTDOWN);
            api.getEventManager().handle(new ShutdownEvent(api, OffsetDateTime.now(), rawCloseCode));
        }
        else
        {
            //reset our zlib decompression tools
            zlibContext = new Inflater();
            readBuffer = null;
            if (isInvalidate)
                invalidate(); // 1000 means our session is dropped so we cannot resume
            api.getEventManager().handle(new DisconnectEvent(api, serverCloseFrame, clientCloseFrame, closedByServer, OffsetDateTime.now()));
            try
            {
                if (sessionId == null)
                    queueReconnect();
                else // if resume is possible
                    reconnect();
            }
            catch (InterruptedException e)
            {
                LOG.error("Failed to resume due to interrupted thread", e);
                invalidate();
                queueReconnect();
            }
        }
    }

    protected void queueReconnect()
    {
        if (!handleIdentifyRateLimit)
            LOG.warn("Got disconnected from WebSocket (Internet?!)... Appending session to reconnect queue");
        try
        {
            this.api.setStatus(JDA.Status.RECONNECT_QUEUED);
            this.connectNode = new ReconnectNode();
            this.api.getSessionController().appendSession(connectNode);
        }
        catch (IllegalStateException ex)
        {
            LOG.error("Reconnect queue rejected session. Shutting down...");
            this.api.setStatus(JDA.Status.SHUTDOWN);
            this.api.getEventManager().handle(
                new ShutdownEvent(api, OffsetDateTime.now(), 1006));
        }
    }

    protected void reconnect() throws InterruptedException
    {
        reconnect(false, true);
    }

    /**
     * This method is used to start the reconnect of the JDA instance.
     * It is public for access from SessionReconnectQueue extensions.
     *
     * @param  callFromQueue
     *         whether this was in SessionReconnectQueue and got polled
     * @param  shouldHandleIdentify
     *         whether SessionReconnectQueue already handled an IDENTIFY rate limit for this session
     */
    public void reconnect(boolean callFromQueue, boolean shouldHandleIdentify) throws InterruptedException
    {
        if (callFromQueue && api.getContextMap() != null)
            api.getContextMap().forEach(MDC::put);
        if (shutdown)
        {
            api.setStatus(JDA.Status.SHUTDOWN);
            api.getEventManager().handle(new ShutdownEvent(api, OffsetDateTime.now(), 1000));
            return;
        }
        if (!handleIdentifyRateLimit)
        {
            if (callFromQueue)
                LOG.warn("Queue is attempting to reconnect a shard...{}",
                    JDALogger.getLazyString(() -> shardInfo != null ? " Shard: " + shardInfo.getShardString() : ""));
            else
                LOG.warn("Got disconnected from WebSocket (Internet?!)...");
            LOG.warn("Attempting to reconnect in {}s", reconnectTimeoutS);
        }
        while (shouldReconnect)
        {
            api.setStatus(JDA.Status.WAITING_TO_RECONNECT);
            int delay = IDENTIFY_DELAY;
            if (handleIdentifyRateLimit && shouldHandleIdentify)
                LOG.error("Encountered IDENTIFY (OP {}) Rate Limit! Waiting {} seconds before trying again!", WebSocketCode.IDENTIFY, IDENTIFY_DELAY);
            else
                delay = reconnectTimeoutS;
            Thread.sleep(delay * 1000);
            handleIdentifyRateLimit = false;
            api.setStatus(JDA.Status.ATTEMPTING_TO_RECONNECT);
            LOG.warn("Attempting to reconnect!");
            try
            {
                connect();
                break;
            }
            catch (RejectedExecutionException ex)
            {
                // JDA has already been shutdown so we can stop here
                api.setStatus(JDA.Status.SHUTDOWN);
                api.getEventManager().handle(new ShutdownEvent(api, OffsetDateTime.now(), 1000));
                return;
            }
            catch (RuntimeException ex)
            {
                reconnectTimeoutS = Math.min(reconnectTimeoutS << 1, api.getMaxReconnectDelay());
                LOG.warn("Reconnect failed! Next attempt in {}s", reconnectTimeoutS);
            }
        }
    }

    @Override
    public void onTextMessage(WebSocket websocket, String message)
    {
        //reading thread
        if (api.getContextMap() != null)
            MDC.setContextMap(api.getContextMap());
        JSONObject content = new JSONObject(message);
        try
        {
            int opCode = content.getInt("op");

            if (!content.isNull("s"))
            {
                api.setResponseTotal(content.getInt("s"));
            }

            switch (opCode)
            {
                case WebSocketCode.DISPATCH:
                    handleEvent(content);
                    break;
                case WebSocketCode.HEARTBEAT:
                    LOG.debug("Got Keep-Alive request (OP 1). Sending response...");
                    sendKeepAlive();
                    break;
                case WebSocketCode.RECONNECT:
                    LOG.debug("Got Reconnect request (OP 7). Closing connection now...");
                    close(4000, "OP 7: RECONNECT");
                    break;
                case WebSocketCode.INVALIDATE_SESSION:
                    LOG.debug("Got Invalidate request (OP 9). Invalidating...");
                    sentAuthInfo = false;
                    final boolean isResume = content.getBoolean("d");
                    // When d: true we can wait a bit and then try to resume again
                    //sending 4000 to not drop session
                    int closeCode = isResume ? 4000 : 1000;
                    if (isResume)
                        LOG.debug("Session can be recovered... Closing and sending new RESUME request");
                    else
                        invalidate();

                    close(closeCode, INVALIDATE_REASON);
                    break;
                case WebSocketCode.HELLO:
                    LOG.debug("Got HELLO packet (OP 10). Initializing keep-alive.");
                    final JSONObject data = content.getJSONObject("d");
                    setupKeepAlive(data.getLong("heartbeat_interval"));
                    if (!data.isNull("_trace"))
                        updateTraces(data.getJSONArray("_trace"), "HELLO", WebSocketCode.HELLO);
                    break;
                case WebSocketCode.HEARTBEAT_ACK:
                    LOG.trace("Got Heartbeat Ack (OP 11).");
                    api.setPing(System.currentTimeMillis() - heartbeatStartTime);
                    break;
                default:
                    LOG.debug("Got unknown op-code: {} with content: {}", opCode, message);
            }
        }
        catch (Exception ex)
        {
            LOG.error("Encountered exception on lifecycle level\nJSON: {}", content, ex);
            api.getEventManager().handle(new ExceptionEvent(api, ex, true));
        }
    }

    protected void setupKeepAlive(long timeout)
    {
        keepAliveThread = new Thread(() ->
        {
            if (api.getContextMap() != null)
                MDC.setContextMap(api.getContextMap());
            while (connected)
            {
                try
                {
                    sendKeepAlive();

                    //Sleep for heartbeat interval
                    Thread.sleep(timeout);
                }
                catch (InterruptedException ex)
                {
                    //connection got cut... terminating keepAliveThread
                    break;
                }
            }
        });
        keepAliveThread.setUncaughtExceptionHandler((thread, throwable) ->
        {
            handleCallbackError(socket, throwable);
            setupKeepAlive(timeout);
        });
        keepAliveThread.setName(api.getIdentifierString() + " MainWS-KeepAlive Thread");
        keepAliveThread.setPriority(Thread.MAX_PRIORITY);
        keepAliveThread.setDaemon(true);
        keepAliveThread.start();
    }

    protected void sendKeepAlive()
    {
        String keepAlivePacket =
                new JSONObject()
                    .put("op", WebSocketCode.HEARTBEAT)
                    .put("d", api.getResponseTotal()
                ).toString();

        if (!send(keepAlivePacket, true))
            ratelimitQueue.addLast(keepAlivePacket);
        heartbeatStartTime = System.currentTimeMillis();
    }

    protected void sendIdentify()
    {
        LOG.debug("Sending Identify-packet...");
        PresenceImpl presenceObj = (PresenceImpl) api.getPresence();
        JSONObject connectionProperties = new JSONObject()
            .put("$os", System.getProperty("os.name"))
            .put("$browser", "JDA")
            .put("$device", "JDA")
            .put("$referring_domain", "")
            .put("$referrer", "");
        JSONObject payload = new JSONObject()
            .put("presence", presenceObj.getFullPresence())
            .put("token", getToken())
            .put("properties", connectionProperties)
            .put("v", DISCORD_GATEWAY_VERSION)
            .put("large_threshold", 250)
            //Used to make the READY event be given
            // as compressed binary data when over a certain size. TY @ShadowLordAlpha
            .put("compress", true);
        JSONObject identify = new JSONObject()
                .put("op", WebSocketCode.IDENTIFY)
                .put("d", payload);
        if (shardInfo != null)
        {
            payload
                .put("shard", new JSONArray()
                    .put(shardInfo.getShardId())
                    .put(shardInfo.getShardTotal()));
        }
        send(identify.toString(), true);
        handleIdentifyRateLimit = true;
        sentAuthInfo = true;
        api.setStatus(JDA.Status.AWAITING_LOGIN_CONFIRMATION);
    }

    protected void sendResume()
    {
        LOG.debug("Sending Resume-packet...");
        JSONObject resume = new JSONObject()
            .put("op", WebSocketCode.RESUME)
            .put("d", new JSONObject()
                .put("session_id", sessionId)
                .put("token", getToken())
                .put("seq", api.getResponseTotal()));
        send(resume.toString(), true);
        //sentAuthInfo = true; set on RESUMED response as this could fail
        api.setStatus(JDA.Status.AWAITING_LOGIN_CONFIRMATION);
    }

    protected void invalidate()
    {
        sessionId = null;
        chunkingAndSyncing = false;
        sentAuthInfo = false;

        chunkSyncQueue.clear();
        api.getTextChannelMap().clear();
        api.getCategoryMap().clear();
        api.getGuildMap().clear();
        api.getUserMap().clear();
        api.getPrivateChannelMap().clear();
        api.getFakeUserMap().clear();
        api.getFakePrivateChannelMap().clear();
        api.getEntityBuilder().clearCache();
        api.getEventCache().clear();
        api.getGuildLock().clear();
        this.<ReadyHandler>getHandler("READY").clearCache();
        this.<GuildMembersChunkHandler>getHandler("GUILD_MEMBERS_CHUNK").clearCache();

        if (api.getAccountType() == AccountType.CLIENT)
        {
            JDAClientImpl client = api.asClient();

            client.getRelationshipMap().clear();
            client.getGroupMap().clear();
            client.getCallUserMap().clear();
        }
    }
    
    protected String getToken()
    {
        if (api.getAccountType() == AccountType.BOT)
            return api.getToken().substring("Bot ".length());
        return api.getToken();
    }

    private List<JSONObject> convertPresencesReplace(long responseTotal, JSONArray array)
    {
        // Needs special handling due to content of "d" being an array
        List<JSONObject> output = new LinkedList<>();
        for (int i = 0; i < array.length(); i++)
        {
            JSONObject presence = array.getJSONObject(i);
            final JSONObject obj = new JSONObject();
            obj.put("jda-field", "This was constructed from a PRESENCES_REPLACE payload")
               .put("op", WebSocketCode.DISPATCH)
               .put("s", responseTotal)
               .put("d", presence)
               .put("t", "PRESENCE_UPDATE");
            output.add(obj);
        }
        return output;
    }

    protected void handleEvent(JSONObject raw)
    {
        String type = raw.getString("t");
        long responseTotal = api.getResponseTotal();

        if (type.equals("GUILD_MEMBER_ADD"))
            ((GuildMembersChunkHandler) getHandler("GUILD_MEMBERS_CHUNK")).modifyExpectedGuildMember(raw.getJSONObject("d").getLong("guild_id"), 1);
        if (type.equals("GUILD_MEMBER_REMOVE"))
            ((GuildMembersChunkHandler) getHandler("GUILD_MEMBERS_CHUNK")).modifyExpectedGuildMember(raw.getJSONObject("d").getLong("guild_id"), -1);

        boolean isJSON = raw.opt("d") instanceof JSONObject;
        //If initiating, only allows READY, RESUMED, GUILD_MEMBERS_CHUNK, GUILD_SYNC, and GUILD_CREATE through.
        // If we are currently chunking, we don't allow GUILD_CREATE through anymore.
        if (initiating &&  !(type.equals("READY")
                || type.equals("GUILD_MEMBERS_CHUNK")
                || type.equals("RESUMED")
                || type.equals("GUILD_SYNC")
                || (!chunkingAndSyncing && type.equals("GUILD_CREATE"))))
        {
            if (!isJSON)
            {
                if (type.equals("PRESENCES_REPLACE"))
                {
                    List<JSONObject> converted = convertPresencesReplace(responseTotal, raw.getJSONArray("d"));
                    LOG.debug("Caching PRESENCES_REPLACE event during init as PRESENCE_UPDATE dispatches!");
                    cachedEvents.addAll(converted);
                }
                else
                {
                    LOG.debug("Received event with unhandled body type during init JSON: {}", raw);
                }
                return;
            }
            //If we are currently GuildStreaming, and we get a GUILD_DELETE informing us that a Guild is unavailable
            // convert it to a GUILD_CREATE for handling.
            JSONObject content = raw.getJSONObject("d");
            if (!chunkingAndSyncing && type.equals("GUILD_DELETE") && content.has("unavailable") && content.getBoolean("unavailable"))
            {
                type = "GUILD_CREATE";
                raw.put("t", "GUILD_CREATE")
                   .put("jda-field","This event was originally a GUILD_DELETE but was converted to GUILD_CREATE for WS init Guild streaming");
            }
            else
            {
                LOG.debug("Caching {} event during init!", type);
                cachedEvents.add(raw);
                return;
            }
        }

        if (!isJSON)
        {
            // Needs special handling due to content of "d" being an array
            if (type.equals("PRESENCES_REPLACE"))
            {
                final JSONArray payload = raw.getJSONArray("d");
                final List<JSONObject> converted = convertPresencesReplace(responseTotal, payload);
                final PresenceUpdateHandler handler = getHandler("PRESENCE_UPDATE");
                LOG.trace(String.format("%s -> %s", type, payload.toString()));
                for (JSONObject o : converted)
                    handler.handle(responseTotal, o);
            }
            else
            {
                LOG.debug("Received event with unhandled body type JSON: {}", raw);
            }
            return;
        }

        JSONObject content = raw.getJSONObject("d");
        LOG.trace("{} -> {}", type, content);

        try
        {
            switch (type)
            {
                //INIT types
                case "READY":
                    api.setStatus(JDA.Status.LOADING_SUBSYSTEMS);
                    processingReady = true;
                    handleIdentifyRateLimit = false;
                    sessionId = content.getString("session_id");
                    if (!content.isNull("_trace"))
                        updateTraces(content.getJSONArray("_trace"), "READY", WebSocketCode.DISPATCH);
                    handlers.get("READY").handle(responseTotal, raw);
                    break;
                case "RESUMED":
                    sentAuthInfo = true;
                    if (!processingReady)
                    {
                        api.setStatus(JDA.Status.LOADING_SUBSYSTEMS);
                        initiating = false;
                        ready();
                    }
                    if (!content.isNull("_trace"))
                        updateTraces(content.getJSONArray("_trace"), "RESUMED", WebSocketCode.DISPATCH);
                    break;
                default:
                    SocketHandler handler = handlers.get(type);
                    if (handler != null)
                        handler.handle(responseTotal, raw);
                    else
                        LOG.debug("Unrecognized event:\n{}", raw);
            }
        }
        catch (JSONException ex)
        {
            LOG.warn("Got an unexpected Json-parse error. Please redirect following message to the devs:\n\t{}\n\t{} -> {}",
                ex.getMessage(), type, content, ex);
        }
        catch (Exception ex)
        {
            LOG.error("Got an unexpected error. Please redirect following message to the devs:\n\t{} -> {}", type, content, ex);
        }
    }

    protected boolean onBufferMessage(byte[] binary) throws IOException
    {
        if (binary.length >= 4 && getInt(binary, binary.length - 4) == ZLIB_SUFFIX)
        {
            extendBuffer(binary);
            return true;
        }

        if (readBuffer != null)
            extendBuffer(binary);
        else
            allocateBuffer(binary);

        return false;
    }

    @Override
    public void onBinaryMessage(WebSocket websocket, byte[] binary) throws IOException, DataFormatException
    {
        if (!onBufferMessage(binary))
            return;
        //Thanks to ShadowLordAlpha and Shredder121 for code and debugging.
        //Get the compressed message and inflate it
        final int size = readBuffer != null ? readBuffer.size() : binary.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream(size * 2);
        try (InflaterOutputStream decompressor = new InflaterOutputStream(out, zlibContext))
        {
            if (readBuffer != null)
                readBuffer.writeTo(decompressor);
            else
                decompressor.write(binary);
            // send the inflated message to the TextMessage method
            onTextMessage(websocket, out.toString("UTF-8"));
        }
        catch (IOException e)
        {
            throw (DataFormatException) new DataFormatException("Malformed").initCause(e);
        }
        finally
        {
            readBuffer = null;
        }
    }

    private static int getInt(byte[] sink, int offset)
    {
        return sink[offset + 3] & 0xFF
            | (sink[offset + 2] & 0xFF) << 8
            | (sink[offset + 1] & 0xFF) << 16
            | (sink[offset    ] & 0xFF) << 24;
    }

    @Override
    public void onUnexpectedError(WebSocket websocket, WebSocketException cause) throws Exception
    {
        handleCallbackError(websocket, cause);
    }

    @Override
    public void handleCallbackError(WebSocket websocket, Throwable cause)
    {
        LOG.error("There was an error in the WebSocket connection", cause);
        api.getEventManager().handle(new ExceptionEvent(api, cause, true));
    }

    @Override
    public void onThreadCreated(WebSocket websocket, ThreadType threadType, Thread thread) throws Exception
    {
        String identifier = api.getIdentifierString();
        switch (threadType)
        {
            case CONNECT_THREAD:
                thread.setName(identifier + " MainWS-ConnectThread");
                break;
            case FINISH_THREAD:
                thread.setName(identifier + " MainWS-FinishThread");
                break;
            case READING_THREAD:
                thread.setName(identifier + " MainWS-ReadThread");
                break;
            case WRITING_THREAD:
                thread.setName(identifier + " MainWS-WriteThread");
                break;
            default:
                thread.setName(identifier + " MainWS-" + threadType);
        }
    }

    public void setChunkingAndSyncing(boolean active)
    {
        chunkingAndSyncing = active;
    }

    private void maybeUnlock()
    {
        if (audioQueueLock.isHeldByCurrentThread())
            audioQueueLock.unlock();
    }

    public Map<String, SocketHandler> getHandlers()
    {
        return handlers;
    }

    public <T> T getHandler(String type)
    {
        return (T) handlers.get(type);
    }

    private void setupHandlers()
    {
        final SocketHandler.NOPHandler nopHandler = new SocketHandler.NOPHandler(api);
        handlers.put("CHANNEL_CREATE",              new ChannelCreateHandler(api));
        handlers.put("CHANNEL_DELETE",              new ChannelDeleteHandler(api));
        handlers.put("CHANNEL_UPDATE",              new ChannelUpdateHandler(api));
        handlers.put("GUILD_BAN_ADD",               new GuildBanHandler(api, true));
        handlers.put("GUILD_BAN_REMOVE",            new GuildBanHandler(api, false));
        handlers.put("GUILD_CREATE",                new GuildCreateHandler(api));
        handlers.put("GUILD_DELETE",                new GuildDeleteHandler(api));
        handlers.put("GUILD_EMOJIS_UPDATE",         new GuildEmojisUpdateHandler(api));
        handlers.put("GUILD_MEMBER_ADD",            new GuildMemberAddHandler(api));
        handlers.put("GUILD_MEMBER_REMOVE",         new GuildMemberRemoveHandler(api));
        handlers.put("GUILD_MEMBER_UPDATE",         new GuildMemberUpdateHandler(api));
        handlers.put("GUILD_MEMBERS_CHUNK",         new GuildMembersChunkHandler(api));
        handlers.put("GUILD_ROLE_CREATE",           new GuildRoleCreateHandler(api));
        handlers.put("GUILD_ROLE_DELETE",           new GuildRoleDeleteHandler(api));
        handlers.put("GUILD_ROLE_UPDATE",           new GuildRoleUpdateHandler(api));
        handlers.put("GUILD_SYNC",                  new GuildSyncHandler(api));
        handlers.put("GUILD_UPDATE",                new GuildUpdateHandler(api));
        handlers.put("MESSAGE_CREATE",              new MessageCreateHandler(api));
        handlers.put("MESSAGE_DELETE",              new MessageDeleteHandler(api));
        handlers.put("MESSAGE_DELETE_BULK",         new MessageBulkDeleteHandler(api));
        handlers.put("MESSAGE_REACTION_ADD",        new MessageReactionHandler(api, true));
        handlers.put("MESSAGE_REACTION_REMOVE",     new MessageReactionHandler(api, false));
        handlers.put("MESSAGE_REACTION_REMOVE_ALL", new MessageReactionBulkRemoveHandler(api));
        handlers.put("MESSAGE_UPDATE",              new MessageUpdateHandler(api));
        handlers.put("PRESENCE_UPDATE",             new PresenceUpdateHandler(api));
        handlers.put("READY",                       new ReadyHandler(api));
        handlers.put("TYPING_START",                new TypingStartHandler(api));
        handlers.put("USER_UPDATE",                 new UserUpdateHandler(api));

        // Unused events
        handlers.put("CHANNEL_PINS_ACK",          nopHandler);
        handlers.put("CHANNEL_PINS_UPDATE",       nopHandler);
        handlers.put("GUILD_INTEGRATIONS_UPDATE", nopHandler);
        handlers.put("PRESENCES_REPLACE",         nopHandler);
        handlers.put("WEBHOOKS_UPDATE",           nopHandler);

        if (api.getAccountType() == AccountType.CLIENT)
        {
            handlers.put("CALL_CREATE",              new CallCreateHandler(api));
            handlers.put("CALL_DELETE",              new CallDeleteHandler(api));
            handlers.put("CALL_UPDATE",              new CallUpdateHandler(api));
            handlers.put("CHANNEL_RECIPIENT_ADD",    new ChannelRecipientAddHandler(api));
            handlers.put("CHANNEL_RECIPIENT_REMOVE", new ChannelRecipientRemoveHandler(api));
            handlers.put("RELATIONSHIP_ADD",         new RelationshipAddHandler(api));
            handlers.put("RELATIONSHIP_REMOVE",      new RelationshipRemoveHandler(api));

            // Unused client events
            handlers.put("MESSAGE_ACK", nopHandler);
        }
    }

    protected abstract class ConnectNode implements SessionController.SessionConnectNode
    {
        @Override
        public JDA getJDA()
        {
            return api;
        }

        @Override
        public JDA.ShardInfo getShardInfo()
        {
            return api.getShardInfo();
        }
    }

    protected class StartingNode extends ConnectNode
    {
        @Override
        public boolean isReconnect()
        {
            return false;
        }

        @Override
        public void run(boolean isLast) throws InterruptedException
        {
            if (shutdown)
                return;
            setupHandlers();
            setupSendingThread();
            connect();
            while (!isLast && api.getStatus().ordinal() < JDA.Status.AWAITING_LOGIN_CONFIRMATION.ordinal())
                Thread.sleep(50);
        }
    }

    protected class ReconnectNode extends ConnectNode
    {
        @Override
        public boolean isReconnect()
        {
            return true;
        }

        @Override
        public void run(boolean isLast) throws InterruptedException
        {
            if (shutdown)
                return;
            reconnect(true, !isLast);
            while (!isLast && api.getStatus().ordinal() < JDA.Status.AWAITING_LOGIN_CONFIRMATION.ordinal())
                Thread.sleep(50);
        }
    }
}
