package de.uniluebeck.itm.uberlay.core.protocols.pvp;

import de.uniluebeck.itm.uberlay.core.LoggingHandler;
import de.uniluebeck.itm.uberlay.core.ProtocolRegistry;
import de.uniluebeck.itm.uberlay.core.protocols.rtt.RoundtripTimeProtocolHandler;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.protobuf.MultiProtobufDecoder;
import org.jboss.netty.handler.codec.protobuf.MultiProtobufEncoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PathVectorChannelPipelineFactory implements ChannelPipelineFactory {


	private final ScheduledExecutorService executorService;

	private final String nodeName;

	private final PathVectorRoutingTable routingTable;

	public PathVectorChannelPipelineFactory(final ScheduledExecutorService executorService, final String nodeName,
											final PathVectorRoutingTable routingTable) {

		this.executorService = executorService;
		this.nodeName = nodeName;
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
				new PathVectorProtocolHandler(nodeName, routingTable, executorService, 10, TimeUnit.SECONDS)
		);
		pipeline.addLast("loggingHandler", new LoggingHandler());

		return pipeline;
	}
}
