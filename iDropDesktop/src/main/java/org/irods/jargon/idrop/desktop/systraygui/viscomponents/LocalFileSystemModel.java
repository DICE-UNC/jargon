
package org.irods.jargon.idrop.desktop.systraygui.viscomponents;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 * (NEW) Implementation of the DefaultTreeModel for the local file system.
 * @author Mike Conway - DICE (www.irods.org)
 */
public class LocalFileSystemModel extends DefaultTreeModel {
    
    public LocalFileSystemModel(DefaultMutableTreeNode node) { 
        super(node);
        // pre-expand the child nodes of the root
        LocalFileNode localFileNode = (LocalFileNode) node;
        localFileNode.lazyLoadOfChildrenOfThisNode();
    }


}
