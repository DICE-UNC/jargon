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

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}
	
	// ticket DELETE tests
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
	// end ticket DELETE tests
	
	// ticket CREATE tests
	@Test
	public void testCreateTicketWithKey() throws Exception {
		TicketAdminInp pi = TicketAdminInp.instanceForCreate(TicketCreateModeEnum.TICKET_CREATE_READ, afile, ticketId);
		
		Assert.assertNotNull(pi);
	}
	
	@Test
	public void testCreateTicketWithKeyCheckXML() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForCreate(TicketCreateModeEnum.TICKET_CREATE_READ, afile, ticketId);
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
		TicketAdminInp pi = TicketAdminInp.instanceForCreate(TicketCreateModeEnum.TICKET_CREATE_READ, afile, null);
		
		Assert.assertNotNull(pi);
	}
	
	@Test
	public void testCreateTicketWithNullKeyCheckXML() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForCreate(TicketCreateModeEnum.TICKET_CREATE_READ, afile, null);
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
		TicketAdminInp pi = TicketAdminInp.instanceForCreate(TicketCreateModeEnum.TICKET_CREATE_READ, afile, "");
		
		Assert.assertNotNull(pi);
	}
	
	@Test
	public void testCreateTicketWithEmptyKeyCheckXML() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForCreate(TicketCreateModeEnum.TICKET_CREATE_READ, afile, "");
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
		TicketAdminInp pi = TicketAdminInp.instanceForCreate(TicketCreateModeEnum.TICKET_CREATE_READ, afile, ticketId);
		
		Assert.assertNotNull(pi);
	}
	
	@Test
	public void testCreatelTicketReadModeCheckXML() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForCreate(TicketCreateModeEnum.TICKET_CREATE_READ, afile, ticketId);
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
		TicketAdminInp pi = TicketAdminInp.instanceForCreate(TicketCreateModeEnum.TICKET_CREATE_WRITE, afile, ticketId);
		
		Assert.assertNotNull(pi);
	}
	
	@Test
	public void testCreatelTicketWriteModeCheckXML() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForCreate(TicketCreateModeEnum.TICKET_CREATE_WRITE, afile, ticketId);
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
	public void testCreateTicketNullObjPath() throws Exception {
		
		TicketAdminInp.instanceForCreate(TicketCreateModeEnum.TICKET_CREATE_READ, null, ticketId);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testCreateTicketEmptyObjPath() throws Exception {
		
		TicketAdminInp.instanceForCreate(TicketCreateModeEnum.TICKET_CREATE_READ, "", ticketId);
	}
	// end ticket CREATE tests
	
	
	// ticket LIST and LIST-ALL tests
	@Test
	public void testListTicket() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForList(ticketId);

		Assert.assertNotNull(pi);
	}
	
	
	@Test
	public void testListTicketCheckXML() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForList(ticketId);
		String tagOut = pi.getParsedTags();

		StringBuilder sb = new StringBuilder();
		sb.append("<ticketAdminInp_PI><arg1>list</arg1>\n");
		sb.append("<arg2>ticket_key</arg2>\n");
		sb.append("<arg3></arg3>\n");
		sb.append("<arg4></arg4>\n");
		sb.append("<arg5></arg5>\n");
		sb.append("<arg6></arg6>\n");
		sb.append("</ticketAdminInp_PI>\n");

		Assert.assertEquals("unexpected XML protocol result", sb.toString(),
				tagOut);
	}
	
	@Test
	public void testListTicketWithNullId() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForList(null);

		Assert.assertNotNull(pi);
	}
	
	@Test
	public void testListTicketWithNullIdCheckXML() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForList(null);
		String tagOut = pi.getParsedTags();

		StringBuilder sb = new StringBuilder();
		sb.append("<ticketAdminInp_PI><arg1>list</arg1>\n");
		sb.append("<arg2></arg2>\n");
		sb.append("<arg3></arg3>\n");
		sb.append("<arg4></arg4>\n");
		sb.append("<arg5></arg5>\n");
		sb.append("<arg6></arg6>\n");
		sb.append("</ticketAdminInp_PI>\n");

		Assert.assertEquals("unexpected XML protocol result", sb.toString(),
				tagOut);
	}
	
	@Test
	public void testListTicketWithEmptyId() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForList("");

		Assert.assertNotNull(pi);
	}
	
	@Test
	public void testListTicketWithEmptyIdCheckXML() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForList("");
		String tagOut = pi.getParsedTags();

		StringBuilder sb = new StringBuilder();
		sb.append("<ticketAdminInp_PI><arg1>list</arg1>\n");
		sb.append("<arg2></arg2>\n");
		sb.append("<arg3></arg3>\n");
		sb.append("<arg4></arg4>\n");
		sb.append("<arg5></arg5>\n");
		sb.append("<arg6></arg6>\n");
		sb.append("</ticketAdminInp_PI>\n");

		Assert.assertEquals("unexpected XML protocol result", sb.toString(),
				tagOut);
	}
	
	@Test
	public void testListAllTicket() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForListAll();

		Assert.assertNotNull(pi);
	}
	
	
	@Test
	public void testListAllTicketCheckXML() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForListAll();
		String tagOut = pi.getParsedTags();

		StringBuilder sb = new StringBuilder();
		sb.append("<ticketAdminInp_PI><arg1>list-all</arg1>\n");
		sb.append("<arg2></arg2>\n");
		sb.append("<arg3></arg3>\n");
		sb.append("<arg4></arg4>\n");
		sb.append("<arg5></arg5>\n");
		sb.append("<arg6></arg6>\n");
		sb.append("</ticketAdminInp_PI>\n");

		Assert.assertEquals("unexpected XML protocol result", sb.toString(),
				tagOut);
	}
	// end ticket LIST and LIST-ALL tests
	
	// TODO: Add tests for modify ticket
	// ticket MODIFY tests
	@Test
	public void testModifyTicketExpire() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyExpiration(ticketId, "2012-05-07.23:00:00");

		Assert.assertNotNull(pi);
	}
	
	@Test
	public void testModifyTicketUses() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyNumberOfUses(ticketId, new Integer(2));

		Assert.assertNotNull(pi);
	}
	
	@Test
	public void testModifyTicketAddUser() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyAddAccess(ticketId, TicketModifyAddOrRemoveTypeEnum.TICKET_MODIFY_USER, "me");

		Assert.assertNotNull(pi);
	}
	
	@Test
	public void testModifyTicketRemoveUser() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyRemoveAccess(ticketId, TicketModifyAddOrRemoveTypeEnum.TICKET_MODIFY_USER, "me");

		Assert.assertNotNull(pi);
	}
	
	@Test
	public void testModifyTicketWriteFile() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyFileWriteNumber(ticketId, new Integer(555555555));

		Assert.assertNotNull(pi);
	}
	// end ticket MODIFY tests

}
