package org.irods.jargon.idrop.desktop.systraygui.services;

import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.idrop.desktop.systraygui.iDrop;
import org.irods.jargon.idrop.exceptions.IdropException;
import org.irods.jargon.transferengine.TransferManager;
import org.slf4j.LoggerFactory;

/**
 * @author Mike Conway - DICE (www.irods.org)
 */
public class QueueSchedulerTimerTask extends TimerTask {

    private final TransferManager transferManager;
    private final iDrop idropGui;
    public final static long EVERY_10_MINUTES = 1000 * 60 * 10;
    public final static long EVERY_30_SECONDS = 1000 * 30;
    public static org.slf4j.Logger log = LoggerFactory.getLogger(QueueSchedulerTimerTask.class);

    public static final QueueSchedulerTimerTask instance(final TransferManager transferManager, final iDrop idropGui) throws IdropException {
        return new QueueSchedulerTimerTask(transferManager, idropGui);
    }

    private QueueSchedulerTimerTask(final TransferManager transferManager, final iDrop idropGui) throws IdropException {
        if (transferManager == null) {
            throw new IdropException("null transfer manager");
        }

        if (idropGui == null) {
            throw new IdropException("null idropGui");
        }

        this.transferManager = transferManager;
        this.idropGui = idropGui;

    }

    @Override
    public void run() {
        log.info("timer task running");

        if (transferManager.isPaused()) {
            log.info("timer is paused");
            return;
        }

        try {
            log.info("***** timer queue asking transfer manager to process next");
            transferManager.processNextInQueueIfIdle();
        } catch (JargonException ex) {
            Logger.getLogger(QueueSchedulerTimerTask.class.getName()).log(Level.SEVERE, null, ex);
            idropGui.showIdropException(ex);
            return;
        }

  
    }
}
