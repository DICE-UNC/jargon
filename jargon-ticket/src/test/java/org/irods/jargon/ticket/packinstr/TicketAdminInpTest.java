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

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}
	
	@Test
	public void testDelTicketCheckXML() throws Exception {
//		User user = new User();
//		user.setName("test");
//		user.setUserDN("dn");
//		user.setUserType(UserTypeEnum.RODS_USER);
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

}
