package de.uniluebeck.itm.uberlay.protocols.pvp;


import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import de.uniluebeck.itm.uberlay.*;
import de.uniluebeck.itm.uberlay.protocols.up.UPRoutingTable;
import de.uniluebeck.itm.uberlay.protocols.up.*;
import de.uniluebeck.itm.uberlay.protocols.up.UPRoutingTableEntry;
import de.uniluebeck.itm.uberlay.protocols.up.UPRoutingTableEntryImpl;
import org.jboss.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class PathVectorProtocolHandler extends SimpleChannelUpstreamHandler {

	private static final Logger log = LoggerFactory.getLogger(PathVectorProtocolHandler.class);

	private final String nodeName;

	private final UPRoutingTable routingTable;

	private final ScheduledExecutorService executorService;

	private final int maxDisseminationInterval;

	private final TimeUnit maxDisseminationIntervalTimeUnit;

	private Channel channel;

	private ScheduledFuture<?> routeDisseminationRunnableSchedule;

	/**
	 * Name of the remote node that we're connected to. This is being learnt by the first PVP message that we receive
	 * over this channel.
	 */
	private String remoteNode;

	private final Runnable disseminationRunnable = new Runnable() {
		@Override
		public void run() {
			if (channel != null) {
				PathVectorMessages.PathVectorUpdate message = buildPathVectorUpdateMessage();
				channel.write(message);
				log.trace("Sent PathVectorUpdateMessage {} update to {}", message, channel);
			}
		}
	};

	private LinkMetric lastLinkMetric;

	public PathVectorProtocolHandler(final String nodeName, final UPRoutingTable routingTable,
									 final ScheduledExecutorService executorService, final int maxDisseminationInterval,
									 final TimeUnit maxDisseminationIntervalTimeUnit) {

		this.nodeName = nodeName;
		this.routingTable = routingTable;
		this.executorService = executorService;
		this.maxDisseminationInterval = maxDisseminationInterval;
		this.maxDisseminationIntervalTimeUnit = maxDisseminationIntervalTimeUnit;
	}

	@Override
	public void channelDisconnected(final ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {

		assert channel != null;

		channel = null;
		routeDisseminationRunnableSchedule.cancel(false);

		if (remoteNode != null) {
			routingTable.removeRoutesOverNextHop(new UPAddress(remoteNode));
			executorService.execute(disseminationRunnable);
		}

		super.channelDisconnected(ctx, e);
	}

	@Override
	public void channelConnected(final ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {

		assert channel == null;

		channel = e.getChannel();
		rescheduleRouteDisseminationRunnable(0);

		super.channelConnected(ctx, e);
	}

	@Override
	public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e) throws Exception {

		if (e.getMessage() instanceof LinkMetric) {
			lastLinkMetric = (LinkMetric) e.getMessage();
			super.messageReceived(ctx, e);
		} else if (e.getMessage() instanceof PathVectorMessages.PathVectorUpdate) {
			handlePathVectorUpdateMessage((PathVectorMessages.PathVectorUpdate) e.getMessage());
		} else {
			super.messageReceived(ctx, e);
		}

	}

	private void rescheduleRouteDisseminationRunnable(long initialDelay) {

		log.debug("Rescheduling route dissemination runnable for initial delay in {} {}.", initialDelay,
				maxDisseminationIntervalTimeUnit
		);

		if (routeDisseminationRunnableSchedule != null) {
			if (!routeDisseminationRunnableSchedule.isCancelled()) {
				routeDisseminationRunnableSchedule.cancel(false);
			}
		}

		routeDisseminationRunnableSchedule = executorService.scheduleWithFixedDelay(
				disseminationRunnable,
				initialDelay,
				maxDisseminationInterval,
				maxDisseminationIntervalTimeUnit
		);
	}

	private void handlePathVectorUpdateMessage(final PathVectorMessages.PathVectorUpdate message) {

		log.debug("handlePathVectorUpdateMessage()");

		final String sender = message.getSender();
		Preconditions.checkArgument(remoteNode == null || remoteNode.equals(sender));
		remoteNode = sender;
		boolean sthChanged = false;

		List<PathVectorMessages.PathVectorUpdate.RoutingTableEntry> entries = message.getRoutingTableEntriesList();
		long linkCost = getLinkCostTo();

		// refresh one-hop route to 'sender'
		log.debug("Refreshing one-hop routing table entry to direct neighbor \"{}\"", sender);
		final UPAddress senderAddress = new UPAddress(sender);
		routingTable.updateEntry(senderAddress, linkCost, Lists.newArrayList(senderAddress), channel);

		// check all routes received by 'sender' if they're shorter than our own routes and add them if so
		for (PathVectorMessages.PathVectorUpdate.RoutingTableEntry routingTableEntryReceived : entries) {

			final String destination = routingTableEntryReceived.getDestination();

			if (!nodeName.equals(destination)) {

				// calculate link cost and make sure accumulated cost is not larger than Long.MAX_VALUE which would
				// result in number overflow, resulting in a negative number
				long entryCost = routingTableEntryReceived.getCost();
				final long cost = (Long.MAX_VALUE - entryCost) > linkCost ? entryCost + linkCost : Long.MAX_VALUE;

				final List<String> pathReceived = routingTableEntryReceived.getPathList();
				final List<UPAddress> path = Lists.newArrayListWithCapacity(pathReceived.size());
				for (String node : pathReceived) {
					path.add(UPAddress.STRING_TO_ADDRESS.apply(node));
				}

				// must add 'sender' as path does not contain 'sender' as the path received by him is his view
				path.add(0, senderAddress);

				final UPAddress destinationAddress = new UPAddress(destination);
				sthChanged = routingTable.updateEntry(destinationAddress, cost, path, channel) || sthChanged;
			}
		}

		if (sthChanged) {
			rescheduleRouteDisseminationRunnable(0);
		}
	}

	private PathVectorMessages.PathVectorUpdate buildPathVectorUpdateMessage() {

		PathVectorMessages.PathVectorUpdate.Builder updateBuilder = PathVectorMessages.PathVectorUpdate.newBuilder()
				.setSender(nodeName);

		for (Map.Entry<UPAddress, UPRoutingTableEntry> entry : routingTable.getEntries().entrySet()) {

			final UPAddress destination = entry.getKey();
			final UPRoutingTableEntry routingTableEntry = entry.getValue();

			final List<String> path = Lists.transform(
					((UPRoutingTableEntryImpl) routingTableEntry).getPath(),
					UPAddress.ADDRESS_TO_STRING
			);

			final PathVectorMessages.PathVectorUpdate.RoutingTableEntry.Builder entryBuilder =
					PathVectorMessages.PathVectorUpdate.RoutingTableEntry.newBuilder()
							.setDestination(destination.toString())
							.setCost(((UPRoutingTableEntryImpl) routingTableEntry).getCost())
							.addAllPath(path);

			updateBuilder.addRoutingTableEntries(entryBuilder);
		}

		return updateBuilder.build();
	}

	private long getLinkCostTo() {
		return lastLinkMetric == null ? Long.MAX_VALUE : lastLinkMetric.getMetric();
	}

}
