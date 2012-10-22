package org.irods.jargon.transfer.engine;

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

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:transfer-dao-beans.xml",
		"classpath:transfer-dao-hibernate-spring.cfg.xml",
		"classpath:test-beans.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class GridAccountServiceImplTest {

	@Autowired
	private GridAccountService gridAccountService;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
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
		// revalidate
		gridAccountService.validatePassPhrase(passPhrase + "oogaooga");
	}

}
