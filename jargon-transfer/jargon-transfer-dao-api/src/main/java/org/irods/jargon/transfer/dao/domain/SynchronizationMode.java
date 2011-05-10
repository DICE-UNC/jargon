package org.irods.jargon.transfer.dao.domain;

/**
 * Enumeration of synchronization modes.
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public enum SynchronizationMode {
	ONE_WAY_LOCAL_TO_IRODS,
	ONE_WAY_IRODS_TO_LOCAL,
	BI_DIRECTIONAL
}
