package de.uniluebeck.itm.uberlay.router;

import de.uniluebeck.itm.uberlay.protocols.up.UPAddress;

public class NoRouteToPeerException extends Exception {

	private UPAddress peerAddress;

	public NoRouteToPeerException(final UPAddress peerAddress) {
		this.peerAddress = peerAddress;
	}

	public NoRouteToPeerException(final UPAddress peerAddress, final Throwable cause) {
		super(cause);
		this.peerAddress = peerAddress;
	}

	public NoRouteToPeerException(final UPAddress peerAddress, final String message) {
		super(message);
		this.peerAddress = peerAddress;
	}

	public NoRouteToPeerException(final UPAddress peerAddress, final String message, final Throwable cause) {
		super(message, cause);
		this.peerAddress = peerAddress;
	}

	public UPAddress getPeerAddress() {
		return peerAddress;
	}
}
