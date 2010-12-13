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
//  MetaDataValueEnum.java  -  edu.sdsc.grid.io.MetaDataValueEnum
//
//  CLASS HIERARCHY
//  java.lang.Object
//      |
//      +-.MetaDataValueEnum
//
//  PRINCIPAL AUTHOR
//  Lucas Gilbert, SDSC/UCSD
//
//
package edu.sdsc.grid.io;

/**
 * Some meta data fields have a specific list of values that they allow. A
 * "meta data value enum" describes one such value.
 * <P>
 * There are no 'set' methods. Once constructed, the object cannot be changed.
 * 
 * @author Lucas Gilbert, San Diego Supercomputer Center
 */
public final class MetaDataValueEnum {
	// ----------------------------------------------------------------------
	// Constants
	// ----------------------------------------------------------------------

	// ----------------------------------------------------------------------
	// Fields
	// ----------------------------------------------------------------------
	private String value;

	private String description;

	// ----------------------------------------------------------------------
	// Constructors and Destructors
	// ----------------------------------------------------------------------
	/**
	 * Construct a a value enum. The constructor is package private and is only
	 * called by MetaDataGroup classes when they set up their descriptions of
	 * themselves.
	 */
	MetaDataValueEnum(final String value, final String description) {
		this.value = value;
		this.description = description;
	}

	// ----------------------------------------------------------------------
	// Getters
	// ----------------------------------------------------------------------
	/**
	 * This is the string that is usable, verbatum, as a value for the field
	 * when building conditionals. It is also one of the expected values that
	 * may be returned by a query containing this field.
	 * 
	 * @return The value string for this enum item.
	 */
	public String getValue() {
		return value;
	}

	/**
	 * This string may be displayed by a GUI. It has no embedded carriage
	 * returns, so the application is expected to insert line breaks to wrap the
	 * text appropriately for its way of displaying it.
	 * 
	 * @return A description string for this enum value.
	 */
	public String getDescription() {
		return description;
	}
}
