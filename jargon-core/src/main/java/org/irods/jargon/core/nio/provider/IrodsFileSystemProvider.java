/**
 * 
 */
package org.irods.jargon.core.nio.provider;

import java.io.IOException;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.Map;
import java.util.Set;

import org.irods.jargon.core.connection.AuthScheme;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.IRODSFileSystemSingletonWrapper;
import org.irods.jargon.core.utils.IRODSUriUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author conwaymc
 *
 */
public class IrodsFileSystemProvider extends FileSystemProvider {

	public static final String PROVIDER_SCHEME = "irods";
	public static final Logger log = LoggerFactory.getLogger(IrodsFileSystemProvider.class);
	private IRODSFileSystem irodsFileSystem = IRODSFileSystemSingletonWrapper.instance();

	public static final String ENV_HOST = "host";
	public static final String ENV_PORT = "port";
	public static final String ENV_ZONE = "zone";
	public static final String ENV_USER = "user";
	public static final String ENV_PASSWORD = "password";
	public static final String ENV_AUTH_SCHEME = "authMethod";
	public static final String ENV_DEF_RESOURCE = "defResource";
	public static final String ENV_PROXY_USER = "proxyUser";
	public static final String ENV_PROXY_ZONE = "proxyZone";

	/**
	 * 
	 */
	public IrodsFileSystemProvider() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.nio.file.spi.FileSystemProvider#getScheme()
	 */
	@Override
	public String getScheme() {
		return PROVIDER_SCHEME;
	}

	/*
	 * create a new iRODS based file system using the provided irods:// uri. The URI
	 * can contain user and password information, or these iRODS auth coordinates
	 * can be passed in through the env in the form of
	 * 
	 * <ul> <li>host</li> <li>port</li> <li>zone</li> <li>user</li>
	 * <li>password</li> <li>authMethod (using the string-ified irods auth scheme
	 * enum values)</li> <li>defResource with an optional default resource</li>
	 * </ul><li>proxyUser</li><li>proxyZone</li>
	 * 
	 * @see java.nio.file.spi.FileSystemProvider#newFileSystem(java.net.URI,
	 * java.util.Map)
	 */
	@Override
	public FileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException {
		log.info("newFileSystem()");
		if (uri == null) {
			throw new IllegalArgumentException("null uri");
		}

		log.info("uri:{}", uri);

		log.info("using env params for account info");
		IRODSAccount irodsAccountFromEnv = processEnv(uri, env);
		String uriPath = IRODSUriUtils.getAbsolutePathFromURI(uri);

		return new IrodsNioFileSystem(irodsFileSystem, irodsAccountFromEnv, env, uriPath, this);

	}

