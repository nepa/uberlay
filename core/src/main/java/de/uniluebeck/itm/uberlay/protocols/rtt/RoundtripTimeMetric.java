package de.uniluebeck.itm.uberlay.protocols.rtt;


import de.uniluebeck.itm.uberlay.LinkMetric;

public class RoundtripTimeMetric implements LinkMetric {

	private final long metric;

	public RoundtripTimeMetric(final long metric) {
		this.metric = metric;
	}

	@Override
	public long getMetric() {
		return metric;
	}

	@Override
	public String toString() {
		return "RoundtripTimeMetric{" +
				"metric=" + metric +
				'}';
	}
}
