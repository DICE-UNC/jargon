/**
 * 
 */
package org.irods.jargon.part.policy.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a policy in iRODS. This is a description of the various archival
 * stages, and the rules, parameters, and required metadata for that stage. This
 * Policy is used to control the archival process for an ingested data object.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class Policy {

	/**
	 * Unique identifying name of the policy
	 */
	private String policyName = "";

	/**
	 * Description of the policy
	 */
	private String policyTextualDescription = "";

	/**
	 * Absolute path where this policy was stored in the IRODS server.
	 */
	private String policyAbsolutePathInIrods = "";

	/**
	 * Identifying name of the policy as a property name for display via
	 * alternative language
	 */
	private String policyNameAsAni18nProperty = "";

	/**
	 * Textual description of the policy as a property name for display via
	 * alternative language
	 */
	private String policyTextualDescriptionAsAni18nProperty = "";

	/**
	 * IRODS user who created this policy version
	 */
	private String irodsUserThatCreatedPolicy = "";

	/**
	 * Does this object need to go to a staging area
	 */
	private boolean requireStaging = false;

	private boolean requireVirusScan = false;
	private boolean requireChecksum = false;
	private int numberOfReplicas = 0;
	private String retentionDays = ""; 

	/**
	 * version number of this policy
	 */
	private int policyVersion = 0;

	/**
	 * Date that this policy version was created
	 */
	private Date policyModifiedDate = new Date();

	/**
	 * Indicates an intermediate staging are. If no staging area defined, it
	 * will be put into the defined series
	 */
	private String policyStagingAreaAbsolutePath = "";

	/**
	 * Mapping of a rule by the name of the rule to a descriptor of the rule as
	 * a policy.
	 */
	private Map<String, PolicyRuleDescriptor> policyMappedByRuleName = new HashMap<String, PolicyRuleDescriptor>();

	/**
	 * List of the required and optional metadata values that will be required
	 * on ingest
	 */
	private List<PolicyRequiredMetadataValue> policyRequiredMetadataValues = new ArrayList<PolicyRequiredMetadataValue>();

	public String getPolicyName() {
		return policyName;
	}

	public void setPolicyName(String policyName) {
		this.policyName = policyName;
	}

	public String getPolicyTextualDescription() {
		return policyTextualDescription;
	}

	public void setPolicyTextualDescription(String policyTextualDescription) {
		this.policyTextualDescription = policyTextualDescription;
	}

	public String getPolicyAbsolutePathInIrods() {
		return policyAbsolutePathInIrods;
	}

	public void setPolicyAbsolutePathInIrods(String policyAbsolutePathInIrods) {
		this.policyAbsolutePathInIrods = policyAbsolutePathInIrods;
	}

	public String getPolicyNameAsAni18nProperty() {
		return policyNameAsAni18nProperty;
	}

	public void setPolicyNameAsAni18nProperty(String policyNameAsAni18nProperty) {
		this.policyNameAsAni18nProperty = policyNameAsAni18nProperty;
	}

	public String getPolicyTextualDescriptionAsAni18nProperty() {
		return policyTextualDescriptionAsAni18nProperty;
	}

	public void setPolicyTextualDescriptionAsAni18nProperty(
			String policyTextualDescriptionAsAni18nProperty) {
		this.policyTextualDescriptionAsAni18nProperty = policyTextualDescriptionAsAni18nProperty;
	}

	public String getIrodsUserThatCreatedPolicy() {
		return irodsUserThatCreatedPolicy;
	}

	public void setIrodsUserThatCreatedPolicy(String irodsUserThatCreatedPolicy) {
		this.irodsUserThatCreatedPolicy = irodsUserThatCreatedPolicy;
	}

	public int getPolicyVersion() {
		return policyVersion;
	}

	public void setPolicyVersion(int policyVersion) {
		this.policyVersion = policyVersion;
	}

	public Date getPolicyModifiedDate() {
		return policyModifiedDate;
	}

	public void setPolicyModifiedDate(Date policyModifiedDate) {
		this.policyModifiedDate = policyModifiedDate;
	}

	public Map<String, PolicyRuleDescriptor> getPolicyRulesMappedByParameterName() {
		return policyMappedByRuleName;
	}

	public void setPolicyRulesMappedByParameterName(
			Map<String, PolicyRuleDescriptor> policyRulesMappedByParameterName) {
		this.policyMappedByRuleName = policyRulesMappedByParameterName;
	}

	public List<PolicyRequiredMetadataValue> getPolicyRequiredMetadataValues() {
		return policyRequiredMetadataValues;
	}

	public void setPolicyRequiredMetadataValues(
			List<PolicyRequiredMetadataValue> policyRequiredMetadataValues) {
		this.policyRequiredMetadataValues = policyRequiredMetadataValues;
	}

	public String getPolicyStagingAreaAbsolutePath() {
		return policyStagingAreaAbsolutePath;
	}

	public void setPolicyStagingAreaAbsolutePath(
			String policyStagingAreaAbsolutePath) {
		this.policyStagingAreaAbsolutePath = policyStagingAreaAbsolutePath;
	}

	public boolean isRequireStaging() {
		return requireStaging;
	}

	public void setRequireStaging(boolean requireStaging) {
		this.requireStaging = requireStaging;
	}

	public boolean isRequireVirusScan() {
		return requireVirusScan;
	}

	public void setRequireVirusScan(boolean requireVirusScan) {
		this.requireVirusScan = requireVirusScan;
	}

	public boolean isRequireChecksum() {
		return requireChecksum;
	}

	public void setRequireChecksum(boolean requireChecksum) {
		this.requireChecksum = requireChecksum;
	}

	public int getNumberOfReplicas() {
		return numberOfReplicas;
	}

	public void setNumberOfReplicas(int numberOfReplicas) {
		this.numberOfReplicas = numberOfReplicas;
	}

	public Map<String, PolicyRuleDescriptor> getPolicyMappedByRuleName() {
		return policyMappedByRuleName;
	}

	public void setPolicyMappedByRuleName(
			Map<String, PolicyRuleDescriptor> policyMappedByRuleName) {
		this.policyMappedByRuleName = policyMappedByRuleName;
	}

	public String getRetentionDays() {
		return retentionDays;
	}

	public void setRetentionDays(String retentionDays) {
		this.retentionDays = retentionDays;
	}

}
