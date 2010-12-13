/**
 * 
 */
package org.irods.jargon.arch.mvc.controllers;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.arch.exception.ArchException;
import org.irods.jargon.arch.mvc.controllers.forms.PolicyForm;
import org.irods.jargon.arch.mvc.controllers.forms.PolicyFormValidator;
import org.irods.jargon.arch.mvc.controllers.forms.PolicyRepositoryForm;
import org.irods.jargon.arch.mvc.controllers.forms.SeriesForm;
import org.irods.jargon.arch.mvc.controllers.forms.SeriesFormValidator;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.part.exception.DuplicateDataException;
import org.irods.jargon.part.policy.domain.Series;
import org.irods.jargon.part.policy.xmlserialize.ObjectToXMLMarshaller;
import org.irods.jargon.part.policy.xmlserialize.XMLToObjectUnmarshaller;
import org.irods.jargon.part.policydriven.PolicyDrivenServiceConfigException;
import org.irods.jargon.part.policydriven.PolicyDrivenServiceListingEntry;
import org.irods.jargon.part.policydriven.PolicyDrivenServiceManager;
import org.irods.jargon.part.policydriven.PolicyManager;
import org.irods.jargon.part.policydriven.SeriesManager;
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
 * Controller for management of a series
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
@RequestMapping("/seriesadmin/**")
@Controller
public final class SeriesController extends AbstractArchController {

	public static Logger log = LoggerFactory.getLogger(SeriesController.class);

	/**
	 * View page to view the series
	 * 
	 * @return
	 * @throws JargonException
	 */
	@RequestMapping(value = "/seriesadmin/seriesdir/view", method = RequestMethod.GET)
	public ModelAndView showSeriesInAppView() throws ArchException {
		log.info("executing showSeriesInAppView");
		checkControllerInjectedContracts();
		final ModelAndView mav = new ModelAndView();
		mav.setViewName("seriesadmin_seriesdir_view");

		IRODSAccount irodsAccount;
		try {
			irodsAccount = this.getAuthenticatedIRODSAccount();
		} catch (JargonException e) {
			log.error("error getting irodsAccount", e);
			throw new ArchException(e);
		}
		log.debug("got irods account");

		// get a list of policy-driven apps
		PolicyDrivenServiceManager policyDrivenServiceManager = getArchServiceFactory()
				.instancePolicyDrivenServiceManager(irodsAccount);

		log
				.debug("have policyDrivenServiceManager, get a list of policy-driven services");

		try {
			List<PolicyDrivenServiceListingEntry> policyServiceListings = policyDrivenServiceManager
					.findPolicyDrivenServiceNames(PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_MARKER_ATTRIBUTE);
			mav.addObject("policyDrivenServiceList", policyServiceListings);
		} catch (PolicyDrivenServiceConfigException e) {
			log.error("error getting listing of policy repositories", e);
			throw new ArchException(e);
		}

		log.debug("returning mav from Controller for view: {}", mav
				.getViewName());
		return mav;
	}

	/**
	 * View series in a service-driven application
	 * 
	 * @return
	 * @throws JargonException
	 */
	@RequestMapping(value = "/seriesadmin/seriesdir/ajax_series_dir_contents", method = RequestMethod.GET)
	public ModelAndView ajaxSeriesDirContents(
			@RequestParam("policyDrivenServiceName") final String policyDrivenServiceName)
			throws ArchException {
		log.info("executing ajaxSeriesDirContents");
		checkControllerInjectedContracts();
		final ModelAndView mav = new ModelAndView();
		mav.setViewName("ajax_series_dir_contents");

		if (policyDrivenServiceName == null
				|| policyDrivenServiceName.isEmpty()) {
			log.error("no policyDrivenServiceName provided");
			throw new ArchException("A policy-driven service name is missing");
		}

		IRODSAccount irodsAccount;
		try {
			irodsAccount = this.getAuthenticatedIRODSAccount();
		} catch (JargonException e) {
			log.error("error getting irodsAccount", e);
			throw new ArchException(e);
		}
		log.debug("got irods account");

		// get a list of policy-driven apps
		SeriesManager seriesManager = getArchServiceFactory()
				.instanceSeriesManager(irodsAccount);

		log
				.debug("have policyDrivenServiceManager, get a list of policy-driven services");

		try {
			List<PolicyDrivenServiceListingEntry> seriesListing = seriesManager
					.listSeries(policyDrivenServiceName);
			mav.addObject("seriesList", seriesListing);
		} catch (PolicyDrivenServiceConfigException e) {
			log.error("error getting listing of series", e);
			throw new ArchException(e);
		}

		log.debug("returning mav from Controller for view: {}", mav
				.getViewName());
		return mav;
	}

	/**
	 * This method responds to a GET operation, and will initialize to add a
	 * series by creating and pre-populating reference data for the
	 * <code>SeriesForm</code>
	 * 
	 * @return
	 * @throws JargonException
	 */
	@RequestMapping(value = "/seriesadmin/seriesdir/add", method = RequestMethod.GET)
	public ModelAndView addSeriesNewAction() throws ArchException {
		log.debug("executing  addSeriesNewAction");
		checkControllerInjectedContracts();
		final ModelAndView mav = new ModelAndView();
		mav.setViewName("seriesadmin_series_dir_add");
		mav.addObject("seriesForm", new SeriesForm());
		log
				.debug("have policyDrivenServiceManager, get a list of policy-driven services");

		IRODSAccount irodsAccount;
		try {
			irodsAccount = this.getAuthenticatedIRODSAccount();
		} catch (JargonException e) {
			log.error("error getting irodsAccount", e);
			throw new ArchException(e);
		}
		log.debug("got irods account");

		// get a listing of policy-driven services and add to the model
		
		addPolicyListToMav(mav, irodsAccount);

		log.debug("returning mav from Controller for view: {}", mav
				.getViewName());
		return mav;
		
	}

