/**
 * 
 */
package org.irods.jargon.part.policy.domain;

/**
 * Describes metadata required at ingest time that will be enforced by the
 * containing <code>Policy</code>
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class PolicyRequiredMetadataValue {

	/*
	 * Type information that assists in translation of the parameter value into
	 * an interface element
	 */
	public enum MetadataType {
		LITERAL_NUMBER, LITERAL_STRING, LITERAL_DECIMAL, LITERAL_BOOLEAN, SYMBOLIC_PARAMETER
	}

	/*
	 * Identifying name of the metadata value that will be stored in the target
	 * object
	 */
	private String metadataAttribute = "";
	
	/*
	 * Additional data that can help resolve the source of the metadata. This is used to map symbolic parameters to their resolvers
	 */
	/**
	 * 
	 */
	private String metadataSource = "";
	
	/*
	 * Relates metadata elements that may be activated by the policy.  The default is blank, and these 
	 * metadata elements are shown by default.  iRODS may activate other metadata groups based on expectations
	 * derived for the client. 
	 */
	private String metadataGroup = "";

	/*
	 * Units the metadata will be expressed in.
	 */
	private String units = "";

	/*
	 * Type information for validation and generation
	 */
	MetadataType metadataType;

	/*
	 * Is this an optional or required metadata field?
	 */
	private boolean required = false;
	
	/*
	 * Was this parameter dynamically created by a rule?
	 */
	private boolean generatedByRule = false;

	/*
	 * A suitable prompt to present at entry time for this metadata value
	 */
	private String metaDataPromptAsText = "";

	/*
	 * A suitable prompt to present at entry time for the metadata, as a
	 * property, for display in an alternative language
	 */
	private String metaDataPromptAsAni18nProperty = "";

	public String getMetadataAttribute() {
		return metadataAttribute;
	}

	public void setMetadataAttribute(String metadataAttribute) {
		this.metadataAttribute = metadataAttribute;
	}

	public String getUnits() {
		return units;
	}

	public void setUnits(String units) {
		this.units = units;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public String getMetaDataPromptAsText() {
		return metaDataPromptAsText;
	}

	public void setMetaDataPromptAsText(String metaDataPromptAsText) {
		this.metaDataPromptAsText = metaDataPromptAsText;
	}

	public String getMetaDataPromptAsAni18nProperty() {
		return metaDataPromptAsAni18nProperty;
	}

	public void setMetaDataPromptAsAni18nProperty(
			String metaDataPromptAsAni18nProperty) {
		this.metaDataPromptAsAni18nProperty = metaDataPromptAsAni18nProperty;
	}

	public MetadataType getMetadataType() {
		return metadataType;
	}

	public void setMetadataType(MetadataType metadataType) {
		this.metadataType = metadataType;
	}

	public boolean isGeneratedByRule() {
		return generatedByRule;
	}

	public void setGeneratedByRule(boolean generatedByRule) {
		this.generatedByRule = generatedByRule;
	}

	/**
	 * @param metadataGroup the metadataGroup to set
	 */
	public void setMetadataGroup(String metadataGroup) {
		this.metadataGroup = metadataGroup;
	}

	/**
	 * @return the metadataGroup
	 */
	public String getMetadataGroup() {
		return metadataGroup;
	}

	public String getMetadataSource() {
		return metadataSource;
	}

	public void setMetadataSource(String metadataSource) {
		this.metadataSource = metadataSource;
	}

}
