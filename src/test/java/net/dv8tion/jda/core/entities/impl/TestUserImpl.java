package net.dv8tion.jda.core.entities.impl;

import org.assertj.core.api.Assertions;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestUserImpl {

  private UserImpl user;

  @BeforeMethod
  public void subject() {
    user = new UserImpl(0, null);
  }

  @Test
  public void getAvatarUrl_whenNoAvatarId_shouldReturnDefaultAvatarUrl() {
    Assertions.assertThat(user.getAvatarUrl())
        .isEqualTo("https://discordapp.com/assets/6debd47ed13483642cf09e832ed0bc1b.png");
  }

  @Test
  public void getAvatarUrl_whenIsAvatarId_shouldReturnAvatarUrl() {
    user.setAvatarId("test_avatar_id");

    Assertions.assertThat(user.getAvatarUrl())
        .isEqualTo("https://cdn.discordapp.com/avatars/0/test_avatar_id.png");
  }

  @Test
  public void getAvatarUrl_whenAvatarIdForGif_shouldReturnGifAvatarUrl() {
    user.setAvatarId("a_test_avatar_id");

    Assertions.assertThat(user.getAvatarUrl())
        .isEqualTo("https://cdn.discordapp.com/avatars/0/a_test_avatar_id.gif");
  }
}
