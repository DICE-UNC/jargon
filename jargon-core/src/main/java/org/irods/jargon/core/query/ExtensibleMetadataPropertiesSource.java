/**
 * 
 */
package org.irods.jargon.core.query;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.irods.jargon.core.exception.JargonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mike Conway - DICE (www.irods.org) Creates an
 *         <code>ExtensibleMetaDataMapping</code> using a
 *         <code>Properties</code> file that will be on the classpath.
 */
public class ExtensibleMetadataPropertiesSource implements
		ExtensibleMetaDataSource {

	private static Logger log = LoggerFactory
			.getLogger(ExtensibleMetadataPropertiesSource.class);

	private Map<String, String> extensibleMetaDataProperties = null;
	private String propertiesFileName = "";

	/**
	 * Default constructor will look for the file
	 * 
	 * <pre>
	 * extended_icat_data.properties
	 * </pre>
	 * 
	 * in the classpath.
	 * 
	 * @throws JargonException
	 */
	public ExtensibleMetadataPropertiesSource() throws JargonException {
		this("extended_icat_data.properties");
	}

	/**
	 * Constructor takes a specific properties file name that contains the
	 * desired extensible metadata mappings.
	 * 
	 * @param propertiesFileName
	 *            <code>String</code> containing a valid
	 *            <code>.properties</code> file that exists on the classpath.
	 * @throws JargonException
	 */
	public ExtensibleMetadataPropertiesSource(final String propertiesFileName)
			throws JargonException {
		if (propertiesFileName == null || propertiesFileName.length() == 0) {
			String msg = "no properties file name defined";
			log.error(msg);
			throw new JargonException(msg);
		}

		if (log.isDebugEnabled()) {
			log.debug("using properties file:" + propertiesFileName);
		}

		this.propertiesFileName = propertiesFileName;
		initialize();
	}

	private void initialize() throws JargonException {
		log.debug("initializing extensible metadata properties");

		if (propertiesFileName == null || propertiesFileName.length() == 0) {
			String msg = "initialization error, no properties file name was defined";
			log.error(msg);
			throw new JargonException(msg);
		}

		ClassLoader loader = this.getClass().getClassLoader();
		InputStream in = loader.getResourceAsStream(propertiesFileName);

		if (in == null) {
			String msg = "no properties file found for:" + propertiesFileName;
			log.error(msg);
			throw new JargonException(msg);
		}

		Properties properties = new Properties();

		try {
			properties.load(in);
		} catch (IOException ioe) {
			throw new JargonException("error loading test properties", ioe);
		} finally {
			try {
				in.close();
			} catch (Exception e) {
				// ignore
			}
		}

		extensibleMetaDataProperties = new HashMap<String, String>();
		String keyString;

		for (Object key : properties.keySet()) {
			keyString = (String) key;
			extensibleMetaDataProperties.put(keyString,
					properties.getProperty((String) key));
		}

		if (log.isDebugEnabled()) {
			log.debug("extensible meta data properties dump======================");
			for (String key : extensibleMetaDataProperties.keySet()) {
				log.debug("   key:" + key + "  value:"
						+ extensibleMetaDataProperties.get(key));
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.irods.jargon.core.query.ExtensibleMetaDataSource#
	 * generateExtensibleMetaDataMapping()
	 */
	@Override
	public ExtensibleMetaDataMapping generateExtensibleMetaDataMapping()
			throws JargonException {

		log.debug("cloning the properties and building an ExtensibleMetaDataMapping");
		if (extensibleMetaDataProperties == null) {
			throw new JargonException(
					"the properties I want to use to build the metadata mapping are null");
		}

		ExtensibleMetaDataMapping mapping = ExtensibleMetaDataMapping
				.instance(extensibleMetaDataProperties);
		return mapping;

	}

}
