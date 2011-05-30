package de.uniluebeck.itm.uberlay.router;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.name.Names;
import com.google.protobuf.ByteString;
import de.uniluebeck.itm.uberlay.router.RoutingTable;
import de.uniluebeck.itm.uberlay.router.NoRouteToPeerException;
import de.uniluebeck.itm.uberlay.router.Router;
import de.uniluebeck.itm.uberlay.router.RouterImpl;
import de.uniluebeck.itm.uberlay.protocols.up.UP;
import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.jboss.netty.channel.Channels.future;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RouterTest {

	private final UPAddress remote1Address = new UPAddress("remote1");

	private final UPAddress remote2Address = new UPAddress("remote2");

	private final UPAddress localAddress = new UPAddress("local");

	private final ChannelBuffer fromLocalToRemote1Packet = ChannelBuffers.wrappedBuffer(new byte[]{1, 2, 3});

	private final ChannelBuffer fromLocalToLocalPacket = ChannelBuffers.wrappedBuffer(new byte[]{1, 2, 3});

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
	private ChannelDownstreamHandler bottomDownstreamHandler;

	@Mock
	private ChannelUpstreamHandler bottomUpstreamHandler;

	@Mock
	private Channel uberlayChannel;

	@Mock
	private ChannelPipeline uberlayChannelPipeline;

	@Mock
	private Channel remote1Channel;

	@Mock
	private Channel remote2Channel;

	@Mock
	private ChannelPipeline remote1ChannelPipeline;

	@Mock
	private ChannelPipeline remote2ChannelPipeline;

	private Router router;

	@Before
	public void setUp() throws Exception {

		final Injector injector = Guice.createInjector(new Module() {
			@Override
			public void configure(final Binder binder) {
				binder.bind(UPAddress.class).annotatedWith(Names.named(Router.INJECTION_NAME_LOCAL_ADDRESS)).toInstance(
						localAddress
				);
				binder.bind(Channel.class).toInstance(uberlayChannel);
				binder.bind(RoutingTable.class).toInstance(routingTable);
				binder.bind(Router.class).to(RouterImpl.class);
			}
		}
		);

		router = injector.getInstance(Router.class);

	}

	@Test
	public void sendPacketToRemotePeer() throws Exception {

		final ChannelFuture future = future(remote1Channel);
		final ChannelEvent event = new DownstreamMessageEvent(
				remote1Channel, future, fromLocalToRemote1Packet, remote1Address
		);

		when(routingTable.getNextHopChannel(remote1Address)).thenReturn(remote1Channel);
		when(remote1Channel.write(Matchers.<UP.UPPacket>any())).thenReturn(new SucceededChannelFuture(remote1Channel));

		router.eventSunk(remote1ChannelPipeline, event);

		verify(routingTable).getNextHopChannel(remote1Address);
		verify(remote1Channel).write(Matchers.<UP.UPPacket>any());

		assertTrue(future.isDone());
	}

	@Test
	public void sendPacketToLoopBack() throws Exception {

		final ChannelFuture future = future(uberlayChannel);
		final ChannelEvent event = new DownstreamMessageEvent(
				uberlayChannel, future, fromLocalToLocalPacket, localAddress
		);

		when(uberlayChannel.getPipeline()).thenReturn(uberlayChannelPipeline);

		router.eventSunk(uberlayChannelPipeline, event);

		verify(uberlayChannel).getPipeline();
		verify(uberlayChannelPipeline).sendUpstream(Matchers.<ChannelEvent>any());
	}

	@Test
	public void receivePacketFromChannelThatHasToBeSentUpstream() throws Exception {

		final ChannelEvent e = new UpstreamMessageEvent(remote1Channel, fromRemote1ToLocalPacket, null);

		when(uberlayChannel.getPipeline()).thenReturn(uberlayChannelPipeline);

		router.handleUpstream(mock(ChannelHandlerContext.class), e);

		verify(uberlayChannel).getPipeline();
		verify(uberlayChannelPipeline).sendUpstream(Matchers.<ChannelEvent>any());
	}

	@Test
	public void receivePacketFromRemoteThatHasToBeForwardedToAnotherRemote() throws Exception {

		final ChannelEvent e = new UpstreamMessageEvent(remote1Channel, fromRemote1ToRemote2Packet, null);

		when(routingTable.getNextHopChannel(remote2Address)).thenReturn(remote2Channel);
		when(remote2Channel.write(Matchers.<UP.UPPacket>any())).thenReturn(new SucceededChannelFuture(remote2Channel));

		router.handleUpstream(mock(ChannelHandlerContext.class), e);

		verify(routingTable).getNextHopChannel(remote2Address);
		verify(remote2Channel).write(Matchers.<UP.UPPacket>any());
	}

	@Test
	public void noRouteToPeer() throws Exception {

		final ChannelFuture future = future(uberlayChannel);
		final ChannelEvent event = new DownstreamMessageEvent(
				uberlayChannel, future, fromLocalToRemote1Packet, remote1Address
		);

		when(routingTable.getNextHopChannel(remote1Address)).thenReturn(null);

		router.eventSunk(uberlayChannelPipeline, event);

		final Throwable cause = future.getCause();

		assertFalse(future.isSuccess());
		assertNotNull(cause);
		assertEquals(remote1Address, ((NoRouteToPeerException) cause).getPeerAddress());
	}

}
