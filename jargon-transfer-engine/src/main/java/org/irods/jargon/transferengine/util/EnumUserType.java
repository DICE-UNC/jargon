/**
 * 
 */
package org.irods.jargon.transferengine.util;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.type.NullableType;
import org.hibernate.type.TypeFactory;
import org.hibernate.usertype.ParameterizedType;
import org.hibernate.usertype.UserType;

public class EnumUserType implements UserType, ParameterizedType {
	private static final String DEFAULT_IDENTIFIER_METHOD_NAME = "name";
	private static final String DEFAULT_VALUE_OF_METHOD_NAME = "valueOf";

	private Class<? extends Enum> enumClass;
	private Class<?> identifierType;
	private Method identifierMethod;
	private Method valueOfMethod;
	private NullableType type;
	private int[] sqlTypes;

	@Override
	public void setParameterValues(final Properties parameters) {
		String enumClassName = parameters.getProperty("enumClassName");
		try {
			enumClass = Class.forName(enumClassName).asSubclass(Enum.class);
		} catch (ClassNotFoundException cfne) {
			throw new HibernateException("Enum class not found", cfne);
		}

		String identifierMethodName = parameters.getProperty(
				"identifierMethod", DEFAULT_IDENTIFIER_METHOD_NAME);

		try {
			identifierMethod = enumClass.getMethod(identifierMethodName,
					new Class[0]);
			identifierType = identifierMethod.getReturnType();
		} catch (Exception e) {
			throw new HibernateException("Failed to obtain identifier method",
					e);
		}

		type = (NullableType) TypeFactory.basic(identifierType.getName());

		if (type == null) {
			throw new HibernateException("Unsupported identifier type "
					+ identifierType.getName());
		}

		sqlTypes = new int[] { type.sqlType() };

		String valueOfMethodName = parameters.getProperty("valueOfMethod",
				DEFAULT_VALUE_OF_METHOD_NAME);

		try {
			valueOfMethod = enumClass.getMethod(valueOfMethodName,
					new Class[] { identifierType });
		} catch (Exception e) {
			throw new HibernateException("Failed to obtain valueOf method", e);
		}
	}

	@Override
	public Class returnedClass() {
		return enumClass;
	}

	@Override
	public Object nullSafeGet(final ResultSet rs, final String[] names,
			final Object owner) throws HibernateException, SQLException {
		Object identifier = type.get(rs, names[0]);
		if (rs.wasNull()) {
			return null;
		}

		try {
			return valueOfMethod.invoke(enumClass, new Object[] { identifier });
		} catch (Exception e) {
			throw new HibernateException(
					"Exception while invoking valueOf method '"
							+ valueOfMethod.getName() + "' of "
							+ "enumeration class '" + enumClass + "'", e);
		}
	}

	@Override
	public void nullSafeSet(final PreparedStatement st, final Object value,
			final int index) throws HibernateException, SQLException {
		try {
			if (value == null) {
				st.setNull(index, type.sqlType());
			} else {
				Object identifier = identifierMethod.invoke(value,
						new Object[0]);
				type.set(st, identifier, index);
			}
		} catch (Exception e) {
			throw new HibernateException(
					"Exception while invoking identifierMethod '"
							+ identifierMethod.getName() + "' of "
							+ "enumeration class '" + enumClass + "'", e);
		}
	}

	@Override
	public int[] sqlTypes() {
		return sqlTypes;
	}

	@Override
	public Object assemble(final Serializable cached, final Object owner)
			throws HibernateException {
		return cached;
	}

	@Override
	public Object deepCopy(final Object value) throws HibernateException {
		return value;
	}

	@Override
	public Serializable disassemble(final Object value)
			throws HibernateException {
		return (Serializable) value;
	}

	@Override
	public boolean equals(final Object x, final Object y)
			throws HibernateException {
		return x == y;
	}

	@Override
	public int hashCode(final Object x) throws HibernateException {
		return x.hashCode();
	}

	@Override
	public boolean isMutable() {
		return false;
	}

	@Override
	public Object replace(final Object original, final Object target,
			final Object owner) throws HibernateException {
		return original;
	}
}
