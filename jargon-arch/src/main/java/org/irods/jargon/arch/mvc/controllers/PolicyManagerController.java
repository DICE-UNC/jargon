/**
 * 
 */
package org.irods.jargon.arch.mvc.controllers;

import java.util.List;

import org.irods.jargon.arch.exception.ArchException;
import org.irods.jargon.arch.mvc.controllers.forms.PolicyForm;
import org.irods.jargon.arch.mvc.controllers.forms.PolicyFormValidator;
import org.irods.jargon.arch.mvc.controllers.forms.PolicyRepositoryForm;
import org.irods.jargon.arch.mvc.controllers.forms.PolicyRepositoryFormValidator;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.part.exception.DuplicateDataException;
import org.irods.jargon.part.policy.domain.Policy;
import org.irods.jargon.part.policy.xmlserialize.ObjectToXMLMarshaller;
import org.irods.jargon.part.policydriven.PolicyDrivenServiceConfigException;
import org.irods.jargon.part.policydriven.PolicyDrivenServiceListingEntry;
import org.irods.jargon.part.policydriven.PolicyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller for maintenance of policies
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */

@RequestMapping("/policyadmin/**")
@Controller
public final class PolicyManagerController extends AbstractArchController {

	public static Logger log = LoggerFactory.getLogger(PolicyManagerController.class);

	/**
	 * View page to view the policy directories
	 * 
	 * @return
	 * @throws JargonException
	 */
	@RequestMapping(value = "/policyadmin/policydir/view", method = RequestMethod.GET)
	public ModelAndView showOverallViewAction() throws ArchException {
		log.info("executing showOverallViewAction");
		checkControllerInjectedContracts();
		final ModelAndView mav = new ModelAndView();
		mav.setViewName("policyadmin_policydir_view");
		log.debug("returning mav from Controller for view: {}", mav
				.getViewName());
		return mav;
	}

