package de.uniluebeck.itm.uberlay.core.router;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import de.uniluebeck.itm.uberlay.core.protocols.pvp.RoutingTable;
import de.uniluebeck.itm.uberlay.core.protocols.router.Router;
import de.uniluebeck.itm.uberlay.core.protocols.router.RouterImpl;
import de.uniluebeck.itm.uberlay.core.protocols.up.UPAddress;
import org.jboss.netty.channel.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.omg.CORBA.CTX_RESTRICT_SCOPE;

import static org.jboss.netty.channel.Channels.future;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RouterTest {

	private Router router;

	@Mock
	private RoutingTable routingTable;

	@Mock
	private ChannelDownstreamHandler bottomDownstreamHandler;

	@Mock
	private ChannelUpstreamHandler bottomUpstreamHandler;

	@Before
	public void setUp() throws Exception {

		final Injector injector = Guice.createInjector(new Module() {
			@Override
			public void configure(final Binder binder) {
				binder.bind(RoutingTable.class).toInstance(routingTable);
				binder.bind(Router.class).to(RouterImpl.class);
			}
		}
		);

		router = injector.getInstance(Router.class);
	}

	@Test
	public void sendToChannel1() throws Exception {

		final ChannelPipeline channelPipeline = mock(ChannelPipeline.class);

		final byte[] messageBytes = new byte[] {1,2,3};
		final Channel channel = mock(Channel.class);
		final ChannelFuture future = future(channel);
		final ChannelEvent event = new DownstreamMessageEvent(channel, future, messageBytes, new UPAddress("host1"));

		when(routingTable.getNextHopChannel("host1")).thenReturn(channel);
		when(channel.write(messageBytes)).thenReturn(new SucceededChannelFuture(channel));

		router.eventSunk(channelPipeline, event);

		verify(routingTable).getNextHopChannel("host1");
		verify(channel).write(messageBytes);

		assertTrue(future.isDone());
	}

	@Test
	public void receiveFromChannel1() throws Exception {

		fail("IN DEVELOPMENT");

		final byte[] messageBytes = new byte[] {1,2,3};
		final ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
		final Channel channel = mock(Channel.class);
		final ChannelEvent e = new UpstreamMessageEvent(channel, messageBytes, null);

		router.handleUpstream(ctx, e);

	}

	@Test
	public void noRouteToHost() throws Exception {
		fail("IN DEVELOPMENT");
	}

}
