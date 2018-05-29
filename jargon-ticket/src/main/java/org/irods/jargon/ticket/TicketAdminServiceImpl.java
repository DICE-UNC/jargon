package org.irods.jargon.ticket;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.Tag;
import org.irods.jargon.core.protovalues.ErrorEnum;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSGenQueryExecutor;
import org.irods.jargon.core.pub.ProtocolExtensionPoint;
import org.irods.jargon.core.pub.UserAO;
import org.irods.jargon.core.pub.domain.ObjStat;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.AbstractIRODSQueryResultSet;
import org.irods.jargon.core.query.GenQueryBuilderException;
import org.irods.jargon.core.query.IRODSGenQuery;
import org.irods.jargon.core.query.IRODSGenQueryBuilder;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.IRODSQueryResultSetInterface;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.QueryConditionOperators;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.irods.jargon.core.utils.IRODSDataConversionUtil;
import org.irods.jargon.ticket.Ticket.TicketObjectType;
import org.irods.jargon.ticket.packinstr.TicketAdminInp;
import org.irods.jargon.ticket.packinstr.TicketCreateModeEnum;
import org.irods.jargon.ticket.packinstr.TicketModifyAddOrRemoveTypeEnum;
import org.irods.jargon.ticket.utils.TicketRandomString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TicketAdminServiceImpl extends AbstractTicketService implements TicketAdminService {

	private static final String PARTIAL_START_INDEX_MUST_BE_0 = "partial start index must be >= 0";
	private static final String EXECUTING_TICKET_PI = "executing ticket PI";
	private static final String COMMA_SPACE = ", ";
	private static final String ERROR_IN_TICKET_QUERY = "error in ticket query";
	private static final String TICKET_NOT_FOUND = "IRODS ticket not found";
	public static final Logger log = LoggerFactory.getLogger(TicketAdminServiceImpl.class);

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
	TicketAdminServiceImpl(final IRODSAccessObjectFactory irodsAccessObjectFactory, final IRODSAccount irodsAccount)
			throws JargonException {
		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
		this.irodsAccount = irodsAccount;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.ticket.TicketAdminService#
	 * createTicketFromTicketObjectAsAdminForGivenUser
	 * (org.irods.jargon.ticket.Ticket, java.lang.String)
	 */
	@Override
	public Ticket createTicketFromTicketObjectAsAdminForGivenUser(final Ticket ticket, final String userName)
			throws DuplicateDataException, DataNotFoundException, JargonException {
		log.info("createTicketFromTicketObjectAsAdminForGivenUser()");

		if (ticket == null) {
			throw new IllegalArgumentException("null ticket");
		}

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		log.info("ticket to update:{}", ticket);
		log.info("userName:{}", userName);

		log.info("create a temp password for the given user, if I am not rodsadmin this will fail");

		// generate a temp password for the given user
		UserAO userAO = getIrodsAccessObjectFactory().getUserAO(irodsAccount);
		String tempPassword = userAO.getTemporaryPasswordForASpecifiedUser(userName);
		IRODSAccount tempUserAccount = IRODSAccount.instance(getIrodsAccount().getHost(), getIrodsAccount().getPort(),
				userName, tempPassword, "", getIrodsAccount().getZone(), getIrodsAccount().getDefaultStorageResource());

		log.info("temp password created, delegate to a service for this user");
		Ticket delegateTicket;

		try {
			TicketServiceFactory delegateServiceFactory = new TicketServiceFactoryImpl(irodsAccessObjectFactory);
			TicketAdminService delegateService = delegateServiceFactory.instanceTicketAdminService(tempUserAccount);
			log.info("delegating call to create ticket");
			delegateTicket = delegateService.createTicketFromTicketObject(ticket);
			log.info("created ticket as user:${}", delegateTicket);
		} finally {
			getIrodsAccessObjectFactory().closeSession(tempUserAccount);
		}

		return delegateTicket;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.ticket.TicketAdminService#createTicketFromTicketObject
	 * (org.irods.jargon.ticket.Ticket)
	 */
	@Override
	public Ticket createTicketFromTicketObject(final Ticket ticket)
			throws DuplicateDataException, DataNotFoundException, JargonException {

		log.info("createTicketFromTicketObjectAndSetAnyGivenLimits");
		if (ticket == null) {
			throw new IllegalArgumentException("null ticket");
		}

		log.info("ticket to update:{}", ticket);

		if (ticket.getType() == null) {
			throw new IllegalArgumentException("null type in ticket");
		}

		if (ticket.getIrodsAbsolutePath() == null || ticket.getIrodsAbsolutePath().isEmpty()) {
			throw new IllegalArgumentException("null or empty irodsAbsolutePath");
		}

		IRODSFile ticketFile = irodsAccessObjectFactory.getIRODSFileFactory(getIrodsAccount())
				.instanceIRODSFile(ticket.getIrodsAbsolutePath());
		if (!ticketFile.exists()) {
			log.error("file for ticket does not exist");
		}

		if (ticketFile.isDirectory()) {
			ticket.setObjectType(TicketObjectType.COLLECTION);
		} else {
			ticket.setObjectType(TicketObjectType.DATA_OBJECT);
		}

		ticket.setOwnerName(getIrodsAccount().getUserName());
		ticket.setOwnerZone(getIrodsAccount().getZone());

		log.info("creating base ticket");
		ticket.setTicketString(createTicket(ticket.getType(), ticketFile, ticket.getTicketString()));
		log.info("adding count values and limits");

		if (ticket.getExpireTime() != null) {
			setTicketExpiration(ticket.getTicketString(), ticket.getExpireTime());
		}

		if (ticket.getUsesLimit() > 0) {
			setTicketUsesLimit(ticket.getTicketString(), ticket.getUsesLimit());
		}

		if (ticket.getWriteByteLimit() > 0) {
			setTicketByteWriteLimit(ticket.getTicketString(), ticket.getWriteByteLimit());
		}

		if (ticket.getWriteFileLimit() > 0) {
			setTicketFileWriteLimit(ticket.getTicketString(), ticket.getWriteFileLimit());
		}

		return ticket;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.ticket.TicketAdminService#createTicket(org.irods.jargon
	 * .ticket.packinstr.TicketCreateModeEnum,
	 * org.irods.jargon.core.pub.io.IRODSFile, java.lang.String)
	 */
	@Override
	public String createTicket(final TicketCreateModeEnum mode, final IRODSFile file, final String ticketId)
			throws JargonException, DuplicateDataException {

		if (file == null) {
			throw new IllegalArgumentException("cannot create ticket with null IRODS file/collection");
		}

		log.info("creating at ticket for :{}", file.getPath());

		if (mode == null) {
			throw new IllegalArgumentException("cannot create ticket with null create mode - read or write access");
		}

		String myTicketId = ticketId;

		if (myTicketId == null || myTicketId.isEmpty()) {
			// create a new ticket string 15 chars in length
			myTicketId = new TicketRandomString(15).nextString();
		}
		log.info("ticket creation mode is:{}", mode);

		/*
		 * If a ticket is created on a collection, then inheritance is set. Otherwise,
		 * others can add files to the colletion and the collection owner may not be
		 * able to manage the files.
		 */
		if (file.isDirectory()) {
			log.info("ticket is for a collection, set inherit to true on collection:{}", file.getAbsolutePath());
			CollectionAO collectionAO = getIrodsAccessObjectFactory().getCollectionAO(getIrodsAccount());
			collectionAO.setAccessPermissionInherit(irodsAccount.getZone(), file.getAbsolutePath(), true);
			log.info("collection inheritance set");

		}

		TicketAdminInp ticketPI = TicketAdminInp.instanceForCreate(mode, file.getAbsolutePath(), myTicketId);
		log.info(EXECUTING_TICKET_PI);

		ProtocolExtensionPoint pep = irodsAccessObjectFactory.getProtocolExtensionPoint(irodsAccount);
		Tag ticketOperationResponse = pep.irodsFunction(ticketPI);

		log.info("received response from ticket operation:{}", ticketOperationResponse);

		return myTicketId;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.irods.jargon.ticket.TicketAdminService#deleteTicket(java.lang.String)
	 */
	@Override
	public boolean deleteTicket(final String ticketId) throws JargonException {

		Tag ticketOperationResponse = null;
		boolean response = true;

		if ((ticketId == null) || (ticketId.isEmpty())) {
			throw new IllegalArgumentException("cannot delete ticket with null or empty ticketId");
		}

		log.info("deleting ticket id/string:{}", ticketId);

		TicketAdminInp ticketPI = TicketAdminInp.instanceForDelete(ticketId);
		log.info(EXECUTING_TICKET_PI);

		ProtocolExtensionPoint pep = irodsAccessObjectFactory.getProtocolExtensionPoint(irodsAccount);
		try {
			ticketOperationResponse = pep.irodsFunction(ticketPI);
		} catch (JargonException e) {
			if (e.getUnderlyingIRODSExceptionCode() == ErrorEnum.CAT_TICKET_INVALID.getInt()) {
				response = false;
			}
		}

		log.info("received response from ticket operation:{}", ticketOperationResponse);

		return response;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.ticket.TicketAdminService#listAllTicketsForDataObjects
	 * (int)
	 */
	@Override
	public List<Ticket> listAllTicketsForDataObjects(final int partialStartIndex) throws JargonException {

		log.info("listAllTicketsForDataObjects()");

		if (partialStartIndex < 0) {
			throw new IllegalArgumentException(PARTIAL_START_INDEX_MUST_BE_0);
		}

		List<Ticket> tickets = new ArrayList<Ticket>();

		StringBuilder sb = new StringBuilder();
		sb.append(buildQuerySelectForLSAllTicketsForDataObjects());

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(sb.toString(),
				irodsAccessObjectFactory.getJargonProperties().getMaxFilesAndDirsQueryMax());

		IRODSGenQueryExecutor irodsGenQueryExecutor = irodsAccessObjectFactory.getIRODSGenQueryExecutor(irodsAccount);

		AbstractIRODSQueryResultSet resultSet = null;

		try {
			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(irodsQuery, partialStartIndex);
			Ticket ticket = null;
			StringBuilder absPathBuilder = null;
			for (IRODSQueryResultRow row : resultSet.getResults()) {
				ticket = new Ticket();
				putResultDataIntoTicketCommonValues(ticket, row);
				absPathBuilder = new StringBuilder();
				absPathBuilder.append(row.getColumn(14));
				absPathBuilder.append('/');
				absPathBuilder.append(row.getColumn(13));
				ticket.setIrodsAbsolutePath(absPathBuilder.toString());
				// add info to track position in records for possible requery
				ticket.setLastResult(row.isLastResult());
				ticket.setCount(row.getRecordCount());
				log.info("adding ticket to results:{}", ticket);
				tickets.add(ticket);
			}

		} catch (JargonQueryException e) {
			log.error("query exception for ticket query:{}", irodsQuery, e);
			throw new JargonException(ERROR_IN_TICKET_QUERY, e);
		}

		return tickets;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.ticket.TicketAdminService#listAllTicketsForCollections
	 * (int)
	 */
	@Override
	public List<Ticket> listAllTicketsForCollections(final int partialStartIndex) throws JargonException {

		log.info("listAllTicketsForCollections()");

		if (partialStartIndex < 0) {
			throw new IllegalArgumentException(PARTIAL_START_INDEX_MUST_BE_0);
		}

		List<Ticket> tickets = new ArrayList<Ticket>();

		StringBuilder sb = new StringBuilder();
		sb.append(buildQuerySelectForLSAllTicketsForCollections());

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(sb.toString(),
				irodsAccessObjectFactory.getJargonProperties().getMaxFilesAndDirsQueryMax());

		IRODSGenQueryExecutor irodsGenQueryExecutor = irodsAccessObjectFactory.getIRODSGenQueryExecutor(irodsAccount);

		AbstractIRODSQueryResultSet resultSet = null;

		try {
			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(irodsQuery, partialStartIndex);
			Ticket ticket = null;
			for (IRODSQueryResultRow row : resultSet.getResults()) {
				ticket = new Ticket();
				putResultDataIntoTicketCommonValues(ticket, row);
				ticket.setIrodsAbsolutePath(row.getColumn(13));
				log.info("adding ticket to results:{}", ticket);
				tickets.add(ticket);
			}

		} catch (JargonQueryException e) {
			log.error("query exception for ticket query:{}", irodsQuery, e);
			throw new JargonException(ERROR_IN_TICKET_QUERY, e);
		}

		return tickets;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.irods.jargon.ticket.TicketAdminService#listAllTicketsForGivenCollection
	 * (java.lang.String, int)
	 */
	@Override
	public List<Ticket> listAllTicketsForGivenCollection(final String irodsAbsolutePath, final int partialStartIndex)
			throws FileNotFoundException, JargonException {

		log.info("listAllTicketsForGivenCollection()");

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty irodsAbsolutePath");
		}

		if (partialStartIndex < 0) {
			throw new IllegalArgumentException(PARTIAL_START_INDEX_MUST_BE_0);
		}

		log.info("irodsAbsolutePath:{}", irodsAbsolutePath);
		log.info("partialStartIndex:{}", partialStartIndex);

		log.info("getting objStat...");

		ObjStat objStat = irodsAccessObjectFactory.getCollectionAndDataObjectListAndSearchAO(getIrodsAccount())
				.retrieveObjectStatForPath(irodsAbsolutePath);

		if (!objStat.isSomeTypeOfCollection()) {
			log.error("ObjStat indicates that this is not some type of collection:{}", objStat);
			throw new JargonException("path is not a collection");
		}

		List<Ticket> tickets = new ArrayList<Ticket>();

		AbstractIRODSQueryResultSet resultSet = null;

		try {
			IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, null);
			addSelectsForTicketsCommonToQueryBuilder(builder);
			addQuerySelectsForListAllTicketsForCollections(builder);
			builder.addConditionAsGenQueryField(RodsGenQueryEnum.COL_TICKET_COLL_NAME, QueryConditionOperators.EQUAL,
					irodsAbsolutePath);

			IRODSGenQueryExecutor irodsGenQueryExecutor = irodsAccessObjectFactory
					.getIRODSGenQueryExecutor(irodsAccount);

			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(
					builder.exportIRODSQueryFromBuilder(
							getIrodsAccessObjectFactory().getJargonProperties().getMaxFilesAndDirsQueryMax()),
					partialStartIndex);
			Ticket ticket = null;
			for (IRODSQueryResultRow row : resultSet.getResults()) {
				ticket = new Ticket();
				putResultDataIntoTicketCommonValues(ticket, row);
				ticket.setIrodsAbsolutePath(row.getColumn(13));
				// add info to track position in records for possible requery
				ticket.setLastResult(row.isLastResult());
				ticket.setCount(row.getRecordCount());
				log.info("adding ticket to results:{}", ticket);
				tickets.add(ticket);
			}

		} catch (JargonQueryException e) {
			log.error("query exception for ticket query", e);
			throw new JargonException(ERROR_IN_TICKET_QUERY, e);
		} catch (GenQueryBuilderException e) {
			log.error("query exception for ticket query", e);
			throw new JargonException(ERROR_IN_TICKET_QUERY, e);
		}

		return tickets;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.irods.jargon.ticket.TicketAdminService#listAllTicketsForGivenDataObject
	 * (java.lang.String, int)
	 */
	@Override
	public List<Ticket> listAllTicketsForGivenDataObject(final String irodsAbsolutePath, final int partialStartIndex)
			throws FileNotFoundException, JargonException {

		log.info("listAllTicketsForGivenDataObject()");

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty irodsAbsolutePath");
		}

		if (partialStartIndex < 0) {
			throw new IllegalArgumentException(PARTIAL_START_INDEX_MUST_BE_0);
		}

		log.info("irodsAbsolutePath:{}", irodsAbsolutePath);
		log.info("partialStartIndex:{}", partialStartIndex);

		log.info("getting objStat...");

		ObjStat objStat = irodsAccessObjectFactory.getCollectionAndDataObjectListAndSearchAO(getIrodsAccount())
				.retrieveObjectStatForPath(irodsAbsolutePath);

		if (objStat.isSomeTypeOfCollection()) {
			log.error("ObjStat indicates that this is not some type of data object:{}", objStat);
			throw new JargonException("path is not a data object");
		}

		List<Ticket> tickets = new ArrayList<Ticket>();

		AbstractIRODSQueryResultSet resultSet = null;
		IRODSFile dataFile = irodsAccessObjectFactory.getIRODSFileFactory(getIrodsAccount())
				.instanceIRODSFile(irodsAbsolutePath);

		try {
			IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, null);
			addSelectsForTicketsCommonToQueryBuilder(builder);
			addQuerySelectsForListAllTicketsForDataObjects(builder);
			builder.addConditionAsGenQueryField(RodsGenQueryEnum.COL_TICKET_DATA_NAME, QueryConditionOperators.EQUAL,
					dataFile.getName()).addConditionAsGenQueryField(RodsGenQueryEnum.COL_TICKET_DATA_COLL_NAME,
							QueryConditionOperators.EQUAL, dataFile.getParent());

			IRODSGenQueryExecutor irodsGenQueryExecutor = irodsAccessObjectFactory
					.getIRODSGenQueryExecutor(irodsAccount);

			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(
					builder.exportIRODSQueryFromBuilder(
							getIrodsAccessObjectFactory().getJargonProperties().getMaxFilesAndDirsQueryMax()),
					partialStartIndex);
			Ticket ticket = null;
			for (IRODSQueryResultRow row : resultSet.getResults()) {
				ticket = new Ticket();
				putResultDataIntoTicketCommonValues(ticket, row);
				StringBuilder absPathBuilder = new StringBuilder();
				absPathBuilder.append(row.getColumn(14));
				absPathBuilder.append('/');
				absPathBuilder.append(row.getColumn(13));
				ticket.setIrodsAbsolutePath(absPathBuilder.toString());
				// add info to track position in records for possible requery
				ticket.setLastResult(row.isLastResult());
				ticket.setCount(row.getRecordCount());
				log.info("adding ticket to results:{}", ticket);
				tickets.add(ticket);
			}

		} catch (JargonQueryException e) {
			log.error("query exception for ticket query", e);
			throw new JargonException(ERROR_IN_TICKET_QUERY, e);
		} catch (GenQueryBuilderException e) {
			log.error("query exception for ticket query", e);
			throw new JargonException(ERROR_IN_TICKET_QUERY, e);
		}

		return tickets;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.ticket.TicketAdminService#listAllTicketsForCollections
	 * (int)
	 */
	@Override
	public List<Ticket> listAllTickets(final int partialStartIndex) throws JargonException {

		log.info("listAllTickets()");

		if (partialStartIndex < 0) {
			throw new IllegalArgumentException(PARTIAL_START_INDEX_MUST_BE_0);
		}

		List<Ticket> tickets = new ArrayList<Ticket>();

		StringBuilder sb = new StringBuilder();
		sb.append(buildQuerySelectForTicketsCommon());

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(sb.toString(),
				irodsAccessObjectFactory.getJargonProperties().getMaxFilesAndDirsQueryMax());

		IRODSGenQueryExecutor irodsGenQueryExecutor = irodsAccessObjectFactory.getIRODSGenQueryExecutor(irodsAccount);

		AbstractIRODSQueryResultSet resultSet = null;

		try {
			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(irodsQuery, partialStartIndex);
			Ticket ticket = null;
			for (IRODSQueryResultRow row : resultSet.getResults()) {
				ticket = new Ticket();
				putResultDataIntoTicketCommonValues(ticket, row);
				log.info("adding ticket to results:{}", ticket);
				tickets.add(ticket);
			}

		} catch (JargonQueryException e) {
			log.error("query exception for ticket query:{}", irodsQuery, e);
			throw new JargonException(ERROR_IN_TICKET_QUERY, e);
		}

		return tickets;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.irods.jargon.ticket.TicketAdminService#getTicketForSpecifiedTicketString
	 * (int)
	 */
	@Override
	public Ticket getTicketForSpecifiedTicketString(final String ticketId)
			throws DataNotFoundException, JargonException {

		Ticket ticket = null;
		IRODSQueryResultSetInterface resultSet = null;
		String queryCommon = null;

		Ticket.TicketObjectType objectType = getTicketObjectType(ticketId);
		if (objectType.equals(Ticket.TicketObjectType.DATA_OBJECT)) {
			queryCommon = buildQuerySelectForLSAllTicketsForDataObjects();
		} else {
			queryCommon = buildQuerySelectForLSAllTicketsForCollections();
		}
		// add where clause
		StringBuilder queryFull = new StringBuilder();
		queryFull.append(queryCommon);
		queryFull.append(" where ");
		queryFull.append(RodsGenQueryEnum.COL_TICKET_STRING.getName());
		queryFull.append(" = ");
		queryFull.append("'");
		queryFull.append(ticketId);
		queryFull.append("'");

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(queryFull.toString(), 2);

		IRODSGenQueryExecutor irodsGenQueryExecutor = irodsAccessObjectFactory.getIRODSGenQueryExecutor(irodsAccount);
		try {
			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(irodsQuery, 0);
			if ((resultSet == null) || (resultSet.getResults().isEmpty())) {
				throw new DataNotFoundException(TICKET_NOT_FOUND);
			}
			IRODSQueryResultRow row = resultSet.getFirstResult();
			ticket = new Ticket();
			putResultDataIntoTicketCommonValues(ticket, row);
			if (objectType.equals(Ticket.TicketObjectType.DATA_OBJECT)) {
				StringBuilder absPathBuilder = new StringBuilder();
				absPathBuilder.append(row.getColumn(14));
				absPathBuilder.append('/');
				absPathBuilder.append(row.getColumn(13));
				ticket.setIrodsAbsolutePath(absPathBuilder.toString());
			} else {
				ticket.setIrodsAbsolutePath(resultSet.getFirstResult().getColumn(13));

			}
		} catch (JargonQueryException e) {
			log.error("query exception for ticket query: {}", irodsQuery, e);
			throw new JargonException(ERROR_IN_TICKET_QUERY, e);
		}

		return ticket;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.ticket.TicketAdminService#
	 * listAllUserRestrictionsForSpecifiedTicket(java.lang.String)
	 */
	@Override
	public List<String> listAllUserRestrictionsForSpecifiedTicket(final String ticketId, final int partialStartIndex)
			throws JargonException {

		return listRestrictionsForSpecifiedTicketCommon(ticketId, RodsGenQueryEnum.COL_TICKET_ALLOWED_USER_NAME,
				partialStartIndex);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.ticket.TicketAdminService#
	 * listAllGroupRestrictionsForSpecifiedTicket(java.lang.String)
	 */
	@Override
	public List<String> listAllGroupRestrictionsForSpecifiedTicket(final String ticketId, final int partialStartIndex)
			throws JargonException {

		return listRestrictionsForSpecifiedTicketCommon(ticketId, RodsGenQueryEnum.COL_TICKET_ALLOWED_GROUP_NAME,
				partialStartIndex);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.ticket.TicketAdminService#
	 * listAllHostRestrictionsForSpecifiedTicket(java.lang.String)
	 */
	@Override
	public List<String> listAllHostRestrictionsForSpecifiedTicket(final String ticketId, final int partialStartIndex)
			throws JargonException {

		return listRestrictionsForSpecifiedTicketCommon(ticketId, RodsGenQueryEnum.COL_TICKET_ALLOWED_HOST,
				partialStartIndex);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.ticket.TicketAdminService#setTicketUsesLimit(java.lang
	 * .String)
	 */
	@Override
	public boolean setTicketUsesLimit(final String ticketId, final int usesLimit) throws JargonException {

		Tag ticketOperationResponse = null;
		boolean response = true;

		if ((ticketId == null) || (ticketId.isEmpty())) {
			throw new IllegalArgumentException("cannot modify ticket with null or empty ticketId");
		}

		if (usesLimit < 0) {
			throw new IllegalArgumentException("cannot modify a ticket with uses count less than 0");
		}

		log.info("modifying ticket id/string:{}", ticketId);

		TicketAdminInp ticketPI = TicketAdminInp.instanceForModifyNumberOfUses(ticketId, usesLimit);
		log.info(EXECUTING_TICKET_PI);

		ProtocolExtensionPoint pep = irodsAccessObjectFactory.getProtocolExtensionPoint(irodsAccount);
		try {
			ticketOperationResponse = pep.irodsFunction(ticketPI);
		} catch (JargonException e) {
			if (e.getUnderlyingIRODSExceptionCode() == ErrorEnum.CAT_TICKET_INVALID.getInt()) {
				response = false;
			}
		}

		log.info("received response from ticket operation:{}", ticketOperationResponse);

		return response;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.ticket.TicketAdminService#setTicketFileWriteLimit(java
	 * .lang.String)
	 */
	@Override
	public boolean setTicketFileWriteLimit(final String ticketId, final int fileWriteLimit) throws JargonException {

		Tag ticketOperationResponse = null;
		boolean response = true;

		if ((ticketId == null) || (ticketId.isEmpty())) {
			throw new IllegalArgumentException("cannot modify ticket with null or empty ticketId");
		}

		if (fileWriteLimit < 0) {
			throw new IllegalArgumentException("cannot modify a ticket with file write less than 0");
		}

		log.info("modifying ticket id/string:{}", ticketId);

		TicketAdminInp ticketPI = TicketAdminInp.instanceForModifyFileWriteNumber(ticketId, fileWriteLimit);
		log.info(EXECUTING_TICKET_PI);

		ProtocolExtensionPoint pep = irodsAccessObjectFactory.getProtocolExtensionPoint(irodsAccount);
		try {
			ticketOperationResponse = pep.irodsFunction(ticketPI);
		} catch (JargonException e) {
			if (e.getUnderlyingIRODSExceptionCode() == ErrorEnum.CAT_TICKET_INVALID.getInt()) {
				response = false;
			}
		}

		log.info("received response from ticket operation:{}", ticketOperationResponse);

		return response;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.ticket.TicketAdminService#setTicketByteWriteLimit(java
	 * .lang.String)
	 */
	@Override
	public boolean setTicketByteWriteLimit(final String ticketId, final long byteWriteLimit) throws JargonException {

		Tag ticketOperationResponse = null;
		boolean response = true;

		if ((ticketId == null) || (ticketId.isEmpty())) {
			throw new IllegalArgumentException("cannot modify ticket with null or empty ticketId");
		}

		if (byteWriteLimit < 0) {
			throw new IllegalArgumentException("cannot modify a ticket with byte write count less than 0");
		}

		log.info("modifying ticket id/string:{}", ticketId);

		TicketAdminInp ticketPI = TicketAdminInp.instanceForModifyByteWriteNumber(ticketId, byteWriteLimit);
		log.info(EXECUTING_TICKET_PI);

		ProtocolExtensionPoint pep = irodsAccessObjectFactory.getProtocolExtensionPoint(irodsAccount);
		try {
			ticketOperationResponse = pep.irodsFunction(ticketPI);
		} catch (JargonException e) {
			if (e.getUnderlyingIRODSExceptionCode() == ErrorEnum.CAT_TICKET_INVALID.getInt()) {
				response = false;
			}
		}

		log.info("received response from ticket operation:{}", ticketOperationResponse);

		return response;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.ticket.TicketAdminService#setTicketExpiration(java.lang
	 * .String)
	 */
	@Override
	public boolean setTicketExpiration(final String ticketId, final Date expirationDate) throws JargonException {

		Tag ticketOperationResponse = null;
		boolean response = true;

		if ((ticketId == null) || (ticketId.isEmpty())) {
			throw new IllegalArgumentException("cannot modify ticket with null or empty ticketId");
		}

		log.info("modifying ticket id/string:{}", ticketId);

		TicketAdminInp ticketPI = TicketAdminInp.instanceForModifyExpiration(ticketId, expirationDate);
		log.info(EXECUTING_TICKET_PI);

		ProtocolExtensionPoint pep = irodsAccessObjectFactory.getProtocolExtensionPoint(irodsAccount);
		try {
			ticketOperationResponse = pep.irodsFunction(ticketPI);
		} catch (JargonException e) {
			if (e.getUnderlyingIRODSExceptionCode() == ErrorEnum.CAT_TICKET_INVALID.getInt()) {
				response = false;
			}
		}

		log.info("received response from ticket operation:{}", ticketOperationResponse);

		return response;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.ticket.TicketAdminService#addTicketUserRestriction(java
	 * .lang.String)
	 */
	@Override
	public boolean addTicketUserRestriction(final String ticketId, final String userId) throws JargonException {

		Tag ticketOperationResponse = null;
		boolean response = true;

		if ((ticketId == null) || (ticketId.isEmpty())) {
			throw new IllegalArgumentException("cannot modify ticket with null or empty ticketId");
		}

		if ((userId == null) || (userId.isEmpty())) {
			throw new IllegalArgumentException("cannot modify ticket with null or empty userId");
		}

		log.info("modifying ticket id/string:{}", ticketId);

		TicketAdminInp ticketPI = TicketAdminInp.instanceForModifyAddAccess(ticketId,
				TicketModifyAddOrRemoveTypeEnum.TICKET_MODIFY_USER, userId);
		log.info(EXECUTING_TICKET_PI);

		ProtocolExtensionPoint pep = irodsAccessObjectFactory.getProtocolExtensionPoint(irodsAccount);
		try {
			ticketOperationResponse = pep.irodsFunction(ticketPI);
		} catch (JargonException e) {
			if (e.getUnderlyingIRODSExceptionCode() == ErrorEnum.CAT_TICKET_INVALID.getInt()) {
				response = false;
			} else {
				throw e;
			}
		}

		log.info("received response from ticket operation:{}", ticketOperationResponse);

		return response;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.ticket.TicketAdminService#addTicketUserRestriction(java
	 * .lang.String)
	 */
	@Override
	public boolean removeTicketUserRestriction(final String ticketId, final String userId) throws JargonException {

		Tag ticketOperationResponse = null;
		boolean response = true;

		if ((ticketId == null) || (ticketId.isEmpty())) {
			throw new IllegalArgumentException("cannot modify ticket with null or empty ticketId");
		}

		if ((userId == null) || (userId.isEmpty())) {
			throw new IllegalArgumentException("cannot modify ticket with null or empty userId");
		}

		log.info("modifying ticket id/string:{}", ticketId);

		TicketAdminInp ticketPI = TicketAdminInp.instanceForModifyRemoveAccess(ticketId,
				TicketModifyAddOrRemoveTypeEnum.TICKET_MODIFY_USER, userId);
		log.info(EXECUTING_TICKET_PI);

		ProtocolExtensionPoint pep = irodsAccessObjectFactory.getProtocolExtensionPoint(irodsAccount);
		try {
			ticketOperationResponse = pep.irodsFunction(ticketPI);
		} catch (JargonException e) {
			if (e.getUnderlyingIRODSExceptionCode() == ErrorEnum.CAT_TICKET_INVALID.getInt()) {
				response = false;
			} else {
				throw e;
			}
		}

		log.info("received response from ticket operation:{}", ticketOperationResponse);

		return response;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.ticket.TicketAdminService#addTicketGroupRestriction(
	 * java.lang.String)
	 */
	@Override
	public boolean addTicketGroupRestriction(final String ticketId, final String groupId) throws JargonException {

		Tag ticketOperationResponse = null;
		boolean response = true;

		if ((ticketId == null) || (ticketId.isEmpty())) {
			throw new IllegalArgumentException("cannot modify ticket with null or empty ticketId");
		}

		if ((groupId == null) || (groupId.isEmpty())) {
			throw new IllegalArgumentException("cannot modify ticket with null or empty groupId");
		}

		log.info("modifying ticket id/string:{}", ticketId);

		TicketAdminInp ticketPI = TicketAdminInp.instanceForModifyAddAccess(ticketId,
				TicketModifyAddOrRemoveTypeEnum.TICKET_MODIFY_GROUP, groupId);
		log.info(EXECUTING_TICKET_PI);

		ProtocolExtensionPoint pep = irodsAccessObjectFactory.getProtocolExtensionPoint(irodsAccount);
		try {
			ticketOperationResponse = pep.irodsFunction(ticketPI);
		} catch (JargonException e) {
			if (e.getUnderlyingIRODSExceptionCode() == ErrorEnum.CAT_TICKET_INVALID.getInt()) {
				response = false;
			} else {
				throw e;
			}
		}

		log.info("received response from ticket operation:{}", ticketOperationResponse);

		return response;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.ticket.TicketAdminService#addTicketGroupRestriction(
	 * java.lang.String)
	 */
	@Override
	public boolean removeTicketGroupRestriction(final String ticketId, final String groupId) throws JargonException {

		Tag ticketOperationResponse = null;
		boolean response = true;

		if ((ticketId == null) || (ticketId.isEmpty())) {
			throw new IllegalArgumentException("cannot modify ticket with null or empty ticketId");
		}

		if ((groupId == null) || (groupId.isEmpty())) {
			throw new IllegalArgumentException("cannot modify ticket with null or empty groupId");
		}

		log.info("modifying ticket id/string:{}", ticketId);

		TicketAdminInp ticketPI = TicketAdminInp.instanceForModifyRemoveAccess(ticketId,
				TicketModifyAddOrRemoveTypeEnum.TICKET_MODIFY_GROUP, groupId);
		log.info(EXECUTING_TICKET_PI);

		ProtocolExtensionPoint pep = irodsAccessObjectFactory.getProtocolExtensionPoint(irodsAccount);
		try {
			ticketOperationResponse = pep.irodsFunction(ticketPI);
		} catch (JargonException e) {
			if (e.getUnderlyingIRODSExceptionCode() == ErrorEnum.CAT_TICKET_INVALID.getInt()) {
				response = false;
			} else {
				throw e;
			}
		}

		log.info("received response from ticket operation:{}", ticketOperationResponse);

		return response;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.ticket.TicketAdminService#addTicketHostRestriction(java
	 * .lang.String)
	 */
	@Override
	public boolean addTicketHostRestriction(final String ticketId, final String host) throws JargonException {

		Tag ticketOperationResponse = null;
		boolean response = true;

		if ((ticketId == null) || (ticketId.isEmpty())) {
			throw new IllegalArgumentException("cannot modify ticket with null or empty ticketId");
		}

		if ((host == null) || (host.isEmpty())) {
			throw new IllegalArgumentException("cannot modify ticket with null or empty host");
		}

		log.info("modifying ticket id/string:{}", ticketId);

		TicketAdminInp ticketPI = TicketAdminInp.instanceForModifyAddAccess(ticketId,
				TicketModifyAddOrRemoveTypeEnum.TICKET_MODIFY_HOST, host);
		log.info(EXECUTING_TICKET_PI);

		ProtocolExtensionPoint pep = irodsAccessObjectFactory.getProtocolExtensionPoint(irodsAccount);
		try {
			ticketOperationResponse = pep.irodsFunction(ticketPI);
		} catch (JargonException e) {
			if (e.getUnderlyingIRODSExceptionCode() == ErrorEnum.CAT_TICKET_INVALID.getInt()) {
				response = false;
			} else {
				throw e;
			}
		}

		log.info("received response from ticket operation:{}", ticketOperationResponse);

		return response;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.ticket.TicketAdminService#addTicketHostRestriction(java
	 * .lang.String)
	 */
	@Override
	public boolean removeTicketHostRestriction(final String ticketId, final String host) throws JargonException {

		Tag ticketOperationResponse = null;
		boolean response = true;

		if ((ticketId == null) || (ticketId.isEmpty())) {
			throw new IllegalArgumentException("cannot modify ticket with null or empty ticketId");
		}

		if ((host == null) || (host.isEmpty())) {
			throw new IllegalArgumentException("cannot modify ticket with null or empty host");
		}

		log.info("modifying ticket id/string:{}", ticketId);

		TicketAdminInp ticketPI = TicketAdminInp.instanceForModifyRemoveAccess(ticketId,
				TicketModifyAddOrRemoveTypeEnum.TICKET_MODIFY_HOST, host);
		log.info(EXECUTING_TICKET_PI);

		ProtocolExtensionPoint pep = irodsAccessObjectFactory.getProtocolExtensionPoint(irodsAccount);
		try {
			ticketOperationResponse = pep.irodsFunction(ticketPI);
		} catch (JargonException e) {
			if (e.getUnderlyingIRODSExceptionCode() == ErrorEnum.CAT_TICKET_INVALID.getInt()) {
				response = false;
			} else {
				throw e;
			}
		}

		log.info("received response from ticket operation:{}", ticketOperationResponse);

		return response;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.ticket.TicketAdminService#deleteAllTicketsForThisUser
	 * (java.lang.String)
	 */
	@Override
	public boolean deleteAllTicketsForThisUser() throws JargonException {

		boolean returnVal = false;

		List<Ticket> tickets = listAllTickets(0);

		for (Ticket ticket : tickets) {
			// TODO: the following is an overhead that might be removable later-
			// MCC
			if (ticket.getTicketString().isEmpty()) {
				log.warn("unable to delete a ticket with an empty string:{}", ticket);
				continue;
			}
			returnVal = true;
			deleteTicket(ticket.getTicketString());
			tickets = listAllTickets(tickets.size());
		}

		return returnVal;
	}

	/**
	 * Given a result row from a ticket query, put the values into the provided
	 * {@code Ticket} common to tickets for collections and data objects
	 *
	 * @param ticket
	 *            {@link Ticket} object that will be initialized with values from
	 *            the query result row. The provided {@code Ticket} in the method
	 *            parameter will be updated by this method.
	 * @param row
	 *            {@link IRODSQueryResultRow} from a query for the ticket as
	 *            specified by the methods internal to this object. This is not a
	 *            generally applicable method,, rather it assumes the columns have
	 *            been requested in a certain order.
	 * @throws JargonException
	 */
	private void putResultDataIntoTicketCommonValues(final Ticket ticket, final IRODSQueryResultRow row)
			throws JargonException {
		ticket.setTicketId(row.getColumn(0));
		ticket.setTicketString(row.getColumn(1));
		ticket.setType(TicketCreateModeEnum.findTypeByString(row.getColumn(2)));
		ticket.setObjectType(findObjectType(row.getColumn(3)));
		ticket.setOwnerName(row.getColumn(4));
		ticket.setOwnerZone(row.getColumn(5));
		ticket.setUsesCount(IRODSDataConversionUtil.getIntOrZeroFromIRODSValue(row.getColumn(6)));
		ticket.setUsesLimit(IRODSDataConversionUtil.getIntOrZeroFromIRODSValue(row.getColumn(7)));
		ticket.setWriteFileCount(IRODSDataConversionUtil.getIntOrZeroFromIRODSValue(row.getColumn(8)));
		ticket.setWriteFileLimit(IRODSDataConversionUtil.getIntOrZeroFromIRODSValue(row.getColumn(9)));
		ticket.setWriteByteCount(IRODSDataConversionUtil.getLongOrZeroFromIRODSValue(row.getColumn(10)));
		ticket.setWriteByteLimit(IRODSDataConversionUtil.getLongOrZeroFromIRODSValue(row.getColumn(11)));
		ticket.setExpireTime(IRODSDataConversionUtil.getDateFromIRODSValue(row.getColumn(12)));
	}

	/**
	 * Build the select part of a query dealing with tickets for collections
	 *
	 * @return {@code String} with a gen query select statemetn for values for
	 *         collections
	 */
	private String buildQuerySelectForLSAllTicketsForCollections() {

		StringBuilder queryString = new StringBuilder();
		queryString.append(buildQuerySelectForTicketsCommon());
		queryString.append(COMMA_SPACE);
		queryString.append(RodsGenQueryEnum.COL_TICKET_COLL_NAME.getName());
		return queryString.toString();
	}

	/**
	 * Add the collection specific elements to a query
	 *
	 * @param builder
	 *            {@link IRODSGenQueryBuilder}
	 * @throws GenQueryBuilderException
	 */
	private void addQuerySelectsForListAllTicketsForCollections(final IRODSGenQueryBuilder builder)
			throws GenQueryBuilderException {

		if (builder == null) {
			throw new IllegalArgumentException("null builder");
		}

		builder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_TICKET_COLL_NAME);
	}

	/**
	 * Given a {@code String} representation of an object type for the ticket in
	 * iRODS, return an emum equivalent value
	 *
	 * @param objectTypeFromTicketData
	 * @return
	 */
	private Ticket.TicketObjectType findObjectType(final String objectTypeFromTicketData) {
		if (objectTypeFromTicketData == null || objectTypeFromTicketData.isEmpty()) {
			throw new IllegalArgumentException("null or empty objectTypeFromTicketData");
		}

		if (objectTypeFromTicketData.equals("data")) {
			return Ticket.TicketObjectType.DATA_OBJECT;
		} else {
			return Ticket.TicketObjectType.COLLECTION;
		}
	}

	/**
	 * Build the select portion of the gen query when querying for tickets
	 * associated with data objects in iRODS (Files)
	 *
	 * @return {@code String} with the iRODS gen query 'SELECT' statement without a
	 *         WHERE keyword or clause. This can be augmented by the caller to
	 *         produce an iRODS gen query with conditions.
	 */
	private String buildQuerySelectForLSAllTicketsForDataObjects() {
		StringBuilder queryString = new StringBuilder();
		queryString.append(buildQuerySelectForTicketsCommon());
		queryString.append(COMMA_SPACE);
		queryString.append(RodsGenQueryEnum.COL_TICKET_DATA_NAME.getName());
		queryString.append(COMMA_SPACE);
		queryString.append(RodsGenQueryEnum.COL_TICKET_DATA_COLL_NAME.getName());

		return queryString.toString();

	}

	/**
	 * Add the data object specific elements to a query
	 *
	 * @param builder
	 *            {@link IRODSGenQueryBuilder}
	 * @throws GenQueryBuilderException
	 */
	private void addQuerySelectsForListAllTicketsForDataObjects(final IRODSGenQueryBuilder builder)
			throws GenQueryBuilderException {

		if (builder == null) {
			throw new IllegalArgumentException("null builder");
		}

		builder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_TICKET_DATA_NAME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_TICKET_DATA_COLL_NAME);
	}

	/**
	 * @param queryString
	 */
	private String buildQuerySelectForTicketsCommon() {
		StringBuilder queryString = new StringBuilder();
		queryString.append("SELECT ");
		queryString.append(RodsGenQueryEnum.COL_TICKET_ID.getName());
		queryString.append(COMMA_SPACE);
		queryString.append(RodsGenQueryEnum.COL_TICKET_STRING.getName());
		queryString.append(COMMA_SPACE);
		queryString.append(RodsGenQueryEnum.COL_TICKET_TYPE.getName());
		queryString.append(COMMA_SPACE);
		queryString.append(RodsGenQueryEnum.COL_TICKET_OBJECT_TYPE.getName());
		queryString.append(COMMA_SPACE);
		queryString.append(RodsGenQueryEnum.COL_TICKET_OWNER_NAME.getName());
		queryString.append(COMMA_SPACE);
		queryString.append(RodsGenQueryEnum.COL_TICKET_OWNER_ZONE.getName());
		queryString.append(COMMA_SPACE);
		queryString.append(RodsGenQueryEnum.COL_TICKET_USES_COUNT.getName());
		queryString.append(COMMA_SPACE);
		queryString.append(RodsGenQueryEnum.COL_TICKET_USES_LIMIT.getName());
		queryString.append(COMMA_SPACE);
		queryString.append(RodsGenQueryEnum.COL_TICKET_WRITE_FILE_COUNT.getName());
		queryString.append(COMMA_SPACE);
		queryString.append(RodsGenQueryEnum.COL_TICKET_WRITE_FILE_LIMIT.getName());
		queryString.append(COMMA_SPACE);
		queryString.append(RodsGenQueryEnum.COL_TICKET_WRITE_BYTE_COUNT.getName());
		queryString.append(COMMA_SPACE);
		queryString.append(RodsGenQueryEnum.COL_TICKET_WRITE_BYTE_LIMIT.getName());
		queryString.append(COMMA_SPACE);
		queryString.append(RodsGenQueryEnum.COL_TICKET_EXPIRY_TS.getName());
		return queryString.toString();
	}

	/**
	 * Add selects (which will be in the established order of the passed in builder)
	 * which are the basics for tickets to use in a gen query
	 *
	 * @param builder
	 *            {@link RIODSGenQueryBuilder} to which the fields will be added
	 */
	private void addSelectsForTicketsCommonToQueryBuilder(final IRODSGenQueryBuilder builder)
			throws GenQueryBuilderException {

		if (builder == null) {
			throw new IllegalArgumentException("null builder");
		}

		builder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_TICKET_ID)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_TICKET_STRING)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_TICKET_TYPE)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_TICKET_OBJECT_TYPE)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_TICKET_OWNER_NAME)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_TICKET_OWNER_ZONE)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_TICKET_USES_COUNT)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_TICKET_USES_LIMIT)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_TICKET_WRITE_FILE_COUNT)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_TICKET_WRITE_FILE_LIMIT)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_TICKET_WRITE_BYTE_COUNT)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_TICKET_WRITE_BYTE_LIMIT)
				.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_TICKET_EXPIRY_TS);
	}

	private List<String> listRestrictionsForSpecifiedTicketCommon(final String ticketId, final RodsGenQueryEnum col,
			final int partialStartIndex) throws JargonException {

		IRODSGenQuery irodsQuery = null;
		List<String> restrictions = new ArrayList<String>();

		StringBuilder queryString = new StringBuilder();
		queryString.append("SELECT ");
		queryString.append(col.getName());
		queryString.append(" WHERE ");
		queryString.append(RodsGenQueryEnum.COL_TICKET_STRING.getName());
		queryString.append(" = ");
		queryString.append("'");
		queryString.append(ticketId);
		queryString.append("'");

		try {
			irodsQuery = IRODSGenQuery.instance(queryString.toString(),
					irodsAccessObjectFactory.getJargonProperties().getMaxFilesAndDirsQueryMax());
			IRODSGenQueryExecutor irodsGenQueryExecutor = irodsAccessObjectFactory
					.getIRODSGenQueryExecutor(irodsAccount);

			AbstractIRODSQueryResultSet resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(irodsQuery,
					partialStartIndex);
			for (IRODSQueryResultRow row : resultSet.getResults()) {
				restrictions.add(row.getColumn(0));
			}

		} catch (JargonQueryException e) {
			log.error("query exception for ticket query:{}", irodsQuery, e);
			throw new JargonException(ERROR_IN_TICKET_QUERY, e);
		}

		return restrictions;
	}

	// need this function to determine whether the ticket being queried for is
	// for a data object or collection
	private Ticket.TicketObjectType getTicketObjectType(final String ticketId) throws JargonException {

		IRODSQueryResultSetInterface resultSet = null;

		if ((ticketId == null) || (ticketId.isEmpty())) {
			throw new IllegalArgumentException("illegal ticket id");
		}

		StringBuilder queryString = new StringBuilder();

		queryString.append("select ");
		queryString.append(RodsGenQueryEnum.COL_TICKET_OBJECT_TYPE.getName());
		queryString.append(" where ");
		queryString.append(RodsGenQueryEnum.COL_TICKET_STRING.getName());
		queryString.append(" = ");
		queryString.append("'");
		queryString.append(ticketId);
		queryString.append("'");

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(queryString.toString(), 1);

		IRODSGenQueryExecutor irodsGenQueryExecutor = irodsAccessObjectFactory.getIRODSGenQueryExecutor(irodsAccount);

		try {
			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(irodsQuery, 0);
		} catch (JargonQueryException e) {
			log.error("query exception for ticket query:{}", queryString, e);
			throw new JargonException(ERROR_IN_TICKET_QUERY, e);
		}
		if ((resultSet != null) && (!resultSet.getResults().isEmpty())) {
			if (resultSet.getFirstResult().getQueryResultColumns().get(0).equals("data")) {
				return Ticket.TicketObjectType.DATA_OBJECT;
			} else {
				return Ticket.TicketObjectType.COLLECTION;
			}
		} else {
			throw new DataNotFoundException(TICKET_NOT_FOUND);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.irods.jargon.ticket.TicketAdminService#isTicketInUse(java.lang.String )
	 */
	@Override
	public boolean isTicketInUse(final String ticketString) throws JargonException {
		log.info("isTicketInUse()");
		if (ticketString == null || ticketString.isEmpty()) {
			throw new IllegalArgumentException("null or empty ticketString");
		}
		log.info("ticketString:{}", ticketString);

		boolean ticketFound = false;
		try {
			IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, null);
			builder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_TICKET_STRING).addConditionAsGenQueryField(
					RodsGenQueryEnum.COL_TICKET_STRING, QueryConditionOperators.EQUAL, ticketString);
			IRODSGenQueryExecutor irodsGenQueryExecutor = irodsAccessObjectFactory
					.getIRODSGenQueryExecutor(irodsAccount);

			IRODSQueryResultSetInterface resultSet = irodsGenQueryExecutor
					.executeIRODSQueryAndCloseResult(builder.exportIRODSQueryFromBuilder(1), 0);

			if (!resultSet.getResults().isEmpty()) {
				log.info("found the ticket");
				ticketFound = true;
			}

		} catch (GenQueryBuilderException e) {
			log.error("GenQueryBuilderException in ticket query", e);
			throw new JargonException("genQueryBuilderException building ticket query", e);
		} catch (JargonQueryException e) {
			log.error("jargonQueryException in ticket query", e);
			throw new JargonException("jargonQueryException building ticket query", e);
		}
		return ticketFound;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.ticket.TicketAdminService#
	 * compareGivenTicketToActualAndUpdateAsNeeded (org.irods.jargon.ticket.Ticket)
	 */
	@Override
	public Ticket compareGivenTicketToActualAndUpdateAsNeeded(final Ticket ticketWithDesiredData)
			throws DataNotFoundException, JargonException {

		log.info("compareGivenTicketToActualAndUpdateAsNeeded()");
		if (ticketWithDesiredData == null) {
			throw new IllegalArgumentException("null ticketWithDesiredData");
		}

		// data not found will occur here if I cannot find the ticket
		Ticket actualTicket = getTicketForSpecifiedTicketString(ticketWithDesiredData.getTicketString());

		/*
		 * Compare things now and call appropriate updates. This is not within a
		 * transaction, but that's the way it works...sorry
		 */

		// uses limit
		if (ticketWithDesiredData.getUsesLimit() != actualTicket.getUsesLimit()) {
			log.info("setting uses limit to:{}", ticketWithDesiredData.getUsesLimit());
			setTicketUsesLimit(actualTicket.getTicketString(), ticketWithDesiredData.getUsesLimit());
		}

		// files limit
		if (ticketWithDesiredData.getWriteFileLimit() != actualTicket.getWriteFileLimit()) {
			log.info("setting files write limit to:{}", ticketWithDesiredData.getWriteFileLimit());
			setTicketFileWriteLimit(actualTicket.getTicketString(), ticketWithDesiredData.getWriteFileLimit());
		}

		// bytes write limit
		if (ticketWithDesiredData.getWriteByteLimit() != actualTicket.getWriteByteLimit()) {
			log.info("setting bytes write limit to:{}", ticketWithDesiredData.getWriteByteLimit());
			setTicketByteWriteLimit(actualTicket.getTicketString(), ticketWithDesiredData.getWriteByteLimit());
		}

		// expires (
		if (!isDateSame(ticketWithDesiredData.getExpireTime(), actualTicket.getExpireTime())) {
			log.info("updating expires limit");
			setTicketExpiration(actualTicket.getTicketString(), ticketWithDesiredData.getExpireTime());
		}

		log.info("ticket updated, read again to return to caller");
		return getTicketForSpecifiedTicketString(ticketWithDesiredData.getTicketString());

	}

	/**
	 * compare two dates on their components, ignoring millis, as stuff goes to
	 * irods as a serialized string in the protocol
	 *
	 * @param date1
	 * @param date2
	 * @return
	 */
	@SuppressWarnings("deprecation")
	private boolean isDateSame(final Date date1, final Date date2) {

		boolean same = true;

		if (date1 == null & date2 == null) {
			same = true;
		}

		if (date1 == null) {
			same = false;
		} else if (date2 == null) {
			same = false;
		} else {

			same = (date1.getHours() == date2.getHours() && date1.getMinutes() == date2.getMinutes()
					&& date1.getSeconds() == date2.getSeconds() && date1.getYear() == date2.getYear()
					&& date1.getDate() == date2.getDate() && date1.getMonth() == date2.getMonth());
		}
		return same;
	}

}