	/**
	 * Handle ajax display listing available policy repositories
	 * 
	 * @return
	 * @throws JargonException
	 */
	@RequestMapping(value = "/policyadmin/policydir/ajax_dirs_list", method = RequestMethod.GET)
	public ModelAndView ajaxPolicyRepositoryList() throws ArchException {
		log.info("executing  ajaxPolicyRepositoryList");
		checkControllerInjectedContracts();
		final ModelAndView mav = new ModelAndView();
		mav.setViewName("ajax_policy_repository_list");

		try {
			final IRODSAccount irodsAccount = this.getAuthenticatedIRODSAccount();
			final PolicyManager policyManager = this.getArchServiceFactory()
					.instancePolicyManager(irodsAccount);
			final List<PolicyDrivenServiceListingEntry> policyRepositoryListings = policyManager
					.listPolicyRepositories();
			log
					.debug(
							"adding policy repositories as 'policyRepositoryListing': {}",
							policyRepositoryListings);
			mav.addObject("policyRepositoryListing", policyRepositoryListings);
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
	 * Handle ajax display listing policies in a given repository
	 * 
	 * @return
	 * @throws JargonException
	 */
	@RequestMapping(value = "/policyadmin/policydir/ajax_policy_dir_contents", method = RequestMethod.GET)
	public ModelAndView ajaxPolicyList(@RequestParam("policyName") final String policyName) throws ArchException {
		log.info("executing  ajaxPolicyRepositoryList");
		checkControllerInjectedContracts();
		final ModelAndView mav = new ModelAndView();
		mav.setViewName("ajax_policy_list");
		
		if (policyName == null || policyName.isEmpty()) {
			throw new ArchException("Error: no policy name was given");
		}

		try {
			final IRODSAccount irodsAccount = this.getAuthenticatedIRODSAccount();
			final PolicyManager policyManager = this.getArchServiceFactory()
					.instancePolicyManager(irodsAccount);
			final List<PolicyDrivenServiceListingEntry> policyListings = policyManager.listPoliciesInPolicyRepository(policyName);
			log
					.debug(
							"adding policy repositories as 'policyListing': {}",
							policyListings);
			mav.addObject("policyListing", policyListings);
		} catch (Exception e) {
			log.error("An error occurred getting a listing of policies", e);
			throw new ArchException(e);
		} finally {
			closeSessionIgnoringExceptions();
		}

		log.debug("returning mav from Controller for view: {}", mav
				.getViewName());
		return mav;
	}
	
	
	/**
	 * This method responds to a GET operation, and will initialize to add a
	 * policyRepository with a subsequent POST action.
	 * 
	 * @return
	 * @throws JargonException
	 */
	@RequestMapping(value = "/policyadmin/policydir/add", method = RequestMethod.GET)
	public ModelAndView addAction() throws ArchException {
		log.debug("executing  addAction, initialize policy add");
		checkControllerInjectedContracts();
		final ModelAndView mav = new ModelAndView();
		mav.setViewName("policyadmin_policydir_add");
		mav.addObject("repositoryForm", new PolicyRepositoryForm());
		log.debug("returning mav from Controller for view: {}", mav
				.getViewName());
		return mav;
	}

	/**
	 * Handle update when adding a policy
	 * 
	 * @return
	 * @throws JargonException
	 */
	@RequestMapping(value = "/policyadmin/policy/add", method = RequestMethod.POST)
	public ModelAndView addPolicyActionUpdateFromPost(
			@ModelAttribute("policyForm") final PolicyForm policyForm,
			final BindingResult result) throws ArchException {
		log.info("executing  addPolicyActionUpdateFromPost()");
		checkControllerInjectedContracts();
		ModelAndView mav = new ModelAndView();

		log.info("got form: {}", policyForm);

		log.debug("calling validate");
		final PolicyFormValidator policyFormValidator = new PolicyFormValidator();
		policyFormValidator.validate(policyForm, result);

		if (result.hasErrors()) {
			return initializePolicyForm(policyForm, mav);
		}

		try {
			log.info("add policy from form {}", policyForm.toString());
			processPolicyAdd(policyForm);
			StringBuilder redirectUrl = new StringBuilder();
			redirectUrl.append("redirect:/policyadmin/policy/view_policy_list?policyName=");
			redirectUrl.append(policyForm.getPolicyRepositoryName());
			mav.setViewName(redirectUrl.toString());
			// TODO: fix redirect to show policy in policy dir
			log.debug("success, redirect to list view");
		} catch (DuplicateDataException e) {
			log.error("duplicate data add", e);
			mav.addObject(ERROR_MESSAGE_MODEL,
					"duplicate repository exists for name or path");
			mav.setViewName("policyadmin_policydir_add");
		} catch (Exception e) {
			log.error("error adding policy repostiory", e);
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
	 * @param policyForm
	 * @param mav
	 * @throws JargonException
	 * @throws ArchException
	 * @throws PolicyDrivenServiceConfigException
	 */
	private ModelAndView initializePolicyForm(
			final PolicyForm policyForm, final ModelAndView mav) throws ArchException {

		log.debug("validate errors!");
		mav.setViewName("policyadmin_policy_add");
		mav.addObject("policyForm", policyForm);
		IRODSAccount irodsAccount;
		try {
			irodsAccount = this.getAuthenticatedIRODSAccount();
		} catch (JargonException e) {
			log.error("error getting irodsAccount", e);
			throw new ArchException(e);
		}
		log.debug("got irods account");
		final PolicyManager policyManager = this.getArchServiceFactory()
				.instancePolicyManager(irodsAccount);
		log.debug("have policyManager");
		List<PolicyDrivenServiceListingEntry> policyRepositoryListings;
		try {
			policyRepositoryListings = policyManager.listPolicyRepositories();
		} catch (PolicyDrivenServiceConfigException e) {
			log.error("error getting listing of policy repositories", e);
			throw new ArchException(e);
		}
		log.debug(
				"adding policy repositories as 'policyRepositoryListing': {}",
				policyRepositoryListings);
		mav.addObject("policyRepositoryListing", policyRepositoryListings);
		return mav;
	}

	private void processPolicyAdd(final PolicyForm policyForm) throws JargonException, PolicyDrivenServiceConfigException, DuplicateDataException, ArchException {
		final IRODSAccount irodsAccount = this.getAuthenticatedIRODSAccount();
		log.debug("got irods account");
		final PolicyManager policyManager = this
				.getArchServiceFactory()
				.instancePolicyManager(irodsAccount);
		log.debug("have policyManager");
		
		final Policy policy = new Policy();
		policy.setIrodsUserThatCreatedPolicy(irodsAccount.getUserName());
		policy.setPolicyName(policyForm.getPolicyName());
		policy.setPolicyTextualDescription(policyForm.getComment());
		policy.setRequireStaging(policyForm.isRequireStaging());
		policy.setRequireChecksum(policyForm.isRequireChecksum());
		policy.setRequireVirusScan(policyForm.isRequireVirusScan());
		policy.setNumberOfReplicas(policyForm.getNumberOfReplicas());
		policy.setRetentionDays(policyForm.getRetentionDays());
		
		log.debug("getting xml marshaller");
		final ObjectToXMLMarshaller marshaller = new ObjectToXMLMarshaller();
		
		log.debug("calling add of policy: {}", policy);		
		policyManager.addPolicyToRepository(policyForm.getPolicyRepositoryName(), policy, marshaller);
		log.info("policy successfully added");
	}

	/**
	 * Page to add/upload a policy
	 * 
	 * @return
	 * @throws JargonException
	 */
	@RequestMapping(value = "/policyadmin/policy/add", method = RequestMethod.GET)
	public ModelAndView addOrUploadPolicy() throws ArchException {
		log.info("executing addOrUploadPollicy");
		checkControllerInjectedContracts();
		final ModelAndView mav = new ModelAndView();
		mav.setViewName("policyadmin_policy_add");
		try {
			final IRODSAccount irodsAccount = this.getAuthenticatedIRODSAccount();
			final PolicyManager policyManager = this.getArchServiceFactory()
					.instancePolicyManager(irodsAccount);
			final List<PolicyDrivenServiceListingEntry> policyRepositoryListings = policyManager
					.listPolicyRepositories();
			final PolicyForm policyForm = new PolicyForm();
			log
					.debug(
							"adding policy repositories as 'policyRepositoryListing': {}",
							policyRepositoryListings);
			mav.addObject("policyRepositoryListing", policyRepositoryListings);
			mav.addObject("policyForm", policyForm);
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
	@RequestMapping(value = "/policyadmin/policydir/add", method = RequestMethod.POST)
	public ModelAndView addActionUpdateFromPost(
			@ModelAttribute("repositoryForm") final  PolicyRepositoryForm repositoryForm,
			final BindingResult result) throws ArchException {
		log.info("executing  addActionUpdateFromPost()");
		checkControllerInjectedContracts();
		final ModelAndView mav = new ModelAndView();

		log.info("got form: {}", repositoryForm);

		log.debug("calling validate");
		final PolicyRepositoryFormValidator policyRepositoryFormValidator = new PolicyRepositoryFormValidator();
		policyRepositoryFormValidator.validate(repositoryForm, result);

		if (result.hasErrors()) {
			log.debug("validate errors!");
			mav.setViewName("policyadmin_policydir_add");
			mav.addObject("repositoryForm", repositoryForm);
			return mav;
		}

		try {
			log.info("add policy repository");
			processPolicyDirAdd(repositoryForm);
			mav.setViewName("redirect:/policyadmin/policydir/view");
			log.debug("success, redirect to list view");
		} catch (DuplicateDataException e) {
			log.error("duplicate data add", e);
			mav.addObject(ERROR_MESSAGE_MODEL,
					"duplicate repository exists for name or path");
			mav.setViewName("policyadmin_policydir_add");
		} catch (Exception e) {
			log.error("error adding policy repostiory", e);
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
	 * View page to show policies by policy repository
	 * 
	 * @return
	 * @throws JargonException
	 */
	@RequestMapping(value = "/policyadmin/policy/view_policy_list", method = RequestMethod.GET)
	public ModelAndView showPolicyList() throws ArchException {
		log.info("executing showPolicyList");
		checkControllerInjectedContracts();
		final ModelAndView mav = new ModelAndView();
		mav.setViewName("policyadmin_policy_list_view");
		
		try {
			final IRODSAccount irodsAccount = this.getAuthenticatedIRODSAccount();
			final PolicyManager policyManager = this.getArchServiceFactory()
					.instancePolicyManager(irodsAccount);
			final List<PolicyDrivenServiceListingEntry> policyRepositoryListings = policyManager
					.listPolicyRepositories();
			log
					.debug(
							"adding policy repositories as 'policyRepositoryListing': {}",
							policyRepositoryListings);
			mav.addObject("policyRepositoryListing", policyRepositoryListings);
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
	
	private void processPolicyDirAdd(final PolicyRepositoryForm repositoryForm)
			throws JargonException, PolicyDrivenServiceConfigException,
			DuplicateDataException, ArchException {
		final IRODSAccount irodsAccount = this.getAuthenticatedIRODSAccount();
		log.debug("got irods account");
		PolicyManager policyManager = this.getArchServiceFactory()
				.instancePolicyManager(irodsAccount);
		log.debug("have policyManager");

		final PolicyDrivenServiceListingEntry policyDrivenServiceListingEntry = PolicyDrivenServiceListingEntry
				.instance(repositoryForm.getRepositoryName(), repositoryForm
						.getRepositoryPath(), repositoryForm.getComment());

		log.debug("calling add of repository: {}",
				policyDrivenServiceListingEntry);
		policyManager.addPolicyRepository(policyDrivenServiceListingEntry);
	}

}
