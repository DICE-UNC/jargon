/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.irods.jargon.idrop.desktop.systraygui.viscomponents;

import javax.swing.JTextField;
import java.awt.Component;
import junit.framework.TestCase;
import org.irods.jargon.part.policy.domain.PolicyRequiredMetadataValue.MetadataType;
import org.irods.jargon.part.policy.domain.PolicyRequiredMetadataValue;
import org.irods.jargon.idrop.exceptions.IdropException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mikeconway
 */
public class PanelRequiredMetadataValueTest {

    public PanelRequiredMetadataValueTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test(expected=IdropException.class)
    public void testCreateNoMetadataValue() throws Exception  {
       new PanelRequiredMetadataValue(null);
    }


    @Test
    public void testPanelWithStringLiteral() throws Exception {
        String prompt = "prompt";
        PolicyRequiredMetadataValue policyRequiredMetadataValue = new PolicyRequiredMetadataValue();
        policyRequiredMetadataValue.setMetaDataPromptAsText(prompt);
        policyRequiredMetadataValue.setMetadataType(MetadataType.LITERAL_STRING);

        PanelRequiredMetadataValue panelRequiredMetadataValue = new PanelRequiredMetadataValue(policyRequiredMetadataValue);

        TestCase.assertEquals("did not set prompt", prompt, panelRequiredMetadataValue.getLblPrompt().getText());

        Component[] components = panelRequiredMetadataValue.getPnlDataEntry().getComponents();
        TestCase.assertEquals("should be one component in the data entry panel", 1, components.length);
        TestCase.assertTrue("did not set up a JText field for the string", components[0] instanceof JTextField);
        TestCase.assertNull("units should not have been set", panelRequiredMetadataValue.getLblUnits());

    }

     @Test
    public void testPanelWithStringLiteralAndUnits() throws Exception {
        String prompt = "prompt";
        String units = "units";
        PolicyRequiredMetadataValue policyRequiredMetadataValue = new PolicyRequiredMetadataValue();
        policyRequiredMetadataValue.setMetaDataPromptAsText(prompt);
        policyRequiredMetadataValue.setMetadataType(MetadataType.LITERAL_STRING);
        policyRequiredMetadataValue.setUnits(units);

        PanelRequiredMetadataValue panelRequiredMetadataValue = new PanelRequiredMetadataValue(policyRequiredMetadataValue);

        TestCase.assertEquals("did not set prompt", prompt, panelRequiredMetadataValue.getLblPrompt().getText());

        Component[] components = panelRequiredMetadataValue.getPnlDataEntry().getComponents();
        TestCase.assertEquals("should be text an units in the data entry panel", 2, components.length);
        TestCase.assertTrue("did not set up a JText field for the string", components[0] instanceof JTextField);
        TestCase.assertNotNull("units should not have been set", panelRequiredMetadataValue.getLblUnits());

    }
}