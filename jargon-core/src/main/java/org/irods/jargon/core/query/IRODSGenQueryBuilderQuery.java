package org.irods.jargon.core.query;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an immutable iRODS gen query built using the gen query builder
 * tool. This is an improvement over the original 'iquest'-like string allowing
 * more expressive queries.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class IRODSGenQueryBuilderQuery {

	private List<GenQuerySelectField> selectFields = new ArrayList<GenQuerySelectField>();
	private List<GenQueryBuilderCondition> conditions = new ArrayList<GenQueryBuilderCondition>();

}
