/**
 * 
 */
package org.irods.jargon.arch.mvc.controllers;

import java.util.List;

import org.irods.jargon.arch.exception.ArchException;
import org.irods.jargon.arch.mvc.controllers.forms.PolicyDrivenServiceForm;
import org.irods.jargon.arch.mvc.controllers.forms.PolicyDrivenServiceFormValidator;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.part.policydriven.PolicyDrivenServiceConfig;
import org.irods.jargon.part.policydriven.PolicyDrivenServiceConfigException;
import org.irods.jargon.part.policydriven.PolicyDrivenServiceListingEntry;
import org.irods.jargon.part.policydriven.PolicyDrivenServiceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller for functions that list and manage policy driven services.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */

@RequestMapping("/appladmin/**")
@Controller
public final class PolicyDrivenServiceManagementController extends
		AbstractArchController {

	@Qualifier("policyDrivenServiceFormValidator")
	private Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * Handle general display of policy driven applications
	 * 
	 * @return
	 * @throws JargonException
	 */
	@RequestMapping(value = "/appladmin/view", method = RequestMethod.GET)
	public ModelAndView showOverallViewAction() throws ArchException {
		log.info("executing showOverallViewAction");
		checkControllerInjectedContracts();
		ModelAndView mav = new ModelAndView();
		mav.setViewName("appladmin_view");

		log.debug("returning mav from Controller for view: {}", mav
				.getViewName());
		return mav;
	}

	/**
	 * Handle add policy driven applications
	 * 
	 * @return
	 * @throws JargonException
	 */
	@RequestMapping(value = "/appladmin/add", method = RequestMethod.GET)
	public ModelAndView addAction() throws ArchException {
		log.debug("executing  addAction");
		checkControllerInjectedContracts();
		ModelAndView mav = new ModelAndView();
		mav.setViewName("appladmin_add");
		mav.addObject("policyDrivenServiceForm", new PolicyDrivenServiceForm());
		log.debug("returning mav from Controller for view: {}", mav
				.getViewName());
		return mav;
	}

	/**
	 * Handle add policy driven applications
	 * 
	 * @return
	 * @throws JargonException
	 */
	@RequestMapping(value = "/appladmin/add", method = RequestMethod.POST)
	public ModelAndView addActionUpdateFromPost(
			@ModelAttribute("policyDrivenServiceForm") PolicyDrivenServiceForm policyDrivenServiceForm, BindingResult result)
			throws ArchException {
		log.info("executing  addActionUpdateFromPost()");
		checkControllerInjectedContracts();
		ModelAndView mav = new ModelAndView();

		log.info("got form: {}", policyDrivenServiceForm);

		log.debug("calling validate");
		PolicyDrivenServiceFormValidator policyDrivenServiceFormValidator = new PolicyDrivenServiceFormValidator();
		policyDrivenServiceFormValidator.validate(policyDrivenServiceForm, result);
		
		if (result.hasErrors()) {
			log.debug("validate errors!");
			mav.setViewName("appladmin_add");
			mav.addObject("policyDrivenServiceForm", new PolicyDrivenServiceForm());
			return mav;
		} 

		try {
			processPolicyAdd(policyDrivenServiceForm);
		} catch (JargonException e) {
			log.error("error in Jargon", e);
			throw new ArchException(e);
		} catch (PolicyDrivenServiceConfigException e) {
			log.error("error getting policyDrivenServiceManager", e);
			throw new ArchException(e);
		} finally {
			try {
				this.getIrodsAccessObjectFactory().closeSession();
			} catch (JargonException e) {
				log
						.error(
								"jargon exception on session close, logged and discarded",
								e);
			}
		}
		
		mav.setViewName("redirect:/appladmin/view");
		log.debug("returning mav from Controller for view: {}", mav
				.getViewName());
		log.debug("return model: {}", mav.getModelMap());
		
		return mav;
	}

	/**
	 * @param mav
	 * @throws JargonException
	 * @throws ArchException
	 * @throws PolicyDrivenServiceConfigException
	 */
	private void processPolicyAdd(final PolicyDrivenServiceForm policyDrivenServiceForm) throws JargonException,
			ArchException, PolicyDrivenServiceConfigException {
		IRODSAccount irodsAccount = this.getAuthenticatedIRODSAccount();
		log.debug("got irods account");
		PolicyDrivenServiceManager policyDrivenServiceManager = this
				.getArchServiceFactory()
				.instancePolicyDrivenServiceManager(irodsAccount);
		log.debug("have policyDrivenServiceManager");
		
		PolicyDrivenServiceConfig policyDrivenServiceConfig = new PolicyDrivenServiceConfig();
		policyDrivenServiceConfig.setServiceName(policyDrivenServiceForm.getServiceName());
		policyDrivenServiceConfig.setServiceRootPath(policyDrivenServiceForm.getServiceRootPath());
		policyDrivenServiceConfig.setServiceDescription(policyDrivenServiceForm.getComment());
		
		log.debug("calling add of config: {}", policyDrivenServiceConfig);
		policyDrivenServiceManager.addPolicyDrivenService(policyDrivenServiceConfig);

	}

	/**
	 * Handle ajax display listing available policy-driven services
	 * 
	 * @return
	 * @throws JargonException
	 */
	@RequestMapping(value = "/appladmin/ajax_service_driven_apps_list", method = RequestMethod.GET)
	public ModelAndView ajaxServiceDrivenAppsList() throws ArchException {
		log.info("executing  ajaxServiceDrivenAppsLit");
		checkControllerInjectedContracts();
		ModelAndView mav = new ModelAndView();
		mav.setViewName("ajax_service_driven_apps_list");

		try {
			IRODSAccount irodsAccount = this.getAuthenticatedIRODSAccount();
			log.debug("got irods account");
			PolicyDrivenServiceManager policyDrivenServiceManager = this.getArchServiceFactory().instancePolicyDrivenServiceManager(irodsAccount);
			log.debug("have policyDrivenServiceManager");
			List<PolicyDrivenServiceListingEntry> serviceListings = policyDrivenServiceManager.findPolicyDrivenServiceNames(PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_MARKER_ATTRIBUTE);
			log.debug("adding service listings as 'serviceListing': {}", serviceListings);
			mav.addObject("serviceListing", serviceListings);		} catch (JargonException e) {
			log.error("error in Jargon", e);
			throw new ArchException(e);
		} catch (PolicyDrivenServiceConfigException e) {
			log.error("error getting policyDrivenServiceManager", e);
			throw new ArchException(e);
		} finally {
			try {
				this.getIrodsAccessObjectFactory().closeSession();
			} catch (JargonException e) {
				log
						.error(
								"jargon exception on session close, logged and discarded",
								e);
			}
		}

		log.debug("returning mav from Controller for view: {}", mav
				.getViewName());
		return mav;
	}

}
