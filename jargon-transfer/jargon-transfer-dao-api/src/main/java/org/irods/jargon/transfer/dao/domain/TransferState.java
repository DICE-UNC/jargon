package org.irods.jargon.transfer.dao.domain;

public enum TransferState {

    IDLE,
    
    PROCESSING,

    PAUSED,

    CANCELLED,

    COMPLETE,

    ENQUEUED;

}
