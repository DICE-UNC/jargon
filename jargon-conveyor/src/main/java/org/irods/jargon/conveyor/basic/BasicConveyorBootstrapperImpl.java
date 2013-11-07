/**
 * 
 */
package org.irods.jargon.conveyor.basic;

import org.irods.jargon.conveyor.core.BootstrapperException;
import org.irods.jargon.conveyor.core.ConveyorBootstrapper;
import org.irods.jargon.conveyor.core.ConveyorService;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Initial bootstrapper process that can later be factored into a more
 * sophisticated processor. This class will wire together a basic functional
 * conveyor processor with the default Derby database.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class BasicConveyorBootstrapperImpl implements ConveyorBootstrapper {

	private final Logger log = LoggerFactory
			.getLogger(BasicConveyorBootstrapperImpl.class);

	private final ConveyorBootstrapConfiguration conveyorBootstrapConfiguration;

	private BeanFactory beanFactory;

	public BasicConveyorBootstrapperImpl(
			final ConveyorBootstrapConfiguration conveyorBootstrapConfiguration) {
		if (conveyorBootstrapConfiguration == null) {
			throw new IllegalArgumentException(
					"null conveyorBootstrapConfiguration");
		}

		this.conveyorBootstrapConfiguration = conveyorBootstrapConfiguration;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.conveyor.basic.ConveyorBootstrapper#bootstrap()
	 */
	@Override
	public ConveyorService bootstrap(
			final IRODSAccessObjectFactory irodsAccessObjectFactory)
			throws BootstrapperException {

		log.info("bootstrapping...");

		if (irodsAccessObjectFactory == null) {
			throw new IllegalArgumentException("null irodsAccessObjectFactory");
		}

		try {
			ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext(
					new String[] { "classpath:transfer-dao-beans.xml",
							"classpath:transfer-dao-hibernate-spring.cfg.xml" });
			// of course, an ApplicationContext is just a BeanFactory
			beanFactory = appContext;
		} catch (Exception e) {
			log.error("error starting app context", e);
			throw new BootstrapperException(e.getMessage());
		}
		log.info("bootstrap complete...");
		ConveyorService conveyorService = (ConveyorService) beanFactory
				.getBean("conveyorService");

		conveyorService.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		return conveyorService;
	}

	/**
	 * @return the conveyorBootstrapConfiguration
	 */
	public ConveyorBootstrapConfiguration getConveyorBootstrapConfiguration() {
		return conveyorBootstrapConfiguration;
	}

}
