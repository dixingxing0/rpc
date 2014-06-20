package org.dxx.rpc.exec;

import java.lang.reflect.Method;

import org.dxx.rpc.Request;
import org.dxx.rpc.Response;
import org.dxx.rpc.exception.RpcException;
import org.dxx.rpc.server.Servers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;

public class RpcRunnable implements Runnable {
	Logger logger = LoggerFactory.getLogger(RpcRunnable.class);

	private Channel channel;

	private Request request;

	public RpcRunnable(Channel channel, Request request) {
		super();
		this.channel = channel;
		this.request = request;
	}

	@Override
	public void run() {
		logger.debug("Receive : {}", request);
		Response r = new Response();
		r.setId(request.getId());
		try {
			Object service = Servers.getRpcService(request.getInterfaceClass());
			if(service == null) {
				r.setError(new RpcException("Service not found : " + request.getInterfaceClass()));
				logger.warn("Service not found : {}", request.getInterfaceClass());
			} else {
				Method m = service.getClass().getMethod(request.getMethodName(), request.getArgTypes());
				r.setObj(m.invoke(service, request.getArgs()));
			}
			
		} catch (Throwable e) {
			logger.warn(e.getMessage(), e);
			r.setError(e);
		} finally {
			channel.writeAndFlush(r);
		}
	}
}