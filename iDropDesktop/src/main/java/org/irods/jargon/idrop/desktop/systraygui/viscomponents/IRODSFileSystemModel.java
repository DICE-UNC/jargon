package org.irods.jargon.idrop.desktop.systraygui.viscomponents;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.tree.DefaultTreeModel;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.idrop.exceptions.IdropException;
import org.irods.jargon.idrop.exceptions.IdropRuntimeException;

/**
 * Model of an underlying file system for browsing in a tree view
 * @author Mike Conway - DICE (www.irods.org)
 */
public class IRODSFileSystemModel extends DefaultTreeModel {

    @Override
    public Object getChild(Object parent, int index) {
        triggerLazyLoading(parent);
        return super.getChild(parent, index);
    }

    @Override
    public int getChildCount(Object parent) {
        triggerLazyLoading(parent);
        return super.getChildCount(parent);
    }

    private void triggerLazyLoading(Object parent) throws IdropRuntimeException {
        // make sure children are loaded before counting
        IRODSNode parentAsNode = (IRODSNode) parent;
        try {
            parentAsNode.lazyLoadOfChildrenOfThisNode();
        } catch (IdropException ex) {
            Logger.getLogger(IRODSFileSystemModel.class.getName()).log(Level.SEVERE, null, ex);
            throw new IdropRuntimeException(ex);
        }
    }
    private final IRODSAccount irodsAccount;

    public IRODSFileSystemModel(final IRODSNode rootNode, final IRODSAccount irodsAccount) throws IdropException {
        super(rootNode);

        if (irodsAccount == null) {
            throw new IdropRuntimeException("null irodsAccount");
        }
        this.irodsAccount = irodsAccount;

        // pre-expand the child nodes of the root

        rootNode.lazyLoadOfChildrenOfThisNode();

    }
}
