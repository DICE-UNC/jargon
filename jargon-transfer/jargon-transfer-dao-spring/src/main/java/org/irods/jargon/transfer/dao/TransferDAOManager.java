package org.irods.jargon.transfer.dao;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 
 * @author jdr0887
 * 
 */
public class TransferDAOManager {

    private static TransferDAOManager instance;

    private ClassPathXmlApplicationContext applicationContext = null;

    public static TransferDAOManager getInstance() {
        if (instance == null) {
            instance = new TransferDAOManager();
        }
        return instance;
    }

    private TransferDAOManager() {
        this.applicationContext = new ClassPathXmlApplicationContext("transfer-dao-beans.xml");
    }

    public TransferDAOBean getTransferDAOBean() {
        TransferDAOBean bean = (TransferDAOBean) applicationContext.getBean("transferDAOBean", TransferDAOBean.class);
        return bean;
    }

}
