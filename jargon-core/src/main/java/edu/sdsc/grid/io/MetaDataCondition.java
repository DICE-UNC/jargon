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
//  MetaDataCondition.java  -  edu.sdsc.grid.io.MetaDataCondition
//
//  CLASS HIERARCHY
//  java.lang.Object
//      |
//      +-.MetaDataCondition
//
//  PRINCIPAL AUTHOR
//  Lucas Gilbert, SDSC/UCSD
//
//
package edu.sdsc.grid.io;

/**
 * A "meta data condition" describes a conditional expression to be used to
 * guide a metadata query. That expression contains three components:
 * <ul>
 * <li>A field name
 * <li>An operator (such as "=")
 * <li>A value
 * </ul>
 * <P>
 * Legal field names depend upon the meta data group and the file server being
 * talked to.
 * <P>
 * Operators are in the set: <br>
 * (Not all operators are supported by every database/filesystem)
 * <ul>
 * <li>=<>
 * <li>< >
 * <li><= >=
 * <li>in not in
 * <li>between not between
 * <li>like not like
 * <li>sounds like sounds not like
 * </ul>
 * <P>
 * Each operator is designated by an "operator code", which is a static final
 * integer defined in this class.
 * <P>
 * A value has a "style" in the set:
 * <ul>
 * <li>A scalar value (such as "42" or "Thursday")
 * <li>A scalar range (such as "42-118")
 * <li>A value list (such as "42,38,52")
 * </UL>
 * There are no 'set' methods - once constructed, the object cannot be changed.
 * 
 * @author Lucas Gilbert, San Diego Supercomputer Center
 */
public final class MetaDataCondition {
	// ----------------------------------------------------------------------
	// Constants
	// ----------------------------------------------------------------------
	/**
	 * "=" where the metadata value exactly equals the conditional value.
	 */
	public static final int EQUAL = 0;

	/**
	 * "!=" where the metadata value is not exactly equal to the conditional
	 * value.
	 */
	public static final int NOT_EQUAL = 1;

/**
   * "<"  where the metadata value is less than the conditional value,
   * lexicographical compare for strings.
   */
	public static final int LESS_THAN = 2;

	/**
	 * ">" where the metadata value is greater than the conditional value,
	 * lexicographical compare for strings.
	 */
	public static final int GREATER_THAN = 3;

	/**
	 * "<=" where the metadata value is less than or equal to the conditional
	 * value, lexicographical compare for strings.
	 */
	public static final int LESS_OR_EQUAL = 4;

	/**
	 * ">=" where the metadata value is greater than or equal to the conditional
	 * value, lexicographical compare for strings.
	 */
	public static final int GREATER_OR_EQUAL = 5;
/**
   * "<"  where the metadata value is less than the conditional value,
   * numerical comparison for strings.
   */
	public static final int NUM_LESS_THAN = 15;

	/**
	 * ">" where the metadata value is greater than the conditional value,
	 * numerical comparison for strings.
	 */
	public static final int NUM_GREATER_THAN = 16;

	/**
	 * "<=" where the metadata value is less than or equal to the conditional
	 * value, numerical comparison for strings.
	 */
	public static final int NUM_LESS_OR_EQUAL = 17;

	/**
	 * ">=" where the metadata value is greater than or equal to the conditional
	 * value, numerical comparison for strings.
	 */
	public static final int NUM_GREATER_OR_EQUAL = 18;

	/**
	 * "in" where the metadata value exactly equals one of the conditional
	 * values in the value list.
	 */
	public static final int IN = 6;

	/**
	 * "not in" where the metadata value does not exactly equals one of the
	 * conditional values in the value list.
	 */
	public static final int NOT_IN = 7;

	/**
	 * "between" where the metadata value is between the two conditional values,
	 * lexicographical compare for strings.
	 */
	public static final int BETWEEN = 8;

