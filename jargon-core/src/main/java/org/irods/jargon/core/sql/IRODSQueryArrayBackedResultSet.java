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
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

/**
 * Version of a <code>java.sql.ResultSet</code> based on the results of an iRODS query using one of the varous query techniques.  The
 * <code>ResultSet</code> is based on data parsed into an array of <code>String</code> data, and includes other information about the generating
 * query, and the capability to page the results.
 * <p/>
 * Notes: 
 * <p/>
 * This might need to be abstract with subclasses for the various query types, need to consider whether this is stateful (page results) or stateless (use offsets).
 * 
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public abstract class IRODSQueryArrayBackedResultSet extends AbstractIRODSSqlResultSet {

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#absolute(int)
	 */
	@Override
	public abstract boolean absolute(int arg0) throws SQLException;

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#afterLast()
	 */
	@Override
	public abstract void afterLast() throws SQLException;

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#beforeFirst()
	 */
	@Override
	public abstract void beforeFirst() throws SQLException;

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#findColumn(java.lang.String)
	 */
	@Override
	public int findColumn(String arg0) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#first()
	 */
	@Override
	public boolean first() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getArray(int)
	 */
	@Override
	public Array getArray(int arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getArray(java.lang.String)
	 */
	@Override
	public Array getArray(String arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getAsciiStream(int)
	 */
	@Override
	public InputStream getAsciiStream(int arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getAsciiStream(java.lang.String)
	 */
	@Override
	public InputStream getAsciiStream(String arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getBigDecimal(int)
	 */
	@Override
	public BigDecimal getBigDecimal(int arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getBigDecimal(java.lang.String)
	 */
	@Override
	public BigDecimal getBigDecimal(String arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getBigDecimal(int, int)
	 */
	@Override
	public BigDecimal getBigDecimal(int arg0, int arg1) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getBigDecimal(java.lang.String, int)
	 */
	@Override
	public BigDecimal getBigDecimal(String arg0, int arg1) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getBinaryStream(int)
	 */
	@Override
	public InputStream getBinaryStream(int arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getBinaryStream(java.lang.String)
	 */
	@Override
	public InputStream getBinaryStream(String arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getBlob(int)
	 */
	@Override
	public Blob getBlob(int arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getBlob(java.lang.String)
	 */
	@Override
	public Blob getBlob(String arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getBoolean(int)
	 */
	@Override
	public boolean getBoolean(int arg0) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getBoolean(java.lang.String)
	 */
	@Override
	public boolean getBoolean(String arg0) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getByte(int)
	 */
	@Override
	public byte getByte(int arg0) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getByte(java.lang.String)
	 */
	@Override
	public byte getByte(String arg0) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getBytes(int)
	 */
	@Override
	public byte[] getBytes(int arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getBytes(java.lang.String)
	 */
	@Override
	public byte[] getBytes(String arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getCharacterStream(int)
	 */
	@Override
	public Reader getCharacterStream(int arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getCharacterStream(java.lang.String)
	 */
	@Override
	public Reader getCharacterStream(String arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getClob(int)
	 */
	@Override
	public Clob getClob(int arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getClob(java.lang.String)
	 */
	@Override
	public Clob getClob(String arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getDate(int)
	 */
	@Override
	public Date getDate(int arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getDate(java.lang.String)
	 */
	@Override
	public Date getDate(String arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getDate(int, java.util.Calendar)
	 */
	@Override
	public Date getDate(int arg0, Calendar arg1) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getDate(java.lang.String, java.util.Calendar)
	 */
	@Override
	public Date getDate(String arg0, Calendar arg1) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getDouble(int)
	 */
	@Override
	public double getDouble(int arg0) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getDouble(java.lang.String)
	 */
	@Override
	public double getDouble(String arg0) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getFloat(int)
	 */
	@Override
	public float getFloat(int arg0) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getFloat(java.lang.String)
	 */
	@Override
	public float getFloat(String arg0) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getInt(int)
	 */
	@Override
	public int getInt(int arg0) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getInt(java.lang.String)
	 */
	@Override
	public int getInt(String arg0) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getLong(int)
	 */
	@Override
	public long getLong(int arg0) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getLong(java.lang.String)
	 */
	@Override
	public long getLong(String arg0) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getNCharacterStream(int)
	 */
	@Override
	public Reader getNCharacterStream(int arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getNCharacterStream(java.lang.String)
	 */
	@Override
	public Reader getNCharacterStream(String arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getNClob(int)
	 */
	@Override
	public NClob getNClob(int arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getNClob(java.lang.String)
	 */
	@Override
	public NClob getNClob(String arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getNString(int)
	 */
	@Override
	public String getNString(int arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getNString(java.lang.String)
	 */
	@Override
	public String getNString(String arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getObject(int)
	 */
	@Override
	public Object getObject(int arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getObject(java.lang.String)
	 */
	@Override
	public Object getObject(String arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getObject(int, java.util.Map)
	 */
	@Override
	public Object getObject(int arg0, Map<String, Class<?>> arg1)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getObject(java.lang.String, java.util.Map)
	 */
	@Override
	public Object getObject(String arg0, Map<String, Class<?>> arg1)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getRow()
	 */
	@Override
	public int getRow() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getSQLXML(int)
	 */
	@Override
	public SQLXML getSQLXML(int arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getSQLXML(java.lang.String)
	 */
	@Override
	public SQLXML getSQLXML(String arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getShort(int)
	 */
	@Override
	public short getShort(int arg0) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getShort(java.lang.String)
	 */
	@Override
	public short getShort(String arg0) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getStatement()
	 */
	@Override
	public Statement getStatement() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getString(int)
	 */
	@Override
	public String getString(int arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getString(java.lang.String)
	 */
	@Override
	public String getString(String arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getTime(int)
	 */
	@Override
	public Time getTime(int arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getTime(java.lang.String)
	 */
	@Override
	public Time getTime(String arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getTime(int, java.util.Calendar)
	 */
	@Override
	public Time getTime(int arg0, Calendar arg1) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getTime(java.lang.String, java.util.Calendar)
	 */
	@Override
	public Time getTime(String arg0, Calendar arg1) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getTimestamp(int)
	 */
	@Override
	public Timestamp getTimestamp(int arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getTimestamp(java.lang.String)
	 */
	@Override
	public Timestamp getTimestamp(String arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getTimestamp(int, java.util.Calendar)
	 */
	@Override
	public Timestamp getTimestamp(int arg0, Calendar arg1) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getTimestamp(java.lang.String, java.util.Calendar)
	 */
	@Override
	public Timestamp getTimestamp(String arg0, Calendar arg1)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getURL(int)
	 */
	@Override
	public URL getURL(int arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getURL(java.lang.String)
	 */
	@Override
	public URL getURL(String arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getUnicodeStream(int)
	 */
	@Override
	public InputStream getUnicodeStream(int arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#getUnicodeStream(java.lang.String)
	 */
	@Override
	public InputStream getUnicodeStream(String arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#isAfterLast()
	 */
	@Override
	public boolean isAfterLast() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#isBeforeFirst()
	 */
	@Override
	public boolean isBeforeFirst() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#isClosed()
	 */
	@Override
	public boolean isClosed() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#isFirst()
	 */
	@Override
	public boolean isFirst() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#isLast()
	 */
	@Override
	public boolean isLast() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#last()
	 */
	@Override
	public boolean last() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#next()
	 */
	@Override
	public boolean next() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#previous()
	 */
	@Override
	public boolean previous() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#relative(int)
	 */
	@Override
	public boolean relative(int arg0) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.sql.AbstractIRODSQueryResultSet#wasNull()
	 */
	@Override
	public boolean wasNull() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

}
