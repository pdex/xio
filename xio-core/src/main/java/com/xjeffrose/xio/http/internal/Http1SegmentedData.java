package com.xjeffrose.xio.http.internal;

import com.xjeffrose.xio.http.Headers;
import com.xjeffrose.xio.http.Message;
import com.xjeffrose.xio.http.SegmentedData;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.LastHttpContent;
import lombok.ToString;

/** Wrap an incoming HttpContent, for use by streaming clients or servers */
@ToString
public class Http1SegmentedData implements SegmentedData {

  private final HttpContent content;
  private final boolean eos;
  private final Headers trailingHeaders;

  static Headers buildHeaders(HttpContent content) {
    if (content instanceof LastHttpContent) {
      LastHttpContent last = (LastHttpContent) content;
      if (last.trailingHeaders() != null) {
        return new Http1Headers(last.trailingHeaders());
      } else {
        return null;
      }
    } else {
      return null;
    }
  }

  public Http1SegmentedData(HttpContent content) {
    this.content = content;
    eos = content instanceof LastHttpContent;
    trailingHeaders = buildHeaders(content);
  }

  @Override
  public int streamId() {
    return Message.H1_STREAM_ID_NONE;
  }

  public ByteBuf content() {
    return content.content();
  }

  public boolean endOfMessage() {
    return eos;
  }

  public Headers trailingHeaders() {
    return trailingHeaders;
  }
}