	/**
	 * "not between" where the metadata value is not between the two conditional
	 * values, lexicographical compare for strings.
	 */
	public static final int NOT_BETWEEN = 9;

	/**
	 * "like" where the metadata value contains the conditional value. Values
	 * using LIKE should be strings with these wild-card characters, * and ?. *
	 * represents any number of characters. ? represents any single character.
	 * For example, <br>
	 * if you have the files with OWNER = testuser, testuser2:<br>
	 * The query "OWNER LIKE testuse_" will return testuser.<br>
	 * The query "OWNER LIKE testuse%" will return testuser, and testuser2.<br>
	 * Conditionals without any wildcard characters will search as a string
	 * fragment, ie. will be treated as though a % was at the beginning and end.
	 * <P>
	 * is the same as %, and ? equals _<br>
	 * note: The characters *,?,%,_ are reserved and may not be used in the
	 * conditional value with the LIKE operator.
	 */
	public static final int LIKE = 10;

	/**
	 * "not like" where the metadata value does not contains the conditional
	 * value. Values using LIKE should be strings with these wild-card
	 * characters, * and ?. * represents any number of characters. ? represents
	 * any single character. For example, <br>
	 * if you have the files with OWNER = testuser, testuser2:<br>
	 * The query "OWNER LIKE testuse?" will return testuser.<br>
	 * The query "OWNER LIKE testuse*" will return testuser, and testuser2.<br>
	 * Conditionals without any wildcard characters will search as a string
	 * fragment, ie. will be treated as though a * was at the beginning and end.
	 * <P>
	 * is the same as %, and ? equals _<br>
	 * note: The characters *,?,%,_ are reserved and may not be used in the
	 * conditional value with the LIKE operator.
	 */
	public static final int NOT_LIKE = 11;

	/**
	 * "sounds like", Implement phonetic name searches
	 */
	public static final int SOUNDS_LIKE = 12;

	/**
	 * "not sounds like", Implement phonetic name searches
	 */
	public static final int SOUNDS_NOT_LIKE = 13;

	/**
	 * The value style for the field.<br>
	 * Styles include: SCALAR, RANGEPAIR, ENUM, TABLE.
	 */
	public static final int SCALAR = 0;

	/**
	 * The value style for the field.<br>
	 * Styles include: SCALAR, RANGEPAIR, ENUM, TABLE.
	 */
	public static final int RANGEPAIR = 1;

	/**
	 * The value style for the field.<br>
	 * Styles include: SCALAR, RANGEPAIR, ENUM, TABLE.
	 */
	public static final int ENUM = 2;

	/**
	 * Table represents both a style and type. It can only be used with
	 * conditionals that use the MetaDataTable as a value.<br>
	 * Styles include: SCALAR, RANGEPAIR, ENUM, TABLE.<br>
	 * Types include: INT, LONG, FLOAT, STRING, DATE, TABLE.
	 */
	public static final int TABLE = 14;

	// ----------------------------------------------------------------------
	// Fields
	// ----------------------------------------------------------------------
	/**
	 * Legal fields depend upon the file server being talked to.
	 */
	MetaDataField field;

	/**
	 * Operators are in the set:
	 * <ul>
	 * <li>=<>
	 * <li>< >
	 * <li><= >=
	 * <li>numerical compare < >
	 * <li>numerical compare <= >=
	 * <li>in not in
	 * <li>between not between
	 * <li>like not like
	 * <li>sounds like sounds not like
	 * </ul>
	 * <P>
	 * Each operator is designated by an "operator code", which is a static
	 * final integer defined in this class.
	 */
	// MetaDataTables have multiple operators,
	// which are contained in the MetaDataTable object.
	int operator;

	/**
	 * Holds the values which define the consequent of the conditional.
	 */
	Object[] values;

	/**
	 * The value style for the conditional.<br>
	 * Styles include: SCALAR, RANGEPAIR, ENUM, TABLE.
	 */
	int style;

