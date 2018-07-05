package org.irods.jargon.datautils.avuautocomplete;

import org.irods.jargon.core.exception.JargonException;

/**
 * Interface for a service to assist in auto-complete and enumeration of Avu
 * attributes and values. The service can provide a list of unique AVU
 * attributes based on a pattern, as well as a list of unique AVU values given
 * an attribute.
 *
 * @author Mike Conway - NIEHS
 *
 */
public interface AvuAutocompleteService {

	public enum AvuTypeEnum {
		COLLECTION, DATA_OBJECT, BOTH
	}

	/**
	 * Given an optional (blank if not used) prefix and an optional (0 if not used)
	 * offset, get a list of unique AVU attributes visible to the user. These
	 * represent metadata already in the system.
	 *
	 * @param prefix
	 *            {@link String} which can be blank. If provided, it will limit the
	 *            attributes that begin with the provided prefix, otherwise, all
	 *            attributes will be returned
	 *
	 * @param offset
	 *            {@link int} with the offset, or 0 if no offset is needed
	 * @param avuTypeEnum
	 *            {@link AvuTypeEnum} dictating the scope of the search
	 * @return {@link AvuSearchResult} with the elements and the paging data
	 * @throws JargonException
	 */
	AvuSearchResult gatherAvailableAttributes(String prefix, int offset, AvuTypeEnum avuTypeEnum)
			throws JargonException;

	/**
	 * Given a required attributed name and an optional prefix (blank if ignored)
	 * for the value and an optional (0 if not used) offset, return a list of unique
	 * avu values for the attribute
	 *
	 * @param forAttribute
	 *            {@link String} with the required attribute name that scopes the
	 *            values
	 * @param prefix
	 *            {@link String} which can be blank. If provided, it will limit the
	 *            values that begin with the provided prefix, otherwise, all values
	 *            will be returned
	 * @param offset
	 *            {@link int} with the offset, or 0 if no offset is needed
	 * @param avuTypeEnum
	 *            {@link AvuTypeEnum} dictating the scope of the search
	 * @return {@link AvuSearchResult} with the elements and the paging data
	 * @throws JargonException
	 */
	AvuSearchResult gatherAvailableValues(String forAttribute, String prefix, int offset, AvuTypeEnum avuTypeEnum)
			throws JargonException;

}