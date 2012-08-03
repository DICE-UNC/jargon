package org.irods.jargon.core.pub;

import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.domain.SpecificQuery;

/**
* Interface for an object to interact with specific query in IRODS.
* 
* @author Lisa Stillwell RENCI (www.renci.org)
* 
**/

public interface SpecificQueryAO extends IRODSAccessObject {
	
	/**
	 * Add a specific query to iRODS
	 * 
	 * @param sqlQuery
	 * 		<code>String</code> with the a valid SQL query
	 * @param alias
	 * 		<code>String</code> with a unique alias name for this SQL query
	 * @throws IllegalArgumentException
	 * @throws DuplicateDataException
	 */
	 void addSpecificQuery(SpecificQuery specificQuery) throws JargonException, DuplicateDataException;
	 
	 
	 /**
	 * Remove a specific query from iRODS
	 * 
	 * @param specificQuery
	 *		{@link org.irods.jargon.core.pub.domain.SpecificQuery} to be added to iRODS.
	 * @throws IllegalArgumentException
	 * @throws DuplicateDataException
	 */
	 void removeSpecificQuery(SpecificQuery specificQuery) throws JargonException;
	 
	 
	 /**
	 * Remove a specific query from iRODS using alias name as identifier
	 * 
	 * @param alias
	 * 		<code>String</code> with a unique alias name for this SQL query
	 * @throws IllegalArgumentException
	 * @throws DuplicateDataException
	 */
	 void removeSpecificQueryByAlias(String alias) throws JargonException;
		
	 
	 /**
	 * Remove a specific query from iRODS using SQL query as identifier
	 * <p>
	 * Please note that this method will remove all existing Specific Queries
	 * that match the provided SQL query String
	 * 
	 * @param sqlQuery
	 * 		<code>String</code> with the a valid SQL query
	 * @throws IllegalArgumentException
	 * @throws DuplicateDataException
	 * 
	 */
	 void removeAllSpecificQueryBySQL(String sqlQuery) throws JargonException, DuplicateDataException;

}
