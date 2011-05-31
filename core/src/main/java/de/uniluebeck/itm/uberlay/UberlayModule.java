package de.uniluebeck.itm.uberlay;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.name.Names;
import de.uniluebeck.itm.uberlay.protocols.pvp.PathVectorRoutingTable;
import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipelineFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UberlayModule implements Module {

	private final ScheduledExecutorService executorService;

	private final UPAddress localAddress;

	public UberlayModule(final ScheduledExecutorService executorService, final UPAddress localAddress) {
		this.executorService = executorService;
		this.localAddress = localAddress;
	}

	@Override
	public void configure(final Binder binder) {

		binder.bind(UPAddress.class)
				.annotatedWith(Names.named(Injection.LOCAL_ADDRESS))
				.toInstance(localAddress);

		binder.bind(Channel.class)
				.annotatedWith(Names.named(Injection.APPLICATION_CHANNEL))
				.to(ApplicationChannelImpl.class);

		binder.bind(ChannelPipelineFactory.class)
				.annotatedWith(Names.named(Injection.UBERLAY_PIPELINE_FACTORY))
				.to(UberlayPipelineFactory.class);

		binder.bind(ScheduledExecutorService.class).toInstance(executorService);
		binder.bind(RoutingTable.class).toInstance(new PathVectorRoutingTable(localAddress, 1, TimeUnit.MINUTES));
		binder.bind(UberlayRouter.class).to(UberlayNexus.class);
	}
}
