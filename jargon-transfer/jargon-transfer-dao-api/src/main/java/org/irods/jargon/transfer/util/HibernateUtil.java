/**
 * 
 */
package org.irods.jargon.transfer.util;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.transfer.dao.domain.LocalIRODSTransfer;
import org.irods.jargon.transfer.dao.domain.LocalIRODSTransferItem;
import org.irods.jargon.transfer.util.StringEncryptor.EncryptionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility to manage hibernate session. Instances are created such that they can compute the desired location of the
 * transfer database. In many cases, the transfer database will be
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class HibernateUtil {

    private static final Logger log = LoggerFactory.getLogger(HibernateUtil.class);

    private final SessionFactory factory;

    /**
     * Create an instance that can open Hibernate sessions using the default hibernate xml configuration.
     * 
     * @return
     * @throws JargonException
     */
    public static HibernateUtil instanceUsingDefaultConfig() throws JargonException {
        return new HibernateUtil();
    }

    /**
     * Create an instance that uses a database at the given path. This is useful for creation of databases within user
     * directories
     * 
     * @param pathToDatabase
     *            <code>String</code> with the full path to the transfer database such that it can be incorporated in
     *            the jdbc url.
     * @return <code>HibernateUtil</code> instance configured to create sessions to the appropriate database.
     * @throws JargonException
     */
    public static HibernateUtil instanceGivingPathToDatabase(final String pathToDatabase) throws JargonException {
        return new HibernateUtil(pathToDatabase);
    }

    /**
     * Create an instance that uses a database at the given path. This is useful for creation of databases within user
     * directories
     * 
     * @param pathToDatabase
     *            <code>String</code> with the full path to the transfer database such that it can be incorporated in
     *            the jdbc url.
     * @return <code>HibernateUtil</code> instance configured to create sessions to the appropriate database.
     * @throws JargonException
     */
    private HibernateUtil(final String pathToDatabase) throws JargonException {
        factory = setUpFactoryUsingGivenDatabasePath(pathToDatabase);
    }

    /**
     * Create an instance using the default Hibernate configuration.
     * 
     * @throws JargonException
     */
    private HibernateUtil() throws JargonException {
        log.info("creating hibernate session factory using default configuration in the hibernate.cfg.xml file");
        factory = new Configuration().configure().buildSessionFactory();
    }

    /**
     * Return a session using the pre-configured Hibernate session configuration information.
     * 
     * @return
     */
    public Session getSession() {
        return factory.getCurrentSession();
    }

    private SessionFactory setUpFactoryUsingGivenDatabasePath(final String transferDatabasePath) throws JargonException {

        String userHomeDirectory = System.getProperty("user.home");
        log.info("user home directory = {}", userHomeDirectory);
        StringBuilder sb = new StringBuilder();
        sb.append(userHomeDirectory);
        sb.append("/.idrop/");
        sb.append(transferDatabasePath);

        StringBuilder jdbcUrlBuilder = new StringBuilder();
        jdbcUrlBuilder.append("jdbc:derby:");
        jdbcUrlBuilder.append(sb);
        jdbcUrlBuilder.append(";create=true");
        // jdbcUrlBuilder.append("target/transferDatabase");

        // Properties p = System.getProperties();
        // p.put("derby.system.home", derbyPath);

        sb.append(transferDatabasePath);
        log.info("computed url for database:{}", jdbcUrlBuilder.toString());

        Configuration cfg = new Configuration();
        cfg.addClass(LocalIRODSTransfer.class);
        cfg.addClass(LocalIRODSTransferItem.class);
        cfg.setProperty("hibernate.connection.driver_class", "org.apache.derby.jdbc.EmbeddedDriver");
        cfg.setProperty("hibernate.connection.password", "transfer");
        cfg.setProperty("hibernate.connection.url", jdbcUrlBuilder.toString());
        cfg.setProperty("hibernate.connection.username", "transfer");
        cfg.setProperty("hibernate.c3p0.min_size", "1");
        cfg.setProperty("hibernate.c3p0.max_size", "3");
        cfg.setProperty("hibernate.c3p0.timeout", "1800");
        cfg.setProperty("hibernate.c3p0.max_statements", "0");
        cfg.setProperty("hibernate.dialect", "org.hibernate.dialect.DerbyDialect");
        cfg.setProperty("hibernate.current_session_context_class", "thread");
        cfg.setProperty("cache.provider_class", "org.hibernate.cache.NoCacheProvider");
        cfg.setProperty("hibernate.hbm2ddl.auto", "update");
        log.info("hibernate config:{}", cfg);

        return cfg.buildSessionFactory();

    }

    public static String obfuscate(final String stringToObfuscate) throws JargonException {
        String encryptionKey = "123456789012345678901234567890";

        StringEncryptor encrypter;
        try {
            encrypter = new StringEncryptor(StringEncryptor.DES_ENCRYPTION_SCHEME, encryptionKey);
            String encryptedString = encrypter.encrypt(stringToObfuscate);
            return encryptedString;
        } catch (EncryptionException e) {
            throw new JargonException("error encrypting password");
        }
    }

    public static String retrieve(final String encryptedString) throws JargonException {
        String encryptionKey = "123456789012345678901234567890";

        StringEncryptor encrypter;
        try {
            encrypter = new StringEncryptor(StringEncryptor.DES_ENCRYPTION_SCHEME, encryptionKey);
            String decryptedString = encrypter.decrypt(encryptedString);
            return decryptedString;
        } catch (EncryptionException e) {
            throw new JargonException("error decrypting password");
        }
    }
}
