package org.irods.jargon.transfer.engine;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.irods.jargon.core.exception.JargonRuntimeException;
import org.irods.jargon.transfer.TransferServiceFactory;
import org.irods.jargon.transfer.TransferServiceFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabasePreparationUtils {

	private final static Logger log = LoggerFactory
			.getLogger(DatabasePreparationUtils.class);

	public static final void makeSureDatabaseIsInitialized() throws Exception {
		TransferServiceFactory transferServiceFactory = new TransferServiceFactoryImpl();
		TransferQueueService transferQueueService = transferServiceFactory
				.instanceTransferQueueService();
		transferQueueService.getCurrentQueue();
	}

	/**
	 * Utility to drop data from tables in correct order
	 * 
	 * @param jdbcURL
	 */
	public static final void clearAllDatabaseForTesting(final String jdbcURL,
			final String user, final String password) {
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(jdbcURL, user, password);
			log.info("delete local_irods_transfer_item");
			Statement st = conn.createStatement();
			String sql = "DELETE FROM local_irods_transfer_item";
			int delete = st.executeUpdate(sql);
			log.info("deleted:{}", delete);

			log.info("delete local_irods_transfer");
			sql = "DELETE FROM local_irods_transfer";
			delete = st.executeUpdate(sql);
			log.info("deleted:{}", delete);

			log.info("delete local_irods_transfer");
			sql = "DELETE FROM synchronization";
			delete = st.executeUpdate(sql);
			log.info("deleted:{}", delete);

			log.info("delete configuration_property");
			sql = "DELETE FROM configuration_property";
			delete = st.executeUpdate(sql);
			log.info("deleted:{}", delete);

		} catch (SQLException e) {
			if (e.getMessage().indexOf("not found") > -1) {
				log.info("no database data, ignore clear");
				try {
					makeSureDatabaseIsInitialized();
				} catch (Exception e1) {
					log.error("unable to initialize database", e1);
					throw new JargonRuntimeException(
							"error initializing database", e1);
				}
			} else {
				log.error("error clearing database", e);
				throw new JargonRuntimeException("error initializing database",
						e);
			}
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					// ignored
				}
			}
		}
	}

}
