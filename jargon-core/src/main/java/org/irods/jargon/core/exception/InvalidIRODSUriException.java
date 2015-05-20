package org.irods.jargon.core.exception;

import java.net.URI;

/**
 * Objects of this class represent the case where an iRODS URI was expected, but
 * another type of URI was provided.
 */
public final class InvalidIRODSUriException extends JargonException {

    /**
     * the constructor
     *
     * @param invalidURI the invalid URI
     */
    public InvalidIRODSUriException(final URI invalidURI) {
        super("The URI, " + invalidURI + ", is not an iRODS URI.");
    }

}
