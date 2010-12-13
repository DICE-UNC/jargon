/**
 * 
 */
package org.irods.jargon.part.policy.xmlserialize;

import org.exolab.castor.xml.Unmarshaller;
import org.irods.jargon.core.pub.io.IRODSFileReader;
import org.irods.jargon.part.policy.domain.Policy;

/**
 * Unmarshall XML to objects using Castor.  Note that this class is not final to assist in testing.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class XMLToObjectUnmarshaller {

	/**
	 * Read in the iRODS file containing an XML policy descriptor and return the Policy object
	 * @param irodsFileReader <code>org.irods.jargon.core.pub.io.FileReader</code> that wraps the XML file
	 * @return {@link org.irods.jargon.part.policy.domain.Policy} with an object representing the XML policy data
	 * @throws XMLMarshallException
	 */
	public Policy unmarshallXMLToPolicy(final IRODSFileReader irodsFileReader) throws XMLMarshallException {
		Policy policy;
		try {
			policy = (Policy) Unmarshaller.unmarshal(Policy.class, irodsFileReader);
			irodsFileReader.close();
		} catch (Exception e) {
			throw new XMLMarshallException("error unmarshalling to XML", e);
		}
		return policy;
	}

}
