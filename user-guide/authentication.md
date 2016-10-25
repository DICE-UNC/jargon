
Jargon authentication allows the use of the following authentication methods:

* Standard iRODS challenge/response, using iRODS passwords
* PAM authentication, which securely passes credentials to iRODS for PAM authentication by the iRODS server
 
### IRODS Accounts

The Jargon IRODSAccount object, in the org.irods.jargon.core.connection.* package, represents the principal that is attempting to log in to the iRODS system.   The IRODSAccount, and subclasses, such as GSIIRODSAccount, describe connection and authentication information.  This includes host, port, zone, and user credentials.  IRODSAccount classes also allow definition of default storage resource, and other information.

It is important to note that the authentication process may augement the contents of IRODSAccount.  For example, GSI authentication is done by providing a certificate.  The user distinguished name, iRODS user name, and iRODS zone are added to create a final IRODSAccount object back to the user.

Please see the documentation on connections for information on how the IRODSAccount information is used to define and manage connections to iRODS.

### AuthSchemes in IRODS Account

The IRODSAccount is the primary object to contain connection information.  This is used for standard and PAM authentication, where the user name and password are provided as text, with the AuthScheme set to the appropriate authentication type.  

The GSIIRODSAccount is used for Globus GSI authentication using a pre-generated user proxy certificate.  This is coverd in more detail below.

 

The AuthScheme in the IRODSAccount object is used by the IRODSSession object to create an authentication mechanism module.  This module is then used to complete the handshake and authentication process.  It is important to set the AuthScheme to the expected value.

### The Authentication Process

Note, as in the connections section, that Jargon manages iRODS connections using a ThreadLocal.  This is a convenience, it helps simplify API usage, and it helps segregate operations when multiple threads are involved.  This is due to the stateful nature of the iRODS protocol.  The IRODSAccount object is used as the 'key' for the connection cache.

Interactions with Jargon are primarily through Access Objects and java.io.* iRODS objects created through and access object and file factory.  These access and IO object factories use the IRODSAccount to either obtain an already defined connection, or to create a new connection.  The mechanism is automatic, such that a new connection may be made and authenticated automatically if one is not available.  

For purposes of client interfaces, a specific authentication method is available in IRODSAccessObjectFactory

```java
/*
 * Cause an <code>IRODSAccount</code> to be authenticated, and return and
 * <code>AuthResponse</code> augmented with information about the principal.
 * <p/>
 * Note that the account information is actually cached in a thread local by
 * the <code>IRODSSession</code>, so this method will return the cached
 * response if already authenticated. If not cached, this method causes an
 * authentication process.
 * 
 * @param irodsAccount
 *         {@IRODSAccount} with the authenticating
 *         principal
 * @return {@link AuthResponse} containing information about the
 *         authenticated principal. Note that the authentication process may
 *         cause the authenticating <code>IRODSAccount</code> to be altered
 *         or augmented. The resulting account that can be cached and
 *         re-used by applications will be in the authenticated account.
 * @throws AuthenticationException
 *         If the principal cannot be authenticated. This will be thrown
 *         on initial authentication
 * @throws JargonException
 */

AuthResponse authenticateIRODSAccount(IRODSAccount irodsAccount)
    throws AuthenticationException, JargonException;
```


 

Note that this method will authenticate the IRODSAccount, and then return back an AuthResponse object.  The AuthResponse contains both the IRODSAccount as presented for authentication, as well as the augmented, authenticated IRODSAccount.  Clients should cache the 'authenticated' IRODSAccount in session or other persistant store.  For example, PAM authentication sends the credentials to iRODS via SSL, and once PAM authentication completes, a temporary password is returned, allowing subsequent calls to iRODS to bypass the PAM step.  If the augmented iRODS account is not used, each call to iRODS could result in repeated PAM authentications.  This will work, but may incur a performance penalty.

Here is a (grails) example of a login process, using the AuthResponse object to store the authenticated IRODS account in the HTTPSession:

 
```java
irodsAccount = IRODSAccount.instance(
    loginCommand.host,
    loginCommand.port,
    userName,
    password,
    "",
    loginCommand.zone,
    resource)

log.info("login mode: ${loginCommand.authMethod}")

if (loginCommand.authMethod == "Standard") {
    irodsAccount.authenticationScheme = IRODSAccount.AuthScheme.STANDARD
} else if (loginCommand.authMethod == "PAM") {
    irodsAccount.authenticationScheme = IRODSAccount.AuthScheme.PAM
} else {
    log.error("authentication scheme invalid", e)
    response.sendError(500,e.message)
    return
}

log.info("built irodsAccount:${irodsAccount}")
AuthResponse authResponse
try {
    authResponse = irodsAccessObjectFactory.authenticateIRODSAccount(irodsAccount)
} catch (JargonException e) {
    ... error handling ...
}

// storing the augmented account in HTTP session for re-login

session["SPRING_SECURITY_CONTEXT"] = authResponse.authenticatedIRODSAccount
redirect(controller:"home")
```
