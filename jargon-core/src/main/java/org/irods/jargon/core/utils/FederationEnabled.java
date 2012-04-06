package org.irods.jargon.core.utils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * An annotation in the source code that indicates that the method is meant to
 * automatically handle operations across federated zones.
 * <p/>
 * This is a new addition, and will be added over time as each relevant method
 * is tested and certified to be cross-zone compatable.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
@Retention(RetentionPolicy.SOURCE)
public @interface FederationEnabled {

}
