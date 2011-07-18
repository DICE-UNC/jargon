package org.irods.jargon.transfer.engine.synch;

/**
 * Exception in synchronization processing caused by a conflicting synch
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class ConflictingSynchException extends SynchException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 4067726475052889087L;

	/**
	 * 
	 */
    public ConflictingSynchException() {
    }

    /**
     * @param arg0
     */
    public ConflictingSynchException(final String arg0) {
        super(arg0);
    }

    /**
     * @param arg0
     */
    public ConflictingSynchException(final Throwable arg0) {
        super(arg0);
    }

    /**
     * @param arg0
     * @param arg1
     */
    public ConflictingSynchException(final String arg0, final Throwable arg1) {
        super(arg0, arg1);
    }

}
