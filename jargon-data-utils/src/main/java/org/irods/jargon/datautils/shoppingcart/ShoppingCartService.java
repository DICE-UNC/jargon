package org.irods.jargon.datautils.shoppingcart;

import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.datautils.DataUtilsService;
import org.irods.jargon.datautils.datacache.DataCacheServiceFactory;

/**
 * Describe a service meant to manipulate a 'shopping cart' of files for
 * download. The service alllows a web interface, for example, to build the cart
 * and store it so that a client application can pick up and process the cart.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface ShoppingCartService extends DataUtilsService {

	/**
	 * Serialize (by creating a text file representation with cart file names
	 * delimited by the \n character) the contents of the shopping cart into an
	 * iRODS file. The cart contents will be encrypted by the provided key. By
	 * default, the cart file goes in a special working directory in the iRODS
	 * home directory of the user represented by the <code>IRODSAccount</code>
	 * provided by this service.
	 * 
	 * @param fileShoppingCart
	 *            {@link FileShoppingCart} with the contents. Note that, if the
	 *            cart is empty, an empty cart file is generated.
	 * @param key
	 *            <code>String</code> with an arbitrary key used to encrypt the
	 *            contents of the file
	 * @return <code>String</code> with the absolute path to the cart file in
	 *         iRODS
	 * @throws JargonException
	 */
	String serializeShoppingCartAsLoggedInUser(
			final FileShoppingCart fileShoppingCart, final String key)
			throws JargonException;

	/**
	 * Get the factory (required) used to create data cache service components.
	 * 
	 * @param dataCacheServiceFactory
	 *            {@link DataCacheServiceFactory}
	 */
	DataCacheServiceFactory getDataCacheServiceFactory();

	/**
	 * Set the factory (required) used to create data cache service components
	 * 
	 * @return {@link DataCacheServiceFactory}
	 */
	void setDataCacheServiceFactory(
			DataCacheServiceFactory dataCacheServiceFactory);

	/**
	 * Retrieve a <code>FileShoppingCart</code> from iRODS. This has been
	 * serialized and encrypted by an arbitrary key for the user that is logged
	 * in.
	 * 
	 * @param key
	 *            <code>String</code> that was used to serialize the shopping
	 *            cart using the
	 *            <code>serializeShoppingCartAsLoggedInUser</code> method.
	 *            Without the correct key, the cart cannot be found or
	 *            de-serialized.
	 * @return {@link FileShoppingCart} representing the de-serialized data
	 * @throws DataNotFoundException
	 *             if the cart cannot be retrieved
	 * @throws JargonException
	 */
	FileShoppingCart retreiveShoppingCartAsLoggedInUser(String key)
			throws DataNotFoundException, JargonException;

	/**
	 * Place the shopping cart as a serialized file in the given user's home
	 * directory and return a temporary password for that specified user that
	 * may be passed to iDrop lite as the password parameter.
	 * 
	 * @param fileShoppingCart
	 * @param key
	 * @return
	 * @throws JargonException
	 */
	String serializeShoppingCartAsSpecifiedUser(
			FileShoppingCart fileShoppingCart, String key)
			throws JargonException;

}