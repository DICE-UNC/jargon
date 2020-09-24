package org.irods.jargon.datautils.shoppingcart;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import org.irods.jargon.core.exception.JargonException;

/**
 * Represents a shopping cart of iRODS files.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class FileShoppingCart implements Serializable {

	private static final long serialVersionUID = 2046906353965566056L;
	private final Map<String, ShoppingCartEntry> shoppingCartEntries = new ConcurrentHashMap<>();

	/**
	 * Static initializer takes creates a shopping cart
	 *
	 *
	 * @return {@code FileShoppingCart} instance.
	 */
	public static FileShoppingCart instance() {
		return new FileShoppingCart();
	}

	/**
	 * Serialize the contents of the shopping cart as a {@code String} where each
	 * file in the cart is one line, followed by a carriage return (\n) character.
	 * This is a suitable format for saving the shopping cart as a text file.
	 *
	 * @return {@code String} with one shopping cart file per line. The value will
	 *         be blank if no files are in the cart
	 */
	public String serializeShoppingCartContentsToStringOneItemPerLine() {
		StringBuilder sb = new StringBuilder();
		for (Entry<String, ShoppingCartEntry> entry : shoppingCartEntries.entrySet()) {
			sb.append(entry.getValue().getFileName());
			sb.append("\n");
		}
		return sb.toString();
	}

	/**
	 * Given a string representation (one file per line, separated by the \n
	 * character, build a {@code FileShoppingCart}. The cart will be empty if no
	 * files are in the serialized string form
	 *
	 * @param stringRepresentation {@code String} with a one line per file,
	 *                             separated by \n, as created by the
	 *                             {@code serializeShoppingCartContentsToStringOneItemPerLine()}
	 *                             method.
	 * @return {@link FileShoppingCart} instance
	 * @throws JargonException {@link JargonException}
	 */
	public static FileShoppingCart instanceFromSerializedStringRepresentation(final String stringRepresentation)
			throws JargonException {

		if (stringRepresentation == null) {
			throw new IllegalArgumentException("Null string representation");
		}

		/*
		 * Go through the entries by splitting on \n and create the new cart. An empty
		 * cart is returned if no entries in the serialized string data
		 */

		FileShoppingCart fileShoppingCart = new FileShoppingCart();
		if (!stringRepresentation.isEmpty()) {
			StringTokenizer tokenizer = new StringTokenizer(stringRepresentation, "\n");
			while (tokenizer.hasMoreTokens()) {
				fileShoppingCart.addAnItem(ShoppingCartEntry.instance(tokenizer.nextToken()));
			}
		}

		return fileShoppingCart;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("FileShoppingCart");
		for (Entry<String, ShoppingCartEntry> entry : shoppingCartEntries.entrySet()) {
			sb.append("\n");
			sb.append(entry.getValue());
		}

		return sb.toString();
	}

	/**
	 * Handy method to check if cart has any entries.
	 *
	 *
	 * @return {@code boolean} of {@code true} if cart has entries
	 */
	public boolean hasItems() {
		return (shoppingCartEntries.entrySet().size() > 0);
	}

	private FileShoppingCart() {

	}

	/**
	 * Place an item in the shopping cart. The cart will overwrite an existing
	 * entry, thus preventing duplicates.
	 *
	 * @param shoppingCartEntry {@link ShoppingCartEntry} to add to the cart
	 */
	public void addAnItem(final ShoppingCartEntry shoppingCartEntry) {

		if (shoppingCartEntry == null) {
			throw new IllegalArgumentException("null shoppingCartEntry");
		}

		shoppingCartEntries.put(shoppingCartEntry.getFileName(), shoppingCartEntry);
	}

	/**
	 * Remove a file from the shopping cart. Silently ignore if the item is not
	 * there
	 *
	 * @param fileName {@code String} with the absolute file path to the item to be
	 *                 removed from the cart
	 */
	public void removeAnItem(final String fileName) {
		if (fileName == null || fileName.isEmpty()) {
			throw new IllegalArgumentException("null or empty fileName");
		}

		shoppingCartEntries.remove(fileName);

	}

	/**
	 *
	 * Get a {@code List<String>} of the file names in the shopping cart
	 *
	 * @return {@code List<String>} with the files in the shopping carts
	 */
	public List<String> getShoppingCartFileList() {
		List<String> fileNames = new ArrayList<>();
		for (ShoppingCartEntry shoppingCartEntry : shoppingCartEntries.values()) {
			fileNames.add(shoppingCartEntry.getFileName());
		}
		return fileNames;
	}

	/**
	 * Clear the contents of the shopping cart
	 */
	public void clearCart() {
		shoppingCartEntries.clear();
	}

}
