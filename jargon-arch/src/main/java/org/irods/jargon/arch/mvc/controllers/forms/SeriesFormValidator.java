package org.irods.jargon.arch.mvc.controllers.forms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validator for <code>SeriesForm</code>
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class SeriesFormValidator implements Validator {
	
	private static final Logger log = LoggerFactory.getLogger(SeriesFormValidator.class);

	@Override
	public boolean supports(Class<?> clazz) {
		log.debug("checking if supports");
		return SeriesForm.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		
		log.debug("in Series Form validator");
		
		final SeriesForm form = (SeriesForm) target;
		
		if (form.getName() == null || form.getName().isEmpty()) {
		    errors.rejectValue("name", "error.field.required");
		}
		
		if (form.getCollectionAbsolutePath() == null || form.getCollectionAbsolutePath().isEmpty()) {
		    errors.rejectValue("collectionAbsolutePath", "error.field.required");
		}
		
		if (form.getContainingServiceName() == null || form.getContainingServiceName().isEmpty()) {
		    errors.rejectValue("containingServiceName", "error.field.required");
		}
		
		if (form.getBoundPolicyName() == null || form.getBoundPolicyName().isEmpty()) {
		    errors.rejectValue("boundPolicyName", "error.field.required");
		}
		
		
	}
}
