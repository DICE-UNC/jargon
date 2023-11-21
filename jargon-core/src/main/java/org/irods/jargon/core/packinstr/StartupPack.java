package org.irods.jargon.core.packinstr;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;

public class StartupPack extends AbstractIRODSPackingInstruction {

	public static final String PI_TAG = "StartupPack_PI";
	public static final String CHALLENGE = "challenge";
	public static final String protocolType = "1"; // 1 = xml protocol
	private final IRODSAccount irodsAccount;
	private int reconnFlag = 0;
	private int connectCnt = 0;
	private String option = "";
	// "jargon" is the default name when reporting through sp_option/spOption; allows jargon
	// applications to report with a name through ips
	private static String appName = "jargon";
	public static final String NEGOTIATE_OPTION = "request_server_negotiation";

	public static String getApplicationName() {
		return appName;
	}

	public static void setApplicationName(String name) {
		if (null == name || name.trim().isEmpty()) {  // name can't contain only spaces
			throw new IllegalArgumentException("null or empty name");
		}

		appName = name;
	}

	public StartupPack(final IRODSAccount irodsAccount) {
		super();
		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}
		this.irodsAccount = irodsAccount;
	}

	public StartupPack(final IRODSAccount irodsAccount, final boolean reconnect, final String option) {
		this(irodsAccount);
		if (reconnect) {
			reconnFlag = 200;
		}
		if (option == null) {
			throw new IllegalArgumentException("null option");
		}
		this.option = appName + option;
	}

	@Override
	public Tag getTagValue() throws JargonException {

		String proxyUser;
		String proxyZone;

		if (irodsAccount.getProxyName().isEmpty()) {
			proxyUser = irodsAccount.getUserName();
		} else {
			proxyUser = irodsAccount.getProxyName();
		}

		if (irodsAccount.getProxyZone().isEmpty()) {
			proxyZone = irodsAccount.getZone();
		} else {
			proxyZone = irodsAccount.getProxyZone();
		}

		Tag startupPacket = new Tag(PI_TAG, new Tag[] { new Tag("irodsProt", "1"), new Tag("reconnFlag", reconnFlag),
				new Tag("connectCnt", connectCnt), new Tag("proxyUser", proxyUser), new Tag("proxyRcatZone", proxyZone),
				new Tag("clientUser", irodsAccount.getUserName()), new Tag("clientRcatZone", irodsAccount.getZone()),
				new Tag("relVersion", IRODSAccount.IRODS_JARGON_RELEASE_NUMBER),
				new Tag("apiVersion", IRODSAccount.IRODS_API_VERSION),
				// new Tag("option", "0") });
				new Tag("option", option) });

		return startupPacket;
	}

}
