package org.irods.jargon.datautils.shoppingcart;

import java.io.Serializable;

/**
 * An entry in the shopping cart of files
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class ShoppingCartEntry implements Serializable {

	private static final long serialVersionUID = 3542741989329890930L;
	private final String fileName;

	/**
	 * Static initializer creates a new shopping cart entry
	 * 
	 * @param fileName
	 *            <code>String</code> with the file name for the entry in the
	 *            shopping cart
	 * @return
	 */
	public static ShoppingCartEntry instance(final String fileName) {
		return new ShoppingCartEntry(fileName);
	}

	private ShoppingCartEntry(final String fileName) {
		if (fileName == null || fileName.isEmpty()) {
			throw new IllegalArgumentException("null or empty fileName");
		}
		this.fileName = fileName;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("entry file:");
		sb.append(fileName);
		return sb.toString();
	}

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

}
