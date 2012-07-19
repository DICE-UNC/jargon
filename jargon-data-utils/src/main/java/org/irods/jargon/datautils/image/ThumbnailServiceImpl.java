package org.irods.jargon.datautils.image;

import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.TransferOptions.ForceOption;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.EnvironmentalInfoAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.RuleProcessingAO;
import org.irods.jargon.core.pub.domain.RemoteCommandInformation;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.rule.IRODSRuleExecResult;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.core.utils.Base64;
import org.irods.jargon.core.utils.LocalFileUtils;
import org.irods.jargon.datautils.AbstractDataUtilsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

/**
 * Manage the creation and maintenance of thumb-nail images for image files
 * stored in iRODS.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
@SuppressWarnings("restriction")
public class ThumbnailServiceImpl extends AbstractDataUtilsServiceImpl
		implements ThumbnailService {

	public static final Logger log = LoggerFactory
			.getLogger(ThumbnailServiceImpl.class);

	/**
	 * Constructor with required dependencies
	 * 
	 * @param irodsAccessObjectFactory
	 *            {@link IRODSAccessObjectFactory} that can create necessary
	 *            objects
	 * @param irodsAccount
	 *            {@link IRODSAccount} that contains the login information
	 */
	public ThumbnailServiceImpl(
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount) {
		super(irodsAccessObjectFactory, irodsAccount);
	}

	/**
	 * Default (no-values) constructor.
	 */
	public ThumbnailServiceImpl() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.datautils.image.ThumbnailService#
	 * isIRODSThumbnailGeneratorAvailable()
	 */
	@Override
	public boolean isIRODSThumbnailGeneratorAvailable() throws JargonException {
		log.info("isIRODSThumbnailGeneratorAvailable()");
		boolean found = false;
		EnvironmentalInfoAO environmentalInfoAO = irodsAccessObjectFactory
				.getEnvironmentalInfoAO(getIrodsAccount());
		try {
			log.info("listing available scripts...");
			List<RemoteCommandInformation> scripts = environmentalInfoAO
					.listAvailableRemoteCommands();

			for (RemoteCommandInformation script : scripts) {
				if (script.getCommand().equals("makeThumbnail.py")) {
					found = true;
					break;
				}
			}

			return found;

		} catch (DataNotFoundException e) {
			log.info("no ability to list commands, assume no thumbnail service");
			return false;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.datautils.image.ThumbnailService#
	 * generateThumbnailForIRODSPathViaRule(java.io.File, java.lang.String)
	 */
	@Override
	public File generateThumbnailForIRODSPathViaRule(
			final File workingDirectory,
			final String irodsAbsolutePathToGenerateThumbnailFor)
			throws IRODSThumbnailProcessUnavailableException, JargonException {

		log.info("generateThumbnailForIRODSPathViaRule()");

		if (workingDirectory == null) {
			throw new IllegalArgumentException("null workingDirectory");
		}

		if (irodsAbsolutePathToGenerateThumbnailFor == null
				|| irodsAbsolutePathToGenerateThumbnailFor.isEmpty()) {
			throw new IllegalArgumentException(
					"nul irodsAbsolutePathToGenerateThumbnailFor");
		}

		File targetTempFile = createWorkingDirectoryImageFile(workingDirectory,
				irodsAbsolutePathToGenerateThumbnailFor);

		if (targetTempFile.exists()) {
			targetTempFile.delete();
		}

		InputStream is = retrieveThumbnailByIRODSAbsolutePathViaRule(irodsAbsolutePathToGenerateThumbnailFor);
		try {
			OutputStream fos = new BufferedOutputStream(new FileOutputStream(
					targetTempFile));
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
			log.error("file not found exception for temp image output stream",
					e);
			throw new JargonException(
					"no file found when generating temp image output stream", e);
		} catch (IOException e) {
			log.error("IOException for temp image output stream", e);
			throw new JargonException(
					"IOException when generating temp image output stream", e);
		}
	}

	/**
	 * Create any necessary directories in the working directory to house the
	 * generated image.
	 * 
	 * @param workingDirectory
	 * @param irodsAbsolutePathToGenerateThumbnailFor
	 * @return
	 * @throws JargonException
	 */
	private File createWorkingDirectoryImageFile(final File workingDirectory,
			final String irodsAbsolutePathToGenerateThumbnailFor)
			throws JargonException {

		log.info("createWorkingDirectoryImageFile(), working directory is:{}",
				workingDirectory);
		log.info("irodsAbsolutePathToGenerateThumbnailFor:{}",
				irodsAbsolutePathToGenerateThumbnailFor);
		if (workingDirectory.exists()) {
			if (workingDirectory.isDirectory()) {
				// OK
			} else {
				throw new IllegalArgumentException("workingDirectory is a file");
			}
		} else {
			log.info("mkdirs on working directory");
			workingDirectory.mkdirs();
		}

		File targetTempFile = new File(workingDirectory,
				irodsAbsolutePathToGenerateThumbnailFor);
		targetTempFile.getParentFile().mkdirs();

		log.info("thumbnail target temp file:${}",
				targetTempFile.getAbsolutePath());
		return targetTempFile;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.datautils.image.ThumbnailService#
	 * retrieveThumbnailByIRODSAbsolutePathViaRule(java.lang.String)
	 */
	@Override
	public InputStream retrieveThumbnailByIRODSAbsolutePathViaRule(
			final String irodsAbsolutePathToGenerateThumbnailFor)
			throws JargonException {

		log.info("retrieveThumbnailByIRODSAbsolutePath()");

		if (irodsAbsolutePathToGenerateThumbnailFor == null
				|| irodsAbsolutePathToGenerateThumbnailFor.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsAbsolutePathToGenerateThumbnailFor");
		}

		log.info("irodsAbsolutePathToGenerateThumbnailFor:{}",
				irodsAbsolutePathToGenerateThumbnailFor);

		// get Base64 Encoded data from a rule invocation, this represents the
		// generated thumbnail

		StringBuilder sb = new StringBuilder();
		sb.append("@external\n makeThumbnailFromObj {\n");
		sb.append("msiSplitPath(*objPath,*collName,*objName);\n");
		sb.append(" msiAddSelectFieldToGenQuery(\"DATA_PATH\", \"null\", *GenQInp);\n");
		sb.append("msiAddSelectFieldToGenQuery(\"RESC_LOC\", \"null\", *GenQInp);\n");
		sb.append(" msiAddConditionToGenQuery(\"COLL_NAME\", \"=\", *collName, *GenQInp);\n");
		sb.append("msiAddConditionToGenQuery(\"DATA_NAME\", \"=\", *objName, *GenQInp);\n");
		sb.append("msiAddConditionToGenQuery(\"DATA_RESC_NAME\",\"=\", *resource, *GenQInp);\n");
		sb.append("msiExecGenQuery(*GenQInp, *GenQOut);\n");
		sb.append("foreach (*GenQOut)\n{\n");
		sb.append(" msiGetValByKey(*GenQOut, \"DATA_PATH\", *data_path);\n");
		sb.append("msiGetValByKey(*GenQOut, \"RESC_LOC\", *resc_loc);\n}\n");
		sb.append("msiExecCmd(\"makeThumbnail.py\", \"'*data_path'\", *resc_loc, \"null\", \"null\", *CmdOut);\n");
		sb.append("msiGetStdoutInExecCmdOut(*CmdOut, *StdoutStr);\n");
		sb.append(" writeLine(\"stdout\", *StdoutStr);\n}\n");
		sb.append("INPUT *objPath=\'");
		sb.append(irodsAbsolutePathToGenerateThumbnailFor.trim());
		sb.append("\',*resource=\'");
		sb.append(irodsAccount.getDefaultStorageResource());
		sb.append("'\n");
		sb.append("OUTPUT ");
		sb.append(THUMBNAIL_RULE_DATA_PARAMETER);
		RuleProcessingAO ruleProcessingAO = getIrodsAccessObjectFactory()
				.getRuleProcessingAO(getIrodsAccount());
		IRODSRuleExecResult result = ruleProcessingAO
				.executeRule(sb.toString());

		String execOut;
		try {
			execOut = (String) result.getOutputParameterResults()
					.get(THUMBNAIL_RULE_DATA_PARAMETER).getResultObject();
		} catch (NullPointerException npe) {
			throw new IRODSThumbnailProcessUnavailableException(
					"no iRODS rule-based thumbnail generation available");
		}

		InputStream is = new java.io.ByteArrayInputStream(
				Base64.fromString(execOut));
		return is;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.datautils.image.ThumbnailService#createThumbnailLocally
	 * (java.io.File, java.lang.String, int, int)
	 */
	@Override
	public File createThumbnailLocally(final File workingDirectory,
			final String irodsAbsolutePathToGenerateThumbnailFor,
			int thumbWidth, int thumbHeight) throws Exception {

		log.info("createThumbnailLocally()");

		if (workingDirectory == null) {
			throw new IllegalArgumentException("null workingDirectory");
		}

		if (irodsAbsolutePathToGenerateThumbnailFor == null
				|| irodsAbsolutePathToGenerateThumbnailFor.isEmpty()) {
			throw new IllegalArgumentException(
					"nul irodsAbsolutePathToGenerateThumbnailFor");
		}

		log.info("workingDirectory:{}", workingDirectory.getAbsolutePath());
		log.info("irodsAbsolutePathToGenerateThumbnailFor:{}",
				irodsAbsolutePathToGenerateThumbnailFor);

		File temp = new File(workingDirectory,
				irodsAbsolutePathToGenerateThumbnailFor);

		StringBuilder targetTempBuilder = new StringBuilder(
				LocalFileUtils.getFileNameUpToExtension(temp.getName()));
		targetTempBuilder
				.append("###-jargon-data-utils-generated-thumbnail-image-###");
		targetTempBuilder.append(System.currentTimeMillis());
		targetTempBuilder.append(".jpg");

		File targetTempFile = createWorkingDirectoryImageFile(
				temp.getParentFile(), targetTempBuilder.toString());

		log.info(">>>targetTempFile:{}", targetTempFile);

		/*
		 * Bring the image file down to the local file system to be the
		 * thumbnail source
		 */

		log.info("bringing down image to generate thumbnail");
		IRODSFile sourceAsFile = this.getIrodsAccessObjectFactory()
				.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(irodsAbsolutePathToGenerateThumbnailFor);
		DataTransferOperations dto = this.irodsAccessObjectFactory
				.getDataTransferOperations(getIrodsAccount());
		dto.getOperation(sourceAsFile, targetTempFile, null, null);
		log.info("image retrieved to: {}, make thumbnail...",
				targetTempFile.getAbsolutePath());

		Image image = Toolkit.getDefaultToolkit().getImage(
				targetTempFile.getAbsolutePath());
		MediaTracker mediaTracker = new MediaTracker(new Container());
		mediaTracker.addImage(image, 0);
		mediaTracker.waitForID(0);
		double thumbRatio = (double) thumbWidth / (double) thumbHeight;
		int imageWidth = image.getWidth(null);
		int imageHeight = image.getHeight(null);
		double imageRatio = (double) imageWidth / (double) imageHeight;
		if (thumbRatio < imageRatio) {
			thumbHeight = (int) (thumbWidth / imageRatio);
		} else {
			thumbWidth = (int) (thumbHeight * imageRatio);
		}
		BufferedImage thumbImage = new BufferedImage(thumbWidth, thumbHeight,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics2D = thumbImage.createGraphics();
		graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		graphics2D.drawImage(image, 0, 0, thumbWidth, thumbHeight, null);
		log.info("creating a (buffered) output stream to temp file:{}",
				targetTempFile.getAbsolutePath());
		BufferedOutputStream out = new BufferedOutputStream(
				new FileOutputStream(targetTempFile.getAbsolutePath()));
		JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
		JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(thumbImage);
		int quality = 100;
		param.setQuality(quality / 100.0f, false);
		encoder.setJPEGEncodeParam(param);
		encoder.encode(thumbImage);
		out.close();

		return targetTempFile;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.datautils.image.ThumbnailService#
	 * createThumbnailLocallyViaJAI(java.io.File, java.lang.String, int)
	 */
	@Override
	public File createThumbnailLocallyViaJAI(final File workingDirectory,
			final String irodsAbsolutePathToGenerateThumbnailFor,
			final int maxEdge) throws Exception {

		log.info("createThumbnailLocallyViaJAI()");

		if (workingDirectory == null) {
			throw new IllegalArgumentException("null workingDirectory");
		}

		if (irodsAbsolutePathToGenerateThumbnailFor == null
				|| irodsAbsolutePathToGenerateThumbnailFor.isEmpty()) {
			throw new IllegalArgumentException(
					"nul irodsAbsolutePathToGenerateThumbnailFor");
		}

		if (maxEdge <= 0) {
			throw new IllegalArgumentException("maxEdge must be > 0");
		}

		// build a temporary path for the thumbnail file that differentiates the
		// irods file from the thumbnail
		File temp = new File(workingDirectory,
				irodsAbsolutePathToGenerateThumbnailFor);

		StringBuilder targetTempBuilder = new StringBuilder(
				LocalFileUtils.getFileNameUpToExtension(temp.getName()));
		targetTempBuilder
				.append("###-jargon-data-utils-generated-thumbnail-image-###");
		targetTempBuilder.append(System.currentTimeMillis());
		targetTempBuilder.append(".png");

		File targetTempFile = createWorkingDirectoryImageFile(
				temp.getParentFile(), targetTempBuilder.toString());

		/*
		 * Bring the image file down to the local file system to be the
		 * thumbnail source
		 */

		log.info("bringing down image to generate thumbnail");
		IRODSFile sourceAsFile = this.getIrodsAccessObjectFactory()
				.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(irodsAbsolutePathToGenerateThumbnailFor);
		DataTransferOperations dto = this.irodsAccessObjectFactory
				.getDataTransferOperations(getIrodsAccount());
		TransferControlBlock tcb = this.getIrodsAccessObjectFactory()
				.buildDefaultTransferControlBlockBasedOnJargonProperties();
		tcb.getTransferOptions().setForceOption(ForceOption.USE_FORCE);
		dto.getOperation(sourceAsFile, temp, null, tcb);
		log.info("image retrieved, create thumbnail image at:{}",
				targetTempFile.getAbsolutePath());

		try {

			ImageTool imageTool = new ImageTool();
			imageTool.load(temp.getAbsolutePath());
			imageTool.thumbnail(maxEdge);
			imageTool.writeResult(targetTempFile.getAbsolutePath(), "PNG");
			if (!targetTempFile.exists()) {
				throw new JargonException(
						"image file was not created for some reason by JAI");
			}
		} catch (Exception e) {
			log.error("exception occurred during thumbnail processing via JAI",
					e);
			throw new JargonException(
					"Error during thumbnail creation via JAI", e);
		}

		log.info("clean up base image, thumbnail image to be deleted by caller");
		temp.delete();
		return targetTempFile;
	}

}
