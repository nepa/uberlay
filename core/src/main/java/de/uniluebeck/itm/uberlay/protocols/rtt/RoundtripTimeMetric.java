package de.uniluebeck.itm.uberlay.protocols.rtt;


public class RoundtripTimeMetric {

	private final long metric;

	public RoundtripTimeMetric(final long metric) {
		this.metric = metric;
	}

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
