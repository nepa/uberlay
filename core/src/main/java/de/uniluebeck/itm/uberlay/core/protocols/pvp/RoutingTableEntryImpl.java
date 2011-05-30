package de.uniluebeck.itm.uberlay.core.protocols.pvp;

import com.google.common.collect.Lists;
import org.jboss.netty.channel.Channel;

import java.util.Arrays;
import java.util.List;

/**
 * A routing table entry. Contains the cost of the complete path and a list of node names, excluding the local node and
 * including the destination node.
 */
public class RoutingTableEntryImpl implements RoutingTableEntry {

	private final long cost;

	private final List<String> path;

	private final Channel nextHopChannel;

	RoutingTableEntryImpl(final long cost, final List<String> path, final Channel nextHopChannel) {
		this.cost = cost;
		this.path = path;
		this.nextHopChannel = nextHopChannel;
	}

	@Override
	public String toString() {
		return "RoutingTableEntryImpl{" +
				"cost=" + cost +
				", path=" + Arrays.toString(path.toArray()) +
				'}';
	}

	public long getCost() {
		return cost;
	}

	public List<String> getPath() {
		return Lists.newArrayList(path);
	}

	@Override
	public String getNextHop() {
		return path.get(0);
	}

	@Override
	public Channel getNextHopChannel() {
		return nextHopChannel;
	}
}