	/**
	 * @param mav
	 * @param irodsAccount
	 * @throws ArchException
	 */
	private void addPolicyListToMav(final ModelAndView mav,
			IRODSAccount irodsAccount) throws ArchException {
		PolicyDrivenServiceManager policyDrivenServiceManager = getArchServiceFactory()
				.instancePolicyDrivenServiceManager(irodsAccount);

		try {
			List<PolicyDrivenServiceListingEntry> serviceListings = policyDrivenServiceManager
					.findPolicyDrivenServiceNames(PolicyDrivenServiceManager.POLICY_DRIVEN_SERVICE_MARKER_ATTRIBUTE);
			mav.addObject("policyDrivenServiceList", serviceListings);
		} catch (PolicyDrivenServiceConfigException e) {
			log.error("error getting listing of policy repositories", e);
			throw new ArchException(e);
		}
		
		// add an initally empty listing of policies, this will be modified via ajax when a service is selected
		List<PolicyDrivenServiceListingEntry> policyListing = new ArrayList<PolicyDrivenServiceListingEntry>();
		mav.addObject("policyList", policyListing);
	}
	
	@RequestMapping(value = "/seriesadmin/seriesdir/ajax_policy_options_for_service", method = RequestMethod.GET)
	public ModelAndView ajaxGetPolicyOptionsForService(@RequestParam("policyDrivenServiceName") final String policyDrivenServiceName) throws ArchException {
		log.info("executing ajaxGetPolicyOptionsForService");
		
		if (policyDrivenServiceName == null || policyDrivenServiceName.isEmpty()) {
			throw new ArchException("A service name must be provided");
		}
		
		checkControllerInjectedContracts();
		final ModelAndView mav = new ModelAndView();
		mav.setViewName("seriesadmin_seriesdir_policy_options_for_service");

		IRODSAccount irodsAccount;
		try {
			irodsAccount = this.getAuthenticatedIRODSAccount();
		} catch (JargonException e) {
			log.error("error getting irodsAccount", e);
			throw new ArchException(e);
		}
		log.debug("got irods account");

		// get a list of policies for the application
		PolicyManager policyManager = getArchServiceFactory()
				.instancePolicyManager(irodsAccount);
		
		// FIXME: test shim just gets all policies, need to be able to discriminate

		log
				.debug("have policyManager, get a list of policies for service: {}", policyDrivenServiceName);

		try {
			List<PolicyDrivenServiceListingEntry> policyServiceListings = policyManager.listAllPolicies();
			mav.addObject("policyList", policyServiceListings);
		} catch (PolicyDrivenServiceConfigException e) {
			log.error("error getting listing of policy repositories", e);
			throw new ArchException(e);
		}

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
	@RequestMapping(value = "/seriesadmin/seriesdir/add", method = RequestMethod.POST)
	public ModelAndView addSeriesActionUpdateFromPost(
			@ModelAttribute("seriesForm") final SeriesForm seriesForm,
			final BindingResult result) throws ArchException {
		log.info("executing addSeriesActionUpdateFromPost()");
		checkControllerInjectedContracts();
		ModelAndView mav = new ModelAndView();

		log.info("got form: {}", seriesForm);

		log.debug("calling validate");
		final SeriesFormValidator seriesFormValidator = new SeriesFormValidator();
		seriesFormValidator.validate(seriesForm, result);
		
		IRODSAccount irodsAccount;
		try {
			irodsAccount = this.getAuthenticatedIRODSAccount();
		} catch (JargonException e) {
			log.error("error getting irodsAccount", e);
			throw new ArchException(e);
		}
		log.debug("got irods account");

		if (result.hasErrors()) {
			log.info("errors occurred in Series add, redisplay form with errors");
			addPolicyListToMav(mav, irodsAccount);		
			mav.setViewName("seriesadmin_series_dir_add");
			return mav;
		}
		
		// no errors, continue

		try {
			log.info("no errors, so continue to add series continues with updates");
			processSeriesAdd(seriesForm);
			StringBuilder redirectUrl = new StringBuilder();
			redirectUrl.append("redirect:/seriesadmin/seriesdir/view");
			mav.setViewName(redirectUrl.toString());
			// TODO: fix redirect to show series in-place
			log.debug("success, redirect to list view");
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

	private void processSeriesAdd(final SeriesForm seriesForm) throws ArchException {
		IRODSAccount irodsAccount;
		try {
			irodsAccount = this.getAuthenticatedIRODSAccount();
		} catch (JargonException e) {
			log.error("error adding series", e);
			throw new ArchException(e);
		}
		log.debug("got irods account");
		SeriesManager seriesManager;
		try {
			seriesManager = this.getArchServiceFactory().instanceSeriesManager(
					irodsAccount);
		} catch (Exception e) {
			log.error("error adding series", e);
			throw new ArchException(e);
		}
		log.debug("have seriesManager");
		
		Series series = new Series();
		series.setBoundPolicyName(seriesForm.getBoundPolicyName());
		series.setCollectionAbsolutePath(seriesForm.getCollectionAbsolutePath());
		series.setContainingServiceName(seriesForm.getContainingServiceName());
		series.setDescription(seriesForm.getDescription());
		series.setName(seriesForm.getName());
		
		try {
			seriesManager.addSeriesToApplication(series, new XMLToObjectUnmarshaller());
		} catch (PolicyDrivenServiceConfigException e) {
			log.error("error adding series", e);
			throw new ArchException(e);
		}
		log.info("series add successful");
	}
	

}
