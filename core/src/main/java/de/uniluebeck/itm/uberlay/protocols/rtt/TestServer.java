package de.uniluebeck.itm.uberlay.protocols.rtt;

import de.uniluebeck.itm.uberlay.DefaultLoggingHandler;
import de.uniluebeck.itm.uberlay.protocols.ProtocolRegistry;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.protobuf.MultiProtobufDecoder;
import org.jboss.netty.handler.codec.protobuf.MultiProtobufEncoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TestServer {

	public static void main(String[] args) {

		final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(10);

		ChannelFactory factory =
				new NioServerSocketChannelFactory(
						executorService,
						executorService
				);

		ServerBootstrap bootstrap = new ServerBootstrap(factory);

		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			public ChannelPipeline getPipeline() {

				ChannelPipeline pipeline = Channels.pipeline();

				// upstream handlers
				pipeline.addLast("frameDecoder", new ProtobufVarint32FrameDecoder());
				pipeline.addLast("protobufDecoders",
						new MultiProtobufDecoder(ProtocolRegistry.REGISTRY, ProtocolRegistry.HEADER_FIELD_LENGTH)
				);

				// downstream handlers
				pipeline.addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender());
				pipeline.addLast("protobufEncoders",
						new MultiProtobufEncoder(ProtocolRegistry.REGISTRY, ProtocolRegistry.HEADER_FIELD_LENGTH)
				);

				// application logic
				pipeline.addLast("rttProtocolHandler",
						new RoundtripTimeProtocolHandler(executorService, 3, TimeUnit.SECONDS)
				);
				pipeline.addLast("loggingHandler", new DefaultLoggingHandler());

				return pipeline;
			}
		}
		);

		bootstrap.setOption("child.tcpNoDelay", true);
		bootstrap.setOption("child.keepAlive", true);

		bootstrap.bind(new InetSocketAddress(8080));
	}

}
