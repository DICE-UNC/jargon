package org.irods.jargon.transfer.dao.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.irods.jargon.core.connection.AuthScheme;
import org.irods.jargon.core.connection.IRODSAccount;

/**
 * Entity implementation class for Entity: GridAccount
 * <p/>
 * This represents a stored grid account, which can be used for login processing
 * (allowing saving of grid accounts for automatic re-authentication). This is
 * also used to store account information for transfers that are in the transfer
 * queue.
 * <p/>
 * Note that the transfer engine encrypts grid account passwords using a general
 * pass-phrase that must be supplied by the user. Clients of the transfer engine
 * must obtain and verify the pass phrase and use it to derive the account
 * passwords.
 * <p/>
 * When dealing with <code>GridAccount</code>, the transfer manager will always
 * expect grid accounts presented for storage or update to have clear text
 * passwords, and the manager will encrypt the password on storage by the pass
 * phrase. By the same token, any <code>GridAccount</code> returned by the
 * transfer manager will have the password encoded. Internally, the transfer
 * manager code will properly decypt the account information as needed.
 * 
 */
@Entity
@Table(name = "grid_account")
public class GridAccount implements Serializable {

	private static final long serialVersionUID = 589659419129682571L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Long id;

	/**
	 * iRODS host name
	 */
	@Column(name = "host", nullable = false)
	private String host = "";

	/**
	 * iRODS port number
	 */
	@Column(name = "port", nullable = false)
	private int port = 0;

	/**
	 * iRODS zone name
	 */
	@Column(name = "zone", nullable = false)
	private String zone = "";

	/**
	 * iRODS user name
	 */
	@Column(name = "user_name", nullable = false)
	private String userName = "";

	/**
	 * iRODS password (note that this is encrypted in the database by a
	 * user-provided pass-phrase
	 */
	@Column(name = "password", nullable = false)
	private String password = "";

	/**
	 * optional default storage resource
	 */
	@Column(name = "default_resource")
	private String defaultResource = "";

	/**
	 * Authentication scheme used for the grid account
	 */
	@Enumerated(EnumType.STRING)
	@Column(name = "auth_scheme", nullable = false)
	private AuthScheme authScheme;

	@Column(name = "preset")
	private boolean preset;

	/**
	 * Optional default path on the iRODS grid to use for things like setting
	 * the root of a displayed tree
	 */
	@Column(name = "default_path", length = 32672)
	private String defaultPath = "";

	/**
	 * Optional free-form comment
	 */
	@Column(name = "comment")
	private String comment = "";

	@Column(name = "created_at", nullable = false)
	private Date createdAt;

	@Column(name = "updated_at", nullable = false)
	private Date updatedAt;

	/**
	 * Run as name
	 */
	@Column(name = "run_as_user_name", nullable = true)
	private String runAsUserName = "";

	/**
	 * Run as Authentication scheme used for the grid account
	 */
	@Enumerated(EnumType.STRING)
	@Column(name = "auth_scheme", nullable = true)
	private AuthScheme runAsAuthScheme;

	@Column(name = "auth_date", nullable = true)
	private Date authDate;

	/**
	 * iRODS password (note that this is encrypted in the database by a
	 * user-provided pass-phrase
	 */
	@Column(name = "run_as_password", nullable = true)
	private String runAsPassword = "";

	@OneToMany(mappedBy = "gridAccount", targetEntity = Transfer.class, fetch = FetchType.LAZY)
	@OrderBy("createdAt DESC")
	@LazyCollection(LazyCollectionOption.TRUE)
	@Cascade({ CascadeType.ALL })
	private final Set<Transfer> transfer = new HashSet<Transfer>();

	@OneToMany(mappedBy = "gridAccount", targetEntity = Synchronization.class, fetch = FetchType.LAZY)
	@OrderBy("name")
	@LazyCollection(LazyCollectionOption.TRUE)
	@Cascade({ CascadeType.ALL })
	private final Set<Synchronization> synchronization = new HashSet<Synchronization>();

