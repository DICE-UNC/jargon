package org.irods.jargon.core.packinstr;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.StartupResponseData;
import org.irods.jargon.core.exception.JargonException;

public class ReconnMsg extends AbstractIRODSPackingInstruction {

	public static final String PI_TAG = "ReconnMsg_PI";
	private final IRODSAccount irodsAccount;
	private final StartupResponseData startupResponseData;

	/**
	 * Constructor for a reconnect message, this is used to renew a socket after
	 * prior arrangement with an iRODS agent in the startup process.
	 * 
	 * @param irodsAccount
	 *            {@link IRODSAccount} for the original connection
	 * @param startupResponseData
	 *            {@link StartupResponseData} that was returned when the startup
	 *            sequence was done, and the reconnect option was set.
	 */
	public ReconnMsg(final IRODSAccount irodsAccount,
			final StartupResponseData startupResponseData) {
		super();
		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}
		if (startupResponseData == null) {
			throw new IllegalArgumentException("null startupResponseData");
		}
		this.irodsAccount = irodsAccount;
		this.startupResponseData = startupResponseData;
		this.setApiNumber(0);
	}

	@Override
	public Tag getTagValue() throws JargonException {
		Tag startupPacket = new Tag(PI_TAG,
				new Tag[] {
				new Tag("status", startupResponseData.getStatus()),
				new Tag("cookie", startupResponseData.getCookie()),
				new Tag("procState", "0"), new Tag("flag", "0") });
		return startupPacket;
	}

	/**
	 * @return the irodsAccount
	 */
	public IRODSAccount getIrodsAccount() {
		return irodsAccount;
	}

	/**
	 * @return the startupResponseData
	 */
	public StartupResponseData getStartupResponseData() {
		return startupResponseData;
	}

}
