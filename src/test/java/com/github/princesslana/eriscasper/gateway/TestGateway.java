package com.github.princesslana.eriscasper.gateway;

import okhttp3.OkHttpClient;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;

public class TestGateway {

  @Mock private OkHttpClient mockClient;

  @Mock private Payloads mockPayloads;

  private Gateway subject;

  @BeforeMethod
  public void subject() {
    subject = new Gateway(mockClient, mockPayloads);
  }
}
