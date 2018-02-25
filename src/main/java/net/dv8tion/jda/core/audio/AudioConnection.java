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

package net.dv8tion.jda.core.audio;

import gnu.trove.map.TIntLongMap;
import gnu.trove.map.hash.TIntLongHashMap;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.audio.factory.IAudioSendSystem;
import net.dv8tion.jda.core.audio.hooks.ConnectionStatus;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.events.ExceptionEvent;
import net.dv8tion.jda.core.managers.impl.AudioManagerImpl;
import net.dv8tion.jda.core.utils.JDALogger;
import net.dv8tion.jda.core.utils.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.MDC;

public class AudioConnection {
  public static final Logger LOG = JDALogger.getLog(AudioConnection.class);

  private final TIntLongMap ssrcMap = new TIntLongHashMap();
  private final HashMap<User, Queue<Pair<Long, short[]>>> combinedQueue = new HashMap<>();

  private final String threadIdentifier;
  private final AudioWebSocket webSocket;
  private final ConcurrentMap<String, String> contextMap;
  private DatagramSocket udpSocket;
  private VoiceChannel channel;
  private volatile AudioReceiveHandler receiveHandler = null;
  private ScheduledExecutorService combinedAudioExecutor;

  private IAudioSendSystem sendSystem;
  private Thread receiveThread;
  private long queueTimeout;

  private volatile boolean couldReceive = false;

  private final byte[] silenceBytes = new byte[] {(byte) 0xF8, (byte) 0xFF, (byte) 0xFE};

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

