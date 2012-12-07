/**
 * 
 */
package org.irods.jargon.conveyor.basic;

import org.apache.log4j.lf5.PassingLogRecordFilter;
import org.irods.jargon.conveyor.core.BootstrapperException;
import org.irods.jargon.conveyor.core.ConveyorBootstrapper;
import org.irods.jargon.conveyor.core.ConveyorExecutorService;
import org.irods.jargon.conveyor.core.ConveyorExecutorServiceImpl;
import org.irods.jargon.conveyor.core.ConveyorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;

/**
 * Initial bootstrapper process that can later be factored into a more sophisticated processor.  This class will wire together a basic
 * functional conveyor processor with the default Derby database.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class BasicConveyorBootstrapperImpl implements ConveyorBootstrapper {
	
	private final Logger log = LoggerFactory
			.getLogger(BasicConveyorBootstrapperImpl.class);
	
	
	private final ConveyorBootstrapConfiguration conveyorBootstrapConfiguration;
	
	private BeanFactory beanFactory;
	
	public BasicConveyorBootstrapperImpl(final ConveyorBootstrapConfiguration conveyorBootstrapConfiguration) {
		if (conveyorBootstrapConfiguration == null) {
			throw new IllegalArgumentException("null conveyorBootstrapConfiguration");
		}
		
		this.conveyorBootstrapConfiguration = conveyorBootstrapConfiguration;
		
	}
	
	/* (non-Javadoc)
	 * @see org.irods.jargon.conveyor.basic.ConveyorBootstrapper#bootstrap()
	 */
	@Override
	public ConveyorService bootstrap() throws BootstrapperException {
		log.info("bootstrapping...");
		
		ConveyorService conveyorService = new BasicConveyorService();
		log.info("creating executor queue...");
		ConveyorExecutorService conveyorExecutorService = new ConveyorExecutorServiceImpl();
		
		conveyorService.setConveyorExecutorService(conveyorExecutorService);
		
		//next...Pass phrase and add grid account manager and queue manager, bring in dao's
		
		log.info("bootstrap complete...");
		return conveyorService;
	}

}
