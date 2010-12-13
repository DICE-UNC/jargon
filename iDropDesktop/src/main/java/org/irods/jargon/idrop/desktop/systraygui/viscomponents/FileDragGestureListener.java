/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.irods.jargon.idrop.desktop.systraygui.viscomponents;

import java.awt.dnd.DragSourceAdapter;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;

/**
 * @author Mike Conway - DICE (www.irods.org)
 */
public class FileDragGestureListener extends DragSourceAdapter {

    @Override
    public void dragDropEnd(DragSourceDropEvent dsde) {
        super.dragDropEnd(dsde);
    }

    @Override
    public void dragEnter(DragSourceDragEvent dsde) {
        super.dragEnter(dsde);
    }

    @Override
    public void dragExit(DragSourceEvent dse) {
        super.dragExit(dse);
    }

}
