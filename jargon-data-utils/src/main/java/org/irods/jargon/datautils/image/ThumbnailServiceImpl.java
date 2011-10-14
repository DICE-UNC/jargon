package org.irods.jargon.datautils.image;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.codec.binary.Base64;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.RuleProcessingAO;
import org.irods.jargon.core.rule.IRODSRuleExecResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage the creation and maintenance of thumbnail images for image files
 * stored in iRODS.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class ThumbnailServiceImpl implements ThumbnailService {

	/**
	 * Factory to create necessary Jargon access objects, which interact with
	 * the iRODS server
	 */
	private IRODSAccessObjectFactory irodsAccessObjectFactory;

	/**
	 * Describes iRODS server and account information
	 */
	private IRODSAccount irodsAccount;

	public static final Logger log = LoggerFactory
			.getLogger(ThumbnailServiceImpl.class);

	/**
	 * Create and service to manage thumbnail images in iRODS
	 * 
	 * @param irodsAccessObjectFactory
	 *            {@link IRODSAccessObjectFactory} to create iRODS objects
	 * @param irodsAccount
	 *            {@link IRODSAccount} that repesents the connection to the
	 *            iRODS server
	 */
	public ThumbnailServiceImpl(
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount) {
		super();

		if (irodsAccessObjectFactory == null) {
			throw new IllegalArgumentException("null irodsAccessObjectFactory");
		}

		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}

		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
		this.irodsAccount = irodsAccount;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.datautils.image.ThumbnailService#generateThumbnailForIRODSPath(java.io.File, java.lang.String)
	 */
	@Override
	public File generateThumbnailForIRODSPath(final File workingDirectory,
			final String irodsAbsolutePathToGenerateThumbnailFor)
			throws JargonException {

		log.info("generateThumbnailForIRODSPath()");

		if (workingDirectory == null) {
			throw new IllegalArgumentException("null workingDirectory");
		}

		if (irodsAbsolutePathToGenerateThumbnailFor == null
				|| irodsAbsolutePathToGenerateThumbnailFor.isEmpty()) {
			throw new IllegalArgumentException(
					"nul irodsAbsolutePathToGenerateThumbnailFor");
		}

		if (workingDirectory.exists() && workingDirectory.isDirectory()) {
			// OK
		} else {
			throw new IllegalArgumentException(
					"working directory non existent or not a directory");
		}
		
		File targetTempFile = new File(workingDirectory, irodsAbsolutePathToGenerateThumbnailFor);
		targetTempFile.getParentFile().mkdirs();
		log.info("thumbnail target temp file:${}", targetTempFile.getAbsolutePath());
		
		
		// get Base64 Encoded data from a rule invocation, this represents the generated thumbnail

		StringBuilder sb = new StringBuilder(
				"getIRODSServerCurrentTime||msiGetSystemTime(*Time,null)##writeLine(stdout, *Time)|nop\n");
		sb.append("null\n");
		sb.append("*Time%ruleExecOut");
		RuleProcessingAO ruleProcessingAO = getIrodsAccessObjectFactory()
				.getRuleProcessingAO(getIrodsAccount());
		IRODSRuleExecResult result = ruleProcessingAO
				.executeRule(sb.toString());
		String execOut = (String) result.getOutputParameterResults()
				.get("*ImageData").getResultObject();

		if (execOut == null) {
			throw new JargonException(
					"no data returned from gen thumbnail rule execution");
		}
		
		InputStream is = new java.io.ByteArrayInputStream(Base64.decodeBase64(execOut));
		try {
			OutputStream fos = new BufferedOutputStream(new FileOutputStream(targetTempFile));
			log.info("have image data, stream to temp file");
			byte[] buffer = new byte[1024];
			int len = is.read(buffer);
			while (len != -1) {
			    fos.write(buffer, 0, len);
			    len = is.read(buffer);
			}
			fos.flush();
			fos.close();
			is.close();
			return targetTempFile;
		} catch (FileNotFoundException e) {
			log.error("file not found exception for temp image output stream", e);
			throw new JargonException("no file found when generating temp image output stream", e);
		} catch (IOException e) {
			log.error("IOException for temp image output stream", e);
			throw new JargonException("IOException when generating temp image output stream", e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.irods.jargon.datautils.image.ThumbnailService#retrieveThumbnailByIRODSAbsolutePath(java.io.File, java.lang.String)
	 */
	@Override
	public InputStream retrieveThumbnailByIRODSAbsolutePath(final File workingDirectory,
			final String irodsAbsolutePathToGenerateThumbnailFor) throws JargonException {
		
		log.info("retrieveThumbnailByIRODSAbsolutePath()");
		File imageFile = generateThumbnailForIRODSPath(workingDirectory, irodsAbsolutePathToGenerateThumbnailFor);
		log.info("thumbnail generated at:{}",imageFile.getAbsolutePath());
		
		try {
			return new FileInputStream(imageFile);
		} catch (FileNotFoundException e) {
			throw new JargonException("Thumbnail not found for given path", e);
		}
		
	}

	/**
	 * @return the irodsAccessObjectFactory
	 */
	public IRODSAccessObjectFactory getIrodsAccessObjectFactory() {
		return irodsAccessObjectFactory;
	}

	/**
	 * @param irodsAccessObjectFactory
	 *            the irodsAccessObjectFactory to set
	 */
	public void setIrodsAccessObjectFactory(
			final IRODSAccessObjectFactory irodsAccessObjectFactory) {
		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
	}

	/**
	 * @return the irodsAccount
	 */
	public IRODSAccount getIrodsAccount() {
		return irodsAccount;
	}

	/**
	 * @param irodsAccount
	 *            the irodsAccount to set
	 */
	public void setIrodsAccount(final IRODSAccount irodsAccount) {
		this.irodsAccount = irodsAccount;
	}

}
