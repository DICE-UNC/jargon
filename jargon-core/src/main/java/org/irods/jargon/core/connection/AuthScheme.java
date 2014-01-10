package org.irods.jargon.core.connection;

import java.util.ArrayList;
import java.util.List;

public enum AuthScheme {
	STANDARD("STANDARD"), GSI("GSI"), KERBEROS("KERBEROS"), PAM("PAM");

	private String textValue;

	AuthScheme(final String textValue) {
		this.textValue = textValue;
	}

	public String getTextValue() {
		return textValue;
	}

	public static List<String> getAuthSchemeList() {

		List<String> authSchemes = new ArrayList<String>();
		for (AuthScheme authScheme : AuthScheme.values()) {
			authSchemes.add(authScheme.textValue);
		}
		return authSchemes;
	}

	public static AuthScheme findTypeByString(final String authType) {
		AuthScheme authSchemeValue = null;
		for (AuthScheme authScheme : AuthScheme.values()) {
			if (authScheme.getTextValue().equals(authType)) {
				authSchemeValue = authScheme;
				break;
			}
		}
		if (authSchemeValue == null) {
			authSchemeValue = AuthScheme.STANDARD;
		}
		return authSchemeValue;

	}

}