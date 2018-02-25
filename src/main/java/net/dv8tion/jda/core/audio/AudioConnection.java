/*
 *     Copyright 2015-2018 Austin Keener & Michael Ritter & Florian Spieß
 *     Copyright 2018-2018 "Princess" Lana Samson
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
package net.dv8tion.jda.core.audio;

import gnu.trove.map.TIntLongMap;
import gnu.trove.map.hash.TIntLongHashMap;
import java.net.DatagramSocket;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.audio.factory.IAudioSendSystem;
import net.dv8tion.jda.core.audio.hooks.ConnectionStatus;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.events.ExceptionEvent;
import net.dv8tion.jda.core.managers.impl.AudioManagerImpl;
import net.dv8tion.jda.core.utils.JDALogger;
import org.slf4j.Logger;
import org.slf4j.MDC;

public class AudioConnection {
  public static final Logger LOG = JDALogger.getLog(AudioConnection.class);

  private final TIntLongMap ssrcMap = new TIntLongHashMap();

  private final String threadIdentifier;
  private final AudioWebSocket webSocket;
  private final ConcurrentMap<String, String> contextMap;
  private DatagramSocket udpSocket;
  private VoiceChannel channel;
  private volatile AudioReceiveHandler receiveHandler = null;
  private ScheduledExecutorService combinedAudioExecutor;

  private IAudioSendSystem sendSystem;
  private Thread receiveThread;

  public AudioConnection(AudioWebSocket webSocket, VoiceChannel channel) {
    this.channel = channel;
    this.webSocket = webSocket;
    this.webSocket.audioConnection = this;

    final JDAImpl api = (JDAImpl) channel.getJDA();
    this.threadIdentifier =
        api.getIdentifierString() + " AudioConnection Guild: " + channel.getGuild().getId();
    this.contextMap = api.getContextMap();
  }

  public void ready() {
    Thread readyThread =
        new Thread(
            AudioManagerImpl.AUDIO_THREADS,
            () -> {
              if (contextMap != null) MDC.setContextMap(contextMap);
              final long timeout = getGuild().getAudioManager().getConnectTimeout();

              final long started = System.currentTimeMillis();
              boolean connectionTimeout = false;
              while (!webSocket.isReady()) {
                if (timeout > 0 && System.currentTimeMillis() - started > timeout) {
                  connectionTimeout = true;
                  break;
                }

                try {
                  Thread.sleep(10);
                } catch (InterruptedException e) {
                  LOG.error("AudioConnection ready thread got interrupted while sleeping", e);
                  Thread.currentThread().interrupt();
                }
              }
              if (!connectionTimeout) {
                this.udpSocket = webSocket.getUdpSocket();

                setupReceiveSystem();
              } else {
                webSocket.close(ConnectionStatus.ERROR_CONNECTION_TIMEOUT);
              }
            });
    readyThread.setUncaughtExceptionHandler(
        (thread, throwable) -> {
          LOG.error("Uncaught exception in Audio ready-thread", throwable);
          JDAImpl api = (JDAImpl) getJDA();
          api.getEventManager().handle(new ExceptionEvent(api, throwable, true));
        });
    readyThread.setDaemon(true);
    readyThread.setName(threadIdentifier + " Ready Thread");
    readyThread.start();
  }

  public void setReceivingHandler(AudioReceiveHandler handler) {
    this.receiveHandler = handler;
    setupReceiveSystem();
  }

  public VoiceChannel getChannel() {
    return channel;
  }

  public void setChannel(VoiceChannel channel) {
    this.channel = channel;
  }

  public JDA getJDA() {
    return channel.getJDA();
  }

  public Guild getGuild() {
    return channel.getGuild();
  }

  public void removeUserSSRC(long userId) {
    final AtomicInteger ssrcRef = new AtomicInteger(0);
    final boolean modified =
        ssrcMap.retainEntries(
            (ssrc, id) -> {
              final boolean isEntry = id == userId;
              if (isEntry) ssrcRef.set(ssrc);
              // if isEntry == true we don't want to retain it
              return !isEntry;
            });
    if (!modified) return;
  }

  protected void updateUserSSRC(int ssrc, long userId) {
    if (ssrcMap.containsKey(ssrc)) {
      long previousId = ssrcMap.get(ssrc);
      if (previousId != userId) {
        // Different User already existed with this ssrc. What should we do? Just replace? Probably
        // should nuke the old opusDecoder.
        // Log for now and see if any user report the error.
        LOG.error(
            "Yeah.. So.. JDA received a UserSSRC update for an ssrc that already had a User set. Inform DV8FromTheWorld.\nChannelId: {} SSRC: {} oldId: {} newId: {}",
            channel.getId(),
            ssrc,
            previousId,
            userId);
      }
    } else {
      ssrcMap.put(ssrc, userId);
    }
  }

  public void close(ConnectionStatus closeStatus) {
    shutdown();
    webSocket.close(closeStatus);
  }

  public synchronized void shutdown() {
    //        setSpeaking(false);
    if (sendSystem != null) {
      sendSystem.shutdown();
      sendSystem = null;
    }
    if (receiveThread != null) {
      receiveThread.interrupt();
      receiveThread = null;
    }
    if (combinedAudioExecutor != null) {
      combinedAudioExecutor.shutdownNow();
      combinedAudioExecutor = null;
    }
  }

  private synchronized void setupReceiveSystem() {
    if (udpSocket != null
        && !udpSocket.isClosed()
        && receiveHandler != null
        && receiveThread == null) {

    } else if (receiveHandler == null && receiveThread != null) {
      receiveThread.interrupt();
      receiveThread = null;

      if (combinedAudioExecutor != null) {
        combinedAudioExecutor.shutdownNow();
        combinedAudioExecutor = null;
      }
    } else if (receiveHandler != null
        && !receiveHandler.canReceiveCombined()
        && combinedAudioExecutor != null) {
      combinedAudioExecutor.shutdownNow();
      combinedAudioExecutor = null;
    }
  }

  public AudioWebSocket getWebSocket() {
    return webSocket;
  }

  @Override
  protected void finalize() throws Throwable {
    shutdown();
  }
}
