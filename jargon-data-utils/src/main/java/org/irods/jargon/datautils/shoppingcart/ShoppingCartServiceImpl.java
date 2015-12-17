package org.irods.jargon.datautils.shoppingcart;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.UserAO;
import org.irods.jargon.datautils.AbstractDataUtilsServiceImpl;
import org.irods.jargon.datautils.datacache.CacheServiceConfiguration;
import org.irods.jargon.datautils.datacache.DataCacheService;
import org.irods.jargon.datautils.datacache.DataCacheServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for general shopping cart handling, including capability to cache the
 * shopping cart and save in a target file, and then retrieve it. This is handy
 * for serializing the shopping cart contents to be picked up by a download
 * client such as iDrop lite
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class ShoppingCartServiceImpl extends AbstractDataUtilsServiceImpl
implements ShoppingCartService {

	public static final Logger log = LoggerFactory
			.getLogger(ShoppingCartServiceImpl.class);

	/**
	 * Factory for <code>DataCacheService</code> creation, must be set via
	 * constructor or injected via setter.
	 */
	private DataCacheServiceFactory dataCacheServiceFactory = null;

	/**
	 * Default (no values) constructor. Note that dependencies may be injected
	 * by setter methods, and will be checked on invocation of the various
	 * service methods.
	 */
	public ShoppingCartServiceImpl() {
		super();
	}

	/**
	 * Constructor creates a <code>ShoppingCartService</code> with necessary
	 * dependencies
	 *
	 * @param irodsAccessObjectFactory
	 *            {@link IRODSAccessObjectFactory}
	 * @param irodsAccount
	 *            {@link IRODSAccount} that will create, and for whom the cart
	 *            contents will be cached
	 * @param dataCacheServiceFactory
	 *            {@link DataCacheServiceFactory} used to create
	 *            DataCacheServiceComponents
	 */
	public ShoppingCartServiceImpl(
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount,
			final DataCacheServiceFactory dataCacheServiceFactory) {
		super(irodsAccessObjectFactory, irodsAccount);

		if (dataCacheServiceFactory == null) {
			throw new IllegalArgumentException("null dataCacheServiceFactory");
		}

		this.dataCacheServiceFactory = dataCacheServiceFactory;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.datautils.shoppingcart.ShoppingCartService#
	 * serializeShoppingCartAsLoggedInUser
	 * (org.irods.jargon.datautils.shoppingcart.FileShoppingCart,
	 * java.lang.String)
	 */
	@Override
	public String serializeShoppingCartAsLoggedInUser(
			final FileShoppingCart fileShoppingCart, final String key)
					throws JargonException {

		log.info("serializeShoppingCartAsLoggedInUser()");

		if (fileShoppingCart == null) {
			throw new IllegalArgumentException("null fileShoppingCart");
		}

		if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException("null or empty key");
		}

		// check for dependencies
		checkContracts();

		log.info("fileShoppingCart:${}", fileShoppingCart);
		log.info("key:${}", key);

		/*
		 * For the shopping cart, have it clean up old carts and cache in the
		 * standard place in the user home directory
		 */
		CacheServiceConfiguration config = new CacheServiceConfiguration();
		config.setDoCleanupDuringRequests(true);
		config.setCacheInHomeDir(true);

		log.info("create data cache service from factory");
		DataCacheService dataCacheService = dataCacheServiceFactory
				.instanceDataCacheService(getIrodsAccount());

		dataCacheService.setCacheServiceConfiguration(config);
		log.info("putting data into cache");
		return dataCacheService.putStringValueIntoCache(fileShoppingCart
				.serializeShoppingCartContentsToStringOneItemPerLine(), key);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.datautils.shoppingcart.ShoppingCartService#
	 * retreiveShoppingCartAsLoggedInUser(java.lang.String)
	 */
	@Override
	public FileShoppingCart retreiveShoppingCartAsLoggedInUser(final String key)
			throws DataNotFoundException, JargonException {

		log.info("retreiveShoppingCartAsLoggedInUser()");

		if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException("null or empty key");
		}

		// check for dependencies
		checkContracts();

		log.info("key:{}", key);

		/*
		 * For the shopping cart, have it clean up old carts and cache in the
		 * standard place in the user home directory
		 */
		CacheServiceConfiguration config = new CacheServiceConfiguration();
		config.setDoCleanupDuringRequests(true);
		config.setCacheInHomeDir(true);

		log.info("create data cache service from factory");
		DataCacheService dataCacheService = dataCacheServiceFactory
				.instanceDataCacheService(getIrodsAccount());

		/*
		 * Use the user name and key value to find the cart file in iRODS. It
		 * will be decrypted and returned back as a string.
		 */
		dataCacheService.setCacheServiceConfiguration(config);
		log.info("retrieve data from cache");
		log.info("serializing back to cart...");
		return FileShoppingCart
				.instanceFromSerializedStringRepresentation(dataCacheService
						.retrieveStringValueFromCache(
								irodsAccount.getUserName(), key));

	}

	@Override
	protected void checkContracts() {
		super.checkContracts();
		if (dataCacheServiceFactory == null) {
			throw new JargonRuntimeException(
					"dataCacheServiceFactory was not set");
		}

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.datautils.shoppingcart.ShoppingCartService#
	 * serializeShoppingCartAsSpecifiedUser
	 * (org.irods.jargon.datautils.shoppingcart.FileShoppingCart,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public String serializeShoppingCartAsSpecifiedUser(
			final FileShoppingCart fileShoppingCart, final String key,
			final String userName) throws JargonException {

		log.info("serializeShoppingCartAsSpecifiedUser()");

		if (fileShoppingCart == null) {
			throw new IllegalArgumentException("null fileShoppingCart");
		}

		if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException("null or empty key");
		}

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		// check for dependencies
		checkContracts();

		log.info("fileShoppingCart:${}", fileShoppingCart);
		log.info("key:${}", key);
		log.info("userName:${}", userName);

		// generate a temp password for the given user
		UserAO userAO = getIrodsAccessObjectFactory().getUserAO(irodsAccount);
		String tempPassword = userAO
				.getTemporaryPasswordForASpecifiedUser(userName);
		IRODSAccount tempUserAccount = IRODSAccount.instance(getIrodsAccount()
				.getHost(), getIrodsAccount().getPort(), userName,
				tempPassword, "", getIrodsAccount().getZone(),
				getIrodsAccount().getDefaultStorageResource());

		log.info("generated temp password and created temp account:${}",
				tempUserAccount);

		/*
		 * For the shopping cart, have it clean up old carts and cache in the
		 * standard place in the user home directory
		 */
		CacheServiceConfiguration config = new CacheServiceConfiguration();
		config.setDoCleanupDuringRequests(true);
		config.setCacheInHomeDir(true);

		log.info("create data cache service from factory");
		DataCacheService dataCacheService = dataCacheServiceFactory
				.instanceDataCacheService(tempUserAccount);

		dataCacheService.setCacheServiceConfiguration(config);
		log.info("putting data into cache");
		dataCacheService.putStringValueIntoCache(fileShoppingCart
				.serializeShoppingCartContentsToStringOneItemPerLine(), key);
		// close and regenerate a temp password to pass to the caller
		getIrodsAccessObjectFactory().closeSession(tempUserAccount);

		log.info("generate a new temp password that the caller can use");

		tempPassword = userAO.getTemporaryPasswordForASpecifiedUser(userName);
		tempUserAccount = IRODSAccount.instance(getIrodsAccount().getHost(),
				getIrodsAccount().getPort(), userName, tempPassword, "",
				getIrodsAccount().getZone(), getIrodsAccount()
				.getDefaultStorageResource());

		log.info("generated temp password and created temp account:${}",
				tempUserAccount);
		return tempPassword;
	}

	/**
	 * Set the factory (required) used to create data cache service components
	 *
	 * @return {@link DataCacheServiceFactory}
	 */
	@Override
	public DataCacheServiceFactory getDataCacheServiceFactory() {
		return dataCacheServiceFactory;
	}

	@Override
	public void setDataCacheServiceFactory(
			final DataCacheServiceFactory dataCacheServiceFactory) {
		this.dataCacheServiceFactory = dataCacheServiceFactory;
	}

}
