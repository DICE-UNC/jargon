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
//  MetaDataTable.java  -  edu.sdsc.grid.io.MetaDataTable
//
//  CLASS HIERARCHY
//  java.lang.Object
//      |
//      +-.MetaDataTable
//
//  PRINCIPAL AUTHOR
//  Lucas Gilbert, SDSC/UCSD
//
//
package edu.sdsc.grid.io;

import java.lang.reflect.Array;
import java.util.Vector;

/**
 * 
 * @author Lucas Gilbert, San Diego Supercomputer Center
 */
public class MetaDataTable {
	// ----------------------------------------------------------------------
	// Constants
	// ----------------------------------------------------------------------

	// ----------------------------------------------------------------------
	// Fields
	// ----------------------------------------------------------------------
	private Vector values;

	private Vector operators;

	// ----------------------------------------------------------------------
	// Constructors and Destructors
	// ----------------------------------------------------------------------
	/*
	 * Construct a metadata values table to be used as the conditional in a
	 * metadata query.<br> values[0][0] operators[0] values[0][1] values[0][2]
	 * values[0][3]...<br> values[1][0] operators[1] values[1][1] values[1][2]
	 * values[1][3]...<br> values[2][0] operators[2] values[2][1] values[2][2]
	 * values[2][3]...<br> ...<br><P>
	 */
	/*
	 * The standard SRB return might look like:<br> fieldName operator value
	 * units length = 5 m<br> source = bitmap<br> color = red rgb<br><P> The
	 * operator only compares values[x][0] to values[x][1]. values[x][2+] are
	 * compared for equality. So the example would only return items that are
	 * red using rgb units, but if the item had an alpha channel, ie the units
	 * are different, then no items would match the query.<P>
	 */
	public MetaDataTable(final int[] operators, final String[][] values) {
		if ((values == null) || (operators == null)) {
			throw new NullPointerException(
					"Values and operators cannot be null.");
		}

		if (operators.length != values.length) {
			throw new IllegalArgumentException(
					"Values and operators must have equal length");
		}

		if (values.length <= 0) {
			// TODO have to create different method in SRBCommands
			// to handle unlimited rows & 0-9 columns.
			throw new IllegalArgumentException("No values were given.");
		}

		this.values = new Vector(values.length);
		this.operators = new Vector(operators.length);
		for (int i = 0; i < values.length; i++) {
			addRow(values[i], operators[i]);
		}
	}

	// ----------------------------------------------------------------------
	// Methods
	// ----------------------------------------------------------------------
	public void setStringValue(final int i, final int j, final String value) {
		Object row = values.get(i);
		Array.set(row, j, value);
		values.set(i, row);
	}

	/**
   *
   */
	public int[] getOperators() {
		int[] temp = new int[operators.size()];
		for (int i = 0; i < temp.length; i++) {
			temp[i] = ((Integer) operators.get(i)).intValue();
		}
		return temp;
	}

	/**
   *
   */
	public int getOperator(final int index) {
		return ((Integer) operators.get(index)).intValue();
	}

	/**
   *
   */
	public int getIntValue(final int row, final int column) {
		String[] temp = (String[]) values.get(row);
		String value = temp[column];

		if ((row >= getRowCount()) || (column >= getColumnCount())) {
			throw new IllegalArgumentException();
		}
		if ((row < 0) || (column < 0)) {
			throw new IllegalArgumentException();
		}

		return Integer.parseInt(value.toString());
	}

	/**
   *
   */
	public float getFloatValue(final int row, final int column) {
		String[] temp = (String[]) values.get(row);
		String value = temp[column];

		if ((row >= getRowCount()) || (column >= getColumnCount())) {
			throw new IllegalArgumentException();
		}
		if ((row < 0) || (column < 0)) {
			throw new IllegalArgumentException();
		}

		return Float.parseFloat(value.toString());
	}

	/**
   *
   */
	public String getStringValue(final int row, final int column) {
		String[] temp = (String[]) values.get(row);
		String value = temp[column];

		if ((row >= getRowCount()) || (column >= getColumnCount())) {
			throw new IllegalArgumentException();
		}
		if ((row < 0) || (column < 0)) {
			throw new IllegalArgumentException();
		}

		return value;
	}

	/**
	 * @return The string matching the second column, the "value" column, from
	 *         the row that's first column, the "attribute" column, equals
	 *         <code>match</code>.
	 */
	public String getStringValue(final String match) {
		for (int i = 0; i < values.size(); i++) {
			if (getStringValue(i, 0).equals(match)) {
				return getStringValue(i, 1);
			}
		}
		return null;
	}

	/**
	 * @return The string matching the <code>column</code> index from the row
	 *         that's first column equals <code>match</code>.
	 */
	public String getStringValue(final String match, final int column) {
		for (int i = 0; i < values.size(); i++) {
			if (getStringValue(i, 0).equals(match)) {
				return getStringValue(i, column);
			}
		}
		return null;
	}

	/**
   *
   */
	public int getRowCount() {
		return values.size();
	}

	/**
   *
   */
	public int getColumnCount() {
		return ((String[]) values.get(0)).length;
	}

	/**
	 * Sets the operator. Must be done after the value is set as it does some
	 * conversions for LIKE.
	 */
	private void setOperator(final int operator) {
		if ((operator < MetaDataCondition.EQUAL)
				|| (operator > MetaDataCondition.SOUNDS_NOT_LIKE)) {
			throw new IllegalArgumentException("Invalid operator.");
		}
		this.operators.add(new Integer(operator));
	}

	/**
	 * Adds these values to this table, as the new last row.
	 */
	public void addRow(final String[] values, final int operator) {
		if (values != null) {
			boolean add = false;
			for (int i = 0; i < values.length; i++) {
				if (values[i] != null) {
					add = true;
					break;
				} else {
					values[i] = "";
				}
			}
			if (add) {
				setOperator(operator);
				this.values.add(values);
			}
		} else {
			throw new NullPointerException("Values array cannot be null");
		}
	}

	/**
	 * Removes the row specified by <code>index</code> from this table.
	 */
	public void removeRow(final int index) {
		values.remove(index);
		operators.remove(index);
	}

	// ----------------------------------------------------------------------
	// Object methods
	// ----------------------------------------------------------------------
	@Override
	public String toString() {
		String value = "";

		for (int i = 0; i < getRowCount(); i++) {
			value += "\n";
			value += getStringValue(i, 0) + " "
					+ MetaDataCondition.getOperatorString(getOperator(i)) + " ";
			for (int j = 1; j < getColumnCount(); j++) {
				value += getStringValue(i, j) + "\t";
			}
		}

		return value;
	}

	/**
   *
   */
	@Override
	public boolean equals(final Object obj) {
		MetaDataTable table = null;

		if (obj == null) {
			return false;
		}

		try {
			table = (MetaDataTable) obj;
		} catch (ClassCastException e) {
			return false;
		}

		if ((table.getRowCount() == getRowCount())
				&& (table.getColumnCount() == getColumnCount())) {
			for (int i = 0; i < getRowCount(); i++) {
				for (int j = 0; j < getColumnCount(); j++) {
					if (!getStringValue(i, j)
							.equals(table.getStringValue(i, j))) {
						return false;
					}
				}
			}
			return true;
		}
		return false;
	}
}