	/**
	 * The value type for the field.<br>
	 * Types include: INT, LONG, FLOAT, STRING, DATE, TABLE.
	 */
	// don't do type here, but for error check on field
	int type;

	// ----------------------------------------------------------------------
	// Constructors and Destructors
	// ----------------------------------------------------------------------
	// SCALAR
	/**
	 * The constructor is package private and is only called by the
	 * MetaDataGroup classes and their factory methods. Those factory methods do
	 * argument checking first to make sure the field name is part of the group,
	 * the data type of the value is appropriate, and that the operator is valid
	 * for this field. Only if all the arguments are right is the object
	 * constructed and returned.
	 * <P>
	 * Construct a scalar conditional with the given field name, operator, and
	 * value. Infer the type from the given value. The operator must be one of
	 * the simple operators (=, <>, <, <=, >, >=) or one of the 'like' operators
	 * (like, not like, sounds like, sounds not like).
	 */
	MetaDataCondition(final MetaDataField field, final int operator,
			final int value) {
		if (field == null) {
			throw new NullPointerException("field cannot be null.");
		}

		this.field = field;
		values = new Object[1];
		values[0] = new Integer(value);
		type = MetaDataField.INT;
		style = SCALAR;
		this.operator = operator;
	}

	/**
	 * The constructor is package private and is only called by the
	 * MetaDataGroup classes and their factory methods. Those factory methods do
	 * argument checking first to make sure the field name is part of the group,
	 * the data type of the value is appropriate, and that the operator is valid
	 * for this field. Only if all the arguments are right is the object
	 * constructed and returned.
	 * <P>
	 * Construct a scalar conditional with the given field name, operator, and
	 * value. Infer the type from the given value. The operator must be one of
	 * the simple operators (=, <>, <, <=, >, >=) or one of the 'like' operators
	 * (like, not like, sounds like, sounds not like).
	 */
	MetaDataCondition(final MetaDataField field, final int operator,
			final float value) {
		if (field == null) {
			throw new NullPointerException("field cannot be null.");
		}

		this.field = field;
		values = new Object[1];
		values[0] = new Float(value);
		type = MetaDataField.FLOAT;
		style = SCALAR;
		this.operator = operator;
	}

	/**
	 * The constructor is package private and is only called by the
	 * MetaDataGroup classes and their factory methods. Those factory methods do
	 * argument checking first to make sure the field name is part of the group,
	 * the data type of the value is appropriate, and that the operator is valid
	 * for this field. Only if all the arguments are right is the object
	 * constructed and returned.
	 * <P>
	 * Construct a scalar conditional with the given field name, operator, and
	 * value. Infer the type from the given value. The operator must be one of
	 * the simple operators (=, <>, <, <=, >, >=) or one of the 'like' operators
	 * (like, not like, sounds like, sounds not like).
	 */
	MetaDataCondition(final MetaDataField field, final int operator,
			final String value) {
		if (field == null) {
			throw new NullPointerException("field cannot be null.");
		}

		this.field = field;
		values = new Object[1];
		values[0] = value;
		type = MetaDataField.STRING;
		style = SCALAR;
		this.operator = operator;
	}

	// RANGEPAIR
	/**
	 * Construct a rangepair conditional with the given field name, operator,
	 * and pair of values. Infer the type from the given values. The operator
	 * must be 'between' or 'not between' or an exception is thrown.
	 */
	MetaDataCondition(final MetaDataField field, final int operator,
			final int value1, final int value2) {
		if (field == null) {
			throw new NullPointerException("field cannot be null.");
		}

		this.field = field;
		values = new Object[2];
		values[0] = new Integer(value1);
		values[1] = new Integer(value2);
		type = MetaDataField.INT;
		style = RANGEPAIR;
		if ((operator == BETWEEN) || (operator == NOT_BETWEEN)) {
			this.operator = operator;
		} else {
			throw new IllegalArgumentException("Wrong operator.");
		}
	}