  public void setQueueTimeout(long queueTimeout) {
    this.queueTimeout = queueTimeout;
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
      setupReceiveThread();
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

  private synchronized void setupReceiveThread() {
    if (receiveThread == null) {
      receiveThread =
          new Thread(
              AudioManagerImpl.AUDIO_THREADS,
              () -> {
                if (contextMap != null) MDC.setContextMap(contextMap);
                try {
                  udpSocket.setSoTimeout(1000);
                } catch (SocketException e) {
                  LOG.error("Couldn't set SO_TIMEOUT for UDP socket", e);
                }
                while (!udpSocket.isClosed() && !Thread.currentThread().isInterrupted()) {
                  DatagramPacket receivedPacket = new DatagramPacket(new byte[1920], 1920);
                  try {
                    udpSocket.receive(receivedPacket);

                    if (receiveHandler != null
                        && (receiveHandler.canReceiveUser() || receiveHandler.canReceiveCombined())
                        && webSocket.getSecretKey() != null) {
                      if (!couldReceive) {
                        couldReceive = true;
                      }
                      AudioPacket decryptedPacket =
                          AudioPacket.decryptAudioPacket(receivedPacket, webSocket.getSecretKey());
                      if (decryptedPacket == null) continue;

                      int ssrc = decryptedPacket.getSSRC();
                      final long userId = ssrcMap.get(ssrc);
                      if (userId == ssrcMap.getNoEntryValue()) {
                        byte[] audio = decryptedPacket.getEncodedAudio();

                        // If the bytes are silence, then this was caused by a User joining the
                        // voice channel,
                        // and as such, we haven't yet received information to pair the SSRC with
                        // the UserId.
                        if (!Arrays.equals(audio, silenceBytes))
                          LOG.debug("Received audio data with an unknown SSRC id. Ignoring");

                        continue;
                      }

                      User user = getJDA().getUserById(userId);
                      if (user == null) {
                        LOG.warn(
                            "Received audio data with a known SSRC, but the userId associate with the SSRC is unknown to JDA!");
                        continue;
                      }
                      //                              if
                      // (decoder.wasPacketLost(decryptedPacket.getSequence()))
                      //                              {
                      //                                  LOG.debug("Packet(s) missed. Using Opus
                      // packetloss-compensation.");
                      //                                  short[] decodedAudio =
                      // decoder.decodeFromOpus(null);
                      //                                  receiveHandler.handleUserAudio(new
                      // UserAudio(user, decodedAudio));
                      //                              }

                      if (receiveHandler.canReceiveCombined()) {
                        Queue<Pair<Long, short[]>> queue = combinedQueue.get(user);
                        if (queue == null) {
                          queue = new ConcurrentLinkedQueue<>();
                          combinedQueue.put(user, queue);
                        }
                      }
                    } else if (couldReceive) {
                      couldReceive = false;
                    }
                  } catch (SocketTimeoutException e) {
                    // Ignore. We set a low timeout so that we wont block forever so we can properly
                    // shutdown the loop.
                  } catch (SocketException e) {
                    // The socket was closed while we were listening for the next packet.
                    // This is expected. Ignore the exception. The thread will exit during the next
                    // while
                    // iteration because the udpSocket.isClosed() will return true.
                  } catch (Exception e) {
                    LOG.error("There was some random exception while waiting for udp packets", e);
                  }
                }
              });
      receiveThread.setUncaughtExceptionHandler(
          (thread, throwable) -> {
            LOG.error("There was some uncaught exception in the audio receive thread", throwable);
            JDAImpl api = (JDAImpl) getJDA();
            api.getEventManager().handle(new ExceptionEvent(api, throwable, true));
          });
      receiveThread.setDaemon(true);
      receiveThread.setName(threadIdentifier + " Receiving Thread");
      receiveThread.start();
    }

    if (receiveHandler.canReceiveCombined()) {
      setupCombinedExecutor();
    }
  }

  private synchronized void setupCombinedExecutor() {
    if (combinedAudioExecutor == null) {
      combinedAudioExecutor =
          Executors.newSingleThreadScheduledExecutor(
              (task) -> {
                Runnable r =
                    () -> {
                      if (contextMap != null) MDC.setContextMap(contextMap);
                      task.run();
                    };
                final Thread t =
                    new Thread(
                        AudioManagerImpl.AUDIO_THREADS, r, threadIdentifier + " Combined Thread");
                t.setDaemon(true);
                t.setUncaughtExceptionHandler(
                    (thread, throwable) -> {
                      LOG.error(
                          "I have no idea how, but there was an uncaught exception in the combinedAudioExecutor",
                          throwable);
                      JDAImpl api = (JDAImpl) getJDA();
                      api.getEventManager().handle(new ExceptionEvent(api, throwable, true));
                    });
                return t;
              });
      combinedAudioExecutor.scheduleAtFixedRate(
          () -> {
            try {
              List<User> users = new LinkedList<>();
              List<short[]> audioParts = new LinkedList<>();
              if (receiveHandler != null && receiveHandler.canReceiveCombined()) {
                long currentTime = System.currentTimeMillis();
                for (Map.Entry<User, Queue<Pair<Long, short[]>>> entry : combinedQueue.entrySet()) {
                  User user = entry.getKey();
                  Queue<Pair<Long, short[]>> queue = entry.getValue();

                  if (queue.isEmpty()) continue;

                  Pair<Long, short[]> audioData = queue.poll();
                  // Make sure the audio packet is younger than 100ms
                  while (audioData != null && currentTime - audioData.getLeft() > queueTimeout) {
                    audioData = queue.poll();
                  }

                  // If none of the audio packets were younger than 100ms, then there is nothing to
                  // add.
                  if (audioData == null) {
                    continue;
                  }
                  users.add(user);
                  audioParts.add(audioData.getRight());
                }

                if (!audioParts.isEmpty()) {
                  int audioLength = audioParts.get(0).length;
                  short[] mix = new short[1920]; // 960 PCM samples for each channel
                  int sample;
                  for (int i = 0; i < audioLength; i++) {
                    sample = 0;
                    for (short[] audio : audioParts) {
                      sample += audio[i];
                    }
                    if (sample > Short.MAX_VALUE) mix[i] = Short.MAX_VALUE;
                    else if (sample < Short.MIN_VALUE) mix[i] = Short.MIN_VALUE;
                    else mix[i] = (short) sample;
                  }
                  receiveHandler.handleCombinedAudio(new CombinedAudio(users, mix));
                } else {
                  // No audio to mix, provide 20 MS of silence. (960 PCM samples for each channel)
                  receiveHandler.handleCombinedAudio(
                      new CombinedAudio(Collections.emptyList(), new short[1920]));
                }
              }
            } catch (Exception e) {
              LOG.error("There was some unexpected exception in the combinedAudioExecutor!", e);
            }
          },
          0,
          20,
          TimeUnit.MILLISECONDS);
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
