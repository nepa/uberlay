package de.uniluebeck.itm.uberlay.core.protocols.pvp;


import com.google.common.collect.Sets;
import de.uniluebeck.itm.tr.util.TimedCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PathVectorRoutingTable {

	private static final Logger log = LoggerFactory.getLogger(PathVectorRoutingTable.class);

	public static class Entry {

		private final String nextHop;

		private final long cost;

		private final List<String> path;

		public Entry(final String nextHop, final long cost, final List<String> path) {
			this.cost = cost;
			this.nextHop = nextHop;
			this.path = path;
		}

		@Override
		public String toString() {
			return "Entry{" +
					"cost=" + cost +
					", nextHop='" + nextHop + '\'' +
					", path=" + path +
					'}';
		}
	}

	private final TimedCache<String, Entry> routingTable;

	public PathVectorRoutingTable(final int cacheEntryLifetime, final TimeUnit cacheEntryLifetimeUnit) {
		routingTable = new TimedCache<String, Entry>(cacheEntryLifetime, cacheEntryLifetimeUnit);
	}

	public Entry getEntry(String destination) {
		return routingTable.get(destination);
	}

	/**
	 * Updates the routing table entry if cost is cheaper and path contains no loops.
	 *
	 * @param destination
	 * @param nextHop
	 * @param cost
	 * @param path
	 * @return {@code true} if updated, {@code false} otherwise
	 */
	public boolean updateEntry(final String destination, final String nextHop, long cost, List<String> path) {

		if (!(Sets.newHashSet(path).size() < path.size())) {

			long oldCost = routingTable.get(destination).cost;

			if (cost < oldCost) {

				Entry entry = new Entry(nextHop, cost, path);
				log.trace("Updating routing table entry: {}", entry);
				routingTable.put(destination, entry);
				return true;

			} else {
				if (log.isTraceEnabled()) {
					log.trace(
							"Not updating routing table entry because the update is more expensive: dst={}, oldcost={}, newcost={}",
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

	/**
	 * Returns the next hop for {@code destination}.
	 *
	 * @param destination
	 * @return next hop or {@code null} if no entry was found in the routing table
	 */
	public String getNextHop(String destination) {
		Entry entry = routingTable.get(destination);
		return entry != null ? entry.nextHop : null;
	}

}
