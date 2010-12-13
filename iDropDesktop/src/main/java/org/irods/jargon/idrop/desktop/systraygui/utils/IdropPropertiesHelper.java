package org.irods.jargon.idrop.desktop.systraygui.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.irods.jargon.idrop.exceptions.IdropException;
import org.irods.jargon.testutils.TestingUtilsException;

/**
 * Help accessing iDrop configuration properties
 * @author Mike Conway - DICE (www.irods.org)
 */
public class IdropPropertiesHelper {

    public static final String POLICY_AWARE_PROPERTY = "policy.aware";
    public static final String LOGIN_PRESET = "login.preset";
    public static final String LOGIN_PRESET_HOST = "login.preset.host";
    public static final String LOGIN_PRESET_PORT = "login.preset.port";
    public static final String LOGIN_PRESET_ZONE = "login.preset.zone";
    public static final String LOGIN_PRESET_RESOURCE = "login.preset.resource";
    public static final String TRANSFER_ENGINE_RECORD_SUCCESSFUL_FILES = "transferengine.record.successful.files";
    public static final String ADVANCED_VIEW_PROPERTY = "advanced.view";
    public static final String TRANSFER_DATABASE_NAME = "transfer.database";
    public static final String ROLLING_LOG_LEVEL = "rolling.log.level";

    /**
     * Load the default iDrop poperties file
     * @return
     * @throws IdropException
     */
    public Properties loadIdropProperties() throws IdropException {
        ClassLoader loader = this.getClass().getClassLoader();
        InputStream in = loader.getResourceAsStream("idrop.properties");
        Properties properties = new Properties();

        try {
            properties.load(in);
        }
        catch (IOException ioe) {
            throw new IdropException("error loading idrop properties",
                    ioe);
        }
        finally {
            try {
                in.close();
            }
            catch (Exception e) {
                // ignore
            }
        }

        return properties;
    }

}
