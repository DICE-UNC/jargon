package org.irods.jargon.datautils.shoppingcart;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.irods.jargon.core.connection.IRODSAccount;

/**
 * Represents a shopping cart of iRODS files
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class FileShoppingCart implements Serializable {

	private static final long serialVersionUID = 2046906353965566056L;
	private final IRODSAccount irodsAccount;
	private final Map<String, ShoppingCartEntry> shoppingCartEntries = new ConcurrentHashMap<String, ShoppingCartEntry>();

	/**
	 * Static initializer takes creates a shopping cart for the given account
	 * @param irodsAccount {@link IRODSAccount} for the current shopping cart
	 * @return <code>FileShoppingCart</code> instane.
	 */
	public static FileShoppingCart instance(final IRODSAccount irodsAccount) {
		return new FileShoppingCart(irodsAccount);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("FileShoppingCart");
		sb.append("\n   irodsAccount:");
		sb.append(irodsAccount);
		for (Entry<String, ShoppingCartEntry> entry : shoppingCartEntries.entrySet()) {
			sb.append("\n");
			sb.append(entry.getValue());
		}
		
		return sb.toString();
	}
	
	/**
	 * Handy method to check if cart has any entries.
	 * @return <code>boolean</code> of <code>true</code> if cart has entries
	 */
	public boolean hasItems() {
		return (shoppingCartEntries.entrySet().size() > 0);
	}

	private FileShoppingCart(final IRODSAccount irodsAccount) {
		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}
		this.irodsAccount = irodsAccount;
	}

	/**
	 * @return the irodsAccount
	 */
	public IRODSAccount getIrodsAccount() {
		return irodsAccount;
	}
	
	/**
	 * Place an item in the shopping cart.  The cart will overwrite an existing entry, thus preventing duplicates.	
	 * @param shoppingCartEntry {@link ShoppingCartEntry} to add to the cart
	 */
	public void addAnItem(final ShoppingCartEntry shoppingCartEntry) {
		
		if (shoppingCartEntry == null) {
			throw new IllegalArgumentException("null shoppingCartEntry");
		}
		
		shoppingCartEntries.put(shoppingCartEntry.getFileName(), shoppingCartEntry);
	}
	
	/**
	 * Get a <code>List<String></code> of the file names in the shopping cart
	 * @return <code>List<String></code> with the files in the shopping cart
	 */
	public List<String> getShoppingCartFileList() {
		List<String> fileNames = new ArrayList<String>();
		for(ShoppingCartEntry shoppingCartEntry : shoppingCartEntries.values()) {
			fileNames.add(shoppingCartEntry.getFileName());
		}
		return fileNames;
	}
	
	
}
