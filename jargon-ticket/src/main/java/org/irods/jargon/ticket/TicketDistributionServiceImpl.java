package org.irods.jargon.ticket;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.utils.IRODSUriUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service to assist in the distribution of tickets. This is meant to provide
 * generated links in URL form for tickets, as well as other future supported
 * generation methods.
 * <p>
 * Essentially, this class can help those developing interfaces that might want
 * to create a ticket and then produce a URL for an application that wants to
 * handle tickets. This service is used internally in iDrop web.
 * <p>
 * Eventually, we can expand the ticket distribution channels to include email
 * and other services.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class TicketDistributionServiceImpl extends AbstractTicketService implements TicketDistributionService {

	private final TicketServiceFactory ticketServiceFactory;
	private final TicketDistributionContext ticketDistributionContext;

	public static final Logger log = LoggerFactory.getLogger(TicketDistributionServiceImpl.class);

	/**
	 * Default constructor takes the objects necessary to communicate with iRODS via
	 * Access Objects
	 *
	 * @param irodsAccessObjectFactory
	 *            {@link IRODSAccessObjectFactory} that can create various access
	 *            objects
	 * @param irodsAccount
	 *            {@link IRODSAccount} with login information for the target grid
	 * @throws JargonException
	 */
	TicketDistributionServiceImpl(final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount, final TicketServiceFactory ticketServiceFactory,
			final TicketDistributionContext ticketDistributionContext) throws JargonException {

		super(irodsAccessObjectFactory, irodsAccount);

		if (ticketServiceFactory == null) {
			throw new IllegalArgumentException("null ticketServiceFactory");
		}

		if (ticketDistributionContext == null) {
			throw new IllegalArgumentException("null ticketDistributionContext");
		}

		this.ticketDistributionContext = ticketDistributionContext;
		this.ticketServiceFactory = ticketServiceFactory;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.ticket.TicketDistributionService#
	 * getTicketDistributionForTicket(org.irods.jargon.ticket.Ticket)
	 */
	@Override
	public TicketDistribution getTicketDistributionForTicket(final Ticket ticket) throws JargonException {

		log.info("getTicketDistributionForTicket()");

		if (ticket == null) {
			throw new IllegalArgumentException("null ticket");
		}

		log.info("ticket: {}", ticket);

		// a few short sanity checks on the ticket

		if (ticket.getTicketString() == null || ticket.getTicketString().isEmpty()) {
			throw new IllegalArgumentException("no ticketString in ticket, appears not to be valid");
		}

		if (ticket.getIrodsAbsolutePath() == null || ticket.getIrodsAbsolutePath().isEmpty()) {
			throw new IllegalArgumentException("no irodsAbsolutePath in ticket, appears not to be valid");
		}

		TicketDistribution ticketDistribution = new TicketDistribution();
		ticketDistribution.setTicket(ticket);
		ticketDistribution.setIrodsAccessURI(IRODSUriUtils
				.buildURIForAnAccountWithNoUserInformationIncluded(irodsAccount, ticket.getIrodsAbsolutePath()));

		if (!ticketDistributionContext.getHost().isEmpty()) {
			log.info(
					"host info is present in ticket distribution context, so add a generated URL given the other information...");
			// create a string in the expected scheme
			// <scheme>://<authority><path>?<query>#<fragment>
			StringBuilder sb = new StringBuilder();
			if (ticketDistributionContext.isSsl()) {
				sb.append("https://");
			} else {
				sb.append("http://");
			}

			sb.append(ticketDistributionContext.getHost());

			if (ticketDistributionContext.getPort() > 0) {
				sb.append(':');
				sb.append(ticketDistributionContext.getPort());
			}

			sb.append(ticketDistributionContext.getContext());
			sb.append("?ticketString=");

			try {
				sb.append(URLEncoder.encode(ticket.getTicketString(), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				log.error("unsupported encoding of ticket string", e);
				throw new JargonException("cannot encode ticketString", e);
			}

			sb.append("&objectType=");
			sb.append(ticket.getObjectType());
			sb.append("&ticketType=");
			sb.append(ticket.getType());

			sb.append("&irodsURI=");
			sb.append(ticketDistribution.getIrodsAccessURI().toASCIIString());
			URL accessURL;
			try {
				accessURL = new URL(sb.toString());
				log.info("generated url:{}", accessURL);
			} catch (MalformedURLException e) {
				log.error("malformed url from:{}", sb.toString(), e);
				throw new JargonException(
						"malformed URL for ticketDistribution, probably a malformed ticketDistributionContext");
			}
			ticketDistribution.setTicketURL(accessURL);

			/*
			 * Tack on a ticket landing page for the url with landing page information. This
			 * URL will request display of an intermediate page if the ticket is redeemed,
			 * versus direct download of a file. The processing of such a request is
			 * dependent on the client.
			 */

			sb.append("&landingPage=true");

			URL landingPageURL;
			try {
				landingPageURL = new URL(sb.toString());
				log.info("generated landing url:{}", accessURL);
			} catch (MalformedURLException e) {
				log.error("malformed url from:{}", sb.toString(), e);
				throw new JargonException(
						"malformed URL for ticketDistribution, probably a malformed ticketDistributionContext");
			}
			ticketDistribution.setTicketURLWithLandingPage(landingPageURL);
		}

		return ticketDistribution;

	}

	/**
	 * @return the ticketServiceFactory
	 */
	public TicketServiceFactory getTicketServiceFactory() {
		return ticketServiceFactory;
	}

	/**
	 * @return the ticketDistributionContext
	 */
	public TicketDistributionContext getTicketDistributionContext() {
		return ticketDistributionContext;
	}

}
