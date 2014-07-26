/**
 * HttpRequestHandler.java
 * org.dxx.rpc.registry.server
 * Copyright (c) 2014, 北京微课创景教育科技有限公司版权所有.
*/

package org.dxx.rpc.monitor;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders.Values;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Handle http request.
 * 
 * @author   dixingxing
 * @Date	 2014-7-25
 */
public class HttpUtils {
	static final Logger logger = LoggerFactory.getLogger(HttpUtils.class);

	private static Map<String, Controller> mappings = new ConcurrentHashMap<String, Controller>();

	public static void addMapping(String uri, Controller c) {
		mappings.put(uri, c);
	}

	public static void handleRequest(ChannelHandlerContext ctx, Object msg) {
		DefaultFullHttpRequest request = (DefaultFullHttpRequest) msg;
		if (request.getUri().endsWith("favicon.ico")) {
			return;
		}

		logger.debug("{} : {}", request.getMethod().name(), request.getUri());
		logger.trace("Received : {}", request);

		QueryStringDecoder qsDecoder = new QueryStringDecoder(request.getUri());
		logger.trace("Query params: {}", qsDecoder.parameters());

		if (request.getMethod() == HttpMethod.POST) {
			HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(request);
			logger.trace("Post params : {}", postDecoder.getBodyHttpDatas());
		}

		writeResponse(ctx, invokeController(request, qsDecoder));
	}

	static String path(QueryStringDecoder queryStringDecoder) {
		String path = queryStringDecoder.path();
		return path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
	}

	private static String invokeController(DefaultFullHttpRequest request, QueryStringDecoder qsDecoder) {
		Controller c = mappings.get(path(qsDecoder));
		if (c == null) {
			logger.warn("Can not find request mapping for uri : {}", path(qsDecoder));
			return "Can not find request mapping for uri : " + request.getUri();
		}

		Map<String, Object> m = new HashMap<String, Object>();
		m.put("screen_content", VelocityUtils.renderFile(c.exec(request, m), m));

		String layout = m.get("layout") != null ? (String) m.get("layout") : "vm/layout/default.html";
		return VelocityUtils.renderFile(layout, m);
	}

	private static void writeResponse(ChannelHandlerContext ctx, String html) {
		byte[] bytes = null;
		try {
			bytes = html.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
		}
		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(bytes));
		response.headers().set(CONTENT_TYPE, "text/html");
		response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
		response.headers().set(CONNECTION, Values.KEEP_ALIVE);
		ctx.write(response);
		ctx.flush();
	}
}