/**
 * 
 */
package org.irods.jargon.transfer.dao.domain;

/**
 * Enumeration for the purposes of a given tranfser attempt
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public enum TransferAttemptTypeEnum {

	NORMAL, RESTART, RESUBMIT, RESTARTED_PROCESSING_TRANSFER_AT_STARTUP

}
