package org.irods.jargon.lingo.mvc.controllers;import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSAccessObjectFactoryI;
import org.irods.jargon.core.pub.IRODSGenQueryExecutor;
import org.irods.jargon.core.query.IRODSQuery;
import org.irods.jargon.core.query.IRODSQueryResultSet;
import org.irods.jargon.core.query.JargonQueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@RequestMapping("/queries/**")
@Controller
public class QueryController {
	
	private Logger log = LoggerFactory.getLogger(QueryController.class);
	private IRODSSession irodsSession;	

    @RequestMapping("/queries")
    public ModelAndView indexAction() throws JargonException {
    	log.debug("query controller action");
        ModelAndView mav = new ModelAndView();
        mav.setViewName("query");
        
		log.debug("returning mav from queryController");
        return mav;
    }
    /*
     * curl --data-urlencode "query_text=SELECT USER_ID, USER_NAME" http://localhost:80
     */
    @RequestMapping(value="/queries/do_query", method=RequestMethod.GET)
    public ModelAndView doQueryAction(@RequestParam("query_text") String queryText) throws JargonException, JargonQueryException {
    	log.debug("query action");
        ModelAndView mav = new ModelAndView();
        mav.setViewName("ajax_query_result");
        
        log.debug("query string:" + queryText);
        
        // do the query, assumes all is cool
        IRODSAccount irodsAccount = IRODSAccount.instance("localhost", 1247, "test1", "test", "/test1/home/test1/", "test1", "test1-resc");
        log.debug("creating login account");
        log.debug(irodsAccount.toString());
        
        IRODSAccessObjectFactoryI irodsAccessObjectFactory = IRODSAccessObjectFactory.instance(irodsSession);
        
        log.debug("getting query ao");
        IRODSGenQueryExecutor genQueryAO = irodsAccessObjectFactory.getIRODSGenQueryExecutor(irodsAccount);
        log.debug("got query AO, create and issue query");
        IRODSQuery query = IRODSQuery.instance(queryText, 500);
        IRODSQueryResultSet resultSet = genQueryAO.executeIRODSQuery(query, 0);
        
        irodsSession.closeSession();
        
        log.debug("session closed...");  
        mav.addObject("query_result", resultSet);
        
		log.debug("returning mav from queryController");
        return mav;
    }
   

	public IRODSSession getIrodsSession() {
		return irodsSession;
	}

	public void setIrodsSession(IRODSSession irodsSession) {
		this.irodsSession = irodsSession;
	}


	

}
