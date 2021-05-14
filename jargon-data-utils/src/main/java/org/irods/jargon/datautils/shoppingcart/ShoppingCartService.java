package org.irods.jargon.datautils.shoppingcart;

import java.util.List;

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
	 * default, the cart file goes in a special working directory in the iRODS home
	 * directory of the user represented by the {@code IRODSAccount} provided by
	 * this service.
	 *
	 *
	 * @param fileShoppingCart {@link FileShoppingCart} with the contents. Note
	 *                         that, if the cart is empty, an empty cart file is
	 *                         generated.
	 * @param key              {@code String} with an arbitrary key used to encrypt
	 *                         the contents of the file
	 * @return {@code String} with the absolute path to the cart file in iRODS
	 * @throws JargonException    {@link JargonException}
	 * @throws EmptyCartException {@link EmptyCartException}
	 */
	String serializeShoppingCartAsLoggedInUser(final FileShoppingCart fileShoppingCart, final String key)
			throws EmptyCartException, JargonException;

	/**
	 * Get the factory (required) used to create data cache service components.
	 *
	 *
	 *
	 * @return {@link DataCacheServiceFactory}
	 */
	DataCacheServiceFactory getDataCacheServiceFactory();

	/**
	 * Set the factory (required) used to create data cache service components
	 *
	 * @param dataCacheServiceFactory {@link DataCacheServiceFactory}
	 */
	void setDataCacheServiceFactory(DataCacheServiceFactory dataCacheServiceFactory);

	/**
	 * Retrieve a {@code FileShoppingCart} from iRODS. This has been serialized and
	 * encrypted by an arbitrary key for the user that is logged in. If the cart
	 * doesn't exist an empty one will be created for the user and returned
	 *
	 * @param key {@code String} that was used to serialize the shopping cart using
	 *            the {@code serializeShoppingCartAsLoggedInUser} method. Without
	 *            the correct key, the cart cannot be found or de-serialized.
	 * @return {@link FileShoppingCart} representing the de-serialized data \
	 *         * @throws JargonException {@link JargonException}
	 */
	FileShoppingCart retreiveShoppingCartAsLoggedInUser(String key) throws JargonException;

	/**
	 * Place the shopping cart as a serialized file in the given user's home
	 * directory and return a temporary password for that specified user that may be
	 * passed to iDrop lite as the password parameter, or used in another client.
	 * <p>
	 * This method may only be called by a rodsadmin user, as it needs to generate a
	 * temporary password. This functionality was added after iRODS 3.0, and will
	 * cause an error if used on a prior iRODS version. This method may only be
	 * called by a rodsadmin user, as it needs to generate a temporary password.
	 * This functionality was added after iRODS 3.0, and will cause an error if used
	 * on a prior iRODS version.
	 *
	 * @param fileShoppingCart {@link FileShoppingCart} representing the file cart
	 *                         data to persist
	 * @param key              {@code String} that will be used to serialize the
	 *                         shopping cart. Without the correct key, the cart
	 *                         cannot be found or de-serialized. This is just an
	 *                         arbitrary shared key value.
	 * @param userName         {@code String} with the name of the user for whom the
	 *                         cart will be serialized.
	 * @return {@code String} with the temporary password that may be used to access
	 *         the cart for the given user and key.
	 * @throws JargonException {@link JargonException}
	 */

	String serializeShoppingCartAsSpecifiedUser(FileShoppingCart fileShoppingCart, String key, String userName)
			throws JargonException;

	/**
	 * Append the list of items to the file shopping cart and store, duplicates will
	 * be collapsed
	 * 
	 * @param key      {@code String} with the cart key
	 * @param fileList {@code List} of {@code String} with the items (iRODS paths)
	 *                 that are the items to add
	 * @return {@link FileShoppingCart} with the new cart
	 * @throws JargonException {@link JargonException}
	 */
	FileShoppingCart appendToShoppingCart(final String key, final List<String> fileList) throws JargonException;

}
