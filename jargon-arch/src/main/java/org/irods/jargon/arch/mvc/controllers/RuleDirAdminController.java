/**
 * 
 */
package org.irods.jargon.arch.mvc.controllers;

import java.util.List;

import org.irods.jargon.arch.exception.ArchException;
import org.irods.jargon.arch.mvc.controllers.forms.RuleRepositoryForm;
import org.irods.jargon.arch.mvc.controllers.forms.RuleRepositoryFormValidator;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.part.exception.DuplicateDataException;
import org.irods.jargon.part.policydriven.PolicyDrivenRulesManager;
import org.irods.jargon.part.policydriven.PolicyDrivenServiceConfigException;
import org.irods.jargon.part.policydriven.PolicyDrivenServiceListingEntry;
import org.irods.jargon.part.policydriven.PolicyDrivenServiceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller for actions that manage rule collections
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */

@RequestMapping("/ruleadmin/ruledir/**")
@Controller
public final class RuleDirAdminController extends AbstractArchController {

	static Logger log = LoggerFactory.getLogger(RuleDirAdminController.class);

	/**
	 * View page to view the rule directories on the server.
	 * 
	 * @return
	 * @throws JargonException
	 */
	@RequestMapping(value = "/ruleadmin/ruledir/view", method = RequestMethod.GET)
	public ModelAndView showOverallViewAction() throws ArchException {
		log.info("executing showOverallViewAction");
		checkControllerInjectedContracts();
		final ModelAndView mav = new ModelAndView();
		mav.setViewName("ruleadmin_ruledir_view");

		try {
			final IRODSAccount irodsAccount = this.getAuthenticatedIRODSAccount();
			final PolicyDrivenRulesManager rulesManager = getArchServiceFactory().instancePolicyDrivenRulesManager(irodsAccount);
	
			final List<PolicyDrivenServiceListingEntry> serviceListings = rulesManager.findRuleRepositories();
			log.debug("adding service listings as 'serviceListing': {}",
					serviceListings);
			mav.addObject("serviceListing", serviceListings);
		} catch (JargonException e) {
			log.error("error in Jargon", e);
			throw new ArchException(e);
		} catch (PolicyDrivenServiceConfigException e) {
			log.error("error getting policyDrivenServiceManager", e);
			throw new ArchException(e);
		} finally {
			closeSessionIgnoringExceptions();
		}

		log.debug("returning mav from Controller for view: {}", mav
				.getViewName());
		return mav;
	}

	// FIXME: move to a controller with general lookup functions
	@RequestMapping(value = "/ruleadmin/ruledir/ajax_service_options", method = RequestMethod.GET)
	public ModelAndView buildAjaxServiceOptions() throws ArchException {
		log.info("executing buildAjaxServiceOptions");
		checkControllerInjectedContracts();
		final ModelAndView mav = new ModelAndView();
		mav.setViewName("ajax_select_services");

		try {
			IRODSAccount irodsAccount = this.getAuthenticatedIRODSAccount();
			PolicyDrivenServiceManager policyDrivenServiceManager = this
					.getArchServiceFactory()
					.instancePolicyDrivenServiceManager(irodsAccount);
			List<PolicyDrivenServiceListingEntry> serviceListings = policyDrivenServiceManager
					.findPolicyDrivenServiceNames(PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_MARKER_ATTRIBUTE);
			log.debug("adding service listings as 'serviceListing': {}",
					serviceListings);
			mav.addObject("serviceListing", serviceListings);
		} catch (JargonException e) {
			log.error("error in Jargon", e);
			throw new ArchException(e);
		} catch (PolicyDrivenServiceConfigException e) {
			log.error("error getting policyDrivenServiceManager", e);
			throw new ArchException(e);
		} finally {
			closeSessionIgnoringExceptions();
		}

		log.debug("returning mav from Controller for view: {}", mav
				.getViewName());
		return mav;
	}

