package com.xjeffrose.xio.http;

import com.xjeffrose.xio.core.internal.UnstableApi;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpVersion;
import lombok.experimental.Accessors;
import lombok.Builder;
import lombok.Getter;
import io.netty.handler.codec.http.HttpResponse;

/**
 * Value class for representing a streaming outgoing HTTP1/2 Response, for use in a server.
 */
@UnstableApi
public interface StreamingResponse extends Response {

}