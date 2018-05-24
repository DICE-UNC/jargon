/**
 *
 */
package org.irods.jargon.pool.conncache;

import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;

/**
 * @author mconway
 *
 */
public class JargonKeyedPoolConfig extends GenericKeyedObjectPoolConfig {

	public static final int TIME_30_SECONDS = 30 * 1000;
	public static final int TIME_60_SECONDS = 30 * 1000;
	public static final int MAX_IDLE = 3;

	private int maxIdlePerKey = MAX_IDLE;

	@Override
	public int getMaxIdlePerKey() {
		return maxIdlePerKey;
	}

	@Override
	public void setMaxIdlePerKey(final int maxIdlePerKey) {
		this.maxIdlePerKey = maxIdlePerKey;
	}

	public JargonKeyedPoolConfig() {
		super();
		setMinEvictableIdleTimeMillis(TIME_60_SECONDS);
		setSoftMinEvictableIdleTimeMillis(TIME_30_SECONDS);
		setTestOnReturn(true);
		setTestOnBorrow(true);
	}

	@Override
	public boolean getBlockWhenExhausted() {
		return super.getBlockWhenExhausted();
	}

	@Override
	public String getEvictionPolicyClassName() {
		return super.getEvictionPolicyClassName();

	}

	@Override
	public boolean getFairness() {
		return super.getFairness();
	}

	@Override
	public boolean getJmxEnabled() {
		return super.getJmxEnabled();
	}

	@Override
	public String getJmxNameBase() {
		return super.getJmxNameBase();
	}

	@Override
	public String getJmxNamePrefix() {
		return super.getJmxNamePrefix();
	}

	@Override
	public boolean getLifo() {
		return super.getLifo();
	}

	@Override
	public long getMaxWaitMillis() {
		return super.getMaxWaitMillis();
	}

	@Override
	public long getMinEvictableIdleTimeMillis() {
		return super.getMinEvictableIdleTimeMillis();
	}

	@Override
	public int getNumTestsPerEvictionRun() {
		return super.getNumTestsPerEvictionRun();
	}

	@Override
	public long getSoftMinEvictableIdleTimeMillis() {
		return super.getSoftMinEvictableIdleTimeMillis();
	}

	@Override
	public boolean getTestOnBorrow() {
		return false;
	}

	@Override
	public boolean getTestOnCreate() {
		return false;
	}

	@Override
	public boolean getTestOnReturn() {
		return false;
	}

	@Override
	public boolean getTestWhileIdle() {
		return super.getTestWhileIdle();
	}

	@Override
	public long getTimeBetweenEvictionRunsMillis() {
		return super.getTimeBetweenEvictionRunsMillis();
	}

	@Override
	public void setBlockWhenExhausted(final boolean blockWhenExhausted) {
		super.setBlockWhenExhausted(blockWhenExhausted);
	}

	@Override
	public void setEvictionPolicyClassName(final String evictionPolicyClassName) {
		super.setEvictionPolicyClassName(evictionPolicyClassName);
	}

	@Override
	public void setFairness(final boolean fairness) {
		super.setFairness(fairness);
	}

	@Override
	public void setJmxEnabled(final boolean jmxEnabled) {
		super.setJmxEnabled(jmxEnabled);
	}

	@Override
	public void setJmxNameBase(final String jmxNameBase) {
		super.setJmxNameBase(jmxNameBase);
	}

	@Override
	public void setJmxNamePrefix(final String jmxNamePrefix) {
		super.setJmxNamePrefix(jmxNamePrefix);
	}

	@Override
	public void setLifo(final boolean lifo) {
		super.setLifo(lifo);
	}

	@Override
	public void setMaxWaitMillis(final long maxWaitMillis) {
		super.setMaxWaitMillis(maxWaitMillis);
	}

	@Override
	public void setMinEvictableIdleTimeMillis(final long minEvictableIdleTimeMillis) {
		super.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
	}

	@Override
	public void setNumTestsPerEvictionRun(final int numTestsPerEvictionRun) {
		super.setNumTestsPerEvictionRun(numTestsPerEvictionRun);
	}

	@Override
	public void setSoftMinEvictableIdleTimeMillis(final long softMinEvictableIdleTimeMillis) {
		super.setSoftMinEvictableIdleTimeMillis(softMinEvictableIdleTimeMillis);
	}

	@Override
	public void setTestOnBorrow(final boolean testOnBorrow) {
		super.setTestOnBorrow(testOnBorrow);
	}

	@Override
	public void setTestOnCreate(final boolean testOnCreate) {
		super.setTestOnCreate(testOnCreate);
	}

	@Override
	public void setTestOnReturn(final boolean testOnReturn) {
		super.setTestOnReturn(testOnReturn);
	}

	@Override
	public void setTestWhileIdle(final boolean testWhileIdle) {
		super.setTestWhileIdle(testWhileIdle);
	}

	@Override
	public void setTimeBetweenEvictionRunsMillis(final long timeBetweenEvictionRunsMillis) {
		super.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
	}

}
