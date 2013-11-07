package org.irods.jargon.transfer.dao;

import static org.junit.Assert.assertTrue;
import junit.framework.Assert;
import junit.framework.TestCase;

import org.irods.jargon.transfer.dao.domain.KeyStore;
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
public class KeyStoreDAOImplTest {

	@Autowired
	private KeyStoreDAO keyStoreDAO;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public final void testSave() throws Exception {

		KeyStore keyStore = new KeyStore();
		keyStore.setId(KeyStore.KEY_STORE_PASS_PHRASE);
		keyStore.setValue("blah");
		keyStoreDAO.save(keyStore);

		assertTrue(keyStore.getId() != null);
	}

	@Test(expected = TransferDAOException.class)
	public final void testSaveNull() throws Exception {

		KeyStore keyStore = new KeyStore();
		keyStore.setId(null);
		keyStore.setValue("blah");
		keyStoreDAO.save(keyStore);

	}

	@Test
	public final void testFindById() throws Exception {
		KeyStore keyStore = new KeyStore();
		keyStore.setId(KeyStore.KEY_STORE_PASS_PHRASE);
		keyStore.setValue("blah");
		keyStoreDAO.save(keyStore);
		// now find
		KeyStore found = keyStoreDAO.findById(KeyStore.KEY_STORE_PASS_PHRASE);
		Assert.assertEquals("did not find value for key", "blah",
				found.getValue());
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testFindByIdNullId() throws Exception {
		keyStoreDAO.findById(null);

	}

	@Test
	public final void testDelete() throws Exception {
		KeyStore keyStore = new KeyStore();
		keyStore.setId(KeyStore.KEY_STORE_PASS_PHRASE);
		keyStore.setValue("blah");
		keyStoreDAO.save(keyStore);
		keyStoreDAO.delete(keyStore);
		KeyStore found = keyStoreDAO.findById(KeyStore.KEY_STORE_PASS_PHRASE);
		TestCase.assertNull("should not find", found);
	}

}
