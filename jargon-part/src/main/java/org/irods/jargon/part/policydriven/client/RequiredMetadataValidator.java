package org.irods.jargon.part.policydriven.client;

import org.irods.jargon.part.policy.domain.PolicyRequiredMetadataValue;
import org.irods.jargon.part.policy.domain.PolicyRequiredMetadataValue.MetadataType;
import org.irods.jargon.part.policydriven.client.exception.ValidationException;

/**
 * Methods to validate entered data versus the required metadata value as
 * reflected in the policy, returning the correctly parsed data value.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class RequiredMetadataValidator {

	private RequiredMetadataValidator() {

	}
	
	public static Object validateAgainstPolicy(final String value,
			final PolicyRequiredMetadataValue policyRequiredMetadataValue)
			throws ValidationException {
		
		if (policyRequiredMetadataValue.getMetadataType() == MetadataType.LITERAL_BOOLEAN) {
			return validateBooleanAgainstPolicy(value, policyRequiredMetadataValue);
		} else if (policyRequiredMetadataValue.getMetadataType() == MetadataType.LITERAL_NUMBER) {
			return validateNumberAgainstPolicy(value, policyRequiredMetadataValue);
		} else if (policyRequiredMetadataValue.getMetadataType() == MetadataType.LITERAL_DECIMAL) {
			return validateDecimalAgainstPolicy(value, policyRequiredMetadataValue);
		} else if (policyRequiredMetadataValue.getMetadataType() == MetadataType.LITERAL_STRING) {
			return validateStringAgainstPolicy(value, policyRequiredMetadataValue);
		} else {
			throw new ValidationException("unknown metadata type, cannot validate:" + policyRequiredMetadataValue);
		}
		
	}

	/**
	 * Validate the given value agaist the policy, returning a String
	 * @param value <code>String</code> value that will be validated by the policy
	 * @param policyRequiredMetadataValue {@link org.irods.jargon.part.policy.domain.PolicyRequiredMetadataValue} that contains
	 * the validation and type information.
	 * @return <code>String</code> with the validated value
	 * @throws ValidationException
	 */
	private static String validateStringAgainstPolicy(final String value,
			final PolicyRequiredMetadataValue policyRequiredMetadataValue)
			throws ValidationException {
		
		if (value == null) {
			throw new ValidationException("value is null");
		}

		if (policyRequiredMetadataValue == null) {
			throw new ValidationException("policyRequiredMetadataValue is null");
		}
		
		if (policyRequiredMetadataValue.getMetadataType() != MetadataType.LITERAL_STRING) {
			throw new ValidationException("This test is for a string, but the metadata type was:" + policyRequiredMetadataValue.getMetadataType());
		}

		// if not required, just accept the data as is
		if (!policyRequiredMetadataValue.isRequired()) {
			return value;
		}

		if (value.isEmpty()) {
			throw new ValidationException("required value was missing");
		}

		return value;

	}

	/**
	 * Validate the given value as an Integer.
	 * @param value <code>String</code> value that will be validated by the policy
	 * @param policyRequiredMetadataValue {@link org.irods.jargon.part.policy.domain.PolicyRequiredMetadataValue} that contains
	 * the validation and type information.
	 * @return <code>Integer</code> with the validated value, or null if no data was provided.
	 * @throws ValidationException
	 */
	private static Integer validateNumberAgainstPolicy(final String value,
			final PolicyRequiredMetadataValue policyRequiredMetadataValue)
			throws ValidationException {
		if (value == null) {
			throw new ValidationException("value is null");
		}

		if (policyRequiredMetadataValue == null) {
			throw new ValidationException("policyRequiredMetadataValue is null");
		}

		if (policyRequiredMetadataValue.getMetadataType() != MetadataType.LITERAL_NUMBER) {
			throw new ValidationException("This test is for a number, but the metadata type was:" + policyRequiredMetadataValue.getMetadataType());
		}
		
		if (policyRequiredMetadataValue.isRequired() && value.isEmpty()) {
			throw new ValidationException("required value was missing");
		}

		try {
			int intVal = Integer.parseInt(value);
			return new Integer(intVal);
		} catch (Exception e) {
			throw new ValidationException("non-numeric data entered");
		}

	}
	
	/**
	 * Validate the given value as a Float.
	 * @param value <code>String</code> value that will be validated by the policy
	 * @param policyRequiredMetadataValue {@link org.irods.jargon.part.policy.domain.PolicyRequiredMetadataValue} that contains
	 * the validation and type information.
	 * @return <code>Float</code> with the validated value, or null if no data was provided.
	 * @throws ValidationException
	 */
	private static Float validateDecimalAgainstPolicy(final String value,
			final PolicyRequiredMetadataValue policyRequiredMetadataValue)
			throws ValidationException {
		
		if (value == null) {
			throw new ValidationException("value is null");
		}

		if (policyRequiredMetadataValue == null) {
			throw new ValidationException("policyRequiredMetadataValue is null");
		}
		
		if (policyRequiredMetadataValue.getMetadataType() != MetadataType.LITERAL_DECIMAL) {
			throw new ValidationException("This test is for a decimal, but the metadata type was:" + policyRequiredMetadataValue.getMetadataType());
		}

		if (policyRequiredMetadataValue.isRequired() && value.isEmpty()) {
			throw new ValidationException("required value was missing");
		}
		
		try {
			float floatVal = Float.parseFloat(value);
			return new Float(floatVal);
		} catch (Exception e) {
			throw new ValidationException("non-numeric data entered");
		}

	}
	
	/**
	 * Validate the given value as a Boolean.  If the value passed is equal to (ignoring case) the literal <code>true</code>, then
	 * the Boolean value will be true, otherwise, it will be false.
	 * @param value <code>String</code> value that will be validated by the policy
	 * @param policyRequiredMetadataValue {@link org.irods.jargon.part.policy.domain.PolicyRequiredMetadataValue} that contains
	 * the validation and type information.
	 * @return <code>Float</code> with the validated value, or null if no data was provided.
	 * @throws ValidationException
	 */
	private static Boolean validateBooleanAgainstPolicy(final String value,
			final PolicyRequiredMetadataValue policyRequiredMetadataValue)
			throws ValidationException {
		
		if (value == null) {
			throw new ValidationException("value is null");
		}

		if (policyRequiredMetadataValue == null) {
			throw new ValidationException("policyRequiredMetadataValue is null");
		}
		
		if (policyRequiredMetadataValue.getMetadataType() != MetadataType.LITERAL_BOOLEAN) {
			throw new ValidationException("This test is for a boolean, but the metadata type was:" + policyRequiredMetadataValue.getMetadataType());
		}

		if (policyRequiredMetadataValue.isRequired() && value.isEmpty()) {
			throw new ValidationException("required value was missing");
		}
		
		try {
			return new Boolean(value);
		} catch (Exception e) {
			throw new ValidationException("invalid data entered, could not convert to a boolean value");
		}

	}

}
