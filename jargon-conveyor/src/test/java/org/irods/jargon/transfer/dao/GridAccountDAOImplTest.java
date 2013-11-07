package org.irods.jargon.transfer.dao;

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.transfer.dao.domain.GridAccount;
import org.irods.jargon.transfer.util.DomainUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:transfer-dao-beans.xml",
		"classpath:transfer-dao-hibernate-spring.cfg.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class GridAccountDAOImplTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();

	@Autowired
	private GridAccountDAO gridAccountDAO;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testSave() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		GridAccount gridAccount = DomainUtils
				.gridAccountFromIRODSAccount(irodsAccount);

		assertTrue(gridAccount.getId() == null);
		gridAccountDAO.save(gridAccount);
		assertTrue(gridAccount.getId() != null);

	}

	@Test
	public void testFindAll() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		GridAccount gridAccount = DomainUtils
				.gridAccountFromIRODSAccount(irodsAccount);

		gridAccountDAO.save(gridAccount);
		List<GridAccount> actual = gridAccountDAO.findAll();
		TestCase.assertFalse("no results returned", actual.isEmpty());

	}

	@Test
	public void testFindByIdExists() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		GridAccount gridAccount = DomainUtils
				.gridAccountFromIRODSAccount(irodsAccount);

		gridAccountDAO.save(gridAccount);
		GridAccount actual = gridAccountDAO.findById(gridAccount.getId());
		TestCase.assertNotNull("not found", actual);

	}

	@Test
	public void testFindByIdNotExists() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		GridAccount gridAccount = DomainUtils
				.gridAccountFromIRODSAccount(irodsAccount);
		gridAccount.setId(new Long(99999999));

		GridAccount actual = gridAccountDAO.findById(gridAccount.getId());
		TestCase.assertNull("should have returned null", actual);

	}

	@Test
	public void testFindByHostZoneUserNameExists() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(
						testingProperties, "testFindByHostZoneUserNameExists",
						"testFindByHostZoneUserNameExists");
		GridAccount gridAccount = DomainUtils
				.gridAccountFromIRODSAccount(irodsAccount);
		gridAccountDAO.save(gridAccount);
		GridAccount actual = gridAccountDAO.findByHostZoneAndUserName(
				irodsAccount.getHost(), irodsAccount.getZone(),
				irodsAccount.getUserName());
		TestCase.assertNotNull("not found", actual);

	}

	@Test
	public void testFindByHostZoneUserNameNotExists() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		irodsAccount.setUserName("testFindByHostZoneUserNameNotExists");

		GridAccount actual = gridAccountDAO.findByHostZoneAndUserName(
				irodsAccount.getHost(), irodsAccount.getZone(),
				irodsAccount.getUserName());
		TestCase.assertNull("no results should be returned", actual);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testFindByHostZoneUserNameHostNull() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		gridAccountDAO.findByHostZoneAndUserName(null, irodsAccount.getZone(),
				irodsAccount.getUserName());

	}

	@Test(expected = IllegalArgumentException.class)
	public void testFindByHostZoneUserNameHostBlank() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		gridAccountDAO.findByHostZoneAndUserName("", irodsAccount.getZone(),
				irodsAccount.getUserName());

	}

	@Test(expected = IllegalArgumentException.class)
	public void testFindByHostZoneUserNameZoneNull() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		gridAccountDAO.findByHostZoneAndUserName(irodsAccount.getUserName(),
				null, irodsAccount.getUserName());

	}

	@Test(expected = IllegalArgumentException.class)
	public void testFindByHostZoneUserNameZoneBlank() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		gridAccountDAO.findByHostZoneAndUserName(irodsAccount.getUserName(),
				"", irodsAccount.getUserName());

	}

	@Test(expected = IllegalArgumentException.class)
	public void testFindByHostZoneUserNameUserNull() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		gridAccountDAO.findByHostZoneAndUserName(irodsAccount.getUserName(),
				irodsAccount.getZone(), null);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testFindByHostZoneUserNameUserBlank() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		gridAccountDAO.findByHostZoneAndUserName(irodsAccount.getUserName(),
				irodsAccount.getZone(), "");

	}

	@Test
	public void testDeleteExists() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		GridAccount gridAccount = DomainUtils
				.gridAccountFromIRODSAccount(irodsAccount);

		gridAccountDAO.save(gridAccount);
		GridAccount actual = gridAccountDAO.findById(gridAccount.getId());
		TestCase.assertNotNull("not found", actual);
		gridAccountDAO.delete(gridAccount);
		actual = gridAccountDAO.findById(gridAccount.getId());
		TestCase.assertNull("found, should have deleted", actual);

	}

	@Test
	public void testDeleteNotExists() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		GridAccount gridAccount = DomainUtils
				.gridAccountFromIRODSAccount(irodsAccount);
		gridAccount.setId(new Long(999999999));
		gridAccountDAO.delete(gridAccount);
		// should just complete without error
	}

}
