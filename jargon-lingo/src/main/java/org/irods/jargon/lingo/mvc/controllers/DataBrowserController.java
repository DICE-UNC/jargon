package org.irods.jargon.lingo.mvc.controllers;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.protovalues.UserTypeEnum;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.UserAO;
import org.irods.jargon.core.pub.domain.User;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.irods.jargon.lingo.exceptions.LingoException;
import org.irods.jargon.lingo.mvc.utils.DirectoryTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller for data browser.
 * @author Mike Conway - DICE (www.irods.org)
 *
 */

@RequestMapping("/data/data_browser/**")
@Controller
public class DataBrowserController extends AbstractLingoController {
	
	private Logger log = LoggerFactory.getLogger(DataBrowserController.class);

	/**
	 * 
	 * @return
	 * @throws JargonException
	 */
	@RequestMapping("/data/data_browser")
	public ModelAndView indexAction() throws JargonException {
		log.info("indexAction");
		ModelAndView mav = new ModelAndView();
		mav.setViewName("data_browser");
		
		log.debug("returning mav from DataBrowserController");
		return mav;
	}
	
	
	@RequestMapping("/data/data_browser/ajax_data_browser_dir_request")
	public ModelAndView ajaxBrowserDirRequest(
			@RequestParam("dir") String dir) throws JargonException, LingoException {
		
		if (dir == null) {
			throw new JargonException("null dir for search");
		}
		
		checkControllerInjectedContracts();
		
		log.debug("ajaxBrowserDirRequest() controller action for dir: {}", dir);
		ModelAndView mav = new ModelAndView();
		mav.setViewName("jsonView");
		IRODSAccount irodsAccount = this.getAuthenticatedIRODSAccount();
		log.debug("logged in as {}", irodsAccount.toString());
		
		CollectionAO collectionAO = this.getIrodsAccessObjectFactory().getCollectionAO(irodsAccount);
		List<CollectionAndDataObjectListingEntry> entries = collectionAO.listDataObjectsAndCollectionsUnderPath(dir);
		
		List<DirectoryTreeNode> treeNodes = new ArrayList<DirectoryTreeNode>();
		// TODO: get rid of intermediate object.
		for (CollectionAndDataObjectListingEntry entry : entries) {
			treeNodes.add(new DirectoryTreeNode(entry.getPathOrName(), entry.getObjectType() == CollectionAndDataObjectListingEntry.ObjectType.DATA_OBJECT));
		}
		
		mav.addObject("directoryList", treeNodes);

		getIrodsAccessObjectFactory().closeSession();

		log.debug("session closed...");
		log.debug("returning mav from DataBrowserController for ajax dir listing call");
		return mav;
	} 

}
