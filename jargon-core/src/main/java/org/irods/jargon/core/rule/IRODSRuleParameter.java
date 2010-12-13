//
//  Copyright (c) 2008  San Diego Supercomputer Center (SDSC),
//  University of California, San Diego (UCSD), San Diego, CA, USA.
//
//  Users and possessors of this source code are hereby granted a
//  nonexclusive, royalty-free copyright and design patent license
//  to use this code in individual software.  License is not granted
//  for commercial resale, in whole or in part, without prior written
//  permission from SDSC/UCSD.  This source is provided "AS IS"
//  without express or implied warranty of any kind.
//
//
//  FILE
//  IRODSRuleParameter.java  -  edu.sdsc.grid.io.irods.Parameter
//
//  CLASS HIERARCHY
//  java.lang.Object
//      |
//      +-edu.sdsc.grid.io.irods.Parameter
//
//
//  PRINCIPAL AUTHOR
//  Lucas Gilbert, SDSC/UCSD
//
//
package org.irods.jargon.core.rule;

import java.lang.reflect.Array;

import edu.sdsc.grid.io.Base64;
import edu.sdsc.grid.io.irods.IRODSConstants;
import edu.sdsc.grid.io.irods.Tag;

/**
 * Represents A microservice/rule parameter for input or output, including type
 * information where known. This object is used internally during the
 * translation of a rule from a plain text value to an object representation.
 */
public class IRODSRuleParameter {
	static final String INT_PI = "INT_PI";
	static final String MY_INT = "myInt";
	static final String STR_PI = "STR_PI";
	static final String MY_STR = "myStr";
	static final String BUF_LEN_PI = "BUF_LEN_PI";
	static final String BIN_BYTES_BUF_PI = "BinBytesBuf_PI";
	static final String EXEC_CMD_OUT_PI = "ExecCmdOut_PI";
	static final String BUFLEN = "buflen";
	static final String BUF = "buf";
	static final String NULL_PI = "NULL_PI";

	// FIXME: does this rule type information have any practical use? If not get
	// rid of it

	private String uniqueName = "";

	private Object value;
	private String type;

	IRODSRuleParameter() {
		this(null, null, STR_PI);
	}

	IRODSRuleParameter(final int value) {
		this(null, new Integer(value), INT_PI);
	}

	IRODSRuleParameter(final String value) {
		this(null, value, STR_PI);
	}

	IRODSRuleParameter(final byte[] value) {
		this(null, value, BUF_LEN_PI);
	}

	IRODSRuleParameter(final String name, final String value) {
		this(name, value, STR_PI);
	}

	IRODSRuleParameter(final String name, final Object value, final String type) {
		if (value == null) {
			setNullValue();
		} else {
			this.value = value;
			this.type = type;
		}

		if (name != null) {
			uniqueName = name;
		} else {
			for (int i = 0; i < 8; i++) {
				uniqueName += ((char) (65 + Math.random() * 25));
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("IRODSRuleParameter");
		sb.append("\n   uniqueName:");
		sb.append(getUniqueName());
		sb.append("\n   type:");
		sb.append(getType());
		sb.append("\n   value:");
		sb.append(getStringValue());
		return sb.toString();
	}

	/**
	 * For parameters that do not have initial values. Parameters that are not
	 * input values for the rule engine.
	 */
	void setNullValue() {
		this.value = "";
		type = NULL_PI;
	}

	void setIntValue(final int value) {
		this.value = new Integer(value);
		type = INT_PI;
	}

	void setStringValue(final String value) {
		this.value = value;
		type = STR_PI;
	}

	void setByteValue(final byte[] value) {
		this.value = value;
		type = BUF_LEN_PI;
	}

	public String getType() {
		return type;
	}

	public int getIntValue() {
		if (value instanceof Integer) {
			return ((Integer) value).intValue();
		} else {
			// will fail on byte[]...
			return Integer.parseInt(value.toString());
		}
	}

	/**
	 * Get the value part of the parameter as a
	 * <code>String/code>.  Note that arrays are translated into Strings.
	 * 
	 * @return <code>String</code> containing the value of the
	 *         IRODSRuleParameter.
	 */
	public String getStringValue() {
		if (value.getClass().isArray() && type.equals(EXEC_CMD_OUT_PI)) {

			StringBuilder stringValue = new StringBuilder();
			String msg = new String();
			int alength = Array.getLength(value);
			for (int ij = 0; ij < alength; ij++) {
				if (Array.get(value, ij) != null) {
					msg = Array.get(value, ij).toString();
					if (msg != null) {
						msg = new String(Base64.fromString(msg));
						if (msg != null) {
							stringValue.append(msg);
						}
					}
				}
			}
			return stringValue.toString();
		} else {
			return value.toString();
		}
	}

	public byte[] getByteValue() {
		if (value instanceof byte[]) {
			return (byte[]) value;
		} else {
			return value.toString().getBytes();
		}
	}

	public String getUniqueName() {
		return uniqueName;
	}

	public Object getValue() {
		return value;
	}

	public Tag createMsParamArray() {
		/*
		 * <MsParamArray_PI> <paramLen>2</paramLen> <oprType>0</oprType>
		 * <MsParam_PI> <label>*A</label> <type>STR_PI</type> <STR_PI>
		 * <myStr>getErrorStr</myStr> </STR_PI> </MsParam_PI> <MsParam_PI>
		 * <label>*B</label> <type>STR_PI</type> <STR_PI> <myStr>513000</myStr>
		 * </STR_PI> </MsParam_PI> </MsParamArray_PI>
		 */
		Tag param = new Tag(IRODSConstants.MsParam_PI, new Tag[] {
				new Tag(IRODSConstants.label, getUniqueName()),
				new Tag(IRODSConstants.type, getType()), });

		if (type.equals(INT_PI)) {
			param.addTag(new Tag(INT_PI, new Tag[] {
			// only one parameter, the int
					new Tag(MY_INT, getIntValue()), }));
		} else if (type.equals(BUF_LEN_PI)) {
			param.addTag(new Tag(BUF_LEN_PI, new Tag[] {
					// send a byte buffer
					new Tag(BUFLEN, getByteValue().length),
					// maybe convert to Base64?
					new Tag(BUF, new String(getByteValue())), }));
		} else {// STR_PI or NULL_PI
			param.addTag(new Tag(STR_PI, new Tag[] {
			// only one parameter, the string
			// if default, try sending the string value, might work...
					new Tag(MY_STR, getStringValue()), }));
		}
		return param;
	}
}