/**
 * 
 */
package org.irods.jargon.part.policy.xmlserialize;

import org.exolab.castor.xml.Marshaller;
import org.irods.jargon.core.pub.io.IRODSFileWriter;
import org.irods.jargon.part.policy.domain.Policy;

/**
 * Marshall objects to XML using Castor
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class ObjectToXMLMarshaller {

	public void marshallPolicyToXML(IRODSFileWriter irodsFileWriter,
			Policy policyToMarshall) throws XMLMarshallException {
		try {
			Marshaller.marshal(policyToMarshall, irodsFileWriter);
			irodsFileWriter.close();
		} catch (Exception e) {
			throw new XMLMarshallException("error marshalling to XML", e);
		}
	}

}
