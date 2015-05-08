package org.irods.jargon.core.utils;

/**
 * This class manages the serializing and deserializing the user info portion of
 * an irods URI.
 *
 * If the user, zone, or password contain a '.', ':' or @, the URI will not be
 * parsable.
 */
final class IRODSUriUserInfo {

    /**
     * Creates an instance that can be used to for user authentication in a
     * local or remote zone.
     *
     * @param user the username
     * @param zone the authentication zone
     * @param password the password used to authenticate the user
     * @return It returns an instance.
     */
    static IRODSUriUserInfo instance(final String user, final String zone,
                                     final String password) {
        return new IRODSUriUserInfo(user, zone, password);
    }

    /**
     * Creates an instance that can be used to for user authentication in the
     * local zone.
     *
     * @param user the username
     * @param password the password used to authenticate the user
     * @return It returns an instance.
     */
    static IRODSUriUserInfo localInstance(final String user,
                                          final String password) {
        return new IRODSUriUserInfo(user, null, password);
    }

    /**
     * Creates an instance that doesn't contain authentication information for a
     * local or remote zone.
     *
     * @param user the username
     * @param zone the authentication zone
     * @return It returns an instance.
     */
    static IRODSUriUserInfo unauthenticatedInstance(final String user,
                                                    final String zone) {
        return new IRODSUriUserInfo(user, zone, null);
    }

    /**
     * Creates an instance that doesn't contain authentication information for
     * the local zone.
     *
     * @param user the username
     * @return It returns an instance.
     */
    static IRODSUriUserInfo unauthenticatedLocalInstance(final String user) {
        return new IRODSUriUserInfo(user, null, null);
    }

    /**
     * Creates an instance from the serialized portion of the URI.
     *
     * @param infoStr The serialized user info portion of an irods URI
     * @return It returns an instance or <code>null</code> if infoStr is
     * <code>null</code> or empty.
     */
    static IRODSUriUserInfo fromString(final String infoStr) {

        if (infoStr == null || infoStr.isEmpty()) {
            return null;
        }

        final int fsIdx = infoStr.indexOf(".");
        final int colonIdx = infoStr.indexOf(":", Math.max(0, fsIdx));
        final int lastIdx = infoStr.length();

        // Only user
        if (fsIdx < 0 && colonIdx < 0) {
            return new IRODSUriUserInfo(infoStr, null, null);
        }

        // User and password
        if (fsIdx < 0 && colonIdx >= 0) {
            return new IRODSUriUserInfo(infoStr.substring(0, colonIdx), null,
                    infoStr.substring(colonIdx + 1, lastIdx));
        }

        // User and zone
        if (fsIdx >= 0 && colonIdx < 0) {
            return new IRODSUriUserInfo(infoStr.substring(0, fsIdx),
                    infoStr.substring(fsIdx + 1, lastIdx), null);
        }

        // user, zone, and password
        return new IRODSUriUserInfo(infoStr.substring(0, fsIdx),
                infoStr.substring(fsIdx + 1, colonIdx),
                infoStr.substring(colonIdx + 1, lastIdx));
    }

    private static String emptyAsNull(final String value) {
        return (value == null || value.isEmpty()) ? null : value;
    }

    private final String user;
    private final String zone;
    private final String password;

    private IRODSUriUserInfo(final String user, final String zone,
                             final String password) {
        if (user == null || user.isEmpty()) {
            throw new IllegalArgumentException("must provide a user name");
        }

        this.user = user;
        this.zone = emptyAsNull(zone);
        this.password = emptyAsNull(password);
    }

    /**
     * @return the username
     */
    String getUserName() {
        return user;
    }

    /**
     * @return the authentication zone
     */
    String getZone() {
        return zone;
    }

    /**
     * @return the password used for authentication of <code>username</code>
     */
    String getPassword() {
        return password;
    }

    /**
     * Serializes the object for inclusion in an irods URI.
     * @return the user info portion of an irods URI
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(user);
        if (zone != null) {
            builder.append(".").append(zone);
        }
        if (password != null) {
            builder.append(":").append(password);
        }
        return builder.toString();
    }

}
