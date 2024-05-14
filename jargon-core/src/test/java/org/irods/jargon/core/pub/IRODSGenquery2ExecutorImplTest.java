package org.irods.jargon.core.pub;

import java.util.List;
import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class IRODSGenquery2ExecutorImplTest {

	private static final TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static final ObjectMapper objectMapper = new ObjectMapper();

	private static Properties testingProperties;
	private static IRODSFileSystem irodsFileSystem;
	private static IRODSAccount irodsAccount;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		irodsFileSystem = IRODSFileSystem.instance();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@Test
	public void testExecuteQueryWithoutZone() throws Exception {
		IRODSAccessObjectFactory aof = irodsFileSystem.getIRODSAccessObjectFactory();

		Assume.assumeTrue("GenQuery2 requires a minimum version of iRODS 4.3.2",
				aof.getIRODSServerProperties(irodsAccount).isAtLeastIrods432());

		IRODSGenquery2Executor gq2e = aof.getIRODSGenquery2Executor(irodsAccount);

		String query = String.format("select ZONE_NAME where ZONE_NAME = '%s'", irodsAccount.getZone());
		String json = gq2e.execute(query);
		List<List<String>> rows = objectMapper.readValue(json, new TypeReference<List<List<String>>>() {
		});
		Assert.assertFalse(rows.isEmpty());
		Assert.assertTrue(irodsAccount.getZone().equals(rows.get(0).get(0)));
	}

	@Test
	public void testExecuteQueryWithZone() throws Exception {
		IRODSAccessObjectFactory aof = irodsFileSystem.getIRODSAccessObjectFactory();

		Assume.assumeTrue("GenQuery2 requires a minimum version of iRODS 4.3.2",
				aof.getIRODSServerProperties(irodsAccount).isAtLeastIrods432());

		IRODSGenquery2Executor gq2e = aof.getIRODSGenquery2Executor(irodsAccount);

		String query = String.format("select ZONE_NAME where ZONE_NAME = '%s'", irodsAccount.getZone());
		String json = gq2e.execute(query, irodsAccount.getZone());
		List<List<String>> rows = objectMapper.readValue(json, new TypeReference<List<List<String>>>() {
		});
		Assert.assertFalse(rows.isEmpty());
		Assert.assertTrue(irodsAccount.getZone().equals(rows.get(0).get(0)));
	}

	@Test
	public void testGetGeneratedSQLWithoutZone() throws Exception {
		IRODSAccessObjectFactory aof = irodsFileSystem.getIRODSAccessObjectFactory();

		Assume.assumeTrue("GenQuery2 requires a minimum version of iRODS 4.3.2",
				aof.getIRODSServerProperties(irodsAccount).isAtLeastIrods432());

		IRODSGenquery2Executor gq2e = aof.getIRODSGenquery2Executor(irodsAccount);

		String sql = gq2e.getGeneratedSQL("select COLL_NAME, DATA_NAME");
		Assert.assertTrue(sql.contains(" R_COLL_MAIN "));
		Assert.assertTrue(sql.contains(" R_DATA_MAIN "));
		Assert.assertTrue(sql.contains(" t0."));
		Assert.assertTrue(sql.contains(" from "));
		Assert.assertTrue(sql.contains(" inner join "));
	}

	@Test
	public void testGetGeneratedSQLWithZone() throws Exception {
		IRODSAccessObjectFactory aof = irodsFileSystem.getIRODSAccessObjectFactory();

		Assume.assumeTrue("GenQuery2 requires a minimum version of iRODS 4.3.2",
				aof.getIRODSServerProperties(irodsAccount).isAtLeastIrods432());

		IRODSGenquery2Executor gq2e = aof.getIRODSGenquery2Executor(irodsAccount);

		String sql = gq2e.getGeneratedSQL("select COLL_NAME, DATA_NAME", irodsAccount.getZone());
		Assert.assertTrue(sql.contains(" R_COLL_MAIN "));
		Assert.assertTrue(sql.contains(" R_DATA_MAIN "));
		Assert.assertTrue(sql.contains(" t0."));
		Assert.assertTrue(sql.contains(" from "));
		Assert.assertTrue(sql.contains(" inner join "));
	}

	@Test
	public void testGetColumnMappingsWithoutZone() throws Exception {
		IRODSAccessObjectFactory aof = irodsFileSystem.getIRODSAccessObjectFactory();

		Assume.assumeTrue("GenQuery2 requires a minimum version of iRODS 4.3.2",
				aof.getIRODSServerProperties(irodsAccount).isAtLeastIrods432());

		IRODSGenquery2Executor gq2e = aof.getIRODSGenquery2Executor(irodsAccount);

		String mappings = gq2e.getColumnMappings();
		Assert.assertTrue(mappings.contains("\"DATA_ID\":{\"R_DATA_MAIN\":\"data_id\"}"));
		Assert.assertTrue(mappings.contains("\"COLL_ID\":{\"R_COLL_MAIN\":\"coll_id\"}"));
		Assert.assertTrue(mappings.contains("\"RESC_NAME\":{\"R_RESC_MAIN\":\"resc_name\"}"));
	}

	@Test
	public void testGetColumnMappingsWithZone() throws Exception {
		IRODSAccessObjectFactory aof = irodsFileSystem.getIRODSAccessObjectFactory();

		Assume.assumeTrue("GenQuery2 requires a minimum version of iRODS 4.3.2",
				aof.getIRODSServerProperties(irodsAccount).isAtLeastIrods432());

		IRODSGenquery2Executor gq2e = aof.getIRODSGenquery2Executor(irodsAccount);

		String mappings = gq2e.getColumnMappings(irodsAccount.getZone());
		Assert.assertTrue(mappings.contains("\"DATA_ID\":{\"R_DATA_MAIN\":\"data_id\"}"));
		Assert.assertTrue(mappings.contains("\"COLL_ID\":{\"R_COLL_MAIN\":\"coll_id\"}"));
		Assert.assertTrue(mappings.contains("\"RESC_NAME\":{\"R_RESC_MAIN\":\"resc_name\"}"));
	}

	@Test
	public void testInvalidInputs() throws Exception {
		IRODSAccessObjectFactory aof = irodsFileSystem.getIRODSAccessObjectFactory();

		Assume.assumeTrue("GenQuery2 requires a minimum version of iRODS 4.3.2",
				aof.getIRODSServerProperties(irodsAccount).isAtLeastIrods432());

		IRODSGenquery2Executor gq2e = aof.getIRODSGenquery2Executor(irodsAccount);

		Assert.assertThrows(IllegalArgumentException.class, () -> gq2e.execute(null));
		Assert.assertThrows(IllegalArgumentException.class, () -> gq2e.execute(""));
		Assert.assertThrows(IllegalArgumentException.class, () -> gq2e.execute(null, irodsAccount.getZone()));
		Assert.assertThrows(IllegalArgumentException.class, () -> gq2e.execute("", irodsAccount.getZone()));
		Assert.assertThrows(IllegalArgumentException.class, () -> gq2e.execute("select ZONE_NAME", null));
		Assert.assertThrows(IllegalArgumentException.class, () -> gq2e.execute("select ZONE_NAME", ""));

		Assert.assertThrows(IllegalArgumentException.class, () -> gq2e.getGeneratedSQL(null));
		Assert.assertThrows(IllegalArgumentException.class, () -> gq2e.getGeneratedSQL(""));
		Assert.assertThrows(IllegalArgumentException.class, () -> gq2e.getGeneratedSQL(null, irodsAccount.getZone()));
		Assert.assertThrows(IllegalArgumentException.class, () -> gq2e.getGeneratedSQL("", irodsAccount.getZone()));
		Assert.assertThrows(IllegalArgumentException.class, () -> gq2e.getGeneratedSQL("select ZONE_NAME", null));
		Assert.assertThrows(IllegalArgumentException.class, () -> gq2e.getGeneratedSQL("select ZONE_NAME", ""));
	}

}
