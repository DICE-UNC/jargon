package org.irods.jargon.idrop.desktop.systraygui.utils;

import java.io.IOException;
import java.util.Properties;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.irods.jargon.idrop.exceptions.IdropException;

/**
 * Access data about the configuration of Idrop
 * @author Mike Conway - DICE (www.irods.org)
 */
public class IdropConfig {

    private final Properties idropProperties;

    public Properties getIdropProperties() {
        return idropProperties;
    }

    public static IdropConfig instance() throws IdropException {
        return new IdropConfig();
    }

    private IdropConfig() throws IdropException {
        IdropPropertiesHelper idropPropertiesHelper = new IdropPropertiesHelper();
        this.idropProperties = idropPropertiesHelper.loadIdropProperties();
    }

    /**
     * Does iDrop need to display policy-aware features?
     * @return <code>boolean</code> that will be <code>true</code> if policy features are displayed.
     */
    public boolean isPolicyAware() {
        boolean policyAware = false;
        String policyAwareValue = idropProperties.getProperty(IdropPropertiesHelper.POLICY_AWARE_PROPERTY);

        if (policyAwareValue != null && policyAwareValue.equals("true")) {
            policyAware = true;
        }

        return policyAware;

    }

    /**
     * Does iDrop need to display advanced options?  Otherwise, a simpler client is presented
     * @return <code>boolean</code> that will be <code>true</code> if policy features are displayed.
     */
    public boolean isAdvancedView() {
        boolean advancedView = false;
        String propValue = idropProperties.getProperty(IdropPropertiesHelper.ADVANCED_VIEW_PROPERTY);

        if (propValue != null && propValue.equals("true")) {
            advancedView = true;
        }

        return advancedView;

    }

    public String getTransferDatabaseName() {
        String propValue = idropProperties.getProperty(IdropPropertiesHelper.TRANSFER_DATABASE_NAME);

        if (propValue != null) {
            return propValue;
        } else {
            return "transferDatabase";
        }
    }

    /**
     * Should iDrop display a preset login limited to a user's home directory?
     * @return
     */
    public boolean isLoginPreset() {
        boolean loginPreset = false;
        String loginPresetValue = idropProperties.getProperty(IdropPropertiesHelper.LOGIN_PRESET);

        if (loginPresetValue != null && loginPresetValue.equals("true")) {
            loginPreset = true;
        }

        return loginPreset;
    }

    /**
     * Should successful transfers be logged to the internal database?
     * @return
     */
    public boolean isLogSuccessfulTransfers() {
        boolean logSuccessful = false;
        String logSuccessfulTransfers = idropProperties.getProperty(IdropPropertiesHelper.TRANSFER_ENGINE_RECORD_SUCCESSFUL_FILES);

        if (logSuccessfulTransfers != null && logSuccessfulTransfers.equals("true")) {
            logSuccessful = true;
        }

        return logSuccessful;
    }

    /**
     * Should I have a rolling log in the user dir?  Will return null of no logging desired, otherwise, will return a log level
     * @return
     */
    public String getLogLevelForRollingLog() {
        String propValue = idropProperties.getProperty(IdropPropertiesHelper.ROLLING_LOG_LEVEL);
        return propValue;

    }

    public void setUpLogging() {
        String rollingLogLevel = getLogLevelForRollingLog();

        if (rollingLogLevel == null) {
            return;
        }

        // log level is specified, set up a rolling logger

        String userHomeDirectory = System.getProperty("user.home");
        StringBuilder sb = new StringBuilder();
        sb.append(userHomeDirectory);
        sb.append("/.idrop/idrop.log");

        org.apache.log4j.Logger rootLogger = org.apache.log4j.Logger.getRootLogger();
        if (rollingLogLevel.equalsIgnoreCase("INFO")) {
            rootLogger.setLevel(Level.INFO);
        } else if (rollingLogLevel.equalsIgnoreCase("DEBUG")) {
            rootLogger.setLevel(Level.DEBUG);
        } else if (rollingLogLevel.equalsIgnoreCase("WARN")) {
            rootLogger.setLevel(Level.WARN);
        } else {
            rootLogger.setLevel(Level.ERROR);
        }

        PatternLayout layout = new PatternLayout("%d %-4r [%t] %-5p %c %x - %m%n");

        try {
            RollingFileAppender rfa = new RollingFileAppender(layout, sb.toString());
            rfa.setMaximumFileSize(1000000);
            rootLogger.addAppender(rfa);
        } catch (IOException e) {
            //  e.printStackTrace();
        }

    }
}
