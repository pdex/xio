package com.xjeffrose.xio.http;

import com.google.common.hash.Funnels;
import com.xjeffrose.xio.client.ClientConfig;
import com.xjeffrose.xio.core.Constants;
import com.xjeffrose.xio.core.SocketAddressHelper;
import com.xjeffrose.xio.server.RendezvousHash;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.internal.PlatformDependent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.val;

public class PersistentProxyHandler extends ProxyHandler {
  private final RendezvousHash<CharSequence> persistentProxyHasher;
  private final Map<String, ClientConfig> clientConfigMap =
      PlatformDependent.newConcurrentHashMap();

  public PersistentProxyHandler(
      ClientFactory factory, ProxyRouteConfig config, SocketAddressHelper addressHelper) {
    super(factory, config, addressHelper);
    val clientConfigs = config.clientConfigs();
    persistentProxyHasher = buildHasher(clientConfigMap, clientConfigs.size());
  }

  private RendezvousHash<CharSequence> buildHasher(
      Map<String, ClientConfig> configMap, int configSize) {
    List<String> randomIdPool = new ArrayList<>();
    val clientConfigs = config.clientConfigs();

    // map each client config to a randomly-generated ID
    for (int i = 0; i < configSize; i++) {
      String id = UUID.randomUUID().toString();
      randomIdPool.add(id);
      configMap.put(id, clientConfigs.get(i));
    }

    return new RendezvousHash<>(Funnels.stringFunnel(Constants.DEFAULT_CHARSET), randomIdPool);
  }

  @Override
  public ClientConfig getClientConfig(ChannelHandlerContext ctx, Request request) {
    String originatingAddress = getOriginatingAddress(ctx, request);
    val hasherPoolId =
        persistentProxyHasher.getOne(originatingAddress.getBytes(Constants.DEFAULT_CHARSET));
    return clientConfigMap.get(hasherPoolId);
  }
}
