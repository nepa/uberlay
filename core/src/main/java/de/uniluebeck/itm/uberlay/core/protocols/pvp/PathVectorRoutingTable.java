package de.uniluebeck.itm.uberlay.core.protocols.pvp;


import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import de.uniluebeck.itm.tr.util.TimedCache;
import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A routing table implementation that holds a mapping from destination node to a tuple of (cost, path). An entry is
 * kept in the table until a configurable timeout occurs. If the entry is updated in the mean time the entry timeout
 * will be reset.
 */
public class PathVectorRoutingTable {

	private static final Logger log = LoggerFactory.getLogger(PathVectorRoutingTable.class);

	private final String nodeName;

	/**
	 * A routing table entry. Contains the cost of the complete path and a list of node names, excluding the local node and
	 * including the destination node.
	 */
	public static class Entry {

		private final long cost;

		private final List<String> path;

		private final Channel channel;

		private Entry(final long cost, final List<String> path, final Channel channel) {

			checkNotNull(path);
			checkArgument(path.size() > 0);
			checkNotNull(channel);

			this.cost = cost;
			this.path = path;
			this.channel = channel;
		}

		@Override
		public String toString() {
			return "Entry{" +
					"cost=" + cost +
					", path=" + Arrays.toString(path.toArray()) +
					", channel=" + channel +
					'}';
		}

		public long getCost() {
			return cost;
		}

		public List<String> getPath() {
			return path;
		}

		public String getNextHop() {
			return path.get(0);
		}

		public Channel getChannel() {
			return channel;
		}
	}

	/**
	 * The actual routing table as a {@link java.util.Map}, mapping the destination node to an {@link Entry}.
	 */
	private final TimedCache<String, Entry> routingTable;

	/**
	 * Constructs a new routing table instance.
	 *
	 * @param nodeName				   the name of this node
	 * @param cacheEntryLifetime		 the maximum life time of a routing table entry after which it will be deleted if
	 *                                   it is not updated before the maximum lifetime occurs
	 * @param cacheEntryLifetimeTimeUnit the time unit of {@code cacheEntryLifeTime}
	 */
	public PathVectorRoutingTable(final String nodeName, final int cacheEntryLifetime,
								  final TimeUnit cacheEntryLifetimeTimeUnit) {
		this.nodeName = nodeName;
		this.routingTable = new TimedCache<String, Entry>(cacheEntryLifetime, cacheEntryLifetimeTimeUnit);
	}

	/**
	 * Checks if the path contains a loop.
	 *
	 * @param path the path to check for loops
	 *
	 * @return {@code true} if the path contains a loop, {@code false} otherwise
	 */
	private boolean containsLoop(final List<String> path) {
		return path.contains(nodeName) || Sets.newHashSet(path).size() < path.size();
	}

	/**
	 * Returns all entries of the routing table as an immutable object.
	 *
	 * @return all entries of the routing table
	 */
	public synchronized ImmutableMap<String, Entry> getEntries() {
		return ImmutableMap.copyOf(routingTable);
	}

	/**
	 * Returns the routing table entry for the given {@code destination}.
	 *
	 * @param destination the destination node
	 *
	 * @return the routing table entry or {@code null} if no entry exists
	 */
	public synchronized Entry getEntry(String destination) {
		return routingTable.get(destination);
	}

	/**
	 * Returns the next hop for {@code destination}.
	 *
	 * @param destination the destination node name
	 *
	 * @return next hop or {@code null} if no entry was found in the routing table
	 */
	public synchronized String getNextHop(String destination) {
		Entry entry = routingTable.get(destination);
		return entry != null ? entry.getNextHop() : null;
	}