	/**
	 * Construct a rangepair conditional with the given field name, operator,
	 * and pair of values. Infer the type from the given values. The operator
	 * must be 'between' or 'not between' or an exception is thrown.
	 */
	MetaDataCondition(final MetaDataField field, final int operator,
			final float value1, final float value2) {
		if (field == null) {
			throw new NullPointerException("field cannot be null.");
		}

		this.field = field;
		values = new Object[2];
		values[0] = new Float(value1);
		values[1] = new Float(value2);
		type = MetaDataField.FLOAT;
		style = RANGEPAIR;
		if ((operator == BETWEEN) || (operator == NOT_BETWEEN)) {
			this.operator = operator;
		} else {
			throw new IllegalArgumentException("Wrong operator.");
		}
	}

	/**
	 * Construct a rangepair conditional with the given field name, operator,
	 * and pair of values. Infer the type from the given values. The operator
	 * must be 'between' or 'not between' or an exception is thrown.
	 */
	MetaDataCondition(final MetaDataField field, final int operator,
			final String value1, final String value2) {
		if (field == null) {
			throw new NullPointerException("field cannot be null.");
		}

		this.field = field;
		values = new Object[2];
		values[0] = value1;
		values[1] = value2;
		type = MetaDataField.STRING;
		style = RANGEPAIR;
		if ((operator == BETWEEN) || (operator == NOT_BETWEEN)) {
			this.operator = operator;
		} else {
			throw new IllegalArgumentException("Wrong operator.");
		}
	}

	// ENUM
	/**
	 * Construct a enum conditional with the given field name, operator, and
	 * enum of values. The type is inferred from the given values. The operator
	 * must be 'in' or 'not in' or an exception is thrown.
	 */
	MetaDataCondition(final MetaDataField field, final int operator,
			final int[] values) {
		if (field == null) {
			throw new NullPointerException("field cannot be null.");
		}

		this.field = field;
		this.values = new Object[values.length];
		for (int i = 0; i < values.length; i++) {
			this.values[i] = new Integer(values[i]);
		}
		type = MetaDataField.INT;
		style = ENUM;
		if ((operator == IN) || (operator == NOT_IN)) {
			this.operator = operator;
		} else {
			throw new IllegalArgumentException("Wrong operator.");
		}
	}

	/**
	 * Construct a enum conditional with the given field name, operator, and
	 * enum of values. The type is inferred from the given values. The operator
	 * must be 'in' or 'not in' or an exception is thrown.
	 */
	MetaDataCondition(final MetaDataField field, final int operator,
			final float[] values) {
		if (field == null) {
			throw new NullPointerException("field cannot be null.");
		}

		this.field = field;
		this.values = new Object[values.length];
		for (int i = 0; i < values.length; i++) {
			this.values[i] = new Float(values[i]);
		}
		type = MetaDataField.FLOAT;
		style = ENUM;
		if ((operator == IN) || (operator == NOT_IN)) {
			this.operator = operator;
		} else {
			throw new IllegalArgumentException("Wrong operator.");
		}
	}

	/**
	 * Construct a enum conditional with the given field name, operator, and
	 * enum of values. The type is inferred from the given values. The operator
	 * must be 'in' or 'not in' or an exception is thrown.
	 */
	MetaDataCondition(final MetaDataField field, final int operator,
			final String[] values) {
		if (field == null) {
			throw new NullPointerException("field cannot be null.");
		}

		this.field = field;
		this.values = new Object[values.length];
		this.values = values;
		type = MetaDataField.STRING;
		style = ENUM;
		if ((operator == IN) || (operator == NOT_IN)) {
			this.operator = operator;
		} else {
			throw new IllegalArgumentException("Wrong operator.");
		}
	}

	// TABLE
	/**
	 * Construct a table conditional with the given field names and
	 * MetaDataTable.
	 */
	MetaDataCondition(final MetaDataField field, final MetaDataTable table) {
		if (field == null) {
			throw new NullPointerException("field cannot be null.");
		}

		this.field = field;
		values = new Object[1];
		values[0] = table;
		type = TABLE;
		style = TABLE;
		// operators are in the table
		operator = EQUAL;
	}

