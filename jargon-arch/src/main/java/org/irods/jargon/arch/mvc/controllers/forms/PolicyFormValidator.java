/**
 * TODO: factor out all of these repository forms into a common one
 */
package org.irods.jargon.arch.mvc.controllers.forms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validator for <code>PolicyForm</code>
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class PolicyFormValidator implements Validator {
	
	private static final Logger log = LoggerFactory.getLogger(PolicyFormValidator.class);

	@Override
	public boolean supports(Class<?> clazz) {
		log.debug("checking if supports");
		return PolicyForm.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		
		log.debug("in Policy Form validator");
		
		final PolicyForm form = (PolicyForm) target;
		
		if (form.getPolicyName() == null || form.getPolicyName().isEmpty()) {
		    errors.rejectValue("policyName", "error.field.required");
		}
		
		if (form.getPolicyRepositoryName() == null || form.getPolicyRepositoryName().isEmpty()) {
		    errors.rejectValue("policyRepositoryName", "error.field.required");
		}
		
	}
}
