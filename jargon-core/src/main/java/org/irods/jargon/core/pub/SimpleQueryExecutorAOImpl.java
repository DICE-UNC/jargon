/**
 * 
 */
package org.irods.jargon.core.pub;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.SimpleQueryInp;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.IRODSQueryResultSetInterface;
import org.irods.jargon.core.query.IRODSSimpleQueryResultSet;
import org.irods.jargon.core.query.SimpleQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.sdsc.grid.io.irods.Tag;

/**
 * Access object to execute queries using the iRODS Simple Query facility. This
 * is mainly used for administrative queries, as in the <code>iadmin</code>
 * icommand. Typically these commands require <code>rodsadmin</code>, and will
 * fail if executed without admin rights.
 * <p/>
 * Simple Query allows the the execution of queries as parameterized SQL. These
 * SQL statements are pre-loaded in iRODS and validated before being allowed to
 * run. Other techniques, such as GenQuery, and the new SpecialQuery facility,
 * allow other methods of querying system and user metadata from the iCAT.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class SimpleQueryExecutorAOImpl extends IRODSGenericAO implements
		SimpleQueryExecutorAO {

	private static final Logger log = LoggerFactory
			.getLogger(SimpleQueryExecutorAOImpl.class);

	public static final String OUT_BUF = "outBuf";

	/**
	 * Standard constructor for access objects.
	 * 
	 * @param irodsSession
	 * @param irodsAccount
	 * @throws JargonException
	 */
	protected SimpleQueryExecutorAOImpl(final IRODSSession irodsSession,
			final IRODSAccount irodsAccount) throws JargonException {
		super(irodsSession, irodsAccount);
	}

	@Override
	public IRODSQueryResultSetInterface executeSimpleQuery(
			final SimpleQuery simpleQuery) throws JargonException {

		if (simpleQuery == null) {
			throw new IllegalArgumentException("null simpleQuery");
		}

		log.info("executeSimpleQuery:{}", simpleQuery);

		SimpleQueryInp simpleQueryInp = SimpleQueryInp.instance(simpleQuery);

		Tag response = null;

		response = getIRODSProtocol().irodsFunction(simpleQueryInp);

		if (response == null) {
			log.info("response from IRODS call indicates no rows found");
			List<IRODSQueryResultRow> result = new ArrayList<IRODSQueryResultRow>();

			IRODSQueryResultSetInterface resultSet = IRODSSimpleQueryResultSet
					.instance(simpleQuery, result, new ArrayList<String>(),
							false);
			return resultSet;
		}

		String rawResponse = response.getTag(OUT_BUF).getStringValue();
		List<IRODSQueryResultRow> result = new ArrayList<IRODSQueryResultRow>();
		IRODSQueryResultRow irodsQueryResultRow;
		
		String[] rows = rawResponse.split("\n");
		
		List<String> colNames = new ArrayList<String>();
		
		
		
		if (rows.length >= 0) {
			String thisCol;
			int idx = rows[0].indexOf(':');
			String firstColName = rows[0].substring(idx + 1);
			colNames.add(firstColName);
			//irodsQueryResultRow = new 
			for(String rowVal:rows) {
				idx = rowVal.indexOf(':');
				thisCol = rowVal.substring(idx + 1);
				if (thisCol.equals(firstColName)) {
					break;
				}
				colNames.add(thisCol);
			}
		}

		IRODSQueryResultSetInterface resultSet = IRODSSimpleQueryResultSet
				.instance(simpleQuery, result, colNames, false);
		return resultSet;

	}

}
