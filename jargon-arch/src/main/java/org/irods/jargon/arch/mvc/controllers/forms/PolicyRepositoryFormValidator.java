/**
 * TODO: factor out all of these repository forms into a common one
 */
package org.irods.jargon.arch.mvc.controllers.forms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validator for <code>PolicyRepositoryForm</code>
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class PolicyRepositoryFormValidator implements Validator {
	
	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Override
	public boolean supports(Class<?> clazz) {
		log.debug("checking if supports");
		return PolicyRepositoryForm.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		
		log.debug("in PolicyRepository Form validator");
		
		final PolicyRepositoryForm form = (PolicyRepositoryForm) target;
		
		if (form.getRepositoryName() == null || form.getRepositoryName().isEmpty()) {
		    errors.rejectValue("repositoryName", "error.field.required");
		}
		
		if (form.getRepositoryPath().trim().length() < 5) {
			errors.rejectValue("repositoryPath", "error.field.length");
		}
	       
		if (form.getRepositoryPath() == null || form.getRepositoryPath().isEmpty()) {
			errors.rejectValue("repositoryPath", "error.field.required");
		} else if (form.getRepositoryPath().trim().length() < 2) {
			errors.rejectValue("repositoryPath", "error.field.length");
		} else if (form.getRepositoryPath().charAt(0) != '/') {
			errors.rejectValue("repositoryPath", "error.path.not.absolute");
		}
		
	}
}
