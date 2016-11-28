package org.irods.jargon.core.packinstr;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.domain.Resource;

/**
 * Packing instructions for manipulating iRODS resources
 *
 * @author Mike Conway - DICE
 *
 */
public class GeneralAdminInpForResources extends GeneralAdminInp {

	/**
	 * Generate the packing instruction suitable for creating a
	 * <code>Resource</code>
	 *
	 * @param Resource
	 *            {@link Resource} to be added to iRODS.
	 * @return {@link GeneralAdminInp}
	 * @throws JargonException
	 */
	public static final GeneralAdminInpForResources instanceForAddResource(
			final Resource resource) throws JargonException {

		if (resource == null) {
			throw new IllegalArgumentException("null resource");
		}

		if (resource.getName() == null || resource.getName().isEmpty()) {
			throw new IllegalArgumentException("resource name is null or empty");
		}

		if (resource.getContextString() == null) {
			throw new IllegalArgumentException("Null or empty context string");
		}

		if (resource.getLocation() == null) {
			throw new IllegalArgumentException("context is null");
		}

		if (resource.getVaultPath() == null) {
			throw new IllegalArgumentException("vaultPath is null");
		}

		if (resource.getType() == null || resource.getType().isEmpty()) {
			throw new IllegalArgumentException("null type");
		}

		String hostPath;
		if (resource.getLocation().isEmpty()
				|| resource.getVaultPath().isEmpty()) {

			if (!resource.getLocation().isEmpty()) {
				throw new IllegalArgumentException(
						"location is not empty but vault path is unspecified");
			}

			if (!resource.getVaultPath().isEmpty()) {
				throw new IllegalArgumentException(
						"vault path is not empty, but resource is unspecified");
			}

			hostPath = "";

		} else {
			StringBuilder sb = new StringBuilder();
			sb.append(resource.getLocation().trim());
			sb.append(':');
			sb.append(resource.getVaultPath().trim());
			hostPath = sb.toString();
		}

		return new GeneralAdminInpForResources("add", "resource",
				resource.getName(), resource.getType(), hostPath,
				resource.getContextString(), BLANK, BLANK, BLANK, BLANK,
				GEN_ADMIN_INP_API_NBR);

	}

	/**
	 * Packing instruction to add a child to a resource
	 *
	 * @param childResourceName
	 *            <code>String</code> with the resource name for the child
	 * @param parentResourceName
	 *            <code>String</code> with the resource name for the parent
	 * @param context
	 *            <code>String</code> with an optional context, blank of not
	 *            needed
	 * @return
	 * @throws JargonException
	 */
	public static final GeneralAdminInpForResources instanceForAddChildToResource(
			final String childResourceName, final String parentResourceName,
			final String context) throws JargonException {

		if (childResourceName == null || childResourceName.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty childResourceName");
		}

		if (parentResourceName == null || parentResourceName.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty parentResourceName");
		}

		if (context == null) {
			throw new IllegalArgumentException("null  context");
		}

		return new GeneralAdminInpForResources("add", "childtoresc",
				parentResourceName, childResourceName, context, BLANK, BLANK,
				BLANK, BLANK, BLANK, GEN_ADMIN_INP_API_NBR);

	}

	public static final GeneralAdminInpForResources instanceForRemoveChildFromResource(
			final String childResourceName, final String parentResourceName)
					throws JargonException {

		if (childResourceName == null || childResourceName.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty childResourceName");
		}

		if (parentResourceName == null || parentResourceName.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty parentResourceName");
		}

		return new GeneralAdminInpForResources("rm", "childfromresc",
				parentResourceName, childResourceName, BLANK, BLANK, BLANK,
				BLANK, BLANK, BLANK, GEN_ADMIN_INP_API_NBR);

	}

	/**
	 * Generate the packing instruction suitable for removing a
	 * <code>Resource</code>
	 *
	 * @param resourceName
	 *            <code>String</code> with the name of the resource
	 *
	 * @return {@link GeneralAdminInp}
	 * @throws JargonException
	 */
	public static final GeneralAdminInpForResources instanceForRemoveResource(
			final String resourceName) throws JargonException {

		if (resourceName == null || resourceName.isEmpty()) {
			throw new IllegalArgumentException("null or empty resourceName");
		}

		return new GeneralAdminInpForResources("rm", "resource", resourceName,
				BLANK, BLANK, BLANK, BLANK, BLANK, BLANK, BLANK,
				GEN_ADMIN_INP_API_NBR);

	}

	private GeneralAdminInpForResources(final String arg0, final String arg1,
			final String arg2, final String arg3, final String arg4,
			final String arg5, final String arg6, final String arg7,
			final String arg8, final String arg9, final int apiNumber)
					throws JargonException {
		super(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9,
				apiNumber);
	}

}
