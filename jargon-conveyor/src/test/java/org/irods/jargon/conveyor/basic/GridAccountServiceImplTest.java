/**
 * 
 */
package org.irods.jargon.conveyor.basic;

import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.irods.jargon.conveyor.core.GridAccountService;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.transfer.dao.domain.GridAccount;
import org.irods.jargon.transfer.exception.PassPhraseInvalidException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
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

	@Autowired
	private GridAccountService gridAccountService;

	@Test
	public final void testAddOrUpdateGridAccountBasedOnIRODSAccountWillBeAdd()
			throws Exception {
		String testUserName = "user1";
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountForIRODSUserFromTestPropertiesForGivenUser(
						testingProperties, testUserName, testUserName);
		String passPhrase = "ooogabooga";
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
		gridAccountService.storePassPhrase(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testStorePassPhraseBlank() throws Exception {
		gridAccountService.storePassPhrase("");
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

}
