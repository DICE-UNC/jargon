package org.irods.jargon.arch.mvc.controllers;import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSAccessObjectFactoryI;
import org.irods.jargon.core.pub.ZoneAO;
import org.irods.jargon.core.pub.domain.Zone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@RequestMapping("/home/**")
@Controller
public class HomeController extends AbstractArchController {
	
	private Logger log = LoggerFactory.getLogger(HomeController.class);

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView indexAction() throws JargonException {
    	log.debug("hello from the home controller via logger");
        ModelAndView mav = new ModelAndView();
        mav.setViewName("home");
		log.debug("returning mav from homeController");
        return mav;
    }

}
