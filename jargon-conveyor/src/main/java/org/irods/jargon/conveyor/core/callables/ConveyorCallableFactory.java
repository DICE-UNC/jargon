/**
 * 
 */
package org.irods.jargon.conveyor.core.callables;

import org.irods.jargon.conveyor.core.AbstractConveyorCallable;
import org.irods.jargon.conveyor.core.ConveyorService;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.transfer.dao.domain.Transfer;

/**
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class ConveyorCallableFactory {

	/**
	 * 
	 */
	public ConveyorCallableFactory() {
		// TODO Auto-generated constructor stub
	}

	public AbstractConveyorCallable instanceCallableForOperation(
			final Transfer transfer, final ConveyorService conveyorService) throws JargonException {
            
            if (transfer == null) {
                        throw new JargonException("transfer is null");
            }

            switch(transfer.getTransferType()) {
                case PUT:
                    return new PutConveyorCallable(transfer, conveyorService);
                case GET:
                    break;
                case REPLICATE:
                    break;
                case COPY:
                    break;
                case SYNCH:
                    break;
                default:
                    break;
                    
            }
            return null;
	}

}
