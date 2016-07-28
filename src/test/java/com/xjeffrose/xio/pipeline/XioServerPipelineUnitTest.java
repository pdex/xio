package com.xjeffrose.xio.pipeline;

import com.xjeffrose.xio.core.ChannelStatistics;
import com.xjeffrose.xio.core.ConnectionContextHandler;
import com.xjeffrose.xio.core.XioExceptionLogger;
import com.xjeffrose.xio.core.XioMessageLogger;
import com.xjeffrose.xio.core.XioNoOpHandler;
import com.xjeffrose.xio.server.XioBehavioralRuleEngine;
import com.xjeffrose.xio.server.XioConnectionLimiter;
import com.xjeffrose.xio.server.XioDeterministicRuleEngine;
import com.xjeffrose.xio.server.XioResponseClassifier;
import com.xjeffrose.xio.server.XioServerConfig;
import com.xjeffrose.xio.server.XioServerState;
import com.xjeffrose.xio.server.XioService;
import com.xjeffrose.xio.server.XioWebApplicationFirewall;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import static org.mockito.Mockito.*;
import org.mockito.InOrder;
import org.junit.Test;

public class XioServerPipelineUnitTest {

  @Test
  public void verifyHandlers() {
    XioServerConfig serverConfig = XioServerConfig.fromConfig("xio.exampleServer");
    XioServerState serverState = XioServerState.fromConfig("xio.exampleApplication");

    // Build class under test
    XioServerPipeline server = new XioServerPipeline() {
      public ChannelHandler getEncryptionHandler() {
        return new XioNoOpHandler();
      }

      public ChannelHandler getAuthenticationHandler() {
        return new XioNoOpHandler();
      }

      public ChannelHandler getCodecHandler() {
        return new XioNoOpHandler();
      }
    };
    ChannelPipeline pipeline = mock(ChannelPipeline.class);
    server.buildHandlers(serverConfig, serverState, pipeline);
    InOrder inOrder = inOrder(pipeline);
    inOrder.verify(pipeline, times(1)).addLast(eq("globalConnectionLimiter"), isA(XioConnectionLimiter.class));
    inOrder.verify(pipeline, times(1)).addLast(eq("serviceConnectionLimiter"), isA(XioConnectionLimiter.class));
    inOrder.verify(pipeline, times(1)).addLast(eq("l4DeterministicRuleEngine"), isA(XioDeterministicRuleEngine.class));
    inOrder.verify(pipeline, times(1)).addLast(eq("l4BehavioralRuleEngine"), isA(XioBehavioralRuleEngine.class));
    inOrder.verify(pipeline, times(1)).addLast(eq("connectionContext"), isA(ConnectionContextHandler.class));
    inOrder.verify(pipeline, times(1)).addLast(eq("globalChannelStatistics"), eq(serverState.channelStatistics()));
    //inOrder.verify(pipeline, times(1)).addLast(eq("encryptionHandler"), isA(ChannelHandler.class));
    inOrder.verify(pipeline, times(1)).addLast(eq("encryptionHandler"), any());

    inOrder.verify(pipeline, times(1)).addLast(eq("messageLogger"), isA(XioMessageLogger.class));
    //inOrder.verify(pipeline, times(1)).addLast(eq("codec"), isA(XioMessageLogger.class));
    inOrder.verify(pipeline, times(1)).addLast(eq("codec"), any());
    inOrder.verify(pipeline, times(1)).addLast(eq("l7DeterministicRuleEngine"), isA(XioDeterministicRuleEngine.class));
    inOrder.verify(pipeline, times(1)).addLast(eq("l7BehavioralRuleEngine"), isA(XioBehavioralRuleEngine.class));
    inOrder.verify(pipeline, times(1)).addLast(eq("webApplicationFirewall"), isA(XioWebApplicationFirewall.class));
    //inOrder.verify(pipeline, times(1)).addLast(eq("authHandler"), isA(ChannelHandler.class));
    inOrder.verify(pipeline, times(1)).addLast(eq("authHandler"), any());
    inOrder.verify(pipeline, times(1)).addLast(eq("xioService"), isA(XioService.class));
    inOrder.verify(pipeline, times(1)).addLast(eq("xioResponseClassifier"), isA(XioResponseClassifier.class));
    inOrder.verify(pipeline, times(1)).addLast(eq("exceptionLogger"), isA(XioExceptionLogger.class));
  }
}
