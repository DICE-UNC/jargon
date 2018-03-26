/**
 *
 */
package org.irods.jargon.core.sql;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

/**
 * <b>Experimental!</b>
 * <p>
 * This is an implementation of the standard {@code java.sql.ResultSet} as an
 * abstract superclass that represents various types of iRODS query results.
 * <p>
 * The primary purpose of this abstract class is to define a subset that can be
 * implemented by the various query types (special query, simple query, gen
 * query), and indicate unsupported operations for the rest.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public abstract class AbstractIRODSSqlResultSet implements ResultSet {

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
	 */
	@Override
	public abstract boolean isWrapperFor(Class<?> arg0) throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.Wrapper#unwrap(java.lang.Class)
	 */
	@Override
	public abstract <T> T unwrap(Class<T> arg0) throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#absolute(int)
	 */
	@Override
	public abstract boolean absolute(int arg0) throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#afterLast()
	 */
	@Override
	public abstract void afterLast() throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#beforeFirst()
	 */
	@Override
	public abstract void beforeFirst() throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#cancelRowUpdates()
	 */
	@Override
	public void cancelRowUpdates() throws SQLException {
		throw new SQLFeatureNotSupportedException();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#clearWarnings()
	 */

	@Override
	public void clearWarnings() throws SQLException {
		// does nothing right now...
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#close()
	 */
	@Override
	public void close() throws SQLException {
		// does nothing right now, IRODSFileSystem takes care of any connection
		// handling.
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#deleteRow()
	 */
	@Override
	public void deleteRow() throws SQLException {
		throw new UnsupportedOperationException("not supported");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#findColumn(java.lang.String)
	 */
	@Override
	public abstract int findColumn(String arg0) throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#first()
	 */
	@Override
	public abstract boolean first() throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getArray(int)
	 */
	@Override
	public abstract Array getArray(int arg0) throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getArray(java.lang.String)
	 */
	@Override
	public abstract Array getArray(String arg0) throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getAsciiStream(int)
	 */
	@Override
	public abstract InputStream getAsciiStream(int arg0) throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getAsciiStream(java.lang.String)
	 */
	@Override
	public abstract InputStream getAsciiStream(String arg0) throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getBigDecimal(int)
	 */
	@Override
	public abstract BigDecimal getBigDecimal(int arg0) throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getBigDecimal(java.lang.String)
	 */
	@Override
	public abstract BigDecimal getBigDecimal(String arg0) throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getBinaryStream(int)
	 */
	@Override
	public abstract InputStream getBinaryStream(int arg0) throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getBinaryStream(java.lang.String)
	 */
	@Override
	public abstract InputStream getBinaryStream(String arg0) throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getBlob(int)
	 */
	@Override
	public abstract Blob getBlob(int arg0) throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getBlob(java.lang.String)
	 */
	@Override
	public abstract Blob getBlob(String arg0) throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getBoolean(int)
	 */
	@Override
	public abstract boolean getBoolean(int arg0) throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getBoolean(java.lang.String)
	 */
	@Override
	public abstract boolean getBoolean(String arg0) throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getByte(int)
	 */
	@Override
	public abstract byte getByte(int arg0) throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getByte(java.lang.String)
	 */
	@Override
	public abstract byte getByte(String arg0) throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getBytes(int)
	 */
	@Override
	public abstract byte[] getBytes(int arg0) throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getBytes(java.lang.String)
	 */
	@Override
	public abstract byte[] getBytes(String arg0) throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getCharacterStream(int)
	 */
	@Override
	public abstract Reader getCharacterStream(int arg0) throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getCharacterStream(java.lang.String)
	 */
	@Override
	public abstract Reader getCharacterStream(String arg0) throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getClob(int)
	 */
	@Override
	public abstract Clob getClob(int arg0) throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getClob(java.lang.String)
	 */
	@Override
	public abstract Clob getClob(String arg0) throws SQLException;

	/**
	 * Gets the concurrency mode for the result set. In this case, the concurrency
	 * defaults to read-only.
	 */
	@Override
	public int getConcurrency() throws SQLException {
		return ResultSet.CONCUR_READ_ONLY;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getCursorName()
	 */
	@Override
	public String getCursorName() throws SQLException {
		throw new SQLFeatureNotSupportedException();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getDate(int)
	 */
	@Override
	public abstract Date getDate(final int arg0) throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getDate(java.lang.String)
	 */
	@Override
	public abstract Date getDate(final String arg0) throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getDate(int, java.util.Calendar)
	 */
	@Override
	public abstract Date getDate(final int arg0, final Calendar arg1) throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getDate(java.lang.String, java.util.Calendar)
	 */
	@Override
	public abstract Date getDate(final String arg0, final Calendar arg1) throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getDouble(int)
	 */
	@Override
	public abstract double getDouble(final int arg0) throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getDouble(java.lang.String)
	 */
	@Override
	public abstract double getDouble(final String arg0) throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getFetchDirection()
	 */
	@Override
	public int getFetchDirection() throws SQLException {
		return ResultSet.FETCH_FORWARD;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getFetchSize()
	 */
	@Override
	public int getFetchSize() throws SQLException {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getFloat(int)
	 */
	@Override
	public abstract float getFloat(final int arg0) throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getFloat(java.lang.String)
	 */
	@Override
	public abstract float getFloat(final String arg0) throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getHoldability()
	 */
	@Override
	public int getHoldability() throws SQLException {
		throw new SQLFeatureNotSupportedException();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getInt(int)
	 */
	@Override
	public abstract int getInt(final int arg0) throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getInt(java.lang.String)
	 */
	@Override
	public abstract int getInt(final String arg0) throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getLong(int)
	 */
	@Override
	public abstract long getLong(final int arg0) throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getLong(java.lang.String)
	 */
	@Override
	public abstract long getLong(final String arg0) throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getMetaData()
	 */
	@Override
	public ResultSetMetaData getMetaData() throws SQLException {
		throw new SQLFeatureNotSupportedException();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getNCharacterStream(int)
	 */
	@Override
	public abstract Reader getNCharacterStream(final int arg0) throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getNCharacterStream(java.lang.String)
	 */
	@Override
	public abstract Reader getNCharacterStream(final String arg0) throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getNClob(int)
	 */
	@Override
	public abstract NClob getNClob(final int arg0) throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getNClob(java.lang.String)
	 */
	@Override
	public abstract NClob getNClob(final String arg0) throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getNString(int)
	 */
	@Override
	public abstract String getNString(final int arg0) throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getNString(java.lang.String)
	 */
	@Override
	public abstract String getNString(final String arg0) throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getObject(int)
	 */
	@Override
	public abstract Object getObject(final int arg0) throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getObject(java.lang.String)
	 */
	@Override
	public abstract Object getObject(final String arg0) throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getObject(int, java.util.Map)
	 */
	@Override
	public abstract Object getObject(final int arg0, final Map<String, Class<?>> arg1) throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getObject(java.lang.String, java.util.Map)
	 */
	@Override
	public abstract Object getObject(final String arg0, final Map<String, Class<?>> arg1) throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getRef(int)
	 */
	@Override
	public Ref getRef(final int arg0) throws SQLException {
		throw new SQLFeatureNotSupportedException();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getRef(java.lang.String)
	 */
	@Override
	public Ref getRef(final String arg0) throws SQLException {
		throw new SQLFeatureNotSupportedException();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getRow()
	 */
	@Override
	public abstract int getRow() throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getRowId(int)
	 */
	@Override
	public RowId getRowId(final int arg0) throws SQLException {
		throw new SQLFeatureNotSupportedException();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getRowId(java.lang.String)
	 */
	@Override
	public RowId getRowId(final String arg0) throws SQLException {
		throw new SQLFeatureNotSupportedException();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getSQLXML(int)
	 */
	@Override
	public abstract SQLXML getSQLXML(final int arg0) throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getSQLXML(java.lang.String)
	 */
	@Override
	public abstract SQLXML getSQLXML(final String arg0) throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getShort(int)
	 */
	@Override
	public abstract short getShort(final int arg0) throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getShort(java.lang.String)
	 */
	@Override
	public abstract short getShort(final String arg0) throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getStatement()
	 */
	@Override
	public abstract Statement getStatement() throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getString(int)
	 */
	@Override
	public abstract String getString(final int arg0) throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getString(java.lang.String)
	 */
	@Override
	public abstract String getString(final String arg0) throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getTime(int)
	 */
	@Override
	public abstract Time getTime(final int arg0) throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getTime(java.lang.String)
	 */
	@Override
	public abstract Time getTime(final String arg0) throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getTime(int, java.util.Calendar)
	 */
	@Override
	public abstract Time getTime(final int arg0, final Calendar arg1) throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getTime(java.lang.String, java.util.Calendar)
	 */
	@Override
	public abstract Time getTime(final String arg0, final Calendar arg1) throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getTimestamp(int)
	 */
	@Override
	public abstract Timestamp getTimestamp(final int arg0) throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getTimestamp(java.lang.String)
	 */
	@Override
	public abstract Timestamp getTimestamp(final String arg0) throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getTimestamp(int, java.util.Calendar)
	 */
	@Override
	public abstract Timestamp getTimestamp(final int arg0, final Calendar arg1) throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getTimestamp(java.lang.String, java.util.Calendar)
	 */
	@Override
	public abstract Timestamp getTimestamp(final String arg0, final Calendar arg1) throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getType()
	 */
	@Override
	public int getType() throws SQLException {
		return ResultSet.TYPE_FORWARD_ONLY;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getURL(int)
	 */
	@Override
	public abstract URL getURL(final int arg0) throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getURL(java.lang.String)
	 */
	@Override
	public abstract URL getURL(final String arg0) throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#getWarnings()
	 */
	@Override
	public SQLWarning getWarnings() throws SQLException {
		throw new SQLFeatureNotSupportedException();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#insertRow()
	 */
	@Override
	public void insertRow() throws SQLException {
		throw new SQLFeatureNotSupportedException();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#isAfterLast()
	 */
	@Override
	public abstract boolean isAfterLast() throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#isBeforeFirst()
	 */
	@Override
	public abstract boolean isBeforeFirst() throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#isClosed()
	 */
	@Override
	public abstract boolean isClosed() throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#isFirst()
	 */
	@Override
	public abstract boolean isFirst() throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#isLast()
	 */
	@Override
	public abstract boolean isLast() throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#last()
	 */
	@Override
	public abstract boolean last() throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#moveToCurrentRow()
	 */
	@Override
	public void moveToCurrentRow() throws SQLException {
		throw new SQLFeatureNotSupportedException();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#moveToInsertRow()
	 */
	@Override
	public void moveToInsertRow() throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#next()
	 */
	@Override
	public abstract boolean next() throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#previous()
	 */
	@Override
	public abstract boolean previous() throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#refreshRow()
	 */
	@Override
	public void refreshRow() throws SQLException {
		throw new SQLFeatureNotSupportedException();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#relative(int)
	 */
	@Override
	public abstract boolean relative(final int arg0) throws SQLException;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#rowDeleted()
	 */
	@Override
	public boolean rowDeleted() throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#rowInserted()
	 */
	@Override
	public boolean rowInserted() throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#rowUpdated()
	 */
	@Override
	public boolean rowUpdated() throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#setFetchDirection(int)
	 */
	@Override
	public void setFetchDirection(final int arg0) throws SQLException {
		throw new SQLFeatureNotSupportedException();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#setFetchSize(int)
	 */
	@Override
	public void setFetchSize(final int arg0) throws SQLException {
		throw new SQLFeatureNotSupportedException();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateArray(int, java.sql.Array)
	 */
	@Override
	public void updateArray(final int arg0, final Array arg1) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateArray(java.lang.String, java.sql.Array)
	 */
	@Override
	public void updateArray(final String arg0, final Array arg1) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateAsciiStream(int, java.io.InputStream)
	 */
	@Override
	public void updateAsciiStream(final int arg0, final InputStream arg1) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateAsciiStream(java.lang.String,
	 * java.io.InputStream)
	 */
	@Override
	public void updateAsciiStream(final String arg0, final InputStream arg1) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateAsciiStream(int, java.io.InputStream, int)
	 */
	@Override
	public void updateAsciiStream(final int arg0, final InputStream arg1, final int arg2) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateAsciiStream(java.lang.String,
	 * java.io.InputStream, int)
	 */
	@Override
	public void updateAsciiStream(final String arg0, final InputStream arg1, final int arg2) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateAsciiStream(int, java.io.InputStream, long)
	 */
	@Override
	public void updateAsciiStream(final int arg0, final InputStream arg1, final long arg2) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateAsciiStream(java.lang.String,
	 * java.io.InputStream, long)
	 */
	@Override
	public void updateAsciiStream(final String arg0, final InputStream arg1, final long arg2) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateBigDecimal(int, java.math.BigDecimal)
	 */
	@Override
	public void updateBigDecimal(final int arg0, final BigDecimal arg1) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateBigDecimal(java.lang.String,
	 * java.math.BigDecimal)
	 */
	@Override
	public void updateBigDecimal(final String arg0, final BigDecimal arg1) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateBinaryStream(int, java.io.InputStream)
	 */
	@Override
	public void updateBinaryStream(final int arg0, final InputStream arg1) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateBinaryStream(java.lang.String,
	 * java.io.InputStream)
	 */
	@Override
	public void updateBinaryStream(final String arg0, final InputStream arg1) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateBinaryStream(int, java.io.InputStream, int)
	 */
	@Override
	public void updateBinaryStream(final int arg0, final InputStream arg1, final int arg2) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateBinaryStream(java.lang.String,
	 * java.io.InputStream, int)
	 */
	@Override
	public void updateBinaryStream(final String arg0, final InputStream arg1, final int arg2) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateBinaryStream(int, java.io.InputStream, long)
	 */
	@Override
	public void updateBinaryStream(final int arg0, final InputStream arg1, final long arg2) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateBinaryStream(java.lang.String,
	 * java.io.InputStream, long)
	 */
	@Override
	public void updateBinaryStream(final String arg0, final InputStream arg1, final long arg2) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateBlob(int, java.sql.Blob)
	 */
	@Override
	public void updateBlob(final int arg0, final Blob arg1) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateBlob(java.lang.String, java.sql.Blob)
	 */
	@Override
	public void updateBlob(final String arg0, final Blob arg1) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateBlob(int, java.io.InputStream)
	 */
	@Override
	public void updateBlob(final int arg0, final InputStream arg1) throws SQLException {
		throw new SQLFeatureNotSupportedException();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateBlob(java.lang.String, java.io.InputStream)
	 */
	@Override
	public void updateBlob(final String arg0, final InputStream arg1) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateBlob(int, java.io.InputStream, long)
	 */
	@Override
	public void updateBlob(final int arg0, final InputStream arg1, final long arg2) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateBlob(java.lang.String, java.io.InputStream,
	 * long)
	 */
	@Override
	public void updateBlob(final String arg0, final InputStream arg1, final long arg2) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateBoolean(int, boolean)
	 */
	@Override
	public void updateBoolean(final int arg0, final boolean arg1) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateBoolean(java.lang.String, boolean)
	 */
	@Override
	public void updateBoolean(final String arg0, final boolean arg1) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateByte(int, byte)
	 */
	@Override
	public void updateByte(final int arg0, final byte arg1) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateByte(java.lang.String, byte)
	 */
	@Override
	public void updateByte(final String arg0, final byte arg1) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateBytes(int, byte[])
	 */
	@Override
	public void updateBytes(final int arg0, final byte[] arg1) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateBytes(java.lang.String, byte[])
	 */
	@Override
	public void updateBytes(final String arg0, final byte[] arg1) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateCharacterStream(int, java.io.Reader)
	 */
	@Override
	public void updateCharacterStream(final int arg0, final Reader arg1) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateCharacterStream(java.lang.String,
	 * java.io.Reader)
	 */
	@Override
	public void updateCharacterStream(final String arg0, final Reader arg1) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateCharacterStream(int, java.io.Reader, int)
	 */
	@Override
	public void updateCharacterStream(final int arg0, final Reader arg1, final int arg2) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateCharacterStream(java.lang.String,
	 * java.io.Reader, int)
	 */
	@Override
	public void updateCharacterStream(final String arg0, final Reader arg1, final int arg2) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateCharacterStream(int, java.io.Reader, long)
	 */
	@Override
	public void updateCharacterStream(final int arg0, final Reader arg1, final long arg2) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateCharacterStream(java.lang.String,
	 * java.io.Reader, long)
	 */
	@Override
	public void updateCharacterStream(final String arg0, final Reader arg1, final long arg2) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateClob(int, java.sql.Clob)
	 */
	@Override
	public void updateClob(final int arg0, final Clob arg1) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateClob(java.lang.String, java.sql.Clob)
	 */
	@Override
	public void updateClob(final String arg0, final Clob arg1) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateClob(int, java.io.Reader)
	 */
	@Override
	public void updateClob(final int arg0, final Reader arg1) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateClob(java.lang.String, java.io.Reader)
	 */
	@Override
	public void updateClob(final String arg0, final Reader arg1) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateClob(int, java.io.Reader, long)
	 */
	@Override
	public void updateClob(final int arg0, final Reader arg1, final long arg2) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateClob(java.lang.String, java.io.Reader, long)
	 */
	@Override
	public void updateClob(final String arg0, final Reader arg1, final long arg2) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateDate(int, java.sql.Date)
	 */
	@Override
	public void updateDate(final int arg0, final Date arg1) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateDate(java.lang.String, java.sql.Date)
	 */
	@Override
	public void updateDate(final String arg0, final Date arg1) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateDouble(int, double)
	 */
	@Override
	public void updateDouble(final int arg0, final double arg1) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateDouble(java.lang.String, double)
	 */
	@Override
	public void updateDouble(final String arg0, final double arg1) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateFloat(int, float)
	 */
	@Override
	public void updateFloat(final int arg0, final float arg1) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateFloat(java.lang.String, float)
	 */
	@Override
	public void updateFloat(final String arg0, final float arg1) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateInt(int, int)
	 */
	@Override
	public void updateInt(final int arg0, final int arg1) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateInt(java.lang.String, int)
	 */
	@Override
	public void updateInt(final String arg0, final int arg1) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateLong(int, long)
	 */
	@Override
	public void updateLong(final int arg0, final long arg1) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateLong(java.lang.String, long)
	 */
	@Override
	public void updateLong(final String arg0, final long arg1) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateNCharacterStream(int, java.io.Reader)
	 */
	@Override
	public void updateNCharacterStream(final int arg0, final Reader arg1) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateNCharacterStream(java.lang.String,
	 * java.io.Reader)
	 */
	@Override
	public void updateNCharacterStream(final String arg0, final Reader arg1) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateNCharacterStream(int, java.io.Reader, long)
	 */
	@Override
	public void updateNCharacterStream(final int arg0, final Reader arg1, final long arg2) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateNCharacterStream(java.lang.String,
	 * java.io.Reader, long)
	 */
	@Override
	public void updateNCharacterStream(final String arg0, final Reader arg1, final long arg2) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateNClob(int, java.sql.NClob)
	 */
	@Override
	public void updateNClob(final int arg0, final NClob arg1) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateNClob(java.lang.String, java.sql.NClob)
	 */
	@Override
	public void updateNClob(final String arg0, final NClob arg1) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateNClob(int, java.io.Reader)
	 */
	@Override
	public void updateNClob(final int arg0, final Reader arg1) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateNClob(java.lang.String, java.io.Reader)
	 */
	@Override
	public void updateNClob(final String arg0, final Reader arg1) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateNClob(int, java.io.Reader, long)
	 */
	@Override
	public void updateNClob(final int arg0, final Reader arg1, final long arg2) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateNClob(java.lang.String, java.io.Reader, long)
	 */
	@Override
	public void updateNClob(final String arg0, final Reader arg1, final long arg2) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateNString(int, java.lang.String)
	 */
	@Override
	public void updateNString(final int arg0, final String arg1) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateNString(java.lang.String, java.lang.String)
	 */
	@Override
	public void updateNString(final String arg0, final String arg1) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateNull(int)
	 */
	@Override
	public void updateNull(final int arg0) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateNull(java.lang.String)
	 */
	@Override
	public void updateNull(final String arg0) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateObject(int, java.lang.Object)
	 */
	@Override
	public void updateObject(final int arg0, final Object arg1) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateObject(java.lang.String, java.lang.Object)
	 */
	@Override
	public void updateObject(final String arg0, final Object arg1) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateObject(int, java.lang.Object, int)
	 */
	@Override
	public void updateObject(final int arg0, final Object arg1, final int arg2) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateObject(java.lang.String, java.lang.Object, int)
	 */
	@Override
	public void updateObject(final String arg0, final Object arg1, final int arg2) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateRef(int, java.sql.Ref)
	 */
	@Override
	public void updateRef(final int arg0, final Ref arg1) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateRef(java.lang.String, java.sql.Ref)
	 */
	@Override
	public void updateRef(final String arg0, final Ref arg1) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateRow()
	 */
	@Override
	public void updateRow() throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateRowId(int, java.sql.RowId)
	 */
	@Override
	public void updateRowId(final int arg0, final RowId arg1) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateRowId(java.lang.String, java.sql.RowId)
	 */
	@Override
	public void updateRowId(final String arg0, final RowId arg1) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateSQLXML(int, java.sql.SQLXML)
	 */
	@Override
	public void updateSQLXML(final int arg0, final SQLXML arg1) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateSQLXML(java.lang.String, java.sql.SQLXML)
	 */
	@Override
	public void updateSQLXML(final String arg0, final SQLXML arg1) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateShort(int, short)
	 */
	@Override
	public void updateShort(final int arg0, final short arg1) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateShort(java.lang.String, short)
	 */
	@Override
	public void updateShort(final String arg0, final short arg1) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateString(int, java.lang.String)
	 */
	@Override
	public void updateString(final int arg0, final String arg1) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateString(java.lang.String, java.lang.String)
	 */
	@Override
	public void updateString(final String arg0, final String arg1) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateTime(int, java.sql.Time)
	 */
	@Override
	public void updateTime(final int arg0, final Time arg1) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateTime(java.lang.String, java.sql.Time)
	 */
	@Override
	public void updateTime(final String arg0, final Time arg1) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateTimestamp(int, java.sql.Timestamp)
	 */
	@Override
	public void updateTimestamp(final int arg0, final Timestamp arg1) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#updateTimestamp(java.lang.String, java.sql.Timestamp)
	 */
	@Override
	public void updateTimestamp(final String arg0, final Timestamp arg1) throws SQLException {
		throw new SQLFeatureNotSupportedException();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.sql.ResultSet#wasNull()
	 */
	@Override
	public abstract boolean wasNull() throws SQLException;
}
