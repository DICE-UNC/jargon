package org.irods.jargon.part.policy.parameter;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.irods.jargon.part.exception.PartException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class DynamicPropertyValuesTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public final void testInstance() throws Exception {
		DynamicPropertyValues.instance("hello", new ArrayList<String>());
	}

	@Test(expected=PartException.class)
	public final void testInstanceBlankProperty() throws Exception {
		DynamicPropertyValues.instance("", new ArrayList<String>());
	}
	
	@Test(expected=PartException.class)
	public final void testInstanceNullProperty() throws Exception {
		DynamicPropertyValues.instance(null, new ArrayList<String>());
	}
	
	@Test(expected=PartException.class)
	public final void testInstanceNullList() throws Exception {
		DynamicPropertyValues.instance("hello", null);
	}
}
