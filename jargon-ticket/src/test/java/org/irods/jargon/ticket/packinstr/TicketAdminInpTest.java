package org.irods.jargon.ticket.packinstr;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class TicketAdminInpTest {

	private String afile = "/test1/home/test1/anExistingFile";
	private String ticketId = "ticket_key";
	private String userName = "me";
	private String groupName = "them";
	private String hostName = "www.irods.org";
	private String validDate = "2012-05-07.23:00:00";

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
		TicketAdminInp pi = TicketAdminInp.instanceForCreate(
				TicketCreateModeEnum.READ, afile, ticketId);

		Assert.assertNotNull(pi);
	}

	@Test
	public void testCreateTicketWithKeyCheckXML() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForCreate(
				TicketCreateModeEnum.READ, afile, ticketId);
		String tagOut = pi.getParsedTags();

		StringBuilder sb = new StringBuilder();
		sb.append("<ticketAdminInp_PI><arg1>create</arg1>\n");
		sb.append("<arg2>ticket_key</arg2>\n");
		sb.append("<arg3>read</arg3>\n");
		sb.append("<arg4>/test1/home/test1/anExistingFile</arg4>\n");
		sb.append("<arg5></arg5>\n");
		sb.append("<arg6></arg6>\n");
		sb.append("</ticketAdminInp_PI>\n");

		Assert.assertEquals("unexpected XML protocol result", sb.toString(),
				tagOut);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateTicketWithNullKey() throws Exception {

		TicketAdminInp.instanceForCreate(
				TicketCreateModeEnum.READ, afile, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateTicketWithEmptyKey() throws Exception {

		TicketAdminInp.instanceForCreate(
				TicketCreateModeEnum.READ, afile, "");
	}

	@Test
	public void testCreateTicketReadMode() throws Exception {
		TicketAdminInp pi = TicketAdminInp.instanceForCreate(
				TicketCreateModeEnum.READ, afile, ticketId);

		Assert.assertNotNull(pi);
	}

	@Test
	public void testCreatelTicketReadModeCheckXML() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForCreate(
				TicketCreateModeEnum.READ, afile, ticketId);
		String tagOut = pi.getParsedTags();

		StringBuilder sb = new StringBuilder();
		sb.append("<ticketAdminInp_PI><arg1>create</arg1>\n");
		sb.append("<arg2>ticket_key</arg2>\n");
		sb.append("<arg3>read</arg3>\n");
		sb.append("<arg4>/test1/home/test1/anExistingFile</arg4>\n");
		sb.append("<arg5></arg5>\n");
		sb.append("<arg6></arg6>\n");
		sb.append("</ticketAdminInp_PI>\n");

		Assert.assertEquals("unexpected XML protocol result", sb.toString(),
				tagOut);
	}

	@Test
	public void testCreateTicketWriteMode() throws Exception {
		TicketAdminInp pi = TicketAdminInp.instanceForCreate(
				TicketCreateModeEnum.WRITE, afile, ticketId);

		Assert.assertNotNull(pi);
	}

	@Test
	public void testCreatelTicketWriteModeCheckXML() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForCreate(
				TicketCreateModeEnum.WRITE, afile, ticketId);
		String tagOut = pi.getParsedTags();

		StringBuilder sb = new StringBuilder();
		sb.append("<ticketAdminInp_PI><arg1>create</arg1>\n");
		sb.append("<arg2>ticket_key</arg2>\n");
		sb.append("<arg3>write</arg3>\n");
		sb.append("<arg4>/test1/home/test1/anExistingFile</arg4>\n");
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

		TicketAdminInp.instanceForCreate(
				TicketCreateModeEnum.READ, null, ticketId);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateTicketEmptyObjPath() throws Exception {

		TicketAdminInp.instanceForCreate(
				TicketCreateModeEnum.READ, "", ticketId);
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

	// ticket MODIFY tests
	// modify add user
	@Test
	public void testModifyTicketAddUser() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyAddAccess(ticketId,
				TicketModifyAddOrRemoveTypeEnum.TICKET_MODIFY_USER, userName);

		Assert.assertNotNull(pi);
	}

	@Test
	public void testModifyTicketAddUserCheckXML() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyAddAccess(ticketId,
				TicketModifyAddOrRemoveTypeEnum.TICKET_MODIFY_USER, userName);
		String tagOut = pi.getParsedTags();

		StringBuilder sb = new StringBuilder();
		sb.append("<ticketAdminInp_PI><arg1>mod</arg1>\n");
		sb.append("<arg2>ticket_key</arg2>\n");
		sb.append("<arg3>add</arg3>\n");
		sb.append("<arg4>user</arg4>\n");
		sb.append("<arg5>me</arg5>\n");
		sb.append("<arg6></arg6>\n");
		sb.append("</ticketAdminInp_PI>\n");

		Assert.assertEquals("unexpected XML protocol result", sb.toString(),
				tagOut);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testModifyTicketAddUserNullTicketId() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyAddAccess(null,
				TicketModifyAddOrRemoveTypeEnum.TICKET_MODIFY_USER, userName);

		Assert.assertNotNull(pi);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testModifyTicketAddUserEmptyTicketId() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyAddAccess("",
				TicketModifyAddOrRemoveTypeEnum.TICKET_MODIFY_USER, userName);

		Assert.assertNotNull(pi);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testModifyTicketAddUserNullAddType() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyAddAccess(ticketId,
				null, userName);

		Assert.assertNotNull(pi);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testModifyTicketAddUserNullUser() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyAddAccess(ticketId,
				TicketModifyAddOrRemoveTypeEnum.TICKET_MODIFY_USER, null);

		Assert.assertNotNull(pi);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testModifyTicketAddUserEmptyUser() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyAddAccess(ticketId,
				TicketModifyAddOrRemoveTypeEnum.TICKET_MODIFY_USER, "");

		Assert.assertNotNull(pi);
	}

	// Modify add group
	@Test
	public void testModifyTicketAddGroup() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyAddAccess(ticketId,
				TicketModifyAddOrRemoveTypeEnum.TICKET_MODIFY_GROUP, groupName);

		Assert.assertNotNull(pi);
	}

	@Test
	public void testModifyTicketAddGroupCheckXML() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyAddAccess(ticketId,
				TicketModifyAddOrRemoveTypeEnum.TICKET_MODIFY_GROUP, groupName);
		String tagOut = pi.getParsedTags();

		StringBuilder sb = new StringBuilder();
		sb.append("<ticketAdminInp_PI><arg1>mod</arg1>\n");
		sb.append("<arg2>ticket_key</arg2>\n");
		sb.append("<arg3>add</arg3>\n");
		sb.append("<arg4>group</arg4>\n");
		sb.append("<arg5>them</arg5>\n");
		sb.append("<arg6></arg6>\n");
		sb.append("</ticketAdminInp_PI>\n");

		Assert.assertEquals("unexpected XML protocol result", sb.toString(),
				tagOut);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testModifyTicketAddGroupNullTicketId() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyAddAccess(null,
				TicketModifyAddOrRemoveTypeEnum.TICKET_MODIFY_GROUP, groupName);

		Assert.assertNotNull(pi);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testModifyTicketAddGroupEmptyTicketId() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyAddAccess("",
				TicketModifyAddOrRemoveTypeEnum.TICKET_MODIFY_GROUP, groupName);

		Assert.assertNotNull(pi);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testModifyTicketAddGroupNullAddType() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyAddAccess(ticketId,
				null, groupName);

		Assert.assertNotNull(pi);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testModifyTicketAddGroupNullGroup() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyAddAccess(ticketId,
				TicketModifyAddOrRemoveTypeEnum.TICKET_MODIFY_GROUP, null);

		Assert.assertNotNull(pi);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testModifyTicketAddGroupEmptyGroup() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyAddAccess(ticketId,
				TicketModifyAddOrRemoveTypeEnum.TICKET_MODIFY_GROUP, "");

		Assert.assertNotNull(pi);
	}

	// Modify add host
	@Test
	public void testModifyTicketAddHost() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyAddAccess(ticketId,
				TicketModifyAddOrRemoveTypeEnum.TICKET_MODIFY_HOST, hostName);

		Assert.assertNotNull(pi);
	}

	@Test
	public void testModifyTicketAddHostCheckXML() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyAddAccess(ticketId,
				TicketModifyAddOrRemoveTypeEnum.TICKET_MODIFY_HOST, hostName);
		String tagOut = pi.getParsedTags();

		StringBuilder sb = new StringBuilder();
		sb.append("<ticketAdminInp_PI><arg1>mod</arg1>\n");
		sb.append("<arg2>ticket_key</arg2>\n");
		sb.append("<arg3>add</arg3>\n");
		sb.append("<arg4>host</arg4>\n");
		sb.append("<arg5>www.irods.org</arg5>\n");
		sb.append("<arg6></arg6>\n");
		sb.append("</ticketAdminInp_PI>\n");

		Assert.assertEquals("unexpected XML protocol result", sb.toString(),
				tagOut);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testModifyTicketAddHostNullTicketId() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyAddAccess(null,
				TicketModifyAddOrRemoveTypeEnum.TICKET_MODIFY_HOST, hostName);

		Assert.assertNotNull(pi);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testModifyTicketAddHostEmptyTicketId() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyAddAccess("",
				TicketModifyAddOrRemoveTypeEnum.TICKET_MODIFY_HOST, hostName);

		Assert.assertNotNull(pi);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testModifyTicketAddHostNullAddType() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyAddAccess(ticketId,
				null, hostName);

		Assert.assertNotNull(pi);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testModifyTicketAddHostNullHost() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyAddAccess(ticketId,
				TicketModifyAddOrRemoveTypeEnum.TICKET_MODIFY_HOST, null);

		Assert.assertNotNull(pi);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testModifyTicketAddHostEmptyHost() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyAddAccess(ticketId,
				TicketModifyAddOrRemoveTypeEnum.TICKET_MODIFY_HOST, "");

		Assert.assertNotNull(pi);
	}

	// modify remove user
	@Test
	public void testModifyTicketRemoveUser() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyRemoveAccess(
				ticketId, TicketModifyAddOrRemoveTypeEnum.TICKET_MODIFY_USER,
				userName);

		Assert.assertNotNull(pi);
	}

	@Test
	public void testModifyTicketRemoveUserCheckXML() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyRemoveAccess(
				ticketId, TicketModifyAddOrRemoveTypeEnum.TICKET_MODIFY_USER,
				userName);
		String tagOut = pi.getParsedTags();

		StringBuilder sb = new StringBuilder();
		sb.append("<ticketAdminInp_PI><arg1>mod</arg1>\n");
		sb.append("<arg2>ticket_key</arg2>\n");
		sb.append("<arg3>remove</arg3>\n");
		sb.append("<arg4>user</arg4>\n");
		sb.append("<arg5>me</arg5>\n");
		sb.append("<arg6></arg6>\n");
		sb.append("</ticketAdminInp_PI>\n");

		Assert.assertEquals("unexpected XML protocol result", sb.toString(),
				tagOut);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testModifyTicketRemoveUserNullTicketId() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyRemoveAccess(null,
				TicketModifyAddOrRemoveTypeEnum.TICKET_MODIFY_USER, userName);

		Assert.assertNotNull(pi);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testModifyTicketRemoveUserEmptyTicketId() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyRemoveAccess("",
				TicketModifyAddOrRemoveTypeEnum.TICKET_MODIFY_USER, userName);

		Assert.assertNotNull(pi);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testModifyTicketRemoveUserNullAddType() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyRemoveAccess(
				ticketId, null, userName);

		Assert.assertNotNull(pi);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testModifyTicketRemoveUserNullUser() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyRemoveAccess(
				ticketId, TicketModifyAddOrRemoveTypeEnum.TICKET_MODIFY_USER,
				null);

		Assert.assertNotNull(pi);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testModifyTicketRemoveUserEmptyUser() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyRemoveAccess(
				ticketId, TicketModifyAddOrRemoveTypeEnum.TICKET_MODIFY_USER,
				"");

		Assert.assertNotNull(pi);
	}

	// modify remove group
	@Test
	public void testModifyTicketRemoveGroup() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyRemoveAccess(
				ticketId, TicketModifyAddOrRemoveTypeEnum.TICKET_MODIFY_GROUP,
				groupName);

		Assert.assertNotNull(pi);
	}

	@Test
	public void testModifyTicketRemoveGroupCheckXML() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyRemoveAccess(
				ticketId, TicketModifyAddOrRemoveTypeEnum.TICKET_MODIFY_GROUP,
				groupName);
		String tagOut = pi.getParsedTags();

		StringBuilder sb = new StringBuilder();
		sb.append("<ticketAdminInp_PI><arg1>mod</arg1>\n");
		sb.append("<arg2>ticket_key</arg2>\n");
		sb.append("<arg3>remove</arg3>\n");
		sb.append("<arg4>group</arg4>\n");
		sb.append("<arg5>them</arg5>\n");
		sb.append("<arg6></arg6>\n");
		sb.append("</ticketAdminInp_PI>\n");

		Assert.assertEquals("unexpected XML protocol result", sb.toString(),
				tagOut);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testModifyTicketRemoveGroupNullTicketId() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyRemoveAccess(null,
				TicketModifyAddOrRemoveTypeEnum.TICKET_MODIFY_GROUP, groupName);

		Assert.assertNotNull(pi);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testModifyTicketRemoveGroupEmptyTicketId() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyRemoveAccess("",
				TicketModifyAddOrRemoveTypeEnum.TICKET_MODIFY_GROUP, groupName);

		Assert.assertNotNull(pi);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testModifyTicketRemoveGroupNullAddType() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyRemoveAccess(
				ticketId, null, groupName);

		Assert.assertNotNull(pi);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testModifyTicketRemoveGroupNullGroup() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyRemoveAccess(
				ticketId, TicketModifyAddOrRemoveTypeEnum.TICKET_MODIFY_GROUP,
				null);

		Assert.assertNotNull(pi);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testModifyTicketRemoveGroupEmptyGroup() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyRemoveAccess(
				ticketId, TicketModifyAddOrRemoveTypeEnum.TICKET_MODIFY_GROUP,
				"");

		Assert.assertNotNull(pi);
	}

	// modify remove host
	@Test
	public void testModifyTicketRemoveHost() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyRemoveAccess(
				ticketId, TicketModifyAddOrRemoveTypeEnum.TICKET_MODIFY_HOST,
				hostName);

		Assert.assertNotNull(pi);
	}

	@Test
	public void testModifyTicketRemoveHostCheckXML() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyRemoveAccess(
				ticketId, TicketModifyAddOrRemoveTypeEnum.TICKET_MODIFY_HOST,
				hostName);
		String tagOut = pi.getParsedTags();

		StringBuilder sb = new StringBuilder();
		sb.append("<ticketAdminInp_PI><arg1>mod</arg1>\n");
		sb.append("<arg2>ticket_key</arg2>\n");
		sb.append("<arg3>remove</arg3>\n");
		sb.append("<arg4>host</arg4>\n");
		sb.append("<arg5>www.irods.org</arg5>\n");
		sb.append("<arg6></arg6>\n");
		sb.append("</ticketAdminInp_PI>\n");

		Assert.assertEquals("unexpected XML protocol result", sb.toString(),
				tagOut);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testModifyTicketRemoveHostNullTicketId() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyRemoveAccess(null,
				TicketModifyAddOrRemoveTypeEnum.TICKET_MODIFY_HOST, hostName);

		Assert.assertNotNull(pi);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testModifyTicketRemoveHostEmptyTicketId() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyRemoveAccess("",
				TicketModifyAddOrRemoveTypeEnum.TICKET_MODIFY_HOST, hostName);

		Assert.assertNotNull(pi);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testModifyTicketRemoveHostNullAddType() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyRemoveAccess(
				ticketId, null, hostName);

		Assert.assertNotNull(pi);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testModifyTicketRemoveHostNullHost() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyRemoveAccess(
				ticketId, TicketModifyAddOrRemoveTypeEnum.TICKET_MODIFY_HOST,
				null);

		Assert.assertNotNull(pi);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testModifyTicketRemoveHostEmptyHost() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyRemoveAccess(
				ticketId, TicketModifyAddOrRemoveTypeEnum.TICKET_MODIFY_HOST,
				"");

		Assert.assertNotNull(pi);
	}

	// modify uses
	@Test
	public void testModifyTicketUses() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyNumberOfUses(
				ticketId, new Integer(2));

		Assert.assertNotNull(pi);
	}

	@Test
	public void testModifyTicketUsestCheckXML() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyNumberOfUses(
				ticketId, 2);
		String tagOut = pi.getParsedTags();

		StringBuilder sb = new StringBuilder();
		sb.append("<ticketAdminInp_PI><arg1>mod</arg1>\n");
		sb.append("<arg2>ticket_key</arg2>\n");
		sb.append("<arg3>uses</arg3>\n");
		sb.append("<arg4>2</arg4>\n");
		sb.append("<arg5></arg5>\n");
		sb.append("<arg6></arg6>\n");
		sb.append("</ticketAdminInp_PI>\n");

		Assert.assertEquals("unexpected XML protocol result", sb.toString(),
				tagOut);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testModifyTicketUsesNullTicketId() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyNumberOfUses(null,
				new Integer(2));

		Assert.assertNotNull(pi);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testModifyTicketUsesEmptyTicketId() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyNumberOfUses("",
				new Integer(2));

		Assert.assertNotNull(pi);
	}

	@Test
	public void testModifyTicketUsesZero() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyNumberOfUses(
				ticketId, 0);

		Assert.assertNotNull(pi);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testModifyTicketUsesLessThanZero() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyNumberOfUses("",
				new Integer(-1));

		Assert.assertNotNull(pi);
	}

	// modify write-file
	@Test
	public void testModifyTicketWriteFile() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyFileWriteNumber(
				ticketId, new Integer(555555555));

		Assert.assertNotNull(pi);
	}

	@Test
	public void testModifyTicketWriteFileCheckXML() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyFileWriteNumber(
				ticketId, 20000);
		String tagOut = pi.getParsedTags();

		StringBuilder sb = new StringBuilder();
		sb.append("<ticketAdminInp_PI><arg1>mod</arg1>\n");
		sb.append("<arg2>ticket_key</arg2>\n");
		sb.append("<arg3>write-file</arg3>\n");
		sb.append("<arg4>20000</arg4>\n");
		sb.append("<arg5></arg5>\n");
		sb.append("<arg6></arg6>\n");
		sb.append("</ticketAdminInp_PI>\n");

		Assert.assertEquals("unexpected XML protocol result", sb.toString(),
				tagOut);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testModifyTicketWriteFileNullTicketId() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyFileWriteNumber(
				null, new Integer(20000));

		Assert.assertNotNull(pi);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testModifyTicketWriteFileEmptyTicketId() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyFileWriteNumber("",
				new Integer(20000));

		Assert.assertNotNull(pi);
	}

	@Test
	public void testModifyTicketWriteFileZero() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyFileWriteNumber(
				ticketId, 0);

		Assert.assertNotNull(pi);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testModifyTicketWriteFileLessThanZero() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyFileWriteNumber("",
				new Integer(-100));

		Assert.assertNotNull(pi);
	}

	// modify write-byte
	@Test
	public void testModifyTicketWriteBytes() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyByteWriteNumber(
				ticketId, new Integer(555555555));

		Assert.assertNotNull(pi);
	}

	@Test
	public void testModifyTicketWriteBytesCheckXML() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyByteWriteNumber(
				ticketId, 500000);
		String tagOut = pi.getParsedTags();

		StringBuilder sb = new StringBuilder();
		sb.append("<ticketAdminInp_PI><arg1>mod</arg1>\n");
		sb.append("<arg2>ticket_key</arg2>\n");
		sb.append("<arg3>write-byte</arg3>\n");
		sb.append("<arg4>500000</arg4>\n");
		sb.append("<arg5></arg5>\n");
		sb.append("<arg6></arg6>\n");
		sb.append("</ticketAdminInp_PI>\n");

		Assert.assertEquals("unexpected XML protocol result", sb.toString(),
				tagOut);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testModifyTicketWriteBytesNullTicketId() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyByteWriteNumber(
				null, new Integer(500000));

		Assert.assertNotNull(pi);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testModifyTicketWriteBytesEmptyTicketId() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyByteWriteNumber("",
				new Integer(500000));

		Assert.assertNotNull(pi);
	}

	@Test
	public void testModifyTicketWriteBytesZero() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyByteWriteNumber(
				ticketId, 0);

		Assert.assertNotNull(pi);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testModifyTicketWriteBytesLessThanZero() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyByteWriteNumber("",
				new Integer(-100));

		Assert.assertNotNull(pi);
	}

	// modify expire
	@Test
	public void testModifyTicketExpire() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyExpiration(
				ticketId, validDate);

		Assert.assertNotNull(pi);
	}

	@Test
	public void testModifyTicketExpireCheckXML() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyExpiration(
				ticketId, validDate);
		String tagOut = pi.getParsedTags();

		StringBuilder sb = new StringBuilder();
		sb.append("<ticketAdminInp_PI><arg1>mod</arg1>\n");
		sb.append("<arg2>ticket_key</arg2>\n");
		sb.append("<arg3>expire</arg3>\n");
		sb.append("<arg4>2012-05-07.23:00:00</arg4>\n");
		sb.append("<arg5></arg5>\n");
		sb.append("<arg6></arg6>\n");
		sb.append("</ticketAdminInp_PI>\n");

		Assert.assertEquals("unexpected XML protocol result", sb.toString(),
				tagOut);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testModifyTicketExpireNullTicketId() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyExpiration(null,
				validDate);

		Assert.assertNotNull(pi);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testModifyTicketExpireEmptyTicketId() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyExpiration("",
				validDate);

		Assert.assertNotNull(pi);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testModifyTicketExpireInvalidDate() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyExpiration(
				ticketId, "2012-01-01");

		Assert.assertNotNull(pi);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testModifyTicketExpireNullDate() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyExpiration(
				ticketId, (String) null);

		Assert.assertNotNull(pi);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testModifyTicketExpireEmptyDate() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyExpiration(
				ticketId, "");

		Assert.assertNotNull(pi);
	}

	@Test
	public void testModifyTicketExpireWithDate() throws Exception {

		Date now = new Date();
		TicketAdminInp pi = TicketAdminInp.instanceForModifyExpiration(
				ticketId, now);

		Assert.assertNotNull(pi);
	}

	@Ignore
	// probably will not work all of the time
	public void testModifyTicketExpireWithDateCheckXML() throws Exception {

		Date now = new Date();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd.HH:mm:ss");
		String formattedDate = df.format(now);

		TicketAdminInp pi = TicketAdminInp.instanceForModifyExpiration(
				ticketId, now);
		String tagOut = pi.getParsedTags();

		StringBuilder sb = new StringBuilder();
		sb.append("<ticketAdminInp_PI><arg1>mod</arg1>\n");
		sb.append("<arg2>ticket_key</arg2>\n");
		sb.append("<arg3>expire</arg3>\n");
		sb.append("<arg4>");
		sb.append(formattedDate);
		sb.append("</arg4>\n");
		sb.append("<arg5></arg5>\n");
		sb.append("<arg6></arg6>\n");
		sb.append("</ticketAdminInp_PI>\n");

		Assert.assertEquals("unexpected XML protocol result", sb.toString(),
				tagOut);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testModifyTicketExpireWithDateNullTicketId() throws Exception {

		Date now = new Date();
		TicketAdminInp pi = TicketAdminInp.instanceForModifyExpiration(null,
				now);

		Assert.assertNotNull(pi);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testModifyTicketExpireWithDateEmptyTicketId() throws Exception {

		Date now = new Date();
		TicketAdminInp pi = TicketAdminInp.instanceForModifyExpiration("", now);

		Assert.assertNotNull(pi);
	}

	@Test
	public void testModifyTicketExpireWitDateNullDate() throws Exception {

		TicketAdminInp pi = TicketAdminInp.instanceForModifyExpiration(
				ticketId, (Date) null);

		Assert.assertNotNull(pi);
	}

	// end ticket MODIFY tests

}
