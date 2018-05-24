/**
 *
 */
package org.irods.jargon.core.connection;

import org.junit.Test;

import junit.framework.Assert;

/**
 * @author Mike Conway - DFC
 *
 */
public class IrodsVersionTest {

	@Test
	public void testCreateAndGetParts() {
		String versionString = "rods4.1.9";
		IrodsVersion version = new IrodsVersion(versionString);
		Assert.assertEquals("missing major string", "4", version.getMajorAsString());
		Assert.assertEquals("missing major int", 4, version.getMajor());
		Assert.assertEquals("missing minor string", "1", version.getMinorAsString());
		Assert.assertEquals("missing minor int", 1, version.getMinor());
		Assert.assertEquals("missing patch string", "9", version.getPatchAsString());
		Assert.assertEquals("missing patch int", 9, version.getPatch());

	}

	@Test
	public void testCompare418to419ExpectNeg1() {
		IrodsVersion version = new IrodsVersion("rods4.1.8");
		IrodsVersion version419 = new IrodsVersion("rods4.1.9");
		int actual = version.compareTo(version419);
		Assert.assertEquals("418 should be lt 419", -1, actual);

	}

	@Test
	public void testCompare419to418Expect1() {
		IrodsVersion version = new IrodsVersion("rods4.1.8");
		IrodsVersion version419 = new IrodsVersion("rods4.1.9");
		int actual = version419.compareTo(version);
		Assert.assertEquals("418 should be lt 419", 1, actual);

	}

	@Test
	public void testCompare31to418Expect1() {
		IrodsVersion version = new IrodsVersion("rods3.1");
		IrodsVersion version419 = new IrodsVersion("rods4.1.9");
		int actual = version419.compareTo(version);
		Assert.assertEquals("31 should be lt 419", 1, actual);

	}

	@Test
	public void testCompare419to419Expect0() {
		IrodsVersion version = new IrodsVersion("rods4.1.9");
		IrodsVersion version419 = new IrodsVersion("rods4.1.9");
		int actual = version419.compareTo(version);
		Assert.assertEquals("419 should be eq 419", 0, actual);

	}

}
