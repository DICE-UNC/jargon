//  Copyright (c) 2005, Regents of the University of California
//  All rights reserved.
//
//  Redistribution and use in source and binary forms, with or without
//  modification, are permitted provided that the following conditions are
//  met:
//
//    * Redistributions of source code must retain the above copyright notice,
//  this list of conditions and the following disclaimer.
//    * Redistributions in binary form must reproduce the above copyright
//  notice, this list of conditions and the following disclaimer in the
//  documentation and/or other materials provided with the distribution.
//    * Neither the name of the University of California, San Diego (UCSD) nor
//  the names of its contributors may be used to endorse or promote products
//  derived from this software without specific prior written permission.
//
//  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
//  IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
//  THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
//  PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
//  EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
//  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
//  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
//  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
//  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
//  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
//
//  FILE
//  MetaDataSet.java  -  edu.sdsc.grid.io.MetaDataSet
//
//  CLASS HIERARCHY
//  java.lang.Object
//      |
//      +-.MetaDataSet
//
//  PRINCIPAL AUTHOR
//  Lucas Gilbert, SDSC/UCSD
//
//
package edu.sdsc.grid.io;

import java.util.Arrays;
import java.util.HashMap;

/**
 * A "Meta data group" is a group of meta data fields. Each MetaDataGroup class
 * includes documentation and introspection methods that describe the group as a
 * whole, the list of fields in the group, a description for those fields, the
 * data types for those fields, and the expected field values.
 * <P>
 * Each subclass has a constructor. None of the constructors are ever public.
 * Leaf classes in the class tree have private constructors, while the rest are
 * protected. The constructors are called by a static initializer method for
 * each class and are strictly used to create a single object for the class that
 * is passed to the MetaDataGroup parent class to register it in a master list.
 * That object also should be saved by the child class and will be passed to
 * constructors for condition and select objects (so that generic handlers of
 * those objects can get back to the metadata group).
 * <P>
 * Each of the subclasses group together meta data for a specific purpose. All
 * of the SRB meta data groups are subclassed off of SRBMetaData. The subclasses
 * may add methods, but most will only implement the required methods.
 * <P>
 * Some meta groups are standard regardless of implementation. To handle
 * standard metadata, we defined interfaces that are implemented by
 * protocol-specific metadata groups. The GeneralMetaData interface contract is
 * that the implementor supports meta data we might find for any file system,
 * such as:
 * <ul>
 * <li>File name
 * <li>File size
 * <li>Creation date
 * <li>Modification date
 * <li>Owner name
 * </ul>
 * <P>
 * An implementation specific class, such as SRBGeneralMetaData must support
 * these fields, but also may support further general metadata fields, such as:
 * <ul>
 * <li>Collection name
 * <li>Replication number
 * <li>File type
 * <li>Data classification name
 * <li>Data classification type
 * <li>Access constraint
 * <li>Comments
 * <li>Comments date
 * <li>Deleted flag
 * <li>Owner domain
 * <li>Owner email address
 * </ul>
 * <br>
 * <BR>
 * <br>
 * 
 * <pre>
 * Meta data groups
 * ------------------------------------------------------------------------
 * Interfaces
 * ----------
 *  StandardMetaData
 *   |
 *   +-->GeneralMetaData
 *   |
 *   +-->DublinCoreMetaData
 *   |
 *   +-->UserMetaData
 * </pre>
 * 
 * @author Lucas Gilbert, San Diego Supercomputer Center
 */
public abstract class MetaDataSet implements GeneralMetaData {
	/**
	 * Group name for metadata relating to Directory/Collection attributes.
	 */
	public final static String GROUP_DIRECTORY = "Directory";

	/**
	 * Group name for metadata relating to File/Dataset attributes.
	 */
	public final static String GROUP_DATA = "Data";

	/**
	 * Group name for metadata relating to User attributes.
	 */
	public final static String GROUP_USER = "User";

	/**
	 * Group name for metadata relating to resource attributes.
	 */
	public final static String GROUP_RESOURCE = "Resource";

	/**
	 * Group name for metadata relating to Zone attributes.
	 */
	public final static String GROUP_ZONE = "Zone";

	/**
	 * Group name for metadata relating to Physical resource attributes.
	 */
	public final static String GROUP_PHYSICAL_RESOURCE = "Physical resource";

	/**
	 * Group name for metadata relating to Logical resource attributes.
	 */
	public final static String GROUP_LOGICAL_RESOURCE = GROUP_RESOURCE;

	/**
	 * Group name for metadata relating to Server attributes.
	 */
	public final static String GROUP_SERVER = "Server";

