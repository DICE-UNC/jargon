/**
 * 
 */
package org.irods.jargon.arch.mvc.controllers.forms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validator for <code>PolicyDrivenServiceForm</code>
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class PolicyDrivenServiceFormValidator implements Validator {
	
	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Override
	public boolean supports(Class<?> clazz) {
		log.debug("checking if supports");
		return PolicyDrivenServiceForm.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		
		log.debug("in PolicyDrivenServiceForm validator");
		
		final PolicyDrivenServiceForm form = (PolicyDrivenServiceForm) target;
		
		if (form.getServiceName() == null || form.getServiceName().isEmpty()) {
		    errors.rejectValue("serviceName", "error.field.required");
		}
		
		if (form.getServiceName().trim().length() < 5) {
			errors.rejectValue("serviceName", "error.field.length");
		}
	       
		if (form.getServiceRootPath() == null || form.getServiceRootPath().isEmpty()) {
			errors.rejectValue("serviceRootPath", "error.field.required");
		} else if (form.getServiceRootPath().trim().length() < 2) {
			errors.rejectValue("serviceRootPath", "error.field.length");
		} else if (form.getServiceRootPath().charAt(0) != '/') {
			errors.rejectValue("serviceRootPath", "error.path.not.absolute");
		}
		
	}
}
