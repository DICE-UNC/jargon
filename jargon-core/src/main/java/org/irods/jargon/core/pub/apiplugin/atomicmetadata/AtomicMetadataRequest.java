/**
 * 
 */
package org.irods.jargon.core.pub.apiplugin.atomicmetadata;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.apiplugin.PluggableApiRequest;

import com.fasterxml.jackson.annotation.JsonGetter;

/**
 * Request for an atomic metadata pluggable api operation
 * 
 * @author conwaymc
 *
 */
public class AtomicMetadataRequest extends PluggableApiRequest {

	/**
	 * Name of the entity (e.g. path) to be decorated as {@code String}
	 */
	private String entityName = "";
	/**
	 * Type of entity (e.g. collection, resource) as {@code String}
	 */
	private String entityType = "";

	/**
	 * Individual metadata operations as
	 * {@link AtomicMetadataRequest.AtomicMetadataOperation}
	 */
	private List<AtomicMetadataOperation> operations = new ArrayList<>();

	/**
	 * 
	 */
	public AtomicMetadataRequest() {
	}

	@JsonGetter("entity_name")
	public String getEntityName() {
		return entityName;
	}

	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	@JsonGetter("entity_type")
	public String getEntityType() {
		return entityType;
	}

	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}

	@JsonGetter("operations")
	public List<AtomicMetadataOperation> getOperations() {
		return operations;
	}

	public void setOperations(List<AtomicMetadataOperation> operations) {
		this.operations = operations;
	}

}
