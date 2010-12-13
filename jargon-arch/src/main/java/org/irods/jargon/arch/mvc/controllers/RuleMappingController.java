/**
 * 
 */
package org.irods.jargon.arch.mvc.controllers;

import java.util.List;

import org.irods.jargon.arch.exception.ArchException;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.part.exception.DataNotFoundException;
import org.irods.jargon.part.policydriven.PolicyDrivenRulesManager;
import org.irods.jargon.part.policydriven.PolicyDrivenServiceConfigException;
import org.irods.jargon.part.policydriven.PolicyDrivenServiceListingEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller handles details of rule mappings within a rule mapping repository
 * @author Mike Conway - DICE (www.irods.org)
 *
 */

@RequestMapping("/rulemapping/**")
@Controller
public class RuleMappingController extends AbstractArchController {

	public static Logger log = LoggerFactory.getLogger(RuleMappingController.class);
	
	/**
	 * View page to view the rule mapping directories that are set up on the server.  
	 * 
	 * @return
	 * @throws JargonException
	 */
	@RequestMapping(value = "/rulemapping/view", method = RequestMethod.GET)
	public ModelAndView listRuleMappings() throws ArchException {
		log.info("executing listRuleMappings");
		checkControllerInjectedContracts();
		final ModelAndView mav = new ModelAndView();
		mav.setViewName("ruleadmin_rulemapping_view");

		try {
			final IRODSAccount irodsAccount = this.getAuthenticatedIRODSAccount();
			final PolicyDrivenRulesManager rulesManager = getArchServiceFactory().instancePolicyDrivenRulesManager(irodsAccount);
	
			// generate a list of mappings for a select option control
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
	
	/**
	 * Page to add/upload a rule mapping
	 * @return
	 * @throws JargonException
	 */
	@RequestMapping(value = "/rulemapping/add", method = RequestMethod.GET)
	public ModelAndView addOrUploadRuleMapping() throws ArchException {
		log.info("executing addOrUploadRuleMapping");
		checkControllerInjectedContracts();
		final ModelAndView mav = new ModelAndView();
		mav.setViewName("ruleadmin_rulemapping_add");

		try {
			final IRODSAccount irodsAccount = this.getAuthenticatedIRODSAccount();
			final PolicyDrivenRulesManager rulesManager = getArchServiceFactory().instancePolicyDrivenRulesManager(irodsAccount);
			// generate a list of mappings for a select option control
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
	
	
	/**
	 * Method to return a list of rule mappings to support various AJAX calls
	 * @return
	 * @throws ArchException
	 */
	@RequestMapping(value = "/rulemapping/ajax_rule_mapping_dirs", method = RequestMethod.GET)
	public ModelAndView buildAjaxRuleMappingList() throws ArchException {
		log.info("executing buildAjaxRuleMappingList");
		checkControllerInjectedContracts();
		final ModelAndView mav = new ModelAndView();
		mav.setViewName("ajax_select_services");

		try {
			IRODSAccount irodsAccount = this.getAuthenticatedIRODSAccount();
			PolicyDrivenRulesManager policyDrivenRulesManager = this
					.getArchServiceFactory()
					.instancePolicyDrivenRulesManager(irodsAccount);
			List<PolicyDrivenServiceListingEntry> serviceListings = policyDrivenRulesManager.findRuleRepositories();
			log.debug("adding service listings as 'serviceListing': {}",
					serviceListings);
			mav.addObject("serviceListing", serviceListings);
		} catch (Exception e) {
			log.error("error getting policyDrivenServiceManager", e);
			throw new ArchException(e);
		} finally {
			closeSessionIgnoringExceptions();
		}

		log.debug("returning mav from Controller for view: {}", mav
				.getViewName());
		return mav;
	}
	
	@RequestMapping(value = "/rulemapping/ajax_rule_mapping_dir_contents", method = RequestMethod.GET)
	public ModelAndView buildAjaxRuleMappingDirContentsList(@RequestParam("serviceListing") String ruleMappingDir) throws ArchException {
		log.info("executing buildAjaxRuleMappingDirContentsList");
		checkControllerInjectedContracts();
		final ModelAndView mav = new ModelAndView();
		mav.setViewName("ajax_rule_mapping_dir_contents");
		
		if (ruleMappingDir == null || ruleMappingDir.isEmpty()) {
			throw new ArchException("rule mapping directory is null or blank");
		}
		
		log.info("obtaining contents of rule mapping directory with name: {}", ruleMappingDir);

		try {
			IRODSAccount irodsAccount = this.getAuthenticatedIRODSAccount();
			PolicyDrivenRulesManager policyDrivenRulesManager = this
					.getArchServiceFactory()
					.instancePolicyDrivenRulesManager(irodsAccount);
			List<PolicyDrivenServiceListingEntry> serviceListings = policyDrivenRulesManager.listRulesInRepository(ruleMappingDir);
			log.debug("adding service listings as 'serviceListing': {}",
					serviceListings);
			mav.addObject("serviceListing", serviceListings);
		} catch (Exception e) {
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
	 * Obtain data about a rule mapping that can be displayed in an AJAX div
	 * @param ruleMappingDirName <code>String</code> with unique identifying name for the rule mapping repository
	 * @return
	 * @throws ArchException
	 */
	@RequestMapping(value = "/rulemapping/ajax_rule_mapping_dir_details", method = RequestMethod.GET)
	public ModelAndView buildAjaxRuleMappingDirDetails(@RequestParam("mappingDirName") String ruleMappingDirName) throws ArchException {
		log.info("executing buildAjaxRuleMappingDirDetails");
		checkControllerInjectedContracts();
		final ModelAndView mav = new ModelAndView();
		mav.setViewName("ajax_rule_mapping_dir_details");
		
		if (ruleMappingDirName == null || ruleMappingDirName.isEmpty()) {
			throw new ArchException("ruleMappingDirName is null or blank");
		}
		
		log.info("obtaining details of rule mapping directory with name: {}", ruleMappingDirName);

		try {
			IRODSAccount irodsAccount = this.getAuthenticatedIRODSAccount();
			PolicyDrivenRulesManager policyDrivenRulesManager = this
					.getArchServiceFactory()
					.instancePolicyDrivenRulesManager(irodsAccount);
			PolicyDrivenServiceListingEntry mappingDirData = policyDrivenRulesManager.findRuleRepository(ruleMappingDirName);
			mav.addObject("mappingDirData", mappingDirData);
		} catch (DataNotFoundException dnf) {
			log.error("data not found for mapping directory:{}", ruleMappingDirName);
			// add error to model and blank data
			StringBuilder msg = new StringBuilder();
			msg.append("No mapping directory found for mapping directory name:");
			msg.append(ruleMappingDirName);
			mav.addObject("actionError", msg.toString());
		} catch (Exception e) {
			log.error("error getting policyDrivenServiceManager", e);
			throw new ArchException(e);
		} finally {
			closeSessionIgnoringExceptions();
		}

		log.debug("returning mav from Controller for view: {}", mav
				.getViewName());
		return mav;
	}

}
