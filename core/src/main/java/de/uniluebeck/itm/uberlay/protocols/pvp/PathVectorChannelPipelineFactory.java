package de.uniluebeck.itm.uberlay.protocols.pvp;

import de.uniluebeck.itm.uberlay.DefaultLoggingHandler;
import de.uniluebeck.itm.uberlay.protocols.up.UPRoutingTable;
import de.uniluebeck.itm.uberlay.protocols.ProtocolRegistry;
import de.uniluebeck.itm.uberlay.protocols.rtt.RoundtripTimeProtocolHandler;
import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.protobuf.MultiProtobufDecoder;
import org.jboss.netty.handler.codec.protobuf.MultiProtobufEncoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PathVectorChannelPipelineFactory implements ChannelPipelineFactory {

	private final ScheduledExecutorService executorService;

	private final UPAddress localAddress;

	private final UPRoutingTable routingTable;

	public PathVectorChannelPipelineFactory(final ScheduledExecutorService executorService,
											final UPAddress localAddress,
											final UPRoutingTable routingTable) {

		this.executorService = executorService;
		this.localAddress = localAddress;
		this.routingTable = routingTable;
	}

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
				new RoundtripTimeProtocolHandler(executorService, 10, TimeUnit.SECONDS)
		);
		pipeline.addLast("pvpHandler",
				new PathVectorProtocolHandler(
						localAddress.toString(), routingTable, executorService, 10, TimeUnit.SECONDS
				)
		);
		pipeline.addLast("loggingHandler", new DefaultLoggingHandler());

		return pipeline;
	}
}
