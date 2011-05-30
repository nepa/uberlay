package de.uniluebeck.itm.uberlay.protocols.up;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

import java.net.SocketAddress;

import static com.google.common.base.Preconditions.checkNotNull;

public class UPAddress extends SocketAddress {

	public static Function<String, UPAddress> STRING_TO_ADDRESS = new Function<String, UPAddress>() {
		@Override
		public UPAddress apply(final String addressString) {
			return new UPAddress(addressString);
		}
	};

	public static Function<UPAddress, String> ADDRESS_TO_STRING = new Function<UPAddress, String>() {
		@Override
		public String apply(final UPAddress input) {
			return input.address;
		}
	};

	private String address;

	public final Predicate<? super UPAddress> EQUALS = new Predicate<UPAddress>() {
		@Override
		public boolean apply(final UPAddress input) {
			return address.equals(input.address);
		}
	};

	public UPAddress(final String address) {
		checkNotNull(address);
		this.address = address;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		final UPAddress upAddress = (UPAddress) o;

		if (!address.equals(upAddress.address)) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return address.hashCode();
	}

	@Override
	public String toString() {
		return address;
	}
}