	private IRODSAccount processEnv(URI uri, Map<String, ?> env) throws IOException {
		try {
			IRODSAccount accountFromUri = IRODSUriUtils.getIRODSAccountFromURI(uri);

			if (env != null) {
				log.info("env overrides");
				if (env.get(ENV_HOST) != null) {
					accountFromUri.setHost((String) env.get(ENV_HOST));
				}

				if (env.get(ENV_PORT) != null) {
					accountFromUri.setPort((Integer) env.get(ENV_PORT));
				}

				if (env.get(ENV_ZONE) != null) {
					accountFromUri.setZone((String) env.get(ENV_ZONE));
				}

				if (env.get(ENV_USER) != null) {
					accountFromUri.setUserName((String) env.get(ENV_USER));
				}

				if (env.get(ENV_PASSWORD) != null) {
					accountFromUri.setPassword((String) env.get(ENV_PASSWORD));
				}

				if (env.get(ENV_AUTH_SCHEME) != null) {
					String envAuthScheme = (String) env.get(ENV_AUTH_SCHEME);
					AuthScheme authScheme = AuthScheme.findTypeByString(envAuthScheme);
					accountFromUri.setAuthenticationScheme(authScheme);
				}

				if (env.get(ENV_DEF_RESOURCE) != null) {
					accountFromUri.setDefaultStorageResource((String) env.get(ENV_DEF_RESOURCE));
				}

				if (env.get(ENV_PROXY_USER) != null) {
					accountFromUri.setProxyName((String) env.get(ENV_PROXY_USER));
				}

				if (env.get(ENV_PROXY_ZONE) != null) {
					accountFromUri.setProxyZone((String) env.get(ENV_PROXY_ZONE));
				}

			}

			return accountFromUri;

		} catch (JargonException e) {
			log.error("error processing env", e);
			throw new IOException("error parsing uri to get env", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.nio.file.spi.FileSystemProvider#getFileSystem(java.net.URI)
	 */
	@Override
	public FileSystem getFileSystem(URI uri) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.nio.file.spi.FileSystemProvider#getPath(java.net.URI)
	 */
	@Override
	public Path getPath(URI uri) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.nio.file.spi.FileSystemProvider#newByteChannel(java.nio.file.Path,
	 * java.util.Set, java.nio.file.attribute.FileAttribute[])
	 */
	@Override
	public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.nio.file.spi.FileSystemProvider#newDirectoryStream(java.nio.file.Path,
	 * java.nio.file.DirectoryStream.Filter)
	 */
	@Override
	public DirectoryStream<Path> newDirectoryStream(Path dir, Filter<? super Path> filter) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.nio.file.spi.FileSystemProvider#createDirectory(java.nio.file.Path,
	 * java.nio.file.attribute.FileAttribute[])
	 */
	@Override
	public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.nio.file.spi.FileSystemProvider#delete(java.nio.file.Path)
	 */
	@Override
	public void delete(Path path) throws IOException {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.nio.file.spi.FileSystemProvider#copy(java.nio.file.Path,
	 * java.nio.file.Path, java.nio.file.CopyOption[])
	 */
	@Override
	public void copy(Path source, Path target, CopyOption... options) throws IOException {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.nio.file.spi.FileSystemProvider#move(java.nio.file.Path,
	 * java.nio.file.Path, java.nio.file.CopyOption[])
	 */
	@Override
	public void move(Path source, Path target, CopyOption... options) throws IOException {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.nio.file.spi.FileSystemProvider#isSameFile(java.nio.file.Path,
	 * java.nio.file.Path)
	 */
	@Override
	public boolean isSameFile(Path path, Path path2) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.nio.file.spi.FileSystemProvider#isHidden(java.nio.file.Path)
	 */
	@Override
	public boolean isHidden(Path path) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.nio.file.spi.FileSystemProvider#getFileStore(java.nio.file.Path)
	 */
	@Override
	public FileStore getFileStore(Path path) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.nio.file.spi.FileSystemProvider#checkAccess(java.nio.file.Path,
	 * java.nio.file.AccessMode[])
	 */
	@Override
	public void checkAccess(Path path, AccessMode... modes) throws IOException {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.nio.file.spi.FileSystemProvider#getFileAttributeView(java.nio.file.Path,
	 * java.lang.Class, java.nio.file.LinkOption[])
	 */
	@Override
	public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.nio.file.spi.FileSystemProvider#readAttributes(java.nio.file.Path,
	 * java.lang.Class, java.nio.file.LinkOption[])
	 */
	@Override
	public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.nio.file.spi.FileSystemProvider#readAttributes(java.nio.file.Path,
	 * java.lang.String, java.nio.file.LinkOption[])
	 */
	@Override
	public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.nio.file.spi.FileSystemProvider#setAttribute(java.nio.file.Path,
	 * java.lang.String, java.lang.Object, java.nio.file.LinkOption[])
	 */
	@Override
	public void setAttribute(Path path, String attribute, Object value, LinkOption... options) throws IOException {
		// TODO Auto-generated method stub

	}

	public IRODSFileSystem getIrodsFileSystem() {
		return irodsFileSystem;
	}

	public void setIrodsFileSystem(IRODSFileSystem irodsFileSystem) {
		this.irodsFileSystem = irodsFileSystem;
	}

}
