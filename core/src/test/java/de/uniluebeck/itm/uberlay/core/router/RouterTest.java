package de.uniluebeck.itm.uberlay.core.router;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.protobuf.ByteString;
import de.uniluebeck.itm.uberlay.core.protocols.pvp.RoutingTable;
import de.uniluebeck.itm.uberlay.core.protocols.router.Router;
import de.uniluebeck.itm.uberlay.core.protocols.router.RouterImpl;
import de.uniluebeck.itm.uberlay.core.protocols.up.UP;
import de.uniluebeck.itm.uberlay.core.protocols.up.UPAddress;
import org.jboss.netty.channel.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.jboss.netty.channel.Channels.future;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RouterTest {

	private final UPAddress remoteAddress = new UPAddress("host2");

	private final UPAddress localAddress = new UPAddress("host1");

	private final UP.UPPacket toRemotePacket = UP.UPPacket.newBuilder()
			.setDestination(remoteAddress.getAddress())
			.setSource(localAddress.getAddress())
			.setPayload(ByteString.copyFrom(new byte[]{1, 2, 3}))
			.build();

	private final UP.UPPacket toLocalPacket = UP.UPPacket.newBuilder()
			.setDestination(localAddress.getAddress())
			.setSource(remoteAddress.getAddress())
			.setPayload(ByteString.copyFrom(new byte[]{1, 2, 3}))
			.build();

	@Mock
	private RoutingTable routingTable;

	@Mock
	private ChannelDownstreamHandler bottomDownstreamHandler;

	@Mock
	private ChannelUpstreamHandler bottomUpstreamHandler;

	@Mock
	private Channel uberlayChannelMock;

	@Mock
	private ChannelPipeline uberlayPipelineMock;

	private Router router;

	@Before
	public void setUp() throws Exception {

		final Injector injector = Guice.createInjector(new Module() {
			@Override
			public void configure(final Binder binder) {
				binder.bind(Channel.class).toInstance(uberlayChannelMock);
				binder.bind(RoutingTable.class).toInstance(routingTable);
				binder.bind(Router.class).to(RouterImpl.class);
			}
		}
		);

		router = injector.getInstance(Router.class);
	}

	@Test
	public void sendPacketToRemotePeer() throws Exception {

		final ChannelPipeline channelPipeline = mock(ChannelPipeline.class);

		final Channel channel = mock(Channel.class);
		final ChannelFuture future = future(channel);
		final ChannelEvent event = new DownstreamMessageEvent(channel, future, toRemotePacket, remoteAddress);

		when(routingTable.getNextHopChannel(remoteAddress)).thenReturn(channel);
		when(channel.write(toRemotePacket)).thenReturn(new SucceededChannelFuture(channel));

		router.eventSunk(channelPipeline, event);

		verify(routingTable).getNextHopChannel(remoteAddress);
		verify(channel).write(toRemotePacket);

		assertTrue(future.isDone());
	}

	@Test
	public void sendPacketToLoopback() throws Exception {
		fail("IN DEVELOPMENT");
	}

	@Test
	public void receivePacketFromChannelThatHasToBeSentUpstream() throws Exception {

		final ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
		final Channel channel = mock(Channel.class);
		final ChannelEvent e = new UpstreamMessageEvent(channel, toLocalPacket, null);

		when(uberlayChannelMock.getPipeline()).thenReturn(uberlayPipelineMock);

		router.handleUpstream(ctx, e);

		verify(uberlayChannelMock).getPipeline();
		verify(uberlayPipelineMock).sendUpstream(Matchers.<ChannelEvent>any());

	}

	@Test
	public void receivePacketFromChannelThatHasToBeForwarded() throws Exception {
		fail("IN DEVELOPMENT");
	}

	@Test
	public void noRouteToHost() throws Exception {
		fail("IN DEVELOPMENT");
	}

}
