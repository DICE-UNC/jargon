package org.irods.jargon.lingo.mvc.controllers;import java.io.BufferedInputStream;
import java.io.IOException;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSAccessObjectFactoryI;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.pub.io.IRODSFileOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

@RequestMapping("/ingest/**")
@Controller
public class IngestController {
	
	private Logger log = LoggerFactory.getLogger(IngestController.class);
	private IRODSSession irodsSession;	
   
    @RequestMapping("/ingest")
    public ModelAndView indexAction() throws JargonException {
    	log.debug("ingest controller action");
        ModelAndView mav = new ModelAndView();
        mav.setViewName("ingest");
        
		log.debug("returning mav from ingestController");
        return mav;
    }
    
    /*
     * curl --data-urlencode "query_text=SELECT USER_ID, USER_NAME" http://localhost:80
     */
    @RequestMapping(value="/ingest/do_ingest", method=RequestMethod.POST)
    public ModelAndView doIngestAction(@RequestParam("name") String name,
            @RequestParam("file") MultipartFile file) throws JargonException {
    	log.debug("ingest action");
        ModelAndView mav = new ModelAndView();
        //mav.setViewName("ajax_ingest_result");
        mav.setViewName("ingest");
        log.debug("file name:" + name);
        
        // do the query, assumes all is cool
        IRODSAccount irodsAccount = IRODSAccount.instance("localhost", 1247, "test1", "test", "/test1/home/test1/", "test1", "test1-resc");
        log.debug("creating login account");
        log.debug(irodsAccount.toString());
        
        IRODSAccessObjectFactoryI irodsAccessObjectFactory = IRODSAccessObjectFactory.instance(irodsSession);
        log.info("getting file factory");
        IRODSFileFactory irodsFileFactory = irodsAccessObjectFactory.getIRODSFileFactory(irodsAccount);
        String loadingDockPath = irodsAccount.getHomeDirectory() + '/' + "loading_dock/";
        
        // get an input stream from the request and wrap with buffer 
        if (file.isEmpty()) {
        	log.error("file is empty");
        	throw new JargonException("file is empty");
        }
        
        IRODSFileOutputStream irodsFileOutputStream = irodsFileFactory.instanceIRODSFileOutputStream(loadingDockPath + name);
        byte buff[] = new byte[1024];
		int numRead = 0;
		long totalRead = 0;
		
		BufferedInputStream bis = null;
        
        try {
			bis = new BufferedInputStream(file.getInputStream());
			
			while ((numRead = bis.read(buff)) >= 0) {
				if (log.isDebugEnabled()) {
					log.debug("read:" + numRead);
				}
				irodsFileOutputStream.write(buff, 0, numRead);
				log.debug("wrote to output buffer");
				totalRead += numRead;
			}
			
		} catch (IOException e) {
			log.error("IOException when processing multipart uploaded file", e);
			e.printStackTrace();
			throw new JargonException(e);
		} finally {
			try {
				bis.close();
			} catch (Exception e) {
				log.warn("exception closing file stream, igonored", e);
			}
			
			try {
				irodsFileOutputStream.close();
			} catch (Exception e) {
				log.warn("exception closing irods file output stream, igonored", e);
			}
		} 
		
        irodsSession.closeSession();
        String message = "successfully ingested";
        
        log.debug("session closed...");  
        mav.addObject("message", message);
        
		log.debug("returning mav from ingest");
        return mav;
    }
   

	public IRODSSession getIrodsSession() {
		return irodsSession;
	}

	public void setIrodsSession(IRODSSession irodsSession) {
		this.irodsSession = irodsSession;
	}


	

}