	/**
	 * Group name for metadata relating to User group attributes.
	 */
	public final static String GROUP_USER_GROUP = "User group";

	/**
	 * Group name for metadata relating to Authentication attributes.
	 */
	public final static String GROUP_AUTHENTICATION = "Authentication";

	/**
	 * Group name for metadata relating to Authorization attributes.
	 */
	public final static String GROUP_AUTHORIZATION = "Authorization";

	/**
	 * Group name for metadata relating to Audit attributes.
	 */
	public final static String GROUP_AUDIT = "Audit";

	/**
	 * Group name for metadata relating to Ticket attributes.
	 */
	public final static String GROUP_TICKET = "Ticket";

	/**
	 * Group name for metadata relating to Container attributes.
	 */
	public final static String GROUP_CONTAINER = "Container";

	/**
	 * Group name for metadata relating to Annotations attributes.
	 */
	public final static String GROUP_ANNOTATIONS = "Annotations";

	/**
	 * Group name for metadata relating to Compound resource attributes.
	 */
	public final static String GROUP_COMPOUND_RESOURCE = "Compound resource";

	/**
	 * Group name for metadata relating to Dublin Core attributes.
	 */
	public final static String GROUP_DUBLIN_CORE = "Dublin Core";

	/**
	 * Group name for metadata relating to Definable metadata attributes.
	 */
	public final static String GROUP_DEFINABLE_METADATA = "Definable Metadata";

	/**
	 * Group name for metadata relating to Index attributes.
	 */
	public final static String GROUP_INDEX = "Index";

	/**
	 * Group name for metadata relating to Structured Metadata attributes.
	 */
	public final static String GROUP_STRUCTURED_METADATA = "Structured Metadata";

	/**
	 * Group name for metadata relating to Method attributes.
	 */
	public final static String GROUP_METHOD = "Method";

	/**
	 * Group name for metadata relating to GUID attributes.
	 */
	public final static String GROUP_GUID = "GUID";

	/**
	 * Group name for metadata relating to Undefined attributes.
	 */
	public final static String GROUP_UNDEFINED = "Undefined";

	/**
	 * Group name for metadata relating to Extensible metadata attributes.
	 */
	public final static String GROUP_EXTENSIBLE = "Extensible";

	/**
	 * Group name for metadata relating to Rule attributes.
	 */
	public final static String GROUP_RULE = "Rule";

	/**
	 * Group name for metadata relating to Token attributes.
	 */
	public final static String GROUP_TOKEN = "Token";

	protected static HashMap metaDataGroups = new HashMap();
	protected static HashMap metaDataFields = new HashMap();

	static {
		new ProtocolCatalog();
	}

	protected static void add(final MetaDataGroup group) {
		metaDataGroups.put(group.getName(), group);
		MetaDataField[] fields = group.getFields();
		for (MetaDataField field : fields) {
			metaDataFields.put(field.getName(), field);
		}
	}

	/**
	 * Return all the various metadata groups.
	 */
	public static MetaDataGroup[] getMetaDataGroups() {
		return (MetaDataGroup[]) metaDataGroups.values().toArray(
				new MetaDataGroup[0]);
	}

	/**
	 * @param sort
	 *            if true, sort alphabetically by name.
	 * @see edu.sdsc.grid.io.MetaDataGroup#compareTo(Object )
	 * @return all the various metadata groups.
	 */
	public static MetaDataGroup[] getMetaDataGroups(final boolean sort) {
		if (!sort) {
			return getMetaDataGroups();
		}

		MetaDataGroup[] groups = getMetaDataGroups();
		Arrays.sort(groups);
		return groups;
	}

	final static String DEFINABLE_METADATA = "jargonUserDefinableAttribute";

	/**
	 * Returns the MetaDataField object associated with this fieldName string.
	 * If there is no match a null value is returned.
	 */
	public static MetaDataField getField(final String fieldName) {
		if (fieldName == null) {
			throw new NullPointerException("The fieldName cannot be null.");
		}

		MetaDataField field = (MetaDataField) metaDataFields.get(fieldName);

		if (field == null) {
			field = new MetaDataField(fieldName, "", MetaDataField.STRING,
					null, DEFINABLE_METADATA);
		}

		return field;
	}

	/**
	 * Returns a new condition object that requests that a given field match/not
	 * match a given value. Valid MetaDataCondition operator codes are: =, <>,
	 * >, <, like, not like, sounds like. If supported by the filesystem being
	 * queried.
	 */
	public static MetaDataCondition newCondition(final String fieldName,
			final int operator, final String value) {
		return new MetaDataCondition(getField(fieldName), operator, value);
	}

