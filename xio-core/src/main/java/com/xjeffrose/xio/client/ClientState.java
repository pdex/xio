package com.xjeffrose.xio.client;

import io.netty.channel.ChannelHandler;
import io.netty.handler.ssl.SslContext;
import java.net.InetSocketAddress;
import java.util.function.Supplier;

public class ClientState {

  public final ClientConfig config;
  public final InetSocketAddress address;
  public final ChannelHandler handler;
  public final SslContext sslContext;
  public final Supplier<ChannelHandler> applicationProtocol;
  public final Supplier<ChannelHandler> tracingHandler;

  public ClientState(
      ClientConfig config,
      InetSocketAddress address,
      ChannelHandler handler,
      SslContext sslContext,
      Supplier<ChannelHandler> applicationProtocol,
      Supplier<ChannelHandler> tracingHandler) {
    this.config = config;
    this.address = address;
    this.handler = handler;
    this.sslContext = sslContext;
    this.applicationProtocol = applicationProtocol;
    this.tracingHandler = tracingHandler;
  }
}
