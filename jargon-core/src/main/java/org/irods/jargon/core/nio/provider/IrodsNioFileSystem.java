/**
 * 
 */
package org.irods.jargon.core.nio.provider;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NIO file system implementation for iRODS
 * 
 * @author conwaymc
 *
 */
public class IrodsNioFileSystem extends FileSystem {

	private String rootAbsolutePath = "/";
	private final IRODSFileSystem irodsFileSystem;
	private final IRODSAccount irodsAccount;
	private final Map<String, ?> environment;
	private final IrodsFileSystemProvider provider;

	public static final Logger log = LoggerFactory.getLogger(IrodsNioFileSystem.class);

	/**
	 * Constructs an iRODS file system NIO implementation.
	 * 
	 * @param irodsFileSystem
	 *            {@link IRODSFileSystem} with underlying Jargon structures
	 * @param irodsAccount
	 *            {@link IRODSAccount} with the underlying account
	 * @param env
	 *            <code> Map<String, ?></code> with the environment values passed in
	 *            on file system creation
	 */
	public IrodsNioFileSystem(IRODSFileSystem irodsFileSystem, IRODSAccount irodsAccount, Map<String, ?> env,
			String irodsPath, IrodsFileSystemProvider provider) {

		if (irodsFileSystem == null) {
			throw new IllegalArgumentException("null irodsFileSystem");
		}

		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}

		if (irodsPath == null || irodsPath.isEmpty()) {
			rootAbsolutePath = irodsAccount.getHomeDirectory();
		} else {
			rootAbsolutePath = irodsPath;
		}

		if (provider == null) {
			throw new IllegalArgumentException("null provider");
		}

		this.irodsAccount = irodsAccount;
		this.irodsFileSystem = irodsFileSystem;
		this.environment = env;
		this.provider = provider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.nio.file.FileSystem#provider()
	 */
	@Override
	public FileSystemProvider provider() {
		return provider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.nio.file.FileSystem#close()
	 */
	@Override
	public void close() throws IOException {
		log.info("closing!");
		irodsFileSystem.closeAndEatExceptions(); // TODO: only closes for current thread...I don't know if this is
													// useful
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.nio.file.FileSystem#isOpen()
	 */
	@Override
	public boolean isOpen() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.nio.file.FileSystem#isReadOnly()
	 */
	@Override
	public boolean isReadOnly() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.nio.file.FileSystem#getSeparator()
	 */
	@Override
	public String getSeparator() {
		return "/";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.nio.file.FileSystem#getRootDirectories()
	 */
	@Override
	public Iterable<Path> getRootDirectories() {
		List<Path> paths = new ArrayList<Path>();
		paths.add(Paths.get(this.rootAbsolutePath));
		return paths;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.nio.file.FileSystem#getFileStores()
	 */
	@Override
	public Iterable<FileStore> getFileStores() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.nio.file.FileSystem#supportedFileAttributeViews()
	 */
	@Override
	public Set<String> supportedFileAttributeViews() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.nio.file.FileSystem#getPath(java.lang.String, java.lang.String[])
	 */
	@Override
	public Path getPath(String first, String... more) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.nio.file.FileSystem#getPathMatcher(java.lang.String)
	 */
	@Override
	public PathMatcher getPathMatcher(String syntaxAndPattern) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.nio.file.FileSystem#getUserPrincipalLookupService()
	 */
	@Override
	public UserPrincipalLookupService getUserPrincipalLookupService() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.nio.file.FileSystem#newWatchService()
	 */
	@Override
	public WatchService newWatchService() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getRootAbsolutePath() {
		return rootAbsolutePath;
	}

	public void setRootAbsolutePath(String rootAbsolutePath) {
		this.rootAbsolutePath = rootAbsolutePath;
	}

	public IRODSFileSystem getIrodsFileSystem() {
		return irodsFileSystem;
	}

	public IRODSAccount getIrodsAccount() {
		return irodsAccount;
	}

	public Map<String, ?> getEnvironment() {
		return environment;
	}

}
