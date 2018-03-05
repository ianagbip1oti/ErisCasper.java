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

import java.util.Objects;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.entities.impl.SelfUserImpl;
import net.dv8tion.jda.core.events.self.*;
import org.json.JSONObject;

public class UserUpdateHandler extends SocketHandler {
  public UserUpdateHandler(JDAImpl api) {
    super(api);
  }

  @Override
  protected Long handleInternally(JSONObject content) {
    SelfUserImpl self = (SelfUserImpl) api.getSelfUser();

    String name = content.getString("username");
    String discriminator = content.getString("discriminator");
    String avatarId = content.optString("avatar", null);
    Boolean verified = content.has("verified") ? content.getBoolean("verified") : null;
    Boolean mfaEnabled = content.has("mfa_enabled") ? content.getBoolean("mfa_enabled") : null;

    if (!Objects.equals(name, self.getName())
        || !Objects.equals(discriminator, self.getDiscriminator())) {
      String oldName = self.getName();
      String oldDiscriminator = self.getDiscriminator();
      self.setName(name);
      self.setDiscriminator(discriminator);
      api.getEventManager()
          .handle(
              new SelfUpdateNameEvent(
                  api, responseNumber,
                  oldName, oldDiscriminator));
    }

    if (!Objects.equals(avatarId, self.getAvatarId())) {
      String oldAvatarId = self.getAvatarId();
      self.setAvatarId(avatarId);
      api.getEventManager().handle(new SelfUpdateAvatarEvent(api, responseNumber, oldAvatarId));
    }

    if (verified != null && verified != self.isVerified()) {
      boolean wasVerified = self.isVerified();
      self.setVerified(verified);
      api.getEventManager().handle(new SelfUpdateVerifiedEvent(api, responseNumber, wasVerified));
    }

    if (mfaEnabled != null && mfaEnabled != self.isMfaEnabled()) {
      boolean wasMfaEnabled = self.isMfaEnabled();
      self.setMfaEnabled(mfaEnabled);
      api.getEventManager().handle(new SelfUpdateMFAEvent(api, responseNumber, wasMfaEnabled));
    }
    return null;
  }
}
