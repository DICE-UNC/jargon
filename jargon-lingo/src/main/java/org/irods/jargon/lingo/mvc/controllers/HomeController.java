package org.irods.jargon.lingo.mvc.controllers;import java.util.List;

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
public class HomeController {
	
	private Logger log = LoggerFactory.getLogger(HomeController.class);
	private IRODSSession irodsSession;	

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView indexAction() throws JargonException {
    	log.debug("hello from the home controller via logger");
        ModelAndView mav = new ModelAndView();
        mav.setViewName("home");
        
        // action test
        IRODSAccount irodsAccount = IRODSAccount.instance("localhost", 1247, "test1", "test", "/test1/home/test1/", "test1", "test1-resc");
        log.debug("creating login account");
        log.debug(irodsAccount.toString());
        
        IRODSAccessObjectFactoryI irodsAccessObjectFactory = IRODSAccessObjectFactory.instance(irodsSession);
        
        log.debug("getting zone ao");
        ZoneAO zoneAO = irodsAccessObjectFactory.getZoneAO(irodsAccount);
        log.debug("got zone AO, getting zones");
        List<Zone> zones = zoneAO.listZones();
        String myZone = "";
        for(Zone zone : zones) {
        	log.debug("got zone:" + zone);
        	myZone = zone.getZoneName();
        }
        
        irodsSession.closeSession();
        
        log.debug("session closed...");        
		mav.addObject("zones", zones);
		log.debug("returning mav from homeController");
        return mav;
    }

	public IRODSSession getIrodsSession() {
		return irodsSession;
	}

	public void setIrodsSession(IRODSSession irodsSession) {
		this.irodsSession = irodsSession;
	}


	

}