	/**
	 * @return
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("GridAccount:");
		sb.append("\n\t host:");
		sb.append(host);
		sb.append("\n\tport:");
		sb.append(port);
		sb.append("\n\tauthScheme:");
		sb.append(authScheme);
		sb.append("\n\tpreset:");
		sb.append(preset);
		sb.append("\n\tcomment:");
		sb.append(comment);
		sb.append("\n\tzone:");
		sb.append(zone);
		sb.append("\n\tuserName:");
		sb.append(userName);
		sb.append("defaultResource:");
		sb.append(defaultResource);
		sb.append("\n\tdefaultPath:");
		sb.append(defaultPath);
		sb.append("\n\tcreatedAt:");
		sb.append(createdAt);
		sb.append("\n\tupdatedAt:");
		sb.append(updatedAt);
		sb.append("\n\tupdatedAt:");
		sb.append(updatedAt);
		return sb.toString();
	}

	public GridAccount() {
		super();
	}

	/**
	 * Create a <code>GridAccount</code> based on the values in a given
	 * <code>IRODSAccount</code>
	 * 
	 * @param irodsAccount
	 *            {@link IRODSAccount}
	 */
	public GridAccount(final IRODSAccount irodsAccount) {

		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}

		authScheme = irodsAccount.getAuthenticationScheme();
		defaultResource = irodsAccount.getDefaultStorageResource();
		createdAt = new Date();
		host = irodsAccount.getHost();
		password = irodsAccount.getPassword();
		port = irodsAccount.getPort();
		updatedAt = createdAt;
		userName = irodsAccount.getUserName();
		zone = irodsAccount.getZone();
		defaultPath = irodsAccount.getHomeDirectory();

	}

	public Long getId() {
		return id;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public String getHost() {
		return host;
	}

	public void setHost(final String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(final int port) {
		this.port = port;
	}

	public String getZone() {
		return zone;
	}

	public void setZone(final String zone) {
		this.zone = zone;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(final String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(final String password) {
		this.password = password;
	}

	public String getDefaultResource() {
		return defaultResource;
	}

	public void setDefaultResource(final String defaultResource) {
		this.defaultResource = defaultResource;
	}

	public AuthScheme getAuthScheme() {
		return authScheme;
	}

	public void setAuthScheme(final AuthScheme authScheme) {
		this.authScheme = authScheme;
	}

	public String getDefaultPath() {
		return defaultPath;
	}

	public void setDefaultPath(final String defaultPath) {
		this.defaultPath = defaultPath;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(final String comment) {
		this.comment = comment;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(final Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(final Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}

		if (!(obj instanceof GridAccount)) {
			return false;
		}

		GridAccount other = (GridAccount) obj;

		/*
		 * consider equal if same host/port/zone/user/password
		 */
		return (getHost().equals(other.getHost())
				&& getPort() == other.getPort()
				&& getZone().equals(other.getZone())
				&& getUserName().equals(other.getUserName())
				&& getPassword().equals(other.getPassword()) && getAuthScheme() == other
					.getAuthScheme());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		/* has generated from host/port/zone/user/password/authscheme */
		return getHost().hashCode() + getPort() + getZone().hashCode()
				+ getUserName().hashCode() + getPassword().hashCode()
				+ getAuthScheme().hashCode();
	}

	/**
	 * @return the preset
	 */
	public boolean isPreset() {
		return preset;
	}

	/**
	 * @param preset
	 *            the preset to set
	 */
	public void setPreset(final boolean preset) {
		this.preset = preset;
	}

	/**
	 * @return the runAsUserName
	 */
	public String getRunAsUserName() {
		return runAsUserName;
	}

	/**
	 * @param runAsUserName
	 *            the runAsUserName to set
	 */
	public void setRunAsUserName(String runAsUserName) {
		this.runAsUserName = runAsUserName;
	}

	/**
	 * @return the runAsAuthScheme
	 */
	public AuthScheme getRunAsAuthScheme() {
		return runAsAuthScheme;
	}

	/**
	 * @param runAsAuthScheme
	 *            the runAsAuthScheme to set
	 */
	public void setRunAsAuthScheme(AuthScheme runAsAuthScheme) {
		this.runAsAuthScheme = runAsAuthScheme;
	}

	/**
	 * @return the authDate
	 */
	public Date getAuthDate() {
		return authDate;
	}

	/**
	 * @param authDate
	 *            the authDate to set
	 */
	public void setAuthDate(Date authDate) {
		this.authDate = authDate;
	}

	/**
	 * @return the runAsPassword
	 */
	public String getRunAsPassword() {
		return runAsPassword;
	}

	/**
	 * @param runAsPassword
	 *            the runAsPassword to set
	 */
	public void setRunAsPassword(String runAsPassword) {
		this.runAsPassword = runAsPassword;
	}

}
