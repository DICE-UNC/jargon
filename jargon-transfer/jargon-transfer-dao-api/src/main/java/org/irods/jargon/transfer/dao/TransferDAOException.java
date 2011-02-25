package org.irods.jargon.transfer.dao;

/**
 * 
 * @author jdr0887
 * 
 */
public class TransferDAOException extends Exception {

    private static final long serialVersionUID = 1L;

    public TransferDAOException() {
        super();
    }

    public TransferDAOException(String message, Throwable cause) {
        super(message, cause);
    }

    public TransferDAOException(String message) {
        super(message);
    }

    public TransferDAOException(Throwable cause) {
        super(cause);
    }

}
