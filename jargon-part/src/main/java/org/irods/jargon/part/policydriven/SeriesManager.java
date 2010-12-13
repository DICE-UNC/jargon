package org.irods.jargon.part.policydriven;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.AVUQueryElement;
import org.irods.jargon.core.query.AVUQueryOperatorEnum;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.core.query.AVUQueryElement.AVUQueryPart;
import org.irods.jargon.part.exception.DataNotFoundException;
import org.irods.jargon.part.policy.domain.Policy;
import org.irods.jargon.part.policy.domain.Series;
import org.irods.jargon.part.policy.xmlserialize.XMLToObjectUnmarshaller;

public interface SeriesManager {
	
	public static final String SERIES_ATTRIBUTE_MARKER_ATTRIBUTE = "PolicyDrivenService:SeriesAttributeMarkerAttribute";

	/**
	 * Given the name of a service-driven application, return a summary listing of each series for the application.
	 * @param serviceDrivenApplicationName <code>String</code> with the name of a service-driven application
	 * @return <code>List</code> of {@link org.irods.jargon.part.policydriven.PolicyDrivenServiceListing} with a summary of each series.
	 * @throws PartException
	 */
	public abstract List<PolicyDrivenServiceListingEntry> listSeries(
			final String serviceDrivenApplicationName)
			throws PolicyDrivenServiceConfigException;

	/**
	 * Verify that a series is unique in name, and that a series does not exist for the given collection.
	 * @param name <code>String</code> with the name of the series.
	 * @param collectionAbsolutePath <code>String</code> with the absolute path to a collection in iRODS that represents the series
	 * @return <code>boolean</code> that will be <code>true</code> if the series is unique;
	 * @throws PartException
	 */
	public boolean checkIfSeriesIsUnique(String name, String collectionAbsolutePath)
			throws PolicyDrivenServiceConfigException;

	/**
	 * Add the given series to the application based on the given data in the
	 * <code>Series</code> object. The series must be: uniquely named, part of
	 * an existing policy driven service, bound to an existing policy, for a
	 * collection (that will be created if non-existent) that is not already
	 * associated with a series.
	 * 
	 * This method will create a directory as the root of the series, and set up
	 * the appropriate AVU's to link the series to the policy and application.
	 * 
	 * @param series
	 *            {@link org.irods.jargon.part.policydriven.domain.Series}
	 *            containing a specification for the series to be added.
	 * @param unmarshaller
	 *            <code>XmlToObjectUnmarshaller</code> that can convert XML to
	 *            domain objects.
	 * @throws PartException
	 */
	public void addSeriesToApplication(final Series series,
			final XMLToObjectUnmarshaller unmarshaller)
			throws PolicyDrivenServiceConfigException;

}