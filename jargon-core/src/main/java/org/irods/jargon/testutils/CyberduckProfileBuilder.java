/**
 * 
 */
package org.irods.jargon.testutils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Properties;

import org.irods.jargon.core.exception.JargonException;

/**
 * @author Mike Conway (DFC) Creates a cyberduck profile from testing
 *         properties, allows integration of cyberduck tests into jargon unit
 *         testing
 *
 */
public class CyberduckProfileBuilder {

	public static final void writeCyberduckProfile(
			final String profileAbsolutePath, final Properties testingProperties)
			throws JargonException {

		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		sb.append("<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">\n");
		sb.append("<plist version=\"1.0\">\n");
		sb.append("<dict>\n");
		sb.append("<key>Protocol</key>\n");
		sb.append("<string>irods</string>\n");
		sb.append("<key>Vendor</key>\n");
		sb.append("<string>iRODS</string>\n");
		sb.append("<key>Description</key>\n");
		sb.append("<string>iRODS Cyberduck Bookmark</string>\n");
		sb.append("<key>Hostname Configurable</key>\n");
		sb.append("<true/>\n");
		sb.append("<key>Port Configurable</key>\n");
		sb.append("<true/>\n");
		sb.append("<key>Default Hostname</key>\n");
		sb.append("<string>");
		sb.append(testingProperties.get(TestingPropertiesHelper.IRODS_HOST_KEY));
		sb.append("</string>\n");
		sb.append("<key>Region</key>\n");
		sb.append("<string>");
		sb.append(testingProperties.get(TestingPropertiesHelper.IRODS_ZONE_KEY));
		sb.append(":");
		sb.append(testingProperties
				.get(TestingPropertiesHelper.IRODS_RESOURCE_KEY));
		sb.append("</string>\n");
		sb.append("<key>Default Port</key>\n");
		sb.append("<string>");
		sb.append(testingProperties.get(TestingPropertiesHelper.IRODS_PORT_KEY));
		sb.append("</string>\n");
		sb.append("<key>Username Placeholder</key>\n");
		sb.append("<string>");
		sb.append(testingProperties.get(TestingPropertiesHelper.IRODS_USER_KEY));
		sb.append("</string>\n");
		sb.append("<key>Password Placeholder</key>\n");
		sb.append("<string>");
		sb.append(testingProperties
				.get(TestingPropertiesHelper.IRODS_PASSWORD_KEY));
		sb.append("</string>\n");
		sb.append(" </dict>\n");
		sb.append("</plist>\n");
		String profile = sb.toString();
		File profileFile = new File(profileAbsolutePath);
		profileFile.delete();
		PrintWriter printWriter;
		try {
			printWriter = new PrintWriter(profileFile);
		} catch (FileNotFoundException e) {
			throw new JargonException("cannot find file for printwriter", e);
		}
		printWriter.println(profile);
		printWriter.close();

	}

}
