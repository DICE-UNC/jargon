package org.irods.jargon.transfer;
import org.irods.jargon.transfer.engine.TransferQueueService;
import org.irods.jargon.transfer.engine.TransferQueueServiceImpl;
import org.irods.jargon.transfer.engine.synch.SynchManagerService;
import org.irods.jargon.transfer.engine.synch.SynchManagerServiceImpl;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Factory for spring-aware services
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class TransferServiceFactoryImpl {

	private BeanFactory beanFactory;

	public TransferServiceFactoryImpl() {
		ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext(
				new String[] { "classpath:transfer-dao-beans.xml",
						"classpath:transfer-dao-hibernate-spring.cfg.xml" });
		// of course, an ApplicationContext is just a BeanFactory
		beanFactory = appContext;
	}

	public TransferQueueService instanceTransferQueueService() {
		return (TransferQueueService) beanFactory.getBean("transferQueueService");
	}
	
	public SynchManagerService instanceSynchManagerService() {
		return beanFactory.getBean(SynchManagerServiceImpl.class);
	}
	
}
