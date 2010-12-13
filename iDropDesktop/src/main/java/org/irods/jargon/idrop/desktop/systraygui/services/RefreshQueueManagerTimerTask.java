/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.irods.jargon.idrop.desktop.systraygui.services;

import java.util.TimerTask;
import org.irods.jargon.idrop.desktop.systraygui.QueueManagerDialog;
import org.irods.jargon.idrop.exceptions.IdropException;
import org.slf4j.LoggerFactory;

/**
 * Timer task to handle refresh of queue manager dialog
 * @author Mike Conway - DICE (www.irods.org)
 */
public class RefreshQueueManagerTimerTask extends TimerTask {

    final QueueManagerDialog queueManagerDialog;
    public static org.slf4j.Logger log = LoggerFactory.getLogger(RefreshQueueManagerTimerTask.class);

    public static final RefreshQueueManagerTimerTask instance(final QueueManagerDialog queueManagerDialog) throws IdropException {
        return new RefreshQueueManagerTimerTask(queueManagerDialog);
    }

    private RefreshQueueManagerTimerTask(final QueueManagerDialog queueManagerDialog)  throws IdropException {
        if (queueManagerDialog == null) {
            throw new IdropException("null queueManagerDialog");
        }
        this.queueManagerDialog = queueManagerDialog;
    }

    @Override
    public void run() {
        queueManagerDialog.refreshTableView(queueManagerDialog.getViewType());
    }

}
