package de.uniluebeck.itm.uberlay;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.name.Names;
import de.uniluebeck.itm.uberlay.protocols.pvp.PathVectorRoutingTable;
import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelSink;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UberlayModule implements Module {

	private final ScheduledExecutorService executorService;

	private final UPAddress localAddress;

	private final ChannelPipeline applicationPipeline;

	public UberlayModule(final ScheduledExecutorService executorService, final UPAddress localAddress,
						 final ChannelPipeline applicationPipeline) {

		this.executorService = executorService;
		this.localAddress = localAddress;
		this.applicationPipeline = applicationPipeline;
	}

	@Override
	public void configure(final Binder binder) {

		binder.bind(ScheduledExecutorService.class)
				.toInstance(executorService);

		binder.bind(UPAddress.class)
				.annotatedWith(Names.named(Injection.LOCAL_ADDRESS))
				.toInstance(localAddress);

		binder.bind(ChannelPipeline.class)
				.annotatedWith(Names.named(Injection.APPLICATION_PIPELINE))
				.toInstance(applicationPipeline);

		binder.bind(Channel.class)
				.annotatedWith(Names.named(Injection.APPLICATION_CHANNEL))
				.to(ApplicationChannelImpl.class);

		binder.bind(ChannelSink.class)
				.annotatedWith(Names.named(Injection.APPLICATION_CHANNEL_SINK))
				.to(UberlayNexusImpl.class);

		binder.bind(UberlayRouter.class)
				.to(UberlayNexusImpl.class);

		binder.bind(UberlayNexus.class)
				.to(UberlayNexusImpl.class);

		binder.bind(ChannelPipelineFactory.class)
				.annotatedWith(Names.named(Injection.UBERLAY_PIPELINE_FACTORY))
				.to(UberlayPipelineFactory.class);

		binder.bind(RoutingTable.class)
				.toInstance(new PathVectorRoutingTable(localAddress, 1, TimeUnit.MINUTES));
	}
}
