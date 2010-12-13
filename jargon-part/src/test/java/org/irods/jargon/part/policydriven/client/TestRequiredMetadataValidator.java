package org.irods.jargon.part.policydriven.client;


import junit.framework.TestCase;

import org.irods.jargon.part.policy.domain.PolicyRequiredMetadataValue;
import org.irods.jargon.part.policy.domain.PolicyRequiredMetadataValue.MetadataType;
import org.irods.jargon.part.policydriven.client.exception.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestRequiredMetadataValidator {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}
	
	@Test(expected=ValidationException.class)
	public void testValidateStringRequiredAndMissing() throws Exception {
		PolicyRequiredMetadataValue policyRequiredMetadataValue = new PolicyRequiredMetadataValue();
		policyRequiredMetadataValue.setMetadataAttribute("description");
		policyRequiredMetadataValue
				.setMetaDataPromptAsText("Description document");
		policyRequiredMetadataValue.setRequired(true);
		policyRequiredMetadataValue
				.setMetadataType(MetadataType.LITERAL_STRING);
		
		RequiredMetadataValidator.validateAgainstPolicy("", policyRequiredMetadataValue);
		
	}
	
	@Test(expected=ValidationException.class)
	public void testValidateStringRequiredAndNull() throws Exception {
		PolicyRequiredMetadataValue policyRequiredMetadataValue = new PolicyRequiredMetadataValue();
		policyRequiredMetadataValue.setMetadataAttribute("description");
		policyRequiredMetadataValue
				.setMetaDataPromptAsText("Description document");
		policyRequiredMetadataValue.setRequired(true);
		policyRequiredMetadataValue
				.setMetadataType(MetadataType.LITERAL_STRING);
		
		RequiredMetadataValidator.validateAgainstPolicy(null, policyRequiredMetadataValue);
		
	}
	
	@Test
	public void testValidateStringRequiredAndValid() throws Exception {
		String val = "val";
		PolicyRequiredMetadataValue policyRequiredMetadataValue = new PolicyRequiredMetadataValue();
		policyRequiredMetadataValue.setMetadataAttribute("description");
		policyRequiredMetadataValue
				.setMetaDataPromptAsText("Description document");
		policyRequiredMetadataValue.setRequired(true);
		policyRequiredMetadataValue
				.setMetadataType(MetadataType.LITERAL_STRING);
		
		Object actualVal = RequiredMetadataValidator.validateAgainstPolicy(val, policyRequiredMetadataValue);
		TestCase.assertEquals(actualVal, val);
		
	}
	
	@Test(expected=ValidationException.class)
	public void testValidateIntRequiredAndMissing() throws Exception {
		PolicyRequiredMetadataValue policyRequiredMetadataValue = new PolicyRequiredMetadataValue();
		policyRequiredMetadataValue.setMetadataAttribute("description");
		policyRequiredMetadataValue
				.setMetaDataPromptAsText("Description document");
		policyRequiredMetadataValue.setRequired(true);
		policyRequiredMetadataValue
				.setMetadataType(MetadataType.LITERAL_NUMBER);
		
		RequiredMetadataValidator.validateAgainstPolicy("", policyRequiredMetadataValue);
		
	}
	
	@Test(expected=ValidationException.class)
	public void testValidateIntNotRequiredAndMissing() throws Exception {
		PolicyRequiredMetadataValue policyRequiredMetadataValue = new PolicyRequiredMetadataValue();
		policyRequiredMetadataValue.setMetadataAttribute("description");
		policyRequiredMetadataValue
				.setMetaDataPromptAsText("Description document");
		policyRequiredMetadataValue.setRequired(false);
		policyRequiredMetadataValue
				.setMetadataType(MetadataType.LITERAL_NUMBER);
		
		Object actualVal = RequiredMetadataValidator.validateAgainstPolicy("", policyRequiredMetadataValue);
		TestCase.assertNull("null expected for empty, non required number", actualVal);
		
	}
	
	@Test(expected=ValidationException.class)
	public void testValidateIntRequiredAndNull() throws Exception {
		PolicyRequiredMetadataValue policyRequiredMetadataValue = new PolicyRequiredMetadataValue();
		policyRequiredMetadataValue.setMetadataAttribute("description");
		policyRequiredMetadataValue
				.setMetaDataPromptAsText("Description document");
		policyRequiredMetadataValue.setRequired(true);
		policyRequiredMetadataValue
				.setMetadataType(MetadataType.LITERAL_NUMBER);
		
		RequiredMetadataValidator.validateAgainstPolicy(null, policyRequiredMetadataValue);
		
	}
	
	@Test
	public void testValidateIntRequiredAndValid() throws Exception {
		String val = "120";
		PolicyRequiredMetadataValue policyRequiredMetadataValue = new PolicyRequiredMetadataValue();
		policyRequiredMetadataValue.setMetadataAttribute("description");
		policyRequiredMetadataValue
				.setMetaDataPromptAsText("Description document");
		policyRequiredMetadataValue.setRequired(true);
		policyRequiredMetadataValue
				.setMetadataType(MetadataType.LITERAL_NUMBER);
		
		Object actualVal = RequiredMetadataValidator.validateAgainstPolicy(val, policyRequiredMetadataValue);
		TestCase.assertEquals(actualVal, new Integer(val));
		
	}

	@Test(expected=ValidationException.class)
	public void testValidateFloatRequiredAndMissing() throws Exception {
		PolicyRequiredMetadataValue policyRequiredMetadataValue = new PolicyRequiredMetadataValue();
		policyRequiredMetadataValue.setMetadataAttribute("description");
		policyRequiredMetadataValue
				.setMetaDataPromptAsText("Description document");
		policyRequiredMetadataValue.setRequired(true);
		policyRequiredMetadataValue
				.setMetadataType(MetadataType.LITERAL_DECIMAL);
		
		RequiredMetadataValidator.validateAgainstPolicy("", policyRequiredMetadataValue);
		
	}
	
	@Test(expected=ValidationException.class)
	public void testValidateFloatNotRequiredAndMissing() throws Exception {
		PolicyRequiredMetadataValue policyRequiredMetadataValue = new PolicyRequiredMetadataValue();
		policyRequiredMetadataValue.setMetadataAttribute("description");
		policyRequiredMetadataValue
				.setMetaDataPromptAsText("Description document");
		policyRequiredMetadataValue.setRequired(false);
		policyRequiredMetadataValue
				.setMetadataType(MetadataType.LITERAL_DECIMAL);
		
		Object actualVal = RequiredMetadataValidator.validateAgainstPolicy("", policyRequiredMetadataValue);
		TestCase.assertNull("null expected for empty, non required number", actualVal);
		
	}
	
	@Test(expected=ValidationException.class)
	public void testValidateFloatRequiredAndNull() throws Exception {
		PolicyRequiredMetadataValue policyRequiredMetadataValue = new PolicyRequiredMetadataValue();
		policyRequiredMetadataValue.setMetadataAttribute("description");
		policyRequiredMetadataValue
				.setMetaDataPromptAsText("Description document");
		policyRequiredMetadataValue.setRequired(true);
		policyRequiredMetadataValue
				.setMetadataType(MetadataType.LITERAL_DECIMAL);
		
		RequiredMetadataValidator.validateAgainstPolicy(null, policyRequiredMetadataValue);
		
	}
	
	@Test
	public void testValidateFloatRequiredAndValid() throws Exception {
		String val = "120.22";
		PolicyRequiredMetadataValue policyRequiredMetadataValue = new PolicyRequiredMetadataValue();
		policyRequiredMetadataValue.setMetadataAttribute("description");
		policyRequiredMetadataValue
				.setMetaDataPromptAsText("Description document");
		policyRequiredMetadataValue.setRequired(true);
		policyRequiredMetadataValue
				.setMetadataType(MetadataType.LITERAL_DECIMAL);
		
		Object actualVal = RequiredMetadataValidator.validateAgainstPolicy(val, policyRequiredMetadataValue);
		TestCase.assertEquals(actualVal, new Float(val));
		
	}
	
