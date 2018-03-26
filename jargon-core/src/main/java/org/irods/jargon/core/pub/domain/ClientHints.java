/**
 * 
 */
package org.irods.jargon.core.pub.domain;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.protovalues.IcatTypeEnum;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * iRODS Client Hints from server JSON
 * 
 * @author Mike Conway - NIEHS
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClientHints {
	@JsonProperty("strict_acls")
	private String strictAcls = "on";
	private List<Plugin> plugins = new ArrayList<>();
	@JsonProperty("hash_scheme")
	private String hashScheme = "";
	@JsonProperty("match_hash_policy")
	private String matchHashPolicy = "";
	@JsonProperty("rules")
	private List<String> rules = new ArrayList<>();
	@JsonProperty("specific_queries")
	private List<String> specificQueries = new ArrayList<>();

	/**
	 * Look at the database plugin type and determine what type of db the ICAT is
	 * 
	 * @return {@link IcatTypeEnum} value that indicates the type of Icat database
	 */
	public IcatTypeEnum whatTypeOfIcatIsIt() {
		// find the db plugin
		String foundPlugin = null;
		for (Plugin plugin : plugins) {
			if (plugin.getType().equals("database")) {
				foundPlugin = plugin.getName();
				break;
			}
		}
		if (foundPlugin == null) {
			return IcatTypeEnum.OTHER;
		} else if (foundPlugin.equals("postgres")) {
			return IcatTypeEnum.POSTGRES;
		} else if (foundPlugin.equals("mysql")) {
			return IcatTypeEnum.MYSQL;
		} else {
			return IcatTypeEnum.OTHER;
		}

	}

	/**
	 * @return the strictAcls
	 */
	public String getStrictAcls() {
		return strictAcls;
	}

	/**
	 * @param strictAcls
	 *            the strictAcls to set
	 */
	public void setStrictAcls(String strictAcls) {
		this.strictAcls = strictAcls;
	}

	/**
	 * @return the plugins
	 */
	public List<Plugin> getPlugins() {
		return plugins;
	}

	/**
	 * @param plugins
	 *            the plugins to set
	 */
	public void setPlugins(List<Plugin> plugins) {
		this.plugins = plugins;
	}

	/**
	 * @return the hashScheme
	 */
	public String getHashScheme() {
		return hashScheme;
	}

	/**
	 * @param hashScheme
	 *            the hashScheme to set
	 */
	public void setHashScheme(String hashScheme) {
		this.hashScheme = hashScheme;
	}

	/**
	 * @return the matchHashPolicy
	 */
	public String getMatchHashPolicy() {
		return matchHashPolicy;
	}

	/**
	 * @param matchHashPolicy
	 *            the matchHashPolicy to set
	 */
	public void setMatchHashPolicy(String matchHashPolicy) {
		this.matchHashPolicy = matchHashPolicy;
	}

	/**
	 * @return the rules
	 */
	public List<String> getRules() {
		return rules;
	}

	/**
	 * @param rules
	 *            the rules to set
	 */
	public void setRules(List<String> rules) {
		this.rules = rules;
	}

	/**
	 * @return the specificQueries
	 */
	public List<String> getSpecificQueries() {
		return specificQueries;
	}

	/**
	 * @param specificQueries
	 *            the specificQueries to set
	 */
	public void setSpecificQueries(List<String> specificQueries) {
		this.specificQueries = specificQueries;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final int maxLen = 10;
		StringBuilder builder = new StringBuilder();
		builder.append("ClientHints [");
		if (strictAcls != null) {
			builder.append("strictAcls=").append(strictAcls).append(", ");
		}
		if (plugins != null) {
			builder.append("plugins=").append(plugins.subList(0, Math.min(plugins.size(), maxLen))).append(", ");
		}
		if (hashScheme != null) {
			builder.append("hashScheme=").append(hashScheme).append(", ");
		}
		if (matchHashPolicy != null) {
			builder.append("matchHashPolicy=").append(matchHashPolicy).append(", ");
		}
		if (rules != null) {
			builder.append("rules=").append(rules.subList(0, Math.min(rules.size(), maxLen))).append(", ");
		}
		if (specificQueries != null) {
			builder.append("specificQueries=")
					.append(specificQueries.subList(0, Math.min(specificQueries.size(), maxLen)));
		}
		builder.append("]");
		return builder.toString();
	}

}

class Plugin {

	@JsonProperty("name")
	private String name = "";
	@JsonProperty("type")
	private String type = "";
	@JsonProperty("version")
	private String version = "";
	@JsonProperty("checksum_sha256")
	private String checksumSha256 = "";

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version
	 *            the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * @return the checksumSha256
	 */
	public String getChecksumSha256() {
		return checksumSha256;
	}

	/**
	 * @param checksumSha256
	 *            the checksumSha256 to set
	 */
	public void setChecksumSha256(String checksumSha256) {
		this.checksumSha256 = checksumSha256;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Plugin [");
		if (name != null) {
			builder.append("name=").append(name).append(", ");
		}
		if (type != null) {
			builder.append("type=").append(type).append(", ");
		}
		if (version != null) {
			builder.append("version=").append(version).append(", ");
		}
		if (checksumSha256 != null) {
			builder.append("checksumSha256=").append(checksumSha256);
		}
		builder.append("]");
		return builder.toString();
	}

}
