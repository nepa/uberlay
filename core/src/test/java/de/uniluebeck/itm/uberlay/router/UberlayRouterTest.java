package de.uniluebeck.itm.uberlay.router;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.name.Names;
import com.google.protobuf.ByteString;
import de.uniluebeck.itm.uberlay.*;
import de.uniluebeck.itm.uberlay.protocols.up.UP;
import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;
import org.jboss.netty.channel.*;
import org.jboss.netty.util.internal.ExecutorUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static org.jboss.netty.channel.Channels.future;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UberlayRouterTest {

	private final UPAddress remote1Address = new UPAddress("remote1");

	private final UPAddress remote2Address = new UPAddress("remote2");

	private final UPAddress localAddress = new UPAddress("local");

	private final UP.UPPacket fromLocalToRemote1Packet = UP.UPPacket.newBuilder()
			.setDestination(remote1Address.toString())
			.setSource(localAddress.toString())
			.setPayload(ByteString.copyFrom(new byte[]{1, 2, 3}))
			.build();

	private final UP.UPPacket fromLocalToLocalPacket = UP.UPPacket.newBuilder()
			.setDestination(localAddress.toString())
			.setSource(localAddress.toString())
			.setPayload(ByteString.copyFrom(new byte[]{1, 2, 3}))
			.build();

	private final UP.UPPacket fromRemote1ToLocalPacket = UP.UPPacket.newBuilder()
			.setDestination(localAddress.toString())
			.setSource(remote1Address.toString())
			.setPayload(ByteString.copyFrom(new byte[]{1, 2, 3}))
			.build();

	private final UP.UPPacket fromRemote1ToRemote2Packet = UP.UPPacket.newBuilder()
			.setDestination(remote2Address.toString())
			.setSource(remote1Address.toString())
			.setPayload(ByteString.copyFrom(new byte[]{1, 2, 3}))
			.build();

	@Mock
	private RoutingTable routingTable;

	@Mock
	private Channel applicationChannel;

	@Mock
	private ChannelPipeline applicationPipeline;

	@Mock
	private ChannelPipelineFactory UberlayPipelineFactory;

	@Mock
	private Channel remote1Channel;

	@Mock
	private Channel remote2Channel;

	private UberlayRouter router;

	private ScheduledExecutorService executorService;

	@Before
	public void setUp() throws Exception {

		executorService = Executors.newScheduledThreadPool(1);

		final Injector injector = Guice.createInjector(new Module() {
			@Override
			public void configure(final Binder binder) {
				binder.bind(ScheduledExecutorService.class).toInstance(executorService);
				binder.bind(UPAddress.class)
						.annotatedWith(Names.named(Injection.LOCAL_ADDRESS))
						.toInstance(localAddress);
				binder.bind(Channel.class)
						.annotatedWith(Names.named(Injection.APPLICATION_CHANNEL))
						.toInstance(applicationChannel);
				binder.bind(ChannelPipelineFactory.class)
						.annotatedWith(Names.named(Injection.UBERLAY_PIPELINE_FACTORY))
						.toInstance(UberlayPipelineFactory);
				binder.bind(RoutingTable.class).toInstance(routingTable);
				binder.bind(UberlayRouter.class).to(UberlayNexus.class);
			}
		}
		);

		router = injector.getInstance(UberlayRouter.class);

	}

	@After
	public void tearDown() throws Exception {
		ExecutorUtil.terminate(executorService);
	}

	@Test
	public void sendPacketToRemotePeer() throws Exception {

		final ChannelFuture future = future(remote1Channel);

		when(routingTable.getNextHopChannel(remote1Address)).thenReturn(remote1Channel);
		when(remote1Channel.write(any())).thenReturn(new SucceededChannelFuture(remote1Channel));

		router.route(fromLocalToRemote1Packet, future);

		verify(routingTable).getNextHopChannel(remote1Address);
		verify(remote1Channel).write(any());

		assertTrue(future.isDone());
	}

	@Test
	public void sendPacketToLoopBack() throws Exception {

		final ChannelFuture future = future(applicationChannel);

		when(applicationChannel.getPipeline()).thenReturn(applicationPipeline);

		router.route(fromLocalToLocalPacket, future);

		verify(applicationChannel).getPipeline();
		verify(applicationPipeline).sendUpstream(Matchers.<ChannelEvent>any());
	}

	@Test
	public void receivePacketFromChannelThatHasToBeSentUpstream() throws Exception {

		final ChannelEvent e = new UpstreamMessageEvent(remote1Channel, fromRemote1ToLocalPacket, null);

		when(applicationChannel.getPipeline()).thenReturn(applicationPipeline);

		router.handleUpstream(mock(ChannelHandlerContext.class), e);

		verify(applicationChannel).getPipeline();
		verify(applicationPipeline).sendUpstream(Matchers.<ChannelEvent>any());
	}

	@Test
	public void receivePacketFromRemoteThatHasToBeForwardedToAnotherRemote() throws Exception {

		final ChannelEvent e = new UpstreamMessageEvent(remote1Channel, fromRemote1ToRemote2Packet, null);

		when(routingTable.getNextHopChannel(remote2Address)).thenReturn(remote2Channel);
		when(remote2Channel.write(any())).thenReturn(new SucceededChannelFuture(remote2Channel));

		router.handleUpstream(mock(ChannelHandlerContext.class), e);

		verify(routingTable).getNextHopChannel(remote2Address);
		verify(remote2Channel).write(any());
	}

	@Test
	public void noRouteToPeer() throws Exception {

		final ChannelFuture future = future(applicationChannel);

		when(routingTable.getNextHopChannel(remote1Address)).thenReturn(null);

		router.route(fromLocalToRemote1Packet, future);

		final Throwable cause = future.getCause();

		assertFalse(future.isSuccess());
		assertNotNull(cause);
		assertEquals(remote1Address, ((NoRouteToPeerException) cause).getPeerAddress());
	}

}
