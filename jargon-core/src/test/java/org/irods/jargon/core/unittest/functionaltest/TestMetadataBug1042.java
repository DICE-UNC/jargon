package org.irods.jargon.core.unittest.functionaltest;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.domain.DataObject;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.query.AVUQueryElement;
import org.irods.jargon.core.query.AVUQueryOperatorEnum;
import org.irods.jargon.core.query.AVUQueryElement.AVUQueryPart;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.junit.Test;

/**
 * Test for 
 * @author mikeconway
 *
 */
public class TestMetadataBug1042 {
	
	/*
	 * 1) set the host port etc to point to a file in iRODS
	 */
	
	private String host = "";
	private int port = 1247;
	private String zone = "";
	private String userName = "";
	private String password = "";
	
	private String fileName = "";
	private IRODSFileSystem irodsFileSystem ;

	@Test
	public void test() throws Exception  {
		irodsFileSystem = IRODSFileSystem.instance();
		String attribute = "testAttribute";
		String value = ""; // put some chinese characters here
		
		IRODSAccount irodsAccount = IRODSAccount.instance(host, port, userName, password, "", zone, "");

		IRODSFile targetIrodsFile = irodsFileSystem.getIRODSFileFactory(
				irodsAccount).instanceIRODSFile(fileName);
		
		DataObjectAO dataObjectAO = irodsFileSystem
				.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		
		/*
		 * 2) if you want to try adding AVU metadata to this file, use below
		 */
		
		/*
		 *  uncomment this if you want to add the avu data here or test this function
		 * 
		AvuData avuData = AvuData.instance(attribute,
				value, "");
		
		dataObjectAO.addAVUMetadata(fileName, avuData);
		*/

		
		/*
		 * This uses the raw xml protocol to query the data, let's see what we get
		 */
		
		List<AVUQueryElement> avuQueryElements = new ArrayList<AVUQueryElement>();
		avuQueryElements.add(AVUQueryElement.instanceForValueQuery(
				AVUQueryPart.ATTRIBUTE, AVUQueryOperatorEnum.EQUAL,
				attribute));

	
		List<MetaDataAndDomainData> metadata = dataObjectAO.findMetadataValuesByMetadataQuery(avuQueryElements);
		
		for(MetaDataAndDomainData actual : metadata) {
			System.out.println("attrib:" + actual.getAvuAttribute() + "\nvalue:" + actual.getAvuValue());
		}
		
		irodsFileSystem.close();
		
		
	}

}
