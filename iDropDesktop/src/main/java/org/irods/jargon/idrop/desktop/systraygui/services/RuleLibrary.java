
package org.irods.jargon.idrop.desktop.systraygui.services;

import org.irods.jargon.idrop.exceptions.IdropException;

/**
 * Library of rules to use from iDrop
 * @author Mike Conway - DICE (www.irods.org)
 */
public class RuleLibrary {

    private RuleLibrary() {

    }

    public static String virusScanOnCollectionRule(final String targetCollectionAbsolutePath) throws IdropException {

        if (targetCollectionAbsolutePath == null || targetCollectionAbsolutePath.isEmpty()) {
            throw new IdropException("null or empty targetCollectionAbsolutePath");
        }

        // FIXME: currently a dummy rule
        StringBuilder sb = new StringBuilder();
        sb.append("flagWithTimestamp||msiGetSystemTime(*Time, human)##msiAddKeyVal(*KVP,\"TIMESTAMP\",*Time)##msiAssociateKeyValuePairsToObj(*KVP,*Collection,\"-C\")##writeLine(stdout, *Time)|nop\n");
        sb.append("*Collection=");
        sb.append(targetCollectionAbsolutePath.trim());
        sb.append("\n");
        sb.append("ruleExecOut");
        return sb.toString();
    }


    public static String collectionRule(final String targetCollectionAbsolutePath) throws IdropException {
          if (targetCollectionAbsolutePath == null || targetCollectionAbsolutePath.isEmpty()) {
            throw new IdropException("null or empty targetCollectionAbsolutePath");
        }

        // FIXME: currently a dummy rule
        StringBuilder sb = new StringBuilder();
        sb.append("scanAndFlag||acScanLoadingRescAndFlagColl(*collection)|nop\n");
        sb.append("*collection=");
        sb.append(targetCollectionAbsolutePath.trim());
        sb.append("\n");
        sb.append("ruleExecOut");
        return sb.toString();

    }
}