	/**
	 * Handle ajax display listing available policy-driven services
	 * 
	 * @return
	 * @throws JargonException
	 */
	@RequestMapping(value = "/ruleadmin/ruledir/ajax_rule_dirs_list", method = RequestMethod.GET)
	public ModelAndView ajaxRuleRepositoryList() throws ArchException {
		log.info("executing  ajaxRuleRepositoryList");
		checkControllerInjectedContracts();
		ModelAndView mav = new ModelAndView();
		mav.setViewName("ajax_rule_repository_list");

		try {
			IRODSAccount irodsAccount = this.getAuthenticatedIRODSAccount();
			final PolicyDrivenRulesManager policyDrivenRulesManager = this
					.getArchServiceFactory().instancePolicyDrivenRulesManager(
							irodsAccount);
			final List<PolicyDrivenServiceListingEntry> serviceListings = policyDrivenRulesManager
					.findRuleRepositories();
			log.debug("adding ruler repositories as 'serviceListing': {}",
					serviceListings);
			mav.addObject("serviceListing", serviceListings);
		} catch (JargonException e) {
			log.error("error in Jargon", e);
			throw new ArchException(e);
		} catch (PolicyDrivenServiceConfigException e) {
			log.error("error getting policyDrivenServiceManager", e);
			throw new ArchException(e);
		} finally {
			closeSessionIgnoringExceptions();
		}

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
	@RequestMapping(value = "/ruleadmin/ruledir/add", method = RequestMethod.GET)
	public ModelAndView addAction() throws ArchException {
		log.debug("executing  addAction");
		checkControllerInjectedContracts();
		ModelAndView mav = new ModelAndView();
		mav.setViewName("ruleadmin_ruledir_add");
		mav.addObject("ruleRepositoryForm", new RuleRepositoryForm());
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
	@RequestMapping(value = "/ruleadmin/ruledir/add", method = RequestMethod.POST)
	public ModelAndView addActionUpdateFromPost(
			@ModelAttribute("ruleRepositoryForm") RuleRepositoryForm ruleRepositoryForm, BindingResult result)
			throws ArchException {
		log.info("executing  addActionUpdateFromPost()");
		checkControllerInjectedContracts();
		ModelAndView mav = new ModelAndView();

		log.info("got form: {}", ruleRepositoryForm);

		log.debug("calling validate");
		RuleRepositoryFormValidator ruleRepositoryFormValidator = new RuleRepositoryFormValidator();
		ruleRepositoryFormValidator.validate(ruleRepositoryForm, result);
		
		if (result.hasErrors()) {
			log.debug("validate errors!");
			mav.setViewName("ruleadmin_ruledir_add");
			mav.addObject("ruleRepositoryForm", ruleRepositoryForm);
			return mav;
		} 

		try {
			log.info("add rule repository");
			processRuleDirAdd(ruleRepositoryForm);
			mav.setViewName("redirect:/ruleadmin/ruledir/view");
			log.debug("success, redirect to list view");
		} catch (DuplicateDataException e) {
			log.error("duplicate data add", e);
			mav.addObject(ERROR_MESSAGE_MODEL, "duplicate repository exists for name or path");
			mav.setViewName("ruleadmin_ruledir_add");
		} catch (Exception e) {
			log.error("error adding rule repostiory", e);
			throw new ArchException(e);
		} finally {
			closeSessionIgnoringExceptions();
		}
		
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
		 * @throws DuplicateDataException 
		 */
		private void processRuleDirAdd(RuleRepositoryForm ruleRepositoryForm) throws JargonException,
				ArchException, PolicyDrivenServiceConfigException, DuplicateDataException {
			IRODSAccount irodsAccount = this.getAuthenticatedIRODSAccount();
			log.debug("got irods account");
			PolicyDrivenRulesManager policyDrivenRulesManager = this
					.getArchServiceFactory()
					.instancePolicyDrivenRulesManager(irodsAccount);
			log.debug("have policyDrivenRulesManager");
			
			PolicyDrivenServiceListingEntry policyDrivenServiceListingEntry = PolicyDrivenServiceListingEntry.instance(
					ruleRepositoryForm.getRuleRepositoryName(), ruleRepositoryForm.getRuleRepositoryPath(), ruleRepositoryForm.getComment());
		
			log.debug("calling add of repository: {}", policyDrivenServiceListingEntry);
			policyDrivenRulesManager.addRuleRepository(policyDrivenServiceListingEntry);
		}
		
	}

