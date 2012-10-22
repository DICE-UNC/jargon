/**
 * 
 */
package org.irods.jargon.transfer.engine;

import org.irods.jargon.transfer.dao.GridAccountDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
@Transactional
public class GridAccountServiceImpl {

	private GridAccountDAO gridAccountDAO;

	private final Logger log = LoggerFactory
			.getLogger(GridAccountServiceImpl.class);

	/**
	 * Default constructor. Note that the <code>GridAccountDAO</code>
	 */
	public GridAccountServiceImpl() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the gridAccountDAO
	 */
	public GridAccountDAO getGridAccountDAO() {
		return gridAccountDAO;
	}

	/**
	 * @param gridAccountDAO
	 *            the gridAccountDAO to set
	 */
	public void setGridAccountDAO(GridAccountDAO gridAccountDAO) {
		this.gridAccountDAO = gridAccountDAO;
	}

}
