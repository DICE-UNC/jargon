/**
 * 
 */
package org.irods.jargon.conveyor.flowmanager.flow;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.irods.jargon.core.utils.LocalFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service to load flow specifications as DSL script files from a set of
 * locations, and find appropriate flow specs given a specification
 * <p/>
 * Since flow specs are groovy DSLs in a text file, this will run a groovy shell
 * on each discovered specification
 * 
 * @author Mike Conway - DICE
 *
 */
public class FlowSpecCacheService {

	private List<String> flowSourceLocalAbsolutePaths = new ArrayList<String>();
	private List<FlowSpec> flowSpecs = new ArrayList<FlowSpec>();

	private static final Logger log = LoggerFactory
			.getLogger(FlowSpecCacheService.class);

	/**
	 * 
	 */
	public FlowSpecCacheService() {
	}

	/**
	 * Scan the library of dsls and add a flowspec for each .groovy file
	 * 
	 * @throws FlowSpecConfigurationException
	 */
	public synchronized void init() throws FlowSpecConfigurationException {

		log.info("init()...scan for dsl files");

		for (String flowPath : flowSourceLocalAbsolutePaths) {
			findFlowsInPath(flowPath);
		}

	}

	/**
	 * Scan the file and load up any flows you find. This does not recurse, so
	 * that one may control the load order by file sort order.
	 * 
	 * @param flowPath
	 * @throws FlowSpecConfigurationException
	 */
	private void findFlowsInPath(String flowPath)
			throws FlowSpecConfigurationException {

		log.info("looking for flows in path:{}", flowPath);

		/*
		 * Ignore missing dirs and just plow ahead. Mabye later add a validate
		 * step?
		 */

		File flowPathFile = new File(flowPath);
		if (!flowPathFile.exists()) {
			log.warn("flow path does not exist:{}", flowPath);
			return;
		}

		if (!flowPathFile.isDirectory()) {
			log.warn("flow path not directory:{}", flowPath);
			return;
		}

		log.info("list chilren...");

		for (File child : flowPathFile.listFiles()) {
			log.info("child:{}", child);

			if (child.isFile()) {
				if (LocalFileUtils.getFileExtension(child.getName()).equals(
						".groovy")) {
					log.info("have groovy file");
					addGroovyDslToFlowSpecs(child);
				} else {
					continue;
				}
			} else {
				// this is a dir, if I want to recurse to it here, for now it's
				// flat to have more reasonable load orders
				continue;
			}
		}
	}

	private void addGroovyDslToFlowSpecs(File child) {

		log.info("parsing groovy sript for flow:{}", child);

	}

	/**
	 * Setter for injecting the list of local absolute paths to scan for .groovy
	 * files that are flow specs.
	 * <p/>
	 * This is saved internally as an immutable list
	 * 
	 * @param flowSourceLocalAbsolutePaths
	 */
	public synchronized void setFlowSourceLocalAbsolutePaths(
			List<String> flowSourceLocalAbsolutePaths) {

		if (flowSourceLocalAbsolutePaths == null) {
			throw new IllegalArgumentException(
					"null flowSourceLocalAbsolutePaths");
		}

		this.flowSourceLocalAbsolutePaths = Collections
				.unmodifiableList(flowSourceLocalAbsolutePaths);
	}

}
