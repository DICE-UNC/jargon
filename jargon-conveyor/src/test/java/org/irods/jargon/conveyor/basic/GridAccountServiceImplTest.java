/**
 * 
 */
package org.irods.jargon.conveyor.basic;

import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.conveyor.core.ConveyorExecutionException;
import org.irods.jargon.conveyor.core.ConveyorExecutorService;
import org.irods.jargon.conveyor.core.GridAccountService;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.transfer.dao.GridAccountDAO;
import org.irods.jargon.transfer.dao.domain.GridAccount;
import org.irods.jargon.transfer.exception.PassPhraseInvalidException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:transfer-dao-beans.xml",
		"classpath:transfer-dao-hibernate-spring.cfg.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class GridAccountServiceImplTest {

	private static Properties testingProperties = new Properties();
	private static org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void before() throws Exception {
		gridAccountService.resetPassPhraseAndAccounts();
	}

	@Autowired
	private GridAccountService gridAccountService;

	@Test
	public final void testAddOrUpdateGridAccountBasedOnIRODSAccountWillBeAdd()
			throws Exception {
		String testUserName = "user1";
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(
						testingProperties, testUserName, testUserName);
		String passPhrase = irodsAccount.getUserName();
		gridAccountService.validatePassPhrase(passPhrase);
		GridAccount gridAccount = gridAccountService
				.addOrUpdateGridAccountBasedOnIRODSAccount(irodsAccount);
		Assert.assertEquals("wrong host", irodsAccount.getHost(),
				gridAccount.getHost());
		Assert.assertEquals("wrong port", irodsAccount.getPort(),
				gridAccount.getPort());
		Assert.assertEquals("wrong zone", irodsAccount.getZone(),
				gridAccount.getZone());
		Assert.assertEquals("wrong user name", irodsAccount.getUserName(),
				gridAccount.getUserName());
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testAddOrUpdateGridAccountBasedOnIRODSAccountNullAccount()
			throws Exception {
		IRODSAccount irodsAccount = null;
		String passPhrase = "ooogabooga";
		gridAccountService.validatePassPhrase(passPhrase);
		gridAccountService
				.addOrUpdateGridAccountBasedOnIRODSAccount(irodsAccount);
	}

	@Test(expected = ConveyorExecutionException.class)
	public final void testAddOrUpdateGridAccountBasedOnIRODSAccountNotValidated()
			throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(
						testingProperties, "test", "test");
		GridAccountServiceImpl gridAccountServiceTest = new GridAccountServiceImpl();
		GridAccountDAO gridAccountDAO = Mockito.mock(GridAccountDAO.class);
		gridAccountServiceTest.setGridAccountDAO(gridAccountDAO);
		ConveyorExecutorService conveyorExecutorService = Mockito
				.mock(ConveyorExecutorService.class);
		gridAccountServiceTest
				.setConveyorExecutorService(conveyorExecutorService);
		gridAccountServiceTest
				.addOrUpdateGridAccountBasedOnIRODSAccount(irodsAccount);
	}

	@Test
	public final void testAddOrUpdateGridAccountBasedOnIRODSAccountWillBeUpdate()
			throws Exception {
		String testUserName = "user1";
		String newResc = "newResc";
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(
						testingProperties, testUserName, testUserName);
		String passPhrase = "ooogabooga";
		gridAccountService.validatePassPhrase(passPhrase);
		GridAccount gridAccount = gridAccountService
				.addOrUpdateGridAccountBasedOnIRODSAccount(irodsAccount);

		irodsAccount = IRODSAccount.instance(irodsAccount.getHost(),
				irodsAccount.getPort(), irodsAccount.getUserName(),
				testUserName, "", irodsAccount.getZone(), newResc);
		gridAccount = gridAccountService
				.addOrUpdateGridAccountBasedOnIRODSAccount(irodsAccount);

		Assert.assertEquals("did not update default resource", newResc,
				gridAccount.getDefaultResource());
	}

	@Test
	public final void testRememberResource() throws Exception {
		String testUserName = "user1";
		String newResc = "newResc";
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(
						testingProperties, testUserName, testUserName);
		String passPhrase = "ooogabooga";
		gridAccountService.validatePassPhrase(passPhrase);
		gridAccountService
				.addOrUpdateGridAccountBasedOnIRODSAccount(irodsAccount);

		gridAccountService
				.rememberDefaultStorageResource(newResc, irodsAccount);

		GridAccount actual = gridAccountService
				.findGridAccountByIRODSAccount(irodsAccount);

		Assert.assertEquals("did not update default resource", newResc,
				actual.getDefaultResource());
	}

	@Test
	public final void testValidatePassPhraseWhenNoneThenRevalidateShouldBeGood()
			throws Exception {
		String passPhrase = "ooogabooga";
		gridAccountService.validatePassPhrase(passPhrase);
		// revalidate
		gridAccountService.validatePassPhrase(passPhrase);
	}

	@Test(expected = PassPhraseInvalidException.class)
	public final void testValidatePassPhraseWhenInvalid() throws Exception {
		String passPhrase = "ooogabooga";
		gridAccountService.validatePassPhrase(passPhrase);
		gridAccountService.validatePassPhrase(passPhrase + "oogaooga");
	}

	@Test
	public final void testDeleteGridAccount() throws Exception {
		String testUserName = "user1";
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(
						testingProperties, testUserName, testUserName);
		String passPhrase = "ooogabooga";
		gridAccountService.validatePassPhrase(passPhrase);
		GridAccount gridAccount = gridAccountService
				.addOrUpdateGridAccountBasedOnIRODSAccount(irodsAccount);
		gridAccountService.deleteGridAccount(gridAccount);
		GridAccount lookedUp = gridAccountService
				.findGridAccountByIRODSAccount(irodsAccount);
		Assert.assertNull("should have deleted", lookedUp);

	}

	@Test
	public final void testDeleteGridAccountNotExists() throws Exception {
		String testUserName = "user1";
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(
						testingProperties, testUserName, testUserName);
		String passPhrase = "ooogabooga";
		gridAccountService.validatePassPhrase(passPhrase);
		GridAccount gridAccount = new GridAccount(irodsAccount);
		gridAccountService.deleteGridAccount(gridAccount);

	}

	@Test(expected = IllegalArgumentException.class)
	public final void testStorePassPhraseNull() throws Exception {
		gridAccountService.changePassPhraseWhenAlreadyValidated(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testStorePassPhraseBlank() throws Exception {
		gridAccountService.changePassPhraseWhenAlreadyValidated("");
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testDeleteGridAccountNullAccount() throws Exception {
		String passPhrase = "ooogabooga";
		gridAccountService.validatePassPhrase(passPhrase);
		GridAccount gridAccount = null;
		gridAccountService.deleteGridAccount(gridAccount);

	}

	@Test(expected = IllegalArgumentException.class)
	public final void testFindGridAccountByIRODSAccountNull() throws Exception {
		IRODSAccount irodsAccount = null;
		String passPhrase = "ooogabooga";
		gridAccountService.validatePassPhrase(passPhrase);
		gridAccountService.findGridAccountByIRODSAccount(irodsAccount);

	}

	@Test
	public final void testFindAll() throws Exception {
		String testUserName = "user1";
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(
						testingProperties, testUserName, testUserName);
		String testUserName2 = "user2";
		IRODSAccount irodsAccount2 = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(
						testingProperties, testUserName2, testUserName2);
		String passPhrase = "ooogabooga";
		gridAccountService.validatePassPhrase(passPhrase);
		gridAccountService
				.addOrUpdateGridAccountBasedOnIRODSAccount(irodsAccount);
		gridAccountService
				.addOrUpdateGridAccountBasedOnIRODSAccount(irodsAccount2);

		List<GridAccount> actual = gridAccountService.findAll();
		Assert.assertEquals("did not get two accounts", 2, actual.size());

	}

	@Test
	public final void testStorePassPhraseWithAlreadyCachedGridAccounts()
			throws Exception {
		String testUserName = "user1";
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(
						testingProperties, testUserName, testUserName);
		String passPhrase = "ooogabooga";
		gridAccountService.validatePassPhrase(passPhrase);
		gridAccountService.deleteAllGridAccounts();

		gridAccountService
				.addOrUpdateGridAccountBasedOnIRODSAccount(irodsAccount);

		/*
		 * account added and encrypted, now store a new pass phrase and make
		 * sure I still can properly decrypt it
		 */
		passPhrase = "boogaoooga";
		gridAccountService.changePassPhraseWhenAlreadyValidated(passPhrase);

		List<GridAccount> gridAccounts = gridAccountService.findAll();
		Assert.assertEquals("should be one account", 1, gridAccounts.size());
		IRODSAccount actual = gridAccountService
				.irodsAccountForGridAccount(gridAccounts.get(0));
		Assert.assertEquals(
				"did not properly decrypt password using new pass phrase",
				actual.getPassword(), irodsAccount.getPassword());

	}

	@Test(expected = PassPhraseInvalidException.class)
	public final void testChangePassPhraseWhenNotAlreadyValidated()
			throws Exception {
		String passPhrase = "ooogabooga";
		GridAccountServiceImpl gridAccountServiceTest = new GridAccountServiceImpl();
		GridAccountDAO gridAccountDAO = Mockito.mock(GridAccountDAO.class);
		gridAccountServiceTest.setGridAccountDAO(gridAccountDAO);
		ConveyorExecutorService conveyorExecutorService = Mockito
				.mock(ConveyorExecutorService.class);
		gridAccountServiceTest
				.setConveyorExecutorService(conveyorExecutorService);
		gridAccountServiceTest.deleteAllGridAccounts();

		gridAccountServiceTest.changePassPhraseWhenAlreadyValidated(passPhrase);

	}

	@Test(expected = IllegalArgumentException.class)
	public final void testValidateNullPassPhrase() throws Exception {
		GridAccountService gridAccountServiceTest = new GridAccountServiceImpl();
		gridAccountServiceTest.validatePassPhrase(null);

	}

	@Test(expected = IllegalArgumentException.class)
	public final void testValidateBlankPassPhrase() throws Exception {

		gridAccountService.validatePassPhrase("");

	}

	@Test(expected = IllegalArgumentException.class)
	public final void testReplacePassPhraseNull() throws Exception {
		GridAccountServiceImpl gridAccountServiceTest = new GridAccountServiceImpl();
		gridAccountServiceTest.changePassPhraseWhenAlreadyValidated(null);
	}

	@Test(expected = ConveyorExecutionException.class)
	public final void testFindGridAccountForIRODSAccountNotValidated()
			throws Exception {
		GridAccountServiceImpl gridAccountServiceTest = new GridAccountServiceImpl();
		GridAccountDAO gridAccountDAO = Mockito.mock(GridAccountDAO.class);
		gridAccountServiceTest.setGridAccountDAO(gridAccountDAO);
		ConveyorExecutorService conveyorExecutorService = Mockito
				.mock(ConveyorExecutorService.class);
		gridAccountServiceTest
				.setConveyorExecutorService(conveyorExecutorService);
		String testUserName = "user1";
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(
						testingProperties, testUserName, testUserName);
		gridAccountService.findGridAccountByIRODSAccount(irodsAccount);

	}

	@Test(expected = IllegalArgumentException.class)
	public final void testFindGridAccountForIRODSAccountNullAccount()
			throws Exception {
		GridAccountServiceImpl gridAccountServiceTest = new GridAccountServiceImpl();
		GridAccountDAO gridAccountDAO = Mockito.mock(GridAccountDAO.class);
		gridAccountServiceTest.setGridAccountDAO(gridAccountDAO);
		ConveyorExecutorService conveyorExecutorService = Mockito
				.mock(ConveyorExecutorService.class);
		gridAccountServiceTest
				.setConveyorExecutorService(conveyorExecutorService);
		gridAccountService.findGridAccountByIRODSAccount(null);

	}

	@Test
	public final void testReset() throws Exception {
		String testUserName = "user1";
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(
						testingProperties, testUserName, testUserName);
		String passPhrase = "ooogabooga";
		gridAccountService.validatePassPhrase(passPhrase);
		gridAccountService.deleteAllGridAccounts();

		gridAccountService
				.addOrUpdateGridAccountBasedOnIRODSAccount(irodsAccount);

		gridAccountService.resetPassPhraseAndAccounts();
		gridAccountService.findAll();

	}

	@Test
	public final void testIsPassPhraseAlreadyStoredWhenAlreadyStored()
			throws Exception {

		String passPhrase = "ooogabooga";
		gridAccountService.validatePassPhrase(passPhrase);
		boolean actual = gridAccountService.isPassPhraseStoredAlready();
		Assert.assertTrue("should show pass phrase as already stored", actual);

	}

	@Test
	public final void testIsPassPhraseAlreadyStoredWhenNotAlreadyStored()
			throws Exception {

		boolean actual = gridAccountService.isPassPhraseStoredAlready();
		Assert.assertFalse("should not show pass phrase as already stored",
				actual);

	}

}
