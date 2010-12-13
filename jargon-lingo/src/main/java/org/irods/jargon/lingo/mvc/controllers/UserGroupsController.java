/**
 * 
 */
package org.irods.jargon.lingo.mvc.controllers;

import org.irods.jargon.core.exception.JargonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller for user group interactions.
 * @author Mike Conway - DICE (www.irods.org)
 *
 */

@RequestMapping("/user_groups/**")
@Controller
public class UserGroupsController extends AbstractLingoController {
	
	private Logger log = LoggerFactory.getLogger(UserGroupsController.class);

	@RequestMapping("/user_groups")
	public ModelAndView indexAction() throws JargonException {
		log.info("indexAction");
		ModelAndView mav = new ModelAndView();
		mav.setViewName("user_groups");
		
		log.debug("returning mav from userGroupsController");
		return mav;
	}

}
