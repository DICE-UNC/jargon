package org.irods.jargon.idrop.desktop.systraygui.viscomponents;

import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.irods.jargon.idrop.exceptions.IdropException;
import org.irods.jargon.part.policy.domain.PolicyRequiredMetadataValue;
import org.irods.jargon.part.policy.domain.PolicyRequiredMetadataValue.MetadataType;

/**
 * A panel that prompts for a required metadata value.  This component is built dynamically based
 * on the required metadata value in the policy.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 */
public class PanelRequiredMetadataValue extends JPanel {

    private final PolicyRequiredMetadataValue policyRequiredMetadataValue;

    private JLabel lblPrompt;
    private JPanel pnlDataEntry;
    private JLabel lblUnits = null;

    public JLabel getLblUnits() {
        return lblUnits;
    }


    /**
     * Default contructor that provides the metadata value from the policy which will be modeled in
     * the panel as a data entry form.
     * @param policyRequiredMetadataValue <code>PolicyRequiredMetadataValue<code> element in the policy.
     * @throws IdropException
     */
    public PanelRequiredMetadataValue(final PolicyRequiredMetadataValue policyRequiredMetadataValue) throws IdropException {
        if (policyRequiredMetadataValue == null) {
            throw new IdropException("null policyRequiredMetadataValue");
        }

        this.policyRequiredMetadataValue = policyRequiredMetadataValue;
        initialize();

    }

    private void initialize() {
        lblPrompt = new javax.swing.JLabel();
        lblPrompt.setText(policyRequiredMetadataValue.getMetaDataPromptAsText());
        pnlDataEntry = new javax.swing.JPanel();
        pnlDataEntry.setLayout(new java.awt.BorderLayout());

        if (policyRequiredMetadataValue.getMetadataType() == MetadataType.LITERAL_STRING) {
            JTextField textField = new JTextField();
            textField.setColumns(80);
            pnlDataEntry.add(textField, BorderLayout.CENTER);
        }



        if (!policyRequiredMetadataValue.getUnits().isEmpty()) {
            lblUnits = new JLabel();
            lblUnits.setText(policyRequiredMetadataValue.getUnits());
            pnlDataEntry.add(lblUnits, BorderLayout.EAST);
        }


        JPanel pnlBottom = new javax.swing.JPanel();

        setMinimumSize(new java.awt.Dimension(300, 100));
        setLayout(new java.awt.BorderLayout());

        lblPrompt.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        add(lblPrompt, java.awt.BorderLayout.NORTH);


        add(pnlDataEntry, java.awt.BorderLayout.CENTER);
    }


    public JLabel getLblPrompt() {
        return lblPrompt;
    }


    public JPanel getPnlDataEntry() {
        return pnlDataEntry;
    }


}