	/**
	 * Construct a table conditional with the given field names and
	 * MetaDataTable.
	 */
	MetaDataCondition(final MetaDataField field, final int operator,
			final MetaDataTable table) {
		if (field == null) {
			throw new NullPointerException("field cannot be null.");
		}

		this.field = field;
		values = new Object[1];
		values[0] = table;
		type = TABLE;
		style = TABLE;
		this.operator = operator;
	}

	/**
	 * Finalizes the object by explicitly letting go of each of its internally
	 * held values.
	 */
	@Override
	protected void finalize() {
		if (field != null) {
			field = null;
		}
		if (values != null) {
			values = null;
		}
	}

	// ----------------------------------------------------------------------
	// Setters & Getters
	// ----------------------------------------------------------------------
	/**
	 * Returns the MetaDataGroup's field object describing the field in the
	 * condition.
	 */
	public MetaDataField getField() {
		return field;
	}

	/**
	 * Returns the field name in the condition. This is a shorthand for:<br>
	 * getField().getName();
	 */
	public String getFieldName() {
		return field.getName();
	}

	/**
	 * Returns the MetaDataGroup's field data type. This is a shorthand for:<br>
	 * getField().getType();
	 */
	public int getFieldType() {
		return field.getType();
	}

	/**
	 * Returns the operator code for the 'rangepair', 'list' and 'scalar' style
	 * conditionals.
	 */
	public int getOperator() {
		return operator;
	}

	/**
	 * Returns the value style for the conditional. Styles include: scalar,
	 * rangepair, list.
	 */
	public int getStyle() {
		return style;
	}

	/**
	 * Returns the length of the value list, if the style is an enum. Returns 2
	 * for rangepairs, and 1 otherwise (there is always at least one value).
	 */
	public int getCount() {
		return values.length;
	}

	/**
	 * For a 'rangepair' style conditional, there are two values at indexes 0,0
	 * and 0,1. These methods return those values, or throw an
	 * IllegalArgumentException if the index is out of range.
	 * <P>
	 * For a 'list' style conditional, there is a list of values at indexes 0,0
	 * to 0,n-1, where n is returned by getCount(). These methods return those
	 * values, or throw an IllegalArgumentException when the index is out of
	 * range.
	 * <P>
	 * For a 'scalar' style conditional, there is only one value at index 0,0.
	 * These methods return this value, or throw an IllegalArgumentException if
	 * the index is not 0,0.
	 */
	public int getIntValue(final int index) {
		if ((index < values.length) && (index >= 0)) {
			if (style == TABLE) {
				throw new UnsupportedOperationException();
			} else {
				if (type == MetaDataField.INT) {
					return ((Integer) values[index]).intValue();
				} else if (type == MetaDataField.FLOAT) {
					return ((Float) values[index]).intValue();
				} else if (type == MetaDataField.STRING) {
					return new Integer(values[index].toString()).intValue();
				}
			}
		}

		throw new IllegalArgumentException();
	}

	/**
	 * For a 'rangepair' style conditional, there are two values at indexes 0,0
	 * and 0,1. These methods return those values, or throw an exception if the
	 * index is out of range.
	 * <P>
	 * For a 'list' style conditional, there is a list of values at indexes 0,0
	 * to 0,n-1, where n is returned by getCount(). These methods return those
	 * values, or throw an exception when the index is out of range.
	 * <P>
	 * For a 'scalar' style conditional, there is only one value at index 0,0.
	 * These methods return this value, or throw an exception if the index is
	 * not 0,0.
	 */
	public float getFloatValue(final int index) {
		if ((index < values.length) && (index >= 0)) {
			if (style == TABLE) {
				throw new UnsupportedOperationException();
			} else {
				if (type == MetaDataField.INT) {
					return ((Integer) values[index]).floatValue();
				} else if (type == MetaDataField.FLOAT) {
					return ((Float) values[index]).floatValue();
				} else if (type == MetaDataField.STRING) {
					return new Float(values[index].toString()).floatValue();
				}
			}
		}

		throw new IllegalArgumentException();
	}

