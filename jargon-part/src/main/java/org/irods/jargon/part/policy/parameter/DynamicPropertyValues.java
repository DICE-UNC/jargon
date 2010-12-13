package org.irods.jargon.part.policy.parameter;

import java.util.List;

import org.irods.jargon.part.exception.PartException;

/**
 * Describes property choices that were dynamically derived from iRODS.  This is an immutable class.
 * @author Mike Conway - DICE (www.irods.org)
 */
public class DynamicPropertyValues {

		private final String dynamicPropertyType;
		private final List<String> dynamicProperties;
		
		/**
		 * Static initializer creates an instance.
		 * @param dynamicPropertyType <code>String</code> with the property type that generated these values
		 * @param dynamicProperties <code>List<String></code> with the returned property values.
		 * @return <code>DynamicPropertyValues</code> instance.
		 * @throws PartException
		 */
		public static DynamicPropertyValues instance(final String dynamicPropertyType, final List<String> dynamicProperties) throws PartException {
			return new DynamicPropertyValues(dynamicPropertyType, dynamicProperties);
		}
		
		DynamicPropertyValues(final String dynamicPropertyType, final List<String> dynamicProperties) throws PartException {
			
			if (dynamicPropertyType == null || dynamicPropertyType.isEmpty()) {
				throw new PartException("null or empty dynamicPropertyType");
			}
			
			if (dynamicProperties == null) {
				throw new PartException("null dynamicPropertyType");
			}
			
			this.dynamicPropertyType = dynamicPropertyType;
			this.dynamicProperties = dynamicProperties;
			
		}
	
		public String getDynamicPropertyType() {
			return dynamicPropertyType;
		}
		public List<String> getDynamicProperties() {
			return dynamicProperties;
		}
	
}
