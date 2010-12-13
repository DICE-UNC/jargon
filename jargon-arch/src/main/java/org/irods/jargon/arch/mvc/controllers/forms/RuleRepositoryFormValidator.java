/**
 * TODO: factor out all of these repository forms into a common one
 */
package org.irods.jargon.arch.mvc.controllers.forms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validator for <code>RuleRepositoryForm</code>
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class RuleRepositoryFormValidator implements Validator {
	
	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Override
	public boolean supports(Class<?> clazz) {
		log.debug("checking if supports");
		return RuleRepositoryForm.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		
		log.debug("in RuleRepository Form validator");
		
		final RuleRepositoryForm form = (RuleRepositoryForm) target;
		
		if (form.getRuleRepositoryName() == null || form.getRuleRepositoryName().isEmpty()) {
		    errors.rejectValue("ruleRepositoryName", "error.field.required");
		}
		
		if (form.getRuleRepositoryPath().trim().length() < 5) {
			errors.rejectValue("ruleRepositoryPath", "error.field.length");
		}
	       
		if (form.getRuleRepositoryPath() == null || form.getRuleRepositoryPath().isEmpty()) {
			errors.rejectValue("ruleRepositoryPath", "error.field.required");
		} else if (form.getRuleRepositoryPath().trim().length() < 2) {
			errors.rejectValue("ruleRepositoryPath", "error.field.length");
		} else if (form.getRuleRepositoryPath().charAt(0) != '/') {
			errors.rejectValue("ruleRepositoryPath", "error.path.not.absolute");
		}
		
	}
}
