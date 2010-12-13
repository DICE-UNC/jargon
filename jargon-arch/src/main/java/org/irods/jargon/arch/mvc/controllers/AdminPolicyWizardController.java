package org.irods.jargon.arch.mvc.controllers;import org.irods.jargon.core.exception.JargonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@RequestMapping("/admin/policy/**")
@Controller
public class AdminPolicyWizardController extends AbstractArchController {
	
	private Logger log = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView indexAction() throws JargonException {
    	log.debug("in default action, will show admin policy index");
        ModelAndView mav = new ModelAndView();
        mav.setViewName("admin_policy_index");
		log.debug("returning mav from controller");
        return mav;
    }
	
    @RequestMapping("/admin/policy/add")
    public ModelAndView policyWizardAction() throws JargonException {
    	log.debug("in default action, will show admin policy wizard");
        ModelAndView mav = new ModelAndView();
        mav.setViewName("admin_policy_wizard");
		log.debug("returning mav from controller");
        return mav;
    }

}
