package org.irods.jargon.ticket.packinstr;


import junit.framework.Assert;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.GeneralAdminInp;
import org.irods.jargon.core.protovalues.UserTypeEnum;
import org.irods.jargon.core.pub.domain.User;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TicketAdminInpTest {
	
	String afile = "/test1/home/test1/anExistingFile";
	String acollection = "/tests1/home/test1";
	String ticketId = "ticket_key";
	String modeRead = "read";
	String modeWrite = "write";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}
	
	// DELETE ticket tests
	@Test
	public void testDeleteTicket() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForDelete(ticketId);

		Assert.assertNotNull(pi);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testDeleteTicketNull() throws Exception {

		TicketAdminInp.instanceForDelete(null);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testDeleteTicketEmpty() throws Exception {

		TicketAdminInp.instanceForDelete("");
	}
	
	
	@Test
	public void testDeleteTicketCheckXML() throws Exception {

		String ticketId = "ticket_key";

		TicketAdminInp pi = TicketAdminInp.instanceForDelete(ticketId);
		String tagOut = pi.getParsedTags();

		StringBuilder sb = new StringBuilder();
		sb.append("<ticketAdminInp_PI><arg1>delete</arg1>\n");
		sb.append("<arg2>ticket_key</arg2>\n");
		sb.append("<arg3></arg3>\n");
		sb.append("<arg4></arg4>\n");
		sb.append("<arg5></arg5>\n");
		sb.append("<arg6></arg6>\n");
		sb.append("</ticketAdminInp_PI>\n");

		Assert.assertEquals("unexpected XML protocol result", sb.toString(),
				tagOut);
	}
	// end DELETE ticket tests
	
	// CREATE ticket tests
	@Test
	public void testCreateTicketWithKey() throws Exception {
		TicketAdminInp pi = TicketAdminInp.instanceForCreate(modeRead, afile, ticketId);
		
		Assert.assertNotNull(pi);
	}
	
	@Test
	public void testCreateTicketWithKeyCheckXML() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForCreate(modeRead, afile, ticketId);
		String tagOut = pi.getParsedTags();

		StringBuilder sb = new StringBuilder();
		sb.append("<ticketAdminInp_PI><arg1>create</arg1>\n");
		sb.append("<arg2>read</arg2>\n");
		sb.append("<arg3>/test1/home/test1/anExistingFile</arg3>\n");
		sb.append("<arg4>ticket_key</arg4>\n");
		sb.append("<arg5></arg5>\n");
		sb.append("<arg6></arg6>\n");
		sb.append("</ticketAdminInp_PI>\n");

		Assert.assertEquals("unexpected XML protocol result", sb.toString(),
				tagOut);
	}
	
	@Test
	public void testCreateTicketWithNullKey() throws Exception {
		TicketAdminInp pi = TicketAdminInp.instanceForCreate(modeRead, afile, null);
		
		Assert.assertNotNull(pi);
	}
	
	@Test
	public void testCreateTicketWithNullKeyCheckXML() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForCreate(modeRead, afile, null);
		String tagOut = pi.getParsedTags();

		StringBuilder sb = new StringBuilder();
		sb.append("<ticketAdminInp_PI><arg1>create</arg1>\n");
		sb.append("<arg2>read</arg2>\n");
		sb.append("<arg3>/test1/home/test1/anExistingFile</arg3>\n");
		sb.append("<arg4></arg4>\n");
		sb.append("<arg5></arg5>\n");
		sb.append("<arg6></arg6>\n");
		sb.append("</ticketAdminInp_PI>\n");

		Assert.assertEquals("unexpected XML protocol result", sb.toString(),
				tagOut);
	}
	
	@Test
	public void testCreateTicketWithEmptyKey() throws Exception {
		TicketAdminInp pi = TicketAdminInp.instanceForCreate(modeRead, afile, "");
		
		Assert.assertNotNull(pi);
	}
	
	@Test
	public void testCreateTicketWithEmptyKeyCheckXML() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForCreate(modeRead, afile, "");
		String tagOut = pi.getParsedTags();

		StringBuilder sb = new StringBuilder();
		sb.append("<ticketAdminInp_PI><arg1>create</arg1>\n");
		sb.append("<arg2>read</arg2>\n");
		sb.append("<arg3>/test1/home/test1/anExistingFile</arg3>\n");
		sb.append("<arg4></arg4>\n");
		sb.append("<arg5></arg5>\n");
		sb.append("<arg6></arg6>\n");
		sb.append("</ticketAdminInp_PI>\n");

		Assert.assertEquals("unexpected XML protocol result", sb.toString(),
				tagOut);
	}
	
	@Test
	public void testCreateTicketReadMode() throws Exception {
		TicketAdminInp pi = TicketAdminInp.instanceForCreate(modeRead, afile, ticketId);
		
		Assert.assertNotNull(pi);
	}
	
	@Test
	public void testCreatelTicketReadModeCheckXML() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForCreate(modeRead, afile, ticketId);
		String tagOut = pi.getParsedTags();

		StringBuilder sb = new StringBuilder();
		sb.append("<ticketAdminInp_PI><arg1>create</arg1>\n");
		sb.append("<arg2>read</arg2>\n");
		sb.append("<arg3>/test1/home/test1/anExistingFile</arg3>\n");
		sb.append("<arg4>ticket_key</arg4>\n");
		sb.append("<arg5></arg5>\n");
		sb.append("<arg6></arg6>\n");
		sb.append("</ticketAdminInp_PI>\n");

		Assert.assertEquals("unexpected XML protocol result", sb.toString(),
				tagOut);
	}
	
	@Test
	public void testCreateTicketWriteMode() throws Exception {
		TicketAdminInp pi = TicketAdminInp.instanceForCreate(modeWrite, afile, ticketId);
		
		Assert.assertNotNull(pi);
	}
	
	@Test
	public void testCreatelTicketWriteModeCheckXML() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForCreate(modeWrite, afile, ticketId);
		String tagOut = pi.getParsedTags();

		StringBuilder sb = new StringBuilder();
		sb.append("<ticketAdminInp_PI><arg1>create</arg1>\n");
		sb.append("<arg2>write</arg2>\n");
		sb.append("<arg3>/test1/home/test1/anExistingFile</arg3>\n");
		sb.append("<arg4>ticket_key</arg4>\n");
		sb.append("<arg5></arg5>\n");
		sb.append("<arg6></arg6>\n");
		sb.append("</ticketAdminInp_PI>\n");

		Assert.assertEquals("unexpected XML protocol result", sb.toString(),
				tagOut);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testCreateTicketNullMode() throws Exception {
		
		TicketAdminInp.instanceForCreate(null, afile, ticketId);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testCreateTicketEmptyMode() throws Exception {
		
		TicketAdminInp.instanceForCreate("", afile, ticketId);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testCreateTicketNonsenseMode() throws Exception {
		
		TicketAdminInp.instanceForCreate("xyzzy", afile, ticketId);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testCreateTicketNullObjPath() throws Exception {
		
		TicketAdminInp.instanceForCreate(modeRead, null, ticketId);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testCreateTicketEmptyObjPath() throws Exception {
		
		TicketAdminInp.instanceForCreate(modeRead, "", ticketId);
	}
	// end CREATE ticket tests

}
