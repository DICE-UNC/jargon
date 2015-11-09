package org.irods.jargon.core.utils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * An annotation in the source code that indicates that the method is an
 * overhead of a reported bug in the iRODS core server. This is for
 * documentation only
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
@Retention(RetentionPolicy.SOURCE)
public @interface Overheaded {

}
