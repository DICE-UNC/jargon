package org.irods.jargon.ticket;

import java.net.URI;
import java.net.URL;

/**
 * Represents a ticket and information about accessing the data for the ticket.
 * This adds a computed URL and other information that can be used to make calls
 * to mid-tier services processing tickets, such as the idrop suite.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class TicketDistribution {

	private Ticket ticket;
	/**
	 * Direct URL for ticket
	 */
	private URL ticketURL;
	private URL ticketURLWithLandingPage;
	private URI irodsAccessURI;

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ticketDistribution:");
		sb.append("\n   ticket:");
		sb.append(ticket);
		sb.append("\n   ticketURL:");
		sb.append(ticketURL);
		sb.append("\n   ticketURLWithLandingPage:");
		sb.append(ticketURLWithLandingPage);
		sb.append("\n   irodsAccessURI:");
		sb.append(irodsAccessURI);
		return sb.toString();
	}

	/**
	 * @return the ticket
	 */
	public Ticket getTicket() {
		return ticket;
	}

	/**
	 * @param ticket
	 *            the ticket to set
	 */
	public void setTicket(final Ticket ticket) {
		this.ticket = ticket;
	}

	/**
	 * @return the ticketURL that can be used to access the data from a mid-tier
	 *         service
	 */
	public URL getTicketURL() {
		return ticketURL;
	}

	/**
	 * Sets the URL that can be used to access the ticket via a mid-tier
	 * service.
	 * 
	 * @param ticketURL
	 *            the ticketURL to set
	 */
	public void setTicketURL(final URL ticketURL) {
		this.ticketURL = ticketURL;
	}

	/**
	 * Return a <ocde>URI</code> in irods:// format that desribes the target
	 * data
	 * 
	 * @return the irodsAccessURI
	 */
	public URI getIrodsAccessURI() {
		return irodsAccessURI;
	}

	/**
	 * @param irodsAccessURI
	 *            the irodsAccessURI to set
	 */
	public void setIrodsAccessURI(final URI irodsAccessURI) {
		this.irodsAccessURI = irodsAccessURI;
	}

	/**
	 * @return the ticketURLWithLandingPage <code>URL</code> with parameters
	 *         that denote that invoking that URL will return an intermediate
	 *         web page appropriate to the given ticket
	 */
	public URL getTicketURLWithLandingPage() {
		return ticketURLWithLandingPage;
	}

	/**
	 * @param ticketURLWithLandingPage
	 *            <code>URL</code> with parameters that denote that invoking
	 *            that URL will return an intermediate web page appropriate to
	 *            the given ticket
	 */
	public void setTicketURLWithLandingPage(final URL ticketURLWithLandingPage) {
		this.ticketURLWithLandingPage = ticketURLWithLandingPage;
	}

}
