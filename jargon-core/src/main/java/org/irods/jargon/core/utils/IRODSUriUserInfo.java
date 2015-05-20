package org.irods.jargon.core.utils;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This class manages the serializing and deserializing the user info portion of
 * an <code>irods:</code> URI.
 */
public final class IRODSUriUserInfo {

    private static final String ZONE_INDICATOR = ".";
    private static final String PASSWORD_INDICATOR = ":";
    private static final byte ESCAPE_INDICATOR = '%';

    private static final Set<Byte> USER_INFO_ALLOWED_CHARS;

    static {
        final byte[] allowedMarks = new byte[] {
                '-', '_', '!', '~', '*', '\'', '(', ')', ';', '&', '=', '+',
				'$', ','
        };

        final HashSet<Byte> chars = new HashSet<>();
        for (byte c = '0'; c <= '9'; c++) {
            chars.add(c);
        }
        for (byte c = 'a'; c <= 'z'; c++) {
            chars.add(c);
        }
        for (byte c = 'A'; c <= 'Z'; c++) {
            chars.add(c);
        }
        for (byte c : allowedMarks) {
            chars.add(c);
        }
        USER_INFO_ALLOWED_CHARS = Collections.unmodifiableSet(chars);
    }

    /**
     * Creates an instance that can be used to for user authentication in a
	 * local or remote zone.
     *
     * @param user the username
     * @param zone the authentication zone
     * @param password the password used to authenticate the user
     * @return It returns an instance.
     */
    public static IRODSUriUserInfo instance(
			final String user, final String zone, final String password) {
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
    public static IRODSUriUserInfo localInstance(
			final String user, final String password)
    {
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
    public static IRODSUriUserInfo unauthenticatedInstance(
			final String user, final String zone) {
        return new IRODSUriUserInfo(user, zone, null);
    }

    /**
     * Creates an instance that doesn't contain authentication information for
	 * the local zone.
     *
     * @param user the username
     * @return It returns an instance.
     */
    public static IRODSUriUserInfo unauthenticatedLocalInstance(
			final String user) {
        return new IRODSUriUserInfo(user, null, null);
    }

    /**
     * Creates an instance from the serialized portion of the URI.
     *
     * @param encodedStr The serialized user info portion of an irods URI
     * @return It returns an instance or <code>null</code> if infoStr is
     * <code>null</code> or empty.
     */
    static IRODSUriUserInfo fromString(final String encodedStr) {
        if (encodedStr == null || encodedStr.isEmpty()) {
            return null;
        }

        final int zIdx = encodedStr.indexOf(ZONE_INDICATOR);
        final int pIdx = encodedStr.indexOf(PASSWORD_INDICATOR);

        String encodedUser = null;
        String encodedZone = null;
        String encodedPassword = null;

        if (zIdx < 0 && pIdx < 0) {
            // Only user
            encodedUser = encodedStr;
        } else if (zIdx < 0 && pIdx >= 0) {
            // User and password
            encodedUser = encodedStr.substring(0, pIdx);
            encodedPassword = encodedStr.substring(pIdx + 1);
        } else if (zIdx >= 0 && pIdx <= zIdx) {
            // User and zone
            encodedUser = encodedStr.substring(0, zIdx);
            encodedZone = encodedStr.substring(zIdx + 1);
        } else {
            // user, zone, and password
            encodedUser = encodedStr.substring(0, zIdx);
            encodedZone = encodedStr.substring(zIdx + 1, pIdx);
            encodedPassword = encodedStr.substring(pIdx + 1);
        }

        return new IRODSUriUserInfo(decode(encodedUser), decode(encodedZone),
                decode(encodedPassword));
    }

    private static String emptyAsNull(final String value) {
        return (value == null || value.isEmpty()) ? null : value;
    }

    private static String decode(final String encodedValue) {
        if (encodedValue == null) {
            return null;
        }
        final byte[] encoded = encodedValue.getBytes(StandardCharsets.US_ASCII);
        final ByteArrayOutputStream decoded = new ByteArrayOutputStream();
        for (int i = 0; i < encoded.length; i++) {
            if (encoded[i] == ESCAPE_INDICATOR) {
                final int ud = Character.digit(encoded[++i], 16);
                final int ld = Character.digit(encoded[++i], 16);
                decoded.write((ud << 4) + ld);
            } else {
                decoded.write(encoded[i]);
            }
        }
        return new String(decoded.toByteArray(), StandardCharsets.UTF_8);
    }

    private static String encode(final String value) {
        final byte[] decoded = value.getBytes(StandardCharsets.UTF_8);
        final ByteArrayOutputStream encoded = new ByteArrayOutputStream();
        for (byte c : decoded) {
            if (USER_INFO_ALLOWED_CHARS.contains(c)) {
                encoded.write(c);
            } else {
                final String hex = Integer.toHexString(c & 0xff).toUpperCase();
                final byte[] digits = hex.getBytes(StandardCharsets.US_ASCII);
                encoded.write(ESCAPE_INDICATOR);
                encoded.write(digits, 0, digits.length);
            }
        }
        return new String(encoded.toByteArray(), StandardCharsets.US_ASCII);
    }

    private final String user;
    private final String zone;
    private final String password;

    private IRODSUriUserInfo(
			final String user, final String zone, final String password) {
        if (user == null || user.isEmpty()) {
            throw new IllegalArgumentException("must provide a user name");
        }

        this.user = user;
        this.zone = emptyAsNull(zone);
        this.password = emptyAsNull(password);
    }

    /**
     * Serializes the object for inclusion in an <code>irods:</code> URI.
     * @return the user info portion of an <code>irods:</code> URI
     */
    @Override
    public String toString() {
        final StringBuilder encoded = new StringBuilder();
        encoded.append(encode(user));
        if (zone != null) {
            encoded.append(ZONE_INDICATOR).append(encode(zone));
        }
        if (password != null) {
            encoded.append(PASSWORD_INDICATOR).append(encode(password));
        }
        return encoded.toString();
    }

    /**
     * @return the username
     */
    public String getUserName() {
        return user;
    }

    /**
     * @return the authentication zone
     */
    public String getZone() {
        return zone;
    }

    /**
     * @return the password used for authentication of the username
     */
    public String getPassword() {
        return password;
    }

}
