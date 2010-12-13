
package org.irods.jargon.idrop.desktop.systraygui.services;

import org.irods.jargon.idrop.desktop.systraygui.iDrop;
import org.irods.jargon.transferengine.TransferManager;

/**
 * Manager of icons for the system gui based on the status.
 * @author Mike Conway - DICE (www.irods.org)
 */
public class IconManager {

    private TransferManager.ErrorStatus errorStatus = null;
    private TransferManager.RunningStatus runningStatus = null;
    private final iDrop idropGui;

    public static IconManager instance(final iDrop idropClient) {
        return new IconManager(idropClient);
    }

    private IconManager(final iDrop idropClient) {
        this.idropGui = idropClient;
    }

    public synchronized void setErrorStatus(final TransferManager.ErrorStatus errorStatus) {
        this.errorStatus = errorStatus;
        updateIcon();
    }

    public synchronized void setRunningStatus(final TransferManager.RunningStatus runningStatus) {
        this.runningStatus = runningStatus;
        updateIcon();
    }

    private void updateIcon() {
        String iconFile = "";
        if (runningStatus == TransferManager.RunningStatus.PAUSED) {
            iconFile="images/media-playback-pause-3.png";
        } else  if (errorStatus == TransferManager.ErrorStatus.ERROR) {
            iconFile = "images/dialog-error-3.png";
        } else if (errorStatus == TransferManager.ErrorStatus.WARNING) {
            iconFile = "images/dialog-warning.png";
        } else if(runningStatus == TransferManager.RunningStatus.IDLE) {
            iconFile = "images/dialog-ok-2.png";
        } else if(runningStatus == TransferManager.RunningStatus.PROCESSING) {
            iconFile = "images/system-run-5.png";
        } else {
           iconFile = "images/dialog-ok-2.png";
        }
        idropGui.updateIcon(iconFile);
    }

}
