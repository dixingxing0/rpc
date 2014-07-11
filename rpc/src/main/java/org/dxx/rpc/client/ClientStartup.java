package org.dxx.rpc.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.Set;

import org.dxx.rpc.codec.DexnDecoder;
import org.dxx.rpc.codec.DexnEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientStartup {
	static Logger logger = LoggerFactory.getLogger(ClientStartup.class);

	private String host;
	private int port;

	private Set<Class<?>> interfaces;

	public ClientStartup(String host, int port) {
		super();
		this.host = host;
		this.port = port;
	}

	public void startup() {
		logger.debug("Try create channel : {}:{}, for : {}", new Object[] { host, port, interfaces });
		EventLoopGroup workerGroup = new NioEventLoopGroup();

		try {
			Bootstrap b = new Bootstrap();
			b.group(workerGroup);
			b.channel(NioSocketChannel.class);
			b.option(ChannelOption.SO_KEEPALIVE, true);
			b.handler(new ChannelInitializer<SocketChannel>() {
				@Override
				public void initChannel(SocketChannel ch) throws Exception {
					ch.pipeline().addLast(new DexnEncoder(), new DexnDecoder(), new ObjectClientHandler());
				}
			});

			ChannelFuture f = b.connect(host, port).sync();
			Channel c = f.channel();

			// store the relation between interface class and channel
			for (Class<?> i : this.interfaces) {
				ChannelContext.add(i, c);
			}
			logger.debug("Channel created : {}", c);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void setInterfaces(Set<Class<?>> interfaces) {
		this.interfaces = interfaces;
	}

	public String getUrl() {
		return this.host + ":" + this.port;
	}

}
