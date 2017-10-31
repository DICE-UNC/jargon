/**
 *
 */
package org.irods.jargon.datautils.rule;

/**
 * Configuration info for user rule services
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class UserRuleServiceConfiguration {

	public static final String RULE_SUBDIR_NAME = ".irods/userRules";

	/**
	 * Subdirectory used for storing user rules
	 */
	private String ruleSubdirName = RULE_SUBDIR_NAME;
	private boolean createIfMissing = true;

	public String getRuleSubdirName() {
		return ruleSubdirName;
	}

	public void setRuleSubdirName(final String ruleSubdirName) {
		this.ruleSubdirName = ruleSubdirName;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("userRuleServiceConfiguration");
		sb.append("\n\t ruleSubdirName:");
		sb.append(ruleSubdirName);
		sb.append("\n\t createIfMissing:");
		sb.append(createIfMissing);
		return sb.toString();
	}

	public boolean isCreateIfMissing() {
		return createIfMissing;
	}

	public void setCreateIfMissing(final boolean createIfMissing) {
		this.createIfMissing = createIfMissing;
	}

}
