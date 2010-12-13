package org.irods.jargon.idrop.desktop.systraygui.services;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.idrop.exceptions.IdropException;
import org.slf4j.LoggerFactory;

/**
 * Client delegate that talks to iRODS services
 * (This is a refactoring to optimize connection usage)
 * @author Mike Conway - DICE (www.irods.org)
 */
public class IRODSClientDelegate {

    private IRODSFileSystem irodsFileSystem = null;
    private final IRODSAccount irodsAccount;

    public static org.slf4j.Logger log = LoggerFactory.getLogger(IRODSClientDelegate.class);

    /**
     * Create an instance of a client delegate.  This delegate will maintain a connection so that
     * multiple services may be invoked.  A disconnect() call will close any open connection.
     * @param irodsAccount <code>IRODSAccount</code>
     * @throws IdropException
     */
    public IRODSClientDelegate(final IRODSAccount irodsAccount) throws IdropException {
        if (irodsAccount == null) {
            throw new IdropException("irodsAccount is null");
        }

        this.irodsAccount = irodsAccount;

    }

    /**
     * Connect to iRODS to process a series of requests.  The connection is retained until disconnect() is closed.
     * @throws IdropException
     */
    public void connect() throws IdropException {

        if (irodsFileSystem != null) {
            log.debug("already have an iRODS file system, proceed");
            return;
        }

        try {
            irodsFileSystem = IRODSFileSystem.instance();
        } catch (JargonException ex) {
            Logger.getLogger(IRODSClientDelegate.class.getName()).log(Level.SEVERE, null, ex);
            throw new IdropException("error connecting to iRODS");
        }
    }

    /**
     * Disconnect from iRODS.  This method is called after the service is no longer needed.
     * @throws IdropException
     */
    public void disconnect() throws IdropException {

        log.info("disconnecting...");

        if (irodsFileSystem == null) {
            log.info("no irodsFileSystem, will just ignore");
            return;
        }
        try {
            irodsFileSystem.close(irodsAccount);
            irodsFileSystem = null;
            log.info("disconnected, irodsFileSystem is set to null");
        } catch (JargonException ex) {
            Logger.getLogger(IRODSClientDelegate.class.getName()).log(Level.SEVERE, null, ex);
            throw new IdropException("error disconnecting from iRODS", ex);
        }



    }




}
