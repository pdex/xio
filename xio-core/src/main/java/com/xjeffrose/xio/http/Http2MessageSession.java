package com.xjeffrose.xio.http;

import com.google.common.collect.Maps;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import java.util.Map;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;

/**
 * A finite state machine to track the current HTTP/2 message session. This class exists to store
 * the connection specific state of the message session
 */
@Slf4j
public class Http2MessageSession {

  private static final AttributeKey<Http2MessageSession> CHANNEL_MESSAGE_SESSION_KEY =
      AttributeKey.newInstance("xio_channel_h2_message_session");

  static Http2MessageSession contextMessageSession(ChannelHandlerContext ctx) {
    Http2MessageSession session = ctx.channel().attr(CHANNEL_MESSAGE_SESSION_KEY).get();
    if (session == null) {
      session = new Http2MessageSession();
      ctx.channel().attr(CHANNEL_MESSAGE_SESSION_KEY).set(session);
    }
    return session;
  }

  private Map<Integer, MessageMetaState> streamIdRequests = Maps.newHashMap();

  private Http2MessageSession() {}

  public void onRequest(Request request) {
    MessageMetaState initialRequest = streamIdRequests.get(request.streamId());
    if (initialRequest == null) {
      if (request.startOfMessage()) {
        streamIdRequests.put(
            request.streamId(), new MessageMetaState(request, request.endOfMessage()));
      } else {
        log.error("Received an h2 message segment without a startOfMessage - request: {}", request);
      }
    } else {
      initialRequest.requestFinished = request.endOfMessage();
    }
  }

  public void onRequestData(SegmentedData data) {
    MessageMetaState initialRequest = streamIdRequests.get(data.streamId());

    if (initialRequest == null) {
      log.error(
          "Received an h2 message SegmentedData without a current Request, dropping data: {}",
          data);
      return;
    }

    if (data.endOfMessage()) {
      initialRequest.requestFinished = true;
    }
  }

  public void onResponse(Response response) {
    MessageMetaState initialRequest = streamIdRequests.get(response.streamId());
    if (initialRequest != null && response.endOfMessage()) {
      initialRequest.responseFinished = true;
    }
  }

  /**
   * Called before a SegmentedData object is sent to the client as part of a Response.
   *
   * @param data The SegmentedData object the server is about to send
   */
  public void onResponseData(SegmentedData data) {
    MessageMetaState initialRequest = streamIdRequests.get(data.streamId());
    if (initialRequest != null) {
      if (data.endOfMessage()) {
        initialRequest.responseFinished = true;
      }
    } else {
      log.error(
          "Attempted to write SegmentedData without a current Request, dropping data: {}", data);
    }
  }

  /**
   * Returns the Request object for the current session (if any).
   *
   * @return the current Request or null
   */
  @Nullable
  public Request currentRequest(int streamId) {
    MessageMetaState initialRequest = streamIdRequests.get(streamId);
    if (initialRequest != null) {
      return initialRequest.request;
    }
    return null;
  }

  /**
   * Check if the message session has completed, if so remove state and prepare for the next
   * session.
   */
  public void flush(int streamId) {
    MessageMetaState initialRequest = streamIdRequests.get(streamId);
    if (initialRequest != null
        && initialRequest.requestFinished
        && initialRequest.responseFinished) {
      streamIdRequests.remove(streamId);
    }
  }

  private static class MessageMetaState {
    public final Request request;
    public boolean requestFinished;
    public boolean responseFinished;

    MessageMetaState(Request request, boolean requestFinished) {
      this.request = request;
      this.requestFinished = requestFinished;
      responseFinished = false;
    }
  }
}
