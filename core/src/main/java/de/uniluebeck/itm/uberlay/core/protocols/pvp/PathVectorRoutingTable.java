package de.uniluebeck.itm.uberlay.core.protocols.pvp;


import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import de.uniluebeck.itm.tr.util.TimedCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

		private Entry(final long cost, final List<String> path) {
			Preconditions.checkArgument(path.size() > 0);
			this.cost = cost;
			this.path = path;
		}

		@Override
		public String toString() {
			return "Entry{" +
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

		public String getNextHop() {
			return path.get(0);
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
	 * @param destination
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
	public synchronized boolean updateEntry(final String destination, long cost, List<String> path) {

		if (!containsLoop(path)) {

			Entry destinationEntry = routingTable.get(destination);
			long oldCost = destinationEntry == null ? Long.MAX_VALUE : destinationEntry.cost;

			if (cost <= oldCost) {

				Entry entry = new Entry(cost, path);
				log.trace("Updating routing table entry: {}", entry);
				routingTable.put(destination, entry);
				return true;

			} else {
				if (log.isTraceEnabled()) {
					log.trace(
							"Not updating routing table entry because the update is not cheaper: dst={}, oldcost={}, newcost={}",
							new Object[]{
									destination, oldCost, cost
							}
					);
				}
				return false;
			}
		} else {

			if (log.isTraceEnabled()) {
				log.trace("Not updating routing table entry because the update contains a loop: dst={}, path={}",
						destination, Arrays.toString(path.toArray())
				);
			}
			return false;
		}
	}

}
