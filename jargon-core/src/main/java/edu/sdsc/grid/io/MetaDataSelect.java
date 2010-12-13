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
//  MetaDataSelect .java  -  edu.sdsc.grid.io.MetaDataSelect
//
//  CLASS HIERARCHY
//  java.lang.Object
//      |
//      +-.MetaDataSelect
//
//  PRINCIPAL AUTHOR
//  Lucas Gilbert, SDSC/UCSD
//
//
package edu.sdsc.grid.io;

/**
 * A "meta data select" indicates a single field that should be returned on a
 * query.
 * <P>
 * There are no 'set' methods - once constructed, the object cannot be changed.
 * 
 * @author Lucas Gilbert, San Diego Supercomputer Center
 */
public class MetaDataSelect {
	// ----------------------------------------------------------------------
	// Constants
	// ----------------------------------------------------------------------
	// TODO various operation constants

	// ----------------------------------------------------------------------
	// Fields
	// ----------------------------------------------------------------------
	/**
	 * The field to be selected by this object
	 */
	private MetaDataField field;

	/**
	 * Operations include:
	 * <ul>
	 * <li>count or count-distinct
	 * <li>max or min
	 * <li>avg or sum
	 * <li>variance or stddev
	 * </ul>
	 */
	private int operation = 1;

	// ----------------------------------------------------------------------
	// Constructors and Destructors
	// ----------------------------------------------------------------------
	/**
	 * The constructor is package private and is only called by the
	 * MetaDataGroup classes on a call to their factory methods to create
	 * selection objects. Those factory methods take a field name and check that
	 * it is legal before they construct a selection object.
	 */
	protected MetaDataSelect(final MetaDataField field) {
		if (field == null) {
			throw new NullPointerException("field cannot be null.");
		}

		this.field = field;
	}

	/**
	 * Constructs a metadata selection object containing the given field name,
	 * and the chosen operation. Operations include:
	 * <ul>
	 * <li>count or count-distinct
	 * <li>max or min
	 * <li>avg or sum
	 * <li>variance or stddev
	 * </ul>
	 */
	protected MetaDataSelect(final MetaDataField field, final int operation) {
		if (field == null) {
			throw new NullPointerException("field cannot be null.");
		}

		this.field = field;
		if (operation >= 0) {
			this.operation = operation;
		}
	}

	// ----------------------------------------------------------------------
	// Getters
	// ----------------------------------------------------------------------
	/**
	 * Returns the name of the field selected by this object.
	 */
	public String getFieldName() {
		return field.getName();
	}

	/**
	 * Returns the MetaDataField object describing the field selected by this
	 * object.
	 */
	public MetaDataField getField() {
		return field;
	}

	/**
	 * Returns the operation code on this selection. Typically this is '1' and
	 * just flags this field as one to return in a query. Other operation codes
	 * include those for:
	 * <ul>
	 * <li>count or count-distinct
	 * <li>max or min
	 * <li>avg or sum
	 * <li>variance or stddev
	 * </ul>
	 */
	public int getOperation() {
		return operation;
	}

	/**
	 * Returns a string representation of the object.
	 */
	@Override
	public String toString() {
		return new String(field.getName());
	}

	/**
	 * Tests this MetaDataSelect object for equality with the given object.
	 * Returns <code>true</code> if and only if the argument is not
	 * <code>null</code> and both are MetaDataSelect objects with equal field
	 * and operation values.
	 * 
	 * @param obj
	 *            The object to be compared with this abstract pathname
	 * 
	 * @return <code>true</code> if and only if the objects are the same;
	 *         <code>false</code> otherwise
	 */
	@Override
	public boolean equals(final Object obj) {
		try {
			if (obj == null) {
				return false;
			}

			MetaDataSelect temp = (MetaDataSelect) obj;

			if (getField().equals(temp.getField())) {
				if (getOperation() == temp.getOperation()) {
					return true;
				}
			}
		} catch (ClassCastException e) {
			return false;
		}
		return false;
	}
}
