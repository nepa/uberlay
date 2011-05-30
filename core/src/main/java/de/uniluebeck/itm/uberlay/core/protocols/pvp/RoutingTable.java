package de.uniluebeck.itm.uberlay.core.protocols.pvp;

import com.google.common.collect.ImmutableMap;
import org.jboss.netty.channel.Channel;

import java.util.List;

public interface RoutingTable {

	/**
	 * Returns all entries of the routing table as an immutable object.
	 *
	 * @return all entries of the routing table
	 */
	ImmutableMap<String, RoutingTableEntry> getEntries();

	/**
	 * Returns the routing table entry for the given {@code destination}.
	 *
	 * @param destination the destination node
	 *
	 * @return the routing table entry or {@code null} if no entry exists
	 */
	RoutingTableEntry getEntry(String destination);

	/**
	 * Returns the next hop for {@code destination}.
	 *
	 * @param destination the final destination
	 *
	 * @return next hop or {@code null} if no entry was found in the routing table
	 */
	String getNextHop(String destination);

	/**
	 * Returns the {@link Channel} instance to the next hop to {@code destination}.
	 *
	 * @param destination the final destination
	 *
	 * @return next hops {@link Channel} instance or {@code null} if no entry was found in the routing table
	 */
	Channel getNextHopChannel(String destination);

	/**
	 * Updates the routing table entry if cost is cheaper and path contains no loops.
	 *
	 * @param destination the destination node
	 * @param cost		the paths costs
	 * @param path		the path to the destination node (excluding the own node, including the destination node)
	 *
	 * @return {@code true} if updated, {@code false} otherwise
	 */
	boolean updateEntry(String destination, long cost, List<String> path, Channel channel);

	/**
	 * Removes all routes from the table that have {@code remoteNode} as the next hop. This method may be called e.g., if
	 * the connection between this host and {@code remoteNode} was dropped and the route is thereby obsolete.
	 *
	 * @param remoteNode the next hop node that is now unavailable
	 */
	void removeRoutesOverNextHop(String remoteNode);
}