	/**
	 * Returns a new condition object that requests that a given field match/not
	 * match a given value. Valid MetaDataCondition operator codes are: =, <>,
	 * >, <, like, not like, sounds like. If supported by the filesystem being
	 * queried.
	 */
	public static MetaDataCondition newCondition(final String fieldName,
			final int operator, final float value) {
		return new MetaDataCondition(getField(fieldName), operator, value);
	}

	/**
	 * Returns a new condition object that requests that a given field match/not
	 * match a given value. Valid MetaDataCondition operator codes are: =, <>,
	 * >, <, like, not like, sounds like. If supported by the filesystem being
	 * queried.
	 */
	public static MetaDataCondition newCondition(final String fieldName,
			final int operator, final int value) {
		return new MetaDataCondition(getField(fieldName), operator, value);
	}

	/**
	 * Returns a new condition object that requests that a given field match/not
	 * match a given value range. Valid operator codes are:
	 * MetaDataCondition.BETWEEN and MetaDataCondition.NOT_BETWEEN If supported
	 * by the filesystem being queried.
	 */
	public static MetaDataCondition newCondition(final String fieldName,
			final int operator, final int value1, final int value2) {
		return new MetaDataCondition(getField(fieldName), operator, value1,
				value2);
	}

	/**
	 * Returns a new condition object that requests that a given field match/not
	 * match a given value range. Valid operator codes are:
	 * MetaDataCondition.BETWEEN and MetaDataCondition.NOT_BETWEEN If supported
	 * by the filesystem being queried.
	 */
	public static MetaDataCondition newCondition(final String fieldName,
			final int operator, final float value1, final float value2) {
		return new MetaDataCondition(getField(fieldName), operator, value1,
				value2);
	}

	/**
	 * Returns a new condition object that requests that a given field match/not
	 * match a given value range. Valid operator codes are:
	 * MetaDataCondition.BETWEEN and MetaDataCondition.NOT_BETWEEN If supported
	 * by the filesystem being queried.
	 */
	public static MetaDataCondition newCondition(final String fieldName,
			final int operator, final String value1, final String value2) {
		return new MetaDataCondition(getField(fieldName), operator, value1,
				value2);
	}

	/**
	 * Returns a new condition object that requests that a given field match/not
	 * match the given values. Valid operator codes are: MetaDataCondition.IN
	 * and MetaDataCondition.NOT_IN If supported by the filesystem being
	 * queried.
	 */
	public static MetaDataCondition newCondition(final String fieldName,
			final int operator, final int[] valueList) {
		return new MetaDataCondition(getField(fieldName), operator, valueList);
	}

	/**
	 * Returns a new condition object that requests that a given field match/not
	 * match the given values. Valid operator codes are: MetaDataCondition.IN
	 * and MetaDataCondition.NOT_IN If supported by the filesystem being
	 * queried.
	 */
	public static MetaDataCondition newCondition(final String fieldName,
			final int operator, final float[] valueList) {
		return new MetaDataCondition(getField(fieldName), operator, valueList);
	}

	/**
	 * Returns a new condition object that requests that a given field match/not
	 * match the given values. Valid operator codes are: MetaDataCondition.IN
	 * and MetaDataCondition.NOT_IN If supported by the filesystem being
	 * queried.
	 */
	public static MetaDataCondition newCondition(final String fieldName,
			final int operator, final String[] valueList) {
		return new MetaDataCondition(getField(fieldName), operator, valueList);
	}

	/**
	 * Returns a new condition object that requests that a given field match the
	 * given values as described by the MetaDataTable. If supported by the
	 * filesystem being queried.
	 */
	public static MetaDataCondition newCondition(final String fieldName,
			final MetaDataTable table) {
		return new MetaDataCondition(getField(fieldName), table);
	}

	/**
	 * Returns a new condition object that requests that a given field match the
	 * given values as described by the MetaDataTable. If supported by the
	 * filesystem being queried.
	 */
	public static MetaDataCondition newCondition(final String fieldName,
			final int operator, final MetaDataTable table) {
		return new MetaDataCondition(getField(fieldName), operator, table);
	}

	/**
	 * The mergeConditons( ... ) methods are conveniences to creates a single
	 * MetaDataCondition array from two (or more) conditions.
	 */
	public static MetaDataCondition[] mergeConditions(
			final MetaDataCondition condition1,
			final MetaDataCondition condition2) {
		if (condition1 == null) {
			MetaDataCondition[] condition = { condition2 };
			return condition;
		} else if (condition2 == null) {
			MetaDataCondition[] condition = { condition1 };
			return condition;
		}

		MetaDataCondition[] condition = { condition1, condition2 };
		return condition;
	}