	/**
	 * Updates the routing table entry if cost is cheaper and path contains no loops.
	 *
	 * @param destination the destination node
	 * @param cost		the paths costs
	 * @param path		the path to the destination node (excluding the own node, including the destination node)
	 *
	 * @return {@code true} if updated, {@code false} otherwise
	 */
	public synchronized boolean updateEntry(final String destination, final long cost, final List<String> path,
											final Channel channel) {

		if (!containsLoop(path)) {

			Entry destinationEntry = routingTable.get(destination);

			// check for longest prefix match
			int tableMatchingPrefixLength = destinationEntry == null ? 0 : getMatchingPrefixLength(
					destination,
					destinationEntry.getNextHop()
			);
			int messageMatchingPrefixLength = getMatchingPrefixLength(destination, path.get(0));

			if (messageMatchingPrefixLength >= tableMatchingPrefixLength) {

				final long tableCost = destinationEntry == null ? Long.MAX_VALUE : destinationEntry.getCost();
				final boolean isBetter = messageMatchingPrefixLength > tableMatchingPrefixLength || cost < tableCost;

				if (isBetter) {

					Entry entry = new Entry(messageMatchingPrefixLength, path, channel);
					log.trace("Updating routing table entry: {}", entry);
					routingTable.put(destination, entry);
					if (log.isDebugEnabled()) {
						logRoutingTable();
					}
					return true;

				} else {
					if (log.isTraceEnabled()) {
						log.trace(
								"Ignoring routing table update for {}: ("
										+ "prefix length table={}, "
										+ "prefix length received={}, "
										+ "cost table={}, "
										+ "cost received={}"
										+ ")!",
								new Object[]{
										destination,
										tableMatchingPrefixLength,
										messageMatchingPrefixLength,
										tableCost,
										cost
								}
						);
					}
					return false;
				}


			} else {
				if (log.isTraceEnabled()) {
					log.trace(
							"Ignoring routing table update for {}: prefix match not longer (table={}, received={})!",
							new Object[]{destination, tableMatchingPrefixLength, messageMatchingPrefixLength}
					);
				}
				return false;
			}

		} else {

			if (log.isTraceEnabled()) {
				log.trace("Ignoring routing table update because it contains a loop: dst={}, path={}",
						destination, Arrays.toString(path.toArray())
				);
			}
			return false;
		}
	}

	private int getMatchingPrefixLength(final String destination, final String entry) {

		final int maxLength = destination.length() < entry.length() ? destination.length() : entry.length();

		int matchingChars = 0;

		final char[] firstChars = destination.toCharArray();
		final char[] secondChars = entry.toCharArray();

		for (int i = 0; i < maxLength; i++) {
			if (firstChars[i] != secondChars[i]) {
				return matchingChars;
			}
			matchingChars++;
		}

		return matchingChars;
	}

	/**
	 * Removes all routes from the table that have {@code remoteNode} as the next hop. This method may be called e.g., if
	 * the connection between this host and {@code remoteNode} was dropped and the route is thereby obsolete.
	 *
	 * @param remoteNode the next hop node that is now unavailable
	 */
	public void removeRoutesOverNextHop(final String remoteNode) {
		for (Iterator<Map.Entry<String, Entry>> iterator = routingTable.entrySet().iterator(); iterator.hasNext(); ) {
			Map.Entry<String, Entry> entry = iterator.next();
			if (entry.getValue().getNextHop().equals(remoteNode)) {
				iterator.remove();
			}
		}
		if (log.isDebugEnabled()) {
			log.debug("Removed routes over \"{}\" as that next hop is now unavailable.", remoteNode);
			logRoutingTable();
		}
	}

	private void logRoutingTable() {
		StringBuilder b = new StringBuilder();
		b.append("RoutingTable:\n");
		for (Map.Entry<String, PathVectorRoutingTable.Entry> entry : getEntries().entrySet()) {
			b.append(entry.getKey());
			b.append(" => [");
			b.append(entry.getValue().getCost());
			b.append("] {");
			b.append(Joiner.on(", ").join(entry.getValue().getPath()));
			b.append("}\n");
		}
		log.debug(b.toString());
	}

}
