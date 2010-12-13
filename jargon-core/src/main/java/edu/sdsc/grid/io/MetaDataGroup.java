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
//  MetaDataGroup.java  -  edu.sdsc.grid.io.MetaDataGroup
//
//  CLASS HIERARCHY
//  java.lang.Object
//      |
//      +-.MetaDataGroup
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
 * that the implementor supports metadata we might find for any file system,
 * such as:
 * <ul>
 * <li>File name
 * <li>File size
 * <li>Creation date
 * <li>Modification date
 * <li>Owner name
 * </ul>
 * <P>
 * An implementation specific group, such as the general SRB metadata group must
 * support these fields, but also may support further general metadata fields,
 * such as:
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
 * 
 * @author Lucas Gilbert, San Diego Supercomputer Center
 */
public class MetaDataGroup implements Comparable {
	// ----------------------------------------------------------------------
	// Constants
	// ----------------------------------------------------------------------

	// ----------------------------------------------------------------------
	// Fields
	// ----------------------------------------------------------------------
	protected HashMap fields = new HashMap();

	protected String groupName;

	protected String description;

	// ----------------------------------------------------------------------
	// Constructors and Destructors
	// ----------------------------------------------------------------------
	/**
	 * Construct a metadata group and register the group in a master list of
	 * groups maintained by the MetaDataSet class.
	 */
	// TODO I still don't like this part
	/*
	 * During registration, the group's fields are checked to insure they are
	 * unique and do not collide with any field names of any previously
	 * registered groups.
	 */
	public MetaDataGroup(final String groupName, final String description) {
		super();

		this.groupName = groupName;
		this.description = description;
	}

	// ----------------------------------------------------------------------
	// Build object
	// ----------------------------------------------------------------------
	/**
	 * Load the group's fields.
	 */
	public void add(final MetaDataField field) {
		String fieldName = field.getName();
		if (isField(fieldName)) {
			MetaDataField oldField = getField(fieldName);
			oldField.addProtocol(field.getProtocol(0));
		} else {
			fields.put(fieldName, field);
		}
	}

	// ----------------------------------------------------------------------
	// Introspection
	// ----------------------------------------------------------------------
	/**
	 * Returns a short name string for this metadata group.
	 */
	public String getName() {
		return groupName;
	}

	/**
	 * This string may be displayed by a GUI. It has no embedded carriage
	 * returns, so the application is expected to insert line breaks to wrap the
	 * text appropriately for its way of displaying it.
	 * 
	 * @return A description string of the meta data group.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns the number of fields known by the group.
	 */
	public int getFieldCount() {
		return fields.size();
	}

	/**
	 * Returns an object describing this field in the group.
	 */
	/*
	 * public MetaDataField getField( int fieldNum ) { //TODO return something;
	 * }
	 */

	/**
	 * Returns an object describing this field in the group.
	 */
	public MetaDataField getField(final String fieldName) {
		return (MetaDataField) fields.get(fieldName);
	}

	/**
	 * Returns all the fields in the group.
	 */
	public MetaDataField[] getFields() {
		return (MetaDataField[]) fields.values().toArray(new MetaDataField[0]);
	}

	/**
	 * @param sort
	 *            if true, sort alphabetically by name.
	 * @see edu.sdsc.grid.io.MetaDataField#compareTo(Object )
	 * @return all the fields in the group.
	 */
	public MetaDataField[] getFields(final boolean sort) {
		if (!sort) {
			return getFields();
		}

		MetaDataField[] mdFields = (MetaDataField[]) fields.values().toArray(
				new MetaDataField[0]);
		Arrays.sort(mdFields);
		return mdFields;
	}

	@Override
	public int compareTo(final Object obj) {

		return toString().compareTo(obj.toString());
	}

	/**
	 * Returns true if the given field name is part of the group. False is
	 * returned otherwise.
	 */
	boolean isField(final String fieldName) {
		if (fields.get(fieldName) != null) {
			return true;
		}

		return false;
	}

	/**
	 * Returns a string representation of the object.
	 */
	@Override
	public String toString() {
		return getName() + " : " + getDescription();
	}
}