	/**
	 * The mergeConditons( ... ) methods are conveniences to creates a single
	 * MetaDataCondition array from two (or more) conditions.
	 */
	public static MetaDataCondition[] mergeConditions(
			final MetaDataCondition condition1,
			final MetaDataCondition[] condition2) {
		if (condition1 == null) {
			return condition2;
		} else if (condition2 == null) {
			MetaDataCondition[] condition = { condition1 };
			return condition;
		}

		MetaDataCondition[] condition = new MetaDataCondition[condition2.length + 1];
		condition[0] = condition1;
		System.arraycopy(condition2, 0, condition, 1, condition2.length);
		return condition;
	}

	/**
	 * The mergeConditons( ... ) methods are conveniences to creates a single
	 * MetaDataCondition array from two (or more) conditions.
	 */
	public static MetaDataCondition[] mergeConditions(
			final MetaDataCondition[] condition1,
			final MetaDataCondition[] condition2) {
		if (condition1 == null) {
			return condition2;
		} else if (condition2 == null) {
			return condition1;
		}

		MetaDataCondition[] condition = new MetaDataCondition[condition1.length
				+ condition2.length + 1];
		System.arraycopy(condition1, 0, condition, 0, condition1.length);
		System.arraycopy(condition2, 0, condition, condition1.length,
				condition2.length);
		return condition;
	}

	/**
	 * Returns a new selection object that requests that records returned by a
	 * query include this field.
	 */
	public static MetaDataSelect newSelection(final String fieldName) {
		return new MetaDataSelect(getField(fieldName));
	}

	/**
	 * Returns a new array of selection objects that request that all fields in
	 * this group be included in returned records from a query.
	 */
	public static MetaDataSelect[] newSelection(final String[] fieldNames) {
		MetaDataSelect[] selects = new MetaDataSelect[fieldNames.length];
		for (int i = 0; i < selects.length; i++) {
			if (fieldNames[i] != null) {
				selects[i] = new MetaDataSelect(getField(fieldNames[i]));
			}
		}
		return selects;
	}

	/**
	 * Returns a new selection object that requests a specific operation be
	 * performed on query results involving this field. Operations include:
	 * <UL>
	 * <LI>count or count-distinct
	 * <LI>max or min
	 * <LI>avg or sum
	 * <LI>variance or stddev
	 * </UL>
	 */
	public static MetaDataSelect newSelection(final String fieldName,
			final int operation) {
		return new MetaDataSelect(getField(fieldName), operation);
	}

	/**
	 * Returns a new array of selection objects that request that all fields in
	 * this group be included in returned records from a query.
	 */
	public static MetaDataSelect[] newSelection(final MetaDataGroup group) {
		MetaDataSelect[] selects = new MetaDataSelect[group.getFieldCount()];
		MetaDataField[] fieldList = group.getFields();
		for (int i = 0; i < selects.length; i++) {
			selects[i] = new MetaDataSelect(fieldList[i]);
		}
		return selects;
	}

	/**
	 * The mergeSelects( ... ) methods are conveniences to creates a single
	 * MetaDataSelect array from two (or more) selects.
	 */
	public static MetaDataSelect[] mergeSelects(final MetaDataSelect select1,
			final MetaDataSelect select2) {
		if (select1 == null) {
			MetaDataSelect[] select = { select2 };
			return select;
		}

		else if (select2 == null) {
			MetaDataSelect[] select = { select1 };
			return select;
		}

		MetaDataSelect[] select = { select1, select2 };
		return select;
	}

	/**
	 * The mergeSelects( ... ) methods are conveniences to creates a single
	 * MetaDataSelect array from two (or more) selects.
	 */
	public static MetaDataSelect[] mergeSelects(final MetaDataSelect select1,
			final MetaDataSelect[] select2) {
		if (select1 == null) {
			return select2;
		} else if (select2 == null) {
			MetaDataSelect[] select = { select1 };
			return select;
		}

		MetaDataSelect[] select = new MetaDataSelect[select2.length + 1];
		select[0] = select1;
		System.arraycopy(select2, 0, select, 1, select2.length);
		return select;
	}

	/**
	 * The mergeSelects( ... ) methods are conveniences to creates a single
	 * MetaDataSelect array from two (or more) selects.
	 */
	public static MetaDataSelect[] mergeSelects(final MetaDataSelect[] select1,
			final MetaDataSelect[] select2) {
		if (select1 == null) {
			return select2;
		} else if (select2 == null) {
			return select1;
		}

		MetaDataSelect[] select = new MetaDataSelect[select1.length
				+ select2.length + 1];
		System.arraycopy(select1, 0, select, 0, select1.length);
		System.arraycopy(select2, 0, select, select1.length, select2.length);
		return select;
	}

}