// boool
	
	@Test(expected=ValidationException.class)
	public void testValidateBooleanRequiredAndMissing() throws Exception {
		PolicyRequiredMetadataValue policyRequiredMetadataValue = new PolicyRequiredMetadataValue();
		policyRequiredMetadataValue.setMetadataAttribute("description");
		policyRequiredMetadataValue
				.setMetaDataPromptAsText("Description document");
		policyRequiredMetadataValue.setRequired(true);
		policyRequiredMetadataValue
				.setMetadataType(MetadataType.LITERAL_BOOLEAN);
		
		RequiredMetadataValidator.validateAgainstPolicy("", policyRequiredMetadataValue);
		
	}
	
	@Test
	public void testValidateBooleanNotRequiredAndMissing() throws Exception {
		PolicyRequiredMetadataValue policyRequiredMetadataValue = new PolicyRequiredMetadataValue();
		policyRequiredMetadataValue.setMetadataAttribute("description");
		policyRequiredMetadataValue
				.setMetaDataPromptAsText("Description document");
		policyRequiredMetadataValue.setRequired(false);
		policyRequiredMetadataValue
				.setMetadataType(MetadataType.LITERAL_BOOLEAN);
		
		Object actualVal = RequiredMetadataValidator.validateAgainstPolicy("", policyRequiredMetadataValue);
		TestCase.assertFalse("false expected for empty, non required boolean", (Boolean) actualVal);
		
	}
	
	@Test(expected=ValidationException.class)
	public void testValidateBooleanRequiredAndNull() throws Exception {
		PolicyRequiredMetadataValue policyRequiredMetadataValue = new PolicyRequiredMetadataValue();
		policyRequiredMetadataValue.setMetadataAttribute("description");
		policyRequiredMetadataValue
				.setMetaDataPromptAsText("Description document");
		policyRequiredMetadataValue.setRequired(true);
		policyRequiredMetadataValue
				.setMetadataType(MetadataType.LITERAL_BOOLEAN);
		
		RequiredMetadataValidator.validateAgainstPolicy(null, policyRequiredMetadataValue);
		
	}
	
	@Test
	public void testValidateBooleanRequiredAndTrue() throws Exception {
		String val = "true";
		PolicyRequiredMetadataValue policyRequiredMetadataValue = new PolicyRequiredMetadataValue();
		policyRequiredMetadataValue.setMetadataAttribute("description");
		policyRequiredMetadataValue
				.setMetaDataPromptAsText("Description document");
		policyRequiredMetadataValue.setRequired(true);
		policyRequiredMetadataValue
				.setMetadataType(MetadataType.LITERAL_BOOLEAN);
		
		Object actualVal = RequiredMetadataValidator.validateAgainstPolicy(val, policyRequiredMetadataValue);
		TestCase.assertEquals(actualVal, new Boolean(val));
		
	}
	
	
}
