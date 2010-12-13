package org.irods.jargon.idrop.desktop.systraygui.services;

import javax.swing.SwingWorker;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.transfer.TransferStatus;
import org.irods.jargon.core.transfer.TransferStatusCallbackListener;
import org.irods.jargon.idrop.exceptions.IdropException;
import org.irods.jargon.transferengine.TransferManager;
import org.slf4j.LoggerFactory;

/**
 * Swing worker to manage local transfers to iRODS.  This method can serve as a bridge for callbacks as well
 * @author Mike Conway - DICE (www.irods.org)
 */
public final class LocalTransferWorker extends SwingWorker  {

    public static org.slf4j.Logger log = LoggerFactory.getLogger(LocalTransferWorker.class);

    private final TransferManager transferManager;
    private final String localSourceAbsolutePath;
    private final String irodsTargetAbsolutePath;
    private final String targetResource;
    private final IRODSAccount irodsAccount;

    public LocalTransferWorker(final TransferManager transferManager, final String localSourceAbsolutePath,
            final String irodsTargetAbsolutePath, final String targetResource, final IRODSAccount irodsAccount) throws IdropException {

        if (transferManager == null) {
            throw new IdropException("null transferManager");
        }

        if (localSourceAbsolutePath == null || localSourceAbsolutePath.isEmpty()) {
            throw new IdropException("null or empty localSourceAbsolutePath");
        }

        if (irodsTargetAbsolutePath == null || irodsTargetAbsolutePath.isEmpty()) {
            throw new IdropException("null or empty irodsTargetAbsolutePath");
        }

        if (targetResource == null) {
            throw new IdropException("null targetResource, leave as blank if default is desired");
        }

          if (irodsAccount == null) {
            throw new IdropException("null irodsAccount, leave as blank if default is desired");
        }

        this.transferManager = transferManager;
        this.localSourceAbsolutePath = localSourceAbsolutePath;
        this.irodsTargetAbsolutePath = irodsTargetAbsolutePath;
        this.targetResource = targetResource;
        this.irodsAccount = irodsAccount;

    }

    @Override
    protected Object doInBackground() throws Exception {
        log.info("initiating transfer");
        transferManager.enqueueAPut(localSourceAbsolutePath, irodsTargetAbsolutePath, targetResource, irodsAccount);  
        // return a final transfer status
        return null;
    }

    @Override
    protected void done() {
        log.info("done!");
        super.done();
    }

}
