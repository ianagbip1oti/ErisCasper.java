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
package net.dv8tion.jda.core.handle;

import net.dv8tion.jda.core.audio.hooks.ConnectionStatus;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.entities.impl.GuildImpl;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.entities.impl.UserImpl;
import net.dv8tion.jda.core.events.channel.category.CategoryDeleteEvent;
import net.dv8tion.jda.core.events.channel.priv.PrivateChannelDeleteEvent;
import net.dv8tion.jda.core.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.core.events.channel.voice.VoiceChannelDeleteEvent;
import net.dv8tion.jda.core.managers.impl.AudioManagerImpl;
import net.dv8tion.jda.core.requests.WebSocketClient;
import org.json.JSONObject;

public class ChannelDeleteHandler extends SocketHandler {
  public ChannelDeleteHandler(JDAImpl api) {
    super(api);
  }

  @Override
  protected Long handleInternally(JSONObject content) {
    ChannelType type = ChannelType.fromId(content.getInt("type"));

    long guildId = 0;
    if (type.isGuild()) {
      guildId = content.getLong("guild_id");
      if (api.getGuildLock().isLocked(guildId)) return guildId;
    }

    final long channelId = content.getLong("id");

    switch (type) {
      case TEXT:
        {
          GuildImpl guild = (GuildImpl) api.getGuildMap().get(guildId);
          TextChannel channel = api.getTextChannelMap().remove(channelId);
          if (channel == null) {
            //                    api.getEventCache().cache(EventCache.Type.CHANNEL, channelId, ()
            // -> handle(responseNumber, allContent));
            WebSocketClient.LOG.debug(
                "CHANNEL_DELETE attempted to delete a text channel that is not yet cached. JSON: {}",
                content);
            return null;
          }

          guild.getTextChannelsMap().remove(channel.getIdLong());
          api.getEventManager().handle(new TextChannelDeleteEvent(api, responseNumber, channel));
          break;
        }
      case VOICE:
        {
          GuildImpl guild = (GuildImpl) api.getGuildMap().get(guildId);
          VoiceChannel channel = guild.getVoiceChannelsMap().remove(channelId);
          if (channel == null) {
            //                    api.getEventCache().cache(EventCache.Type.CHANNEL, channelId, ()
            // -> handle(responseNumber, allContent));
            WebSocketClient.LOG.debug(
                "CHANNEL_DELETE attempted to delete a voice channel that is not yet cached. JSON: {}",
                content);
            return null;
          }

          // We use this instead of getAudioManager(Guild) so we don't create a new instance.
          // Efficiency!
          AudioManagerImpl manager =
              (AudioManagerImpl) api.getAudioManagerMap().get(guild.getIdLong());
          if (manager != null
              && manager.isConnected()
              && manager.getConnectedChannel().getIdLong() == channel.getIdLong()) {
            manager.closeAudioConnection(ConnectionStatus.DISCONNECTED_CHANNEL_DELETED);
          }
          guild.getVoiceChannelsMap().remove(channel.getIdLong());
          api.getEventManager().handle(new VoiceChannelDeleteEvent(api, responseNumber, channel));
          break;
        }
      case CATEGORY:
        {
          GuildImpl guild = (GuildImpl) api.getGuildMap().get(guildId);
          Category category = api.getCategoryMap().remove(channelId);
          if (category == null) {
            //                    api.getEventCache().cache(EventCache.Type.CHANNEL, channelId, ()
            // -> handle(responseNumber, allContent));
            WebSocketClient.LOG.debug(
                "CHANNEL_DELETE attempted to delete a category channel that is not yet cached. JSON: {}",
                content);
            return null;
          }

          guild.getCategoriesMap().remove(channelId);
          api.getEventManager().handle(new CategoryDeleteEvent(api, responseNumber, category));
          break;
        }
      case PRIVATE:
        {
          PrivateChannel channel = api.getPrivateChannelMap().remove(channelId);

          if (channel == null) channel = api.getFakePrivateChannelMap().remove(channelId);
          if (channel == null) {
            //                    api.getEventCache().cache(EventCache.Type.CHANNEL, channelId, ()
            // -> handle(responseNumber, allContent));
            WebSocketClient.LOG.debug(
                "CHANNEL_DELETE attempted to delete a private channel that is not yet cached. JSON: {}",
                content);
            return null;
          }

          if (channel.getUser().isFake())
            api.getFakeUserMap().remove(channel.getUser().getIdLong());

          ((UserImpl) channel.getUser()).setPrivateChannel(null);

          api.getEventManager().handle(new PrivateChannelDeleteEvent(api, responseNumber, channel));
          break;
        }
      case GROUP:
        // do nothing
        break;
      default:
        throw new IllegalArgumentException(
            "CHANNEL_DELETE provided an unknown channel type. JSON: " + content);
    }
    api.getEventCache().clear(EventCache.Type.CHANNEL, channelId);
    return null;
  }
}
