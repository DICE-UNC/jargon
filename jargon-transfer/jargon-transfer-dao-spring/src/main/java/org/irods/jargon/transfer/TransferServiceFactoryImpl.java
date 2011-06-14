package org.irods.jargon.transfer;

import org.irods.jargon.core.pub.DataTransferOperationsImpl;
import org.irods.jargon.transfer.engine.ConfigurationService;
import org.irods.jargon.transfer.engine.TransferQueueService;
import org.irods.jargon.transfer.engine.synch.SynchManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	private static Logger log = LoggerFactory
			.getLogger(TransferServiceFactoryImpl.class);

	public TransferServiceFactoryImpl() throws TransferEngineException {
		try {
			ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext(
					new String[] { "classpath:transfer-dao-beans.xml",
							"classpath:transfer-dao-hibernate-spring.cfg.xml" });
			// of course, an ApplicationContext is just a BeanFactory
			beanFactory = appContext;
		} catch (Exception e) {
			log.error("error starting app context", e);
			throw new TransferEngineException(e.getMessage());
		}
	}

	public TransferQueueService instanceTransferQueueService() {
		return (TransferQueueService) beanFactory
				.getBean("transferQueueService");
	}

	public SynchManagerService instanceSynchManagerService() {
		return (SynchManagerService) beanFactory.getBean("synchManagerService");
	}

	public ConfigurationService instanceConfigurationService() {
		return (ConfigurationService) beanFactory
				.getBean("configurationService");
	}

}
