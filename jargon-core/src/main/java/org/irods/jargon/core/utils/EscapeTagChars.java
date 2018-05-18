/**
 * Utility to escape characters in xml packing instructions for transmission to iRODS
 */
package org.irods.jargon.core.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public final class EscapeTagChars {

	/**
	 * Escape characters for text appearing in Tag markup.
	 * 
	 * @param aText
	 *            {@code String} to escape
	 * @return {@code String} escaped
	 *
	 */
	public static String forHTML(final String aText) {
		final StringBuilder result = new StringBuilder();
		final StringCharacterIterator iterator = new StringCharacterIterator(aText);
		char character = iterator.current();
		while (character != CharacterIterator.DONE) {
			if (character == '<') {
				result.append("&lt;");
			} else if (character == '>') {
				result.append("&gt;");
			} else if (character == '&') {
				result.append("&amp;");
			} else if (character == '\"') {
				result.append("&quot;");
			} else if (character == '\t') {
				addCharEntity(9, result);
			} else if (character == '!') {
				addCharEntity(33, result);
			} else if (character == '#') {
				addCharEntity(35, result);
			} else if (character == '$') {
				addCharEntity(36, result);
			} else if (character == '%') {
				addCharEntity(37, result);
			} else if (character == '\'') {
				addCharEntity(39, result);
			} else if (character == '(') {
				addCharEntity(40, result);
			} else if (character == ')') {
				addCharEntity(41, result);
			} else if (character == '*') {
				addCharEntity(42, result);
			} else if (character == '+') {
				addCharEntity(43, result);
			} else if (character == ',') {
				addCharEntity(44, result);
			} else if (character == '-') {
				addCharEntity(45, result);
			} else if (character == '.') {
				addCharEntity(46, result);
			} else if (character == '/') {
				addCharEntity(47, result);
			} else if (character == ':') {
				addCharEntity(58, result);
			} else if (character == ';') {
				addCharEntity(59, result);
			} else if (character == '=') {
				addCharEntity(61, result);
			} else if (character == '?') {
				addCharEntity(63, result);
			} else if (character == '@') {
				addCharEntity(64, result);
			} else if (character == '[') {
				addCharEntity(91, result);
			} else if (character == '\\') {
				addCharEntity(92, result);
			} else if (character == ']') {
				addCharEntity(93, result);
			} else if (character == '^') {
				addCharEntity(94, result);
			} else if (character == '_') {
				addCharEntity(95, result);
			} else if (character == '`') {
				addCharEntity(96, result);
			} else if (character == '{') {
				addCharEntity(123, result);
			} else if (character == '|') {
				addCharEntity(124, result);
			} else if (character == '}') {
				addCharEntity(125, result);
			} else if (character == '~') {
				addCharEntity(126, result);
			} else {
				// the char is not a special one
				// add it to the result as is
				result.append(character);
			}
			character = iterator.next();
		}
		return result.toString();
	}

	/**
	 * Escape all ampersand characters in a URL.
	 *
	 * <P>
	 * Replaces all {@code '&'} characters with {@code '&amp;'}.
	 *
	 * <P>
	 * An ampersand character may appear in the query string of a URL. The ampersand
	 * character is indeed valid in a URL. <em>However, URLs usually appear as an
	 * {@code HREF} attribute, and such attributes have the additional constraint
	 * that ampersands must be escaped.</em>
	 *
	 * <P>
	 * The JSTL {@code <c:url>} tag does indeed perform proper URL encoding of query
	 * parameters. But it does not, in general, produce text which is valid as an
	 * {@code HREF} attribute, simply because it does not escape the ampersand
	 * character. This is a nuisance when multiple query parameters appear in the
	 * URL, since it requires a little extra work.
	 * 
	 * @param aURL
	 *            {@code String} with the parameter to escape
	 * @return {@code String} with the result
	 */
	public static String forHrefAmpersand(final String aURL) {
		return aURL.replace("&", "&amp;");
	}

	/**
	 * Synonym for {@code URLEncoder.encode(String, "UTF-8")}.
	 *
	 * <P>
	 * Used to ensure that HTTP query strings are in proper form, by escaping
	 * special characters such as spaces.
	 *
	 * <P>
	 * It is important to note that if a query string appears in an {@code HREF}
	 * attribute, then there are two issues - ensuring the query string is valid
	 * HTTP (it is URL-encoded), and ensuring it is valid HTML (ensuring the
	 * ampersand is escaped).
	 * 
	 * @param aURLFragment
	 *            {@code String} to escape
	 * @return {@code String} escaped
	 */
	public static String forURL(final String aURLFragment) {
		String result = null;
		try {
			result = URLEncoder.encode(aURLFragment, "UTF-8");
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException("UTF-8 not supported", ex);
		}
		return result;
	}

	/**
	 * Escape characters for text appearing as XML data, between tags.
	 *
	 * <P>
	 * The following characters are replaced with corresponding character entities :
	 * <table border='1' cellpadding='3' cellspacing='0'>
	 * <caption>character replacements</caption>
	 * <tr>
	 * <th>Character</th>
	 * <th>Encoding</th>
	 * </tr>
	 * <tr>
	 * <td>&lt;</td>
	 * <td>&amp;lt;</td>
	 * </tr>
	 * <tr>
	 * <td>&gt;</td>
	 * <td>&amp;gt;</td>
	 * </tr>
	 * <tr>
	 * <td>&amp;</td>
	 * <td>&amp;amp;</td>
	 * </tr>
	 * <tr>
	 * <td>"</td>
	 * <td>&quot;</td>
	 * </tr>
	 * <tr>
	 * <td>'</td>
	 * <td>&#039;</td>
	 * </tr>
	 * </table>
	 *
	 * <P>
	 * Note that JSTL's {@code <c:out>} escapes the exact same set of characters as
	 * this method. <span class='highlight'>That is, {@code <c:out>} is good for
	 * escaping to produce valid XML, but not for producing safe HTML.</span>
	 * 
	 * @param aText
	 *            {@code String} xml
	 * @return {@code String} escaped
	 */
	public static String forXML(final String aText) {
		final StringBuilder result = new StringBuilder();
		final StringCharacterIterator iterator = new StringCharacterIterator(aText);
		char character = iterator.current();
		while (character != CharacterIterator.DONE) {
			if (character == '<') {
				result.append("&lt;");
			} else if (character == '>') {
				result.append("&gt;");
			} else if (character == '\"') {
				result.append("&quot;");
			} else if (character == '`') {
				result.append("&apos;");
			} else if (character == '&') {
				result.append("&amp;");
			} else {
				// the char is not a special one
				// add it to the result as is
				result.append(character);
			}
			character = iterator.next();
		}
		return result.toString();
	}

	/**
	 * Escapes characters for text appearing as data in the
	 * <a href='http://www.json.org/'>Javascript Object Notation</a> (JSON) data
	 * interchange format.
	 *
	 * <P>
	 * The following commonly used control characters are escaped :
	 * <table border='1' cellpadding='3' cellspacing='0'>
	 * <caption>escaped characters</caption>
	 * <tr>
	 * <th>Character</th>
	 * <th>Escaped As</th>
	 * </tr>
	 * <tr>
	 * <td>"</td>
	 * <td>\"</td>
	 * </tr>
	 * <tr>
	 * <td>\</td>
	 * <td>\\</td>
	 * </tr>
	 * <tr>
	 * <td>/</td>
	 * <td>\/</td>
	 * </tr>
	 * <tr>
	 * <td>back space</td>
	 * <td>\b</td>
	 * </tr>
	 * <tr>
	 * <td>form feed</td>
	 * <td>\f</td>
	 * </tr>
	 * <tr>
	 * <td>line feed</td>
	 * <td>\n</td>
	 * </tr>
	 * <tr>
	 * <td>carriage return</td>
	 * <td>\r</td>
	 * </tr>
	 * <tr>
	 * <td>tab</td>
	 * <td>\t</td>
	 * </tr>
	 * </table>
	 *
	 * <P>
	 * 
	 * @param aText
	 *            {@code String} to escape
	 * @return {@code String} with the escaped text
	 * 
	 *         See <a href='http://www.ietf.org/rfc/rfc4627.txt'>RFC 4627</a> for
	 *         more information.
	 */
	public static String forJSON(final String aText) {
		final StringBuilder result = new StringBuilder();
		StringCharacterIterator iterator = new StringCharacterIterator(aText);
		char character = iterator.current();
		while (character != CharacterIterator.DONE) {
			if (character == '\"') {
				result.append("\\\"");
			} else if (character == '\\') {
				result.append("\\\\");
			} else if (character == '/') {
				result.append("\\/");
			} else if (character == '\b') {
				result.append("\\b");
			} else if (character == '\f') {
				result.append("\\f");
			} else if (character == '\n') {
				result.append("\\n");
			} else if (character == '\r') {
				result.append("\\r");
			} else if (character == '\t') {
				result.append("\\t");
			} else {
				// the char is not a special one
				// add it to the result as is
				result.append(character);
			}
			character = iterator.next();
		}
		return result.toString();
	}

	/**
	 * Return {@code aText} with all {@code '<'} and {@code '>'} characters replaced
	 * by their escaped equivalents.
	 * 
	 * @param aText
	 *            {@code String} to escape
	 * @return {@code String} with escaped text
	 */
	public static String toDisableTags(final String aText) {
		final StringBuilder result = new StringBuilder();
		final StringCharacterIterator iterator = new StringCharacterIterator(aText);
		char character = iterator.current();
		while (character != CharacterIterator.DONE) {
			if (character == '<') {
				result.append("&lt;");
			} else if (character == '>') {
				result.append("&gt;");
			} else {
				// the char is not a special one
				// add it to the result as is
				result.append(character);
			}
			character = iterator.next();
		}
		return result.toString();
	}

	/**
	 * Replace characters having special meaning in regular expressions with their
	 * escaped equivalents, preceded by a '\' character.
	 *
	 * <P>
	 * The escaped characters include :
	 * <ul>
	 * <li>.
	 * <li>\
	 * <li>?, * , and +
	 * <li>&amp;
	 * <li>:
	 * <li>{ and }
	 * <li>[ and ]
	 * <li>( and )
	 * <li>^ and $
	 * </ul>
	 * 
	 * @param aRegexFragment
	 *            {@code String}
	 * @return {@code String}
	 */
	public static String forRegex(final String aRegexFragment) {
		final StringBuilder result = new StringBuilder();

		final StringCharacterIterator iterator = new StringCharacterIterator(aRegexFragment);
		char character = iterator.current();
		while (character != CharacterIterator.DONE) {
			/*
			 * All literals need to have backslashes doubled.
			 */
			if (character == '.') {
				result.append("\\.");
			} else if (character == '\\') {
				result.append("\\\\");
			} else if (character == '?') {
				result.append("\\?");
			} else if (character == '*') {
				result.append("\\*");
			} else if (character == '+') {
				result.append("\\+");
			} else if (character == '&') {
				result.append("\\&");
			} else if (character == ':') {
				result.append("\\:");
			} else if (character == '{') {
				result.append("\\{");
			} else if (character == '}') {
				result.append("\\}");
			} else if (character == '[') {
				result.append("\\[");
			} else if (character == ']') {
				result.append("\\]");
			} else if (character == '(') {
				result.append("\\(");
			} else if (character == ')') {
				result.append("\\)");
			} else if (character == '^') {
				result.append("\\^");
			} else if (character == '$') {
				result.append("\\$");
			} else {
				// the char is not a special one
				// add it to the result as is
				result.append(character);
			}
			character = iterator.next();
		}
		return result.toString();
	}

	/**
	 * Escape characters in replacement strings.
	 *
	 * If replacement text can contain arbitrary characters, then you will usually
	 * need to escape that text, to ensure special characters are interpreted
	 * literally.
	 * 
	 * @param aInput
	 *            {@code String}
	 * @return {@code String} escaped
	 */
	public static String forReplacementString(final String aInput) {
		return Matcher.quoteReplacement(aInput);
	}

	public static String forScriptTagsOnly(final String aText) {
		String result = null;
		Matcher matcher = SCRIPT.matcher(aText);
		result = matcher.replaceAll("&lt;SCRIPT>");
		matcher = SCRIPT_END.matcher(result);
		result = matcher.replaceAll("&lt;/SCRIPT>");
		return result;
	}

	private EscapeTagChars() {
		// empty - prevent construction
	}

	private static final Pattern SCRIPT = Pattern.compile("<SCRIPT>", Pattern.CASE_INSENSITIVE);
	private static final Pattern SCRIPT_END = Pattern.compile("</SCRIPT>", Pattern.CASE_INSENSITIVE);

	private static void addCharEntity(final Integer aIdx, final StringBuilder aBuilder) {
		String padding = "";
		if (aIdx <= 9) {
			padding = "00";
		} else if (aIdx <= 99) {
			padding = "0";
		} else {
			// no prefix
		}
		String number = padding + aIdx.toString();
		aBuilder.append("&#" + number + ";");
	}
}
