package org.irods.jargon.core.pub;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.Genquery2Input;
import org.irods.jargon.core.packinstr.Tag;

public class IRODSGenquery2ExecutorImpl extends IRODSGenericAO implements IRODSGenquery2Executor {

	public IRODSGenquery2ExecutorImpl(IRODSSession irodsSession, IRODSAccount irodsAccount) throws JargonException {
		super(irodsSession, irodsAccount);
	}

	@Override
	public String execute(final String queryString) throws JargonException {
		return execute(queryString, getIRODSAccount().getZone());
	}

	@Override
	public String execute(final String queryString, final String zone) throws JargonException {
		if (null == queryString || queryString.isEmpty()) {
			throw new IllegalArgumentException("Query string is null or empty");
		}

		if (null == zone || zone.isEmpty()) {
			throw new IllegalArgumentException("Zone string is null or empty");
		}

		final Genquery2Input input = Genquery2Input.instanceForQuery(queryString, zone);
		final Tag tag = getIRODSProtocol().irodsFunction(input);

		if (null == tag) {
			return null;
		}

		return tag.getTag("myStr").getStringValue();
	}

	@Override
	public String getGeneratedSQL(final String queryString) throws JargonException {
		return getGeneratedSQL(queryString, getIRODSAccount().getZone());
	}

	@Override
	public String getGeneratedSQL(final String queryString, final String zone) throws JargonException {
		if (null == queryString || queryString.isEmpty()) {
			throw new IllegalArgumentException("Query string is null or empty");
		}

		if (null == zone || zone.isEmpty()) {
			throw new IllegalArgumentException("Zone string is null or empty");
		}

		final Genquery2Input input = Genquery2Input.instanceForSqlOnly(queryString, zone);
		final Tag tag = getIRODSProtocol().irodsFunction(input);

		if (null == tag) {
			return null;
		}

		return tag.getTag("myStr").getStringValue();
	}

	@Override
	public String getColumnMappings() throws JargonException {
		final Genquery2Input input = Genquery2Input.instanceForColumnMappings(getIRODSAccount().getZone());
		final Tag tag = getIRODSProtocol().irodsFunction(input);

		if (null == tag) {
			return null;
		}

		return tag.getTag("myStr").getStringValue();
	}

	@Override
	public String getColumnMappings(final String zone) throws JargonException {
		final Genquery2Input input = Genquery2Input.instanceForColumnMappings(zone);
		final Tag tag = getIRODSProtocol().irodsFunction(input);

		if (null == tag) {
			return null;
		}

		return tag.getTag("myStr").getStringValue();
	}

}