	/**
	 * For a 'rangepair' style conditional, there are two values at indexes 0
	 * and 1. These methods return those values, or throw an exception if the
	 * index is out of range.
	 * <P>
	 * For a 'list' style conditional, there is a list of values at indexes 0 to
	 * n-1, where n is returned by getCount(). These methods return those
	 * values, or throw an exception when the index is out of range.
	 * <P>
	 * For a 'scalar' style conditional, there is only one value at index 0.
	 * These methods return this value, or throw an exception if the index is
	 * not 0.
	 */
	public String getStringValue(final int index) {
		if ((index < values.length) && (index >= 0)) {
			if (style == TABLE) {
				return "TABLE";
			} else {
				if (type == MetaDataField.INT) {
					return ((Integer) values[index]).toString();
				} else if (type == MetaDataField.FLOAT) {
					return ((Float) values[index]).toString();
				} else if (type == MetaDataField.STRING) {
					if (values[index] == null) {
						return "";
					} else {
						return values[index].toString();
					}
				}
			}
		}

		throw new IllegalArgumentException();
	}

	// No rangepair or enum for tables

	/**
	 * These methods are the same as calling the above list methods, but with an
	 * index of 0.
	 */
	public int getIntValue() {
		return getIntValue(0);
	}

	/**
	 * These methods are the same as calling the above list methods, but with an
	 * index of 0.
	 */
	public float getFloatValue() {
		return getFloatValue(0);
	}

	/**
	 * These methods are the same as calling the above list methods, but with an
	 * index of 0.
	 */
	public String getStringValue() {
		return getStringValue(0);
	}

	/**
	 * Returns the table stored in this conditional. A 'table' conditional is
	 * both a type and a style. This method returns the table value at index 0.
	 */
	public MetaDataTable getTableValue() {
		if (style != TABLE) {
			return null;
			// throw new UnsupportedOperationException();
		} else {
			return (MetaDataTable) values[0];
		}
	}

	/**
	 * Returns a string representation of the object.
	 */
	@Override
	public String toString() {
		String toString = new String(field.getName());
		toString += " " + getOperatorString(operator);
		for (Object value : values) {
			toString += " " + value;
		}
		return toString;
	}

	/**
	 * Tests this MetaDataCondition object for equality with the given object.
	 * Returns <code>true</code> if and only if the argument is not
	 * <code>null</code> and both are MetaDataCondition objects with equal
	 * field, operators and values.
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

			MetaDataCondition temp = (MetaDataCondition) obj;

			if (getField().equals(temp.getField())) {
				if (getOperator() == temp.getOperator()) {
					for (int i = 0; i < values.length; i++) {
						if (!values[i].equals(temp.values[i])) {
							return false;
						}
					}
					return true;
				}
			}
		} catch (ClassCastException e) {
			return false;
		}
		return false;
	}

	private static String[] operatorStrings = { "=", "<>", "<", ">", "<=",
			">=", "in", "not in", "between", "not between", "like", "not like",
			"sounds like", "sounds not like", "TABLE", "num<", "num>", "num<=",
			"num>=", };

	public static String getOperatorString(final int operator) {
		if ((operator < 0) || (operator >= operatorStrings.length)) {
			throw new IllegalArgumentException();
		}

		return operatorStrings[operator];
	}

	public static String[] getOperatorStrings() {
		return operatorStrings;
	}

	public String getOperatorString() {
		if ((operator < 0) || (operator >= operatorStrings.length)) {
			throw new IllegalArgumentException();
		}

		return operatorStrings[operator];
	}
}
