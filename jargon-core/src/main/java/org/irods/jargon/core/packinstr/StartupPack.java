package org.irods.jargon.core.packinstr;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;

public class StartupPack extends AbstractIRODSPackingInstruction {

	public static final String PI_TAG = "StartupPack_PI";
	public static final String CHALLENGE = "challenge";
	public static final String SP_OPTION_IINIT = "iinit";
	public static final String protocolType = "1"; // 1 = xml protocol
	private final IRODSAccount irodsAccount;
	private int reconnFlag = 0;
	private int connectCnt = 0;
	private String option = "";
	public static final String NEGOTIATE_OPTION = "request_server_negotiation";

	public StartupPack(final IRODSAccount irodsAccount) {
		super();
		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}
		this.irodsAccount = irodsAccount;
	}

	public StartupPack(final IRODSAccount irodsAccount,
			final boolean reconnect, final String option) {
		this(irodsAccount);
		if (reconnect) {
			reconnFlag = 200;
		}
		if (option == null) {
			throw new IllegalArgumentException("null option");
		}
		this.option = option;
	}

	@Override
	public Tag getTagValue() throws JargonException {
		Tag startupPacket = new Tag(PI_TAG,
				new Tag[] {
				new Tag("irodsProt", "1"),
				new Tag("reconnFlag", reconnFlag),
				new Tag("connectCnt", connectCnt),
				new Tag("proxyUser", irodsAccount.getProxyName()),
				new Tag("proxyRcatZone", irodsAccount.getProxyZone()),
				new Tag("clientUser", irodsAccount.getUserName()),
				new Tag("clientRcatZone", irodsAccount.getZone()),
				new Tag("relVersion",
						IRODSAccount.IRODS_JARGON_RELEASE_NUMBER),
						new Tag("apiVersion", IRODSAccount.IRODS_API_VERSION),
						// new Tag("option", "0") });
						new Tag("option", option) });
		return startupPacket;
	}

}
