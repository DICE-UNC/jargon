package org.irods.jargon.idrop.desktop.systraygui.services;

import javax.swing.SwingWorker;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.rule.IRODSRuleExecResult;
import org.irods.jargon.idrop.desktop.systraygui.iDrop;
import org.irods.jargon.idrop.exceptions.IdropException;
import org.slf4j.LoggerFactory;

/**
 * Swing worker to manage rule execution, and a bridge to handle callbacks between the rule and the client
 * @author Mike Conway - DICE (www.irods.org)
 */
public final class RuleExecutionWorker extends SwingWorker  {

    public static org.slf4j.Logger log = LoggerFactory.getLogger(RuleExecutionWorker.class);

    private final String irodsTargetAbsolutePath;
    private final String targetResource;
    private final IRODSAccount irodsAccount;
    private final iDrop idropGui;
    private IRODSRuleExecResult execResult = null;

    public RuleExecutionWorker(final iDrop idropGui,
            final String irodsTargetAbsolutePath, final String targetResource, final IRODSAccount irodsAccount) throws IdropException {

        if (idropGui == null) {
            throw new IdropException("null idropGui");
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

        this.idropGui = idropGui;
        this.irodsTargetAbsolutePath = irodsTargetAbsolutePath;
        this.targetResource = targetResource;
        this.irodsAccount = irodsAccount;

    }

    @Override
    protected Object doInBackground() throws Exception {
        log.info("initiating rule execution");
        IRODSFileService irodsFileService = new IRODSFileService(irodsAccount, idropGui.getIrodsFileSystem());
        execResult = irodsFileService.runIRODSRule(RuleLibrary.collectionRule(irodsTargetAbsolutePath));
        return execResult;
    }

    @Override
    protected void done() {
        log.info("rule execution finished with result:{}", execResult);
        idropGui.showMessageFromOperation("rule execution finished");
    }

}
