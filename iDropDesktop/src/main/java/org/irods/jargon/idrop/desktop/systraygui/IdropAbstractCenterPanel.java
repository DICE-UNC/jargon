package org.irods.jargon.idrop.desktop.systraygui;

import java.awt.Color;
import java.awt.EventQueue;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.irods.jargon.idrop.desktop.systraygui.utils.IconHelper;
import org.irods.jargon.idrop.desktop.systraygui.viscomponents.CollectionViewHolder;
import org.irods.jargon.idrop.desktop.systraygui.viscomponents.RoundedBorder;
import org.irods.jargon.idrop.exceptions.IdropException;

/**
 * @author Mike Conway - DICE (www.irods.org)
 */
public abstract class IdropAbstractCenterPanel extends JPanel {

    
    private final iDrop iDropParentForm;
    private final Color backgroundColor;
    public static final String checksumAvuAttrib = "PolicyDrivenService:PolicyProcessingResultAttribute:FixityCheck";
    public static final String virusAvuAttrib = "PolicyDrivenService:PolicyProcessingResultAttribute:VirusScan";

    public IdropAbstractCenterPanel(final iDrop iDropParentForm,  final Color backgroundColor) throws IdropException {

        if (iDropParentForm == null) {
            throw new IdropException("null iDropParentForm");
        }

      
        if (backgroundColor == null) {
            throw new IdropException("null backgroundColor");
        }

        this.backgroundColor = backgroundColor;
        this.iDropParentForm = iDropParentForm;

    }

  

    public iDrop getiDropParentForm() {
        return iDropParentForm;
    }

    /**
     * @return the backgroundColor
     */
    public Color getBackgroundColor() {
        return backgroundColor;
    }
}
