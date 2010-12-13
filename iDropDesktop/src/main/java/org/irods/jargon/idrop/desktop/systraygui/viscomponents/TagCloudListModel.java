/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.irods.jargon.idrop.desktop.systraygui.viscomponents;

import javax.swing.DefaultListModel;
import org.irods.jargon.idrop.exceptions.IdropException;
import org.irods.jargon.usertagging.domain.TagCloudEntry;
import org.irods.jargon.usertagging.domain.UserTagCloudView;

/**
 * List model for tag cloud view list panel
 * @author Mike Conway - DICE (www.irods.org)
 */
public class TagCloudListModel extends DefaultListModel {

    private final UserTagCloudView userTagCloudView;
    private final Object[] entryKeys;

    @Override
    public Object getElementAt(int index) {

        StringBuilder sb = new StringBuilder();
        TagCloudEntry entry = userTagCloudView.getTagCloudEntries().get(entryKeys[index]);
        sb.append(entry.getIrodsTagValue().getTagData());
        sb.append(" (f=");
        sb.append(entry.getCountOfFiles());
        sb.append(" c=");
        sb.append(entry.getCountOfCollections());
        sb.append(")");
        return sb.toString();
    }

    @Override
    public int getSize() {
        return userTagCloudView.getTagCloudEntries().size();
    }

    /**
     * Constructor for list model used in user tag view.
     * @param userTagCloudView <code>UserTagCloudView</code> with a summary of user tags.
     * @throws IdropException
     */
    public TagCloudListModel(final UserTagCloudView userTagCloudView) throws IdropException {
        if (userTagCloudView == null) {
            throw new IdropException("null userTagCloudView");
        }

        this.userTagCloudView = userTagCloudView;
        entryKeys = userTagCloudView.getTagCloudEntries().keySet().toArray();

    }

    /**
     * Method to return the actual tag cloud object.
     * @param index <code>int</code> with the position of the desired entry
     * @return <code>TagCloudEntry</code> with the value at the given index, or null if no value is found
     */
    public TagCloudEntry getTagCloudEntry(final int index) {
        return userTagCloudView.getTagCloudEntries().get(entryKeys[index]);
    }
}
