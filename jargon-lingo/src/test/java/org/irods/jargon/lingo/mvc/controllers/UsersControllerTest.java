package org.irods.jargon.lingo.mvc.controllers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.protovalues.UserTypeEnum;
import org.irods.jargon.core.pub.IRODSAccessObjectFactoryI;
import org.irods.jargon.core.pub.UserAO;
import org.irods.jargon.core.pub.domain.User;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.irods.jargon.lingo.mvc.controllers.forms.UserAddForm;
import org.irods.jargon.lingo.web.security.IRODSAuthenticationToken;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.ModelAndView;


public class UsersControllerTest {
	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		testingProperties = testingPropertiesHelper.getTestProperties();

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public final void testIndexAction() throws Exception {
		UsersController usersController = new UsersController();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAuthenticationToken token = new IRODSAuthenticationToken(
				irodsAccount);
		SecurityContextHolder.getContext().setAuthentication(token);
		IRODSAccessObjectFactoryI irodsAccessObjectFactory = mock(IRODSAccessObjectFactoryI.class);
		usersController.setIrodsAccessObjectFactory(irodsAccessObjectFactory);

		ModelAndView mav = usersController.indexAction();
		TestCase.assertNotNull("mav was null from controller", mav);

		TestCase.assertEquals("wrong view returned", mav.getViewName(),
				"users");
		
		TestCase.assertTrue("no user types returned", mav.getModelMap().containsKey("userTypes"));
		
	}
	
	@Test
	public final void testAjaxUsersTable() throws Exception {
		UsersController usersController = new UsersController();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAuthenticationToken token = new IRODSAuthenticationToken(
				irodsAccount);
		SecurityContextHolder.getContext().setAuthentication(token);
		IRODSAccessObjectFactoryI irodsAccessObjectFactory = mock(IRODSAccessObjectFactoryI.class);
		usersController.setIrodsAccessObjectFactory(irodsAccessObjectFactory);

		List<User> users = new ArrayList<User>();
		User testUser = new User();
		testUser.setName("test1");
		users.add(testUser);

		UserAO userAO = mock(UserAO.class);
		when(irodsAccessObjectFactory.getUserAO(irodsAccount)).thenReturn(
				userAO);
		when(userAO.findAll()).thenReturn(users);

		ModelAndView mav = usersController.ajaxUsersTable();
		TestCase.assertNotNull("mav was null from controller", mav);

		TestCase.assertEquals("wrong view returned", mav.getViewName(),
				"ajax_users_table");
		TestCase.assertTrue("no users returned", mav.getModelMap().containsKey(
				"users"));	
	}

	@Test
	public final void testAjaxUsersSearchByUserName() throws Exception {
		UsersController usersController = new UsersController();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAuthenticationToken token = new IRODSAuthenticationToken(
				irodsAccount);
		SecurityContextHolder.getContext().setAuthentication(token);
		IRODSAccessObjectFactoryI irodsAccessObjectFactory = mock(IRODSAccessObjectFactoryI.class);
		usersController.setIrodsAccessObjectFactory(irodsAccessObjectFactory);

		List<User> users = new ArrayList<User>();
		User testUser = new User();
		testUser.setName("test1");
		users.add(testUser);

		StringBuilder sb = new StringBuilder();
		sb.append(RodsGenQueryEnum.COL_USER_NAME.getName());
		sb.append(" LIKE '");
		sb.append("test");
		sb.append("%'");

		UserAO userAO = mock(UserAO.class);
		when(irodsAccessObjectFactory.getUserAO(irodsAccount)).thenReturn(
				userAO);
		when(userAO.findWhere(sb.toString())).thenReturn(users);

		ModelAndView mav = usersController.ajaxUsersSearchByUserName("test1");
		TestCase.assertNotNull("mav was null from controller", mav);

		TestCase.assertEquals("wrong view returned", mav.getViewName(),
				"ajax_users_table");
		TestCase.assertTrue("no users returned", mav.getModelMap().containsKey(
				"users"));
	}
	
	
	@Test
	public final void testAjaxUsersSearchByUserNameNoUserName()
			throws Exception {
		UsersController usersController = new UsersController();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAuthenticationToken token = new IRODSAuthenticationToken(
				irodsAccount);
		SecurityContextHolder.getContext().setAuthentication(token);
		IRODSAccessObjectFactoryI irodsAccessObjectFactory = mock(IRODSAccessObjectFactoryI.class);
		usersController.setIrodsAccessObjectFactory(irodsAccessObjectFactory);

		List<User> users = new ArrayList<User>();
		User testUser = new User();
		testUser.setName("test1");
		users.add(testUser);

		StringBuilder sb = new StringBuilder();
		sb.append(RodsGenQueryEnum.COL_USER_NAME.getName());
		sb.append(" LIKE '");
		sb.append("test");
		sb.append("%'");

		UserAO userAO = mock(UserAO.class);
		when(irodsAccessObjectFactory.getUserAO(irodsAccount)).thenReturn(
				userAO);
		when(userAO.findWhere(sb.toString())).thenReturn(users);

		ModelAndView mav = usersController.ajaxUsersSearchByUserName("");
		TestCase.assertNotNull("mav was null from controller", mav);

		TestCase.assertEquals("wrong view returned", mav.getViewName(),
				"ajax_users_table");
		TestCase.assertTrue("no users returned", mav.getModelMap().containsKey(
				"users"));
	}

	@Test(expected = JargonException.class)
	public final void testAjaxUsersSearchByUserNameNullUserName()
			throws Exception {
		UsersController usersController = new UsersController();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAuthenticationToken token = new IRODSAuthenticationToken(
				irodsAccount);
		SecurityContextHolder.getContext().setAuthentication(token);
		IRODSAccessObjectFactoryI irodsAccessObjectFactory = mock(IRODSAccessObjectFactoryI.class);
		usersController.setIrodsAccessObjectFactory(irodsAccessObjectFactory);

		List<User> users = new ArrayList<User>();
		User testUser = new User();
		testUser.setName("test1");
		users.add(testUser);

		StringBuilder sb = new StringBuilder();
		sb.append(RodsGenQueryEnum.COL_USER_NAME.getName());
		sb.append(" LIKE '");
		sb.append("test");
		sb.append("%'");

		UserAO userAO = mock(UserAO.class);
		when(irodsAccessObjectFactory.getUserAO(irodsAccount)).thenReturn(
				userAO);
		when(userAO.findWhere(sb.toString())).thenReturn(users);

		usersController.ajaxUsersSearchByUserName(null);

	}
	
	@Test
	public final void testAjaxUsersSearchByUserGroup() throws Exception {
		UsersController usersController = new UsersController();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAuthenticationToken token = new IRODSAuthenticationToken(
				irodsAccount);
		SecurityContextHolder.getContext().setAuthentication(token);
		IRODSAccessObjectFactoryI irodsAccessObjectFactory = mock(IRODSAccessObjectFactoryI.class);
		usersController.setIrodsAccessObjectFactory(irodsAccessObjectFactory);

		List<User> users = new ArrayList<User>();
		User testUser = new User();
		testUser.setName("test1");
		users.add(testUser);

		StringBuilder sb = new StringBuilder();
		sb.append(RodsGenQueryEnum.COL_USER_NAME.getName());
		sb.append(" LIKE '");
		sb.append("test");
		sb.append("%'");

		UserAO userAO = mock(UserAO.class);
		when(irodsAccessObjectFactory.getUserAO(irodsAccount)).thenReturn(
				userAO);
		when(userAO.findWhere(sb.toString())).thenReturn(users);

		ModelAndView mav = usersController.ajaxUsersSearchByUserGroup("test1");
		TestCase.assertNotNull("mav was null from controller", mav);

		TestCase.assertEquals("wrong view returned", mav.getViewName(),
				"ajax_users_table");
		TestCase.assertTrue("no users returned", mav.getModelMap().containsKey(
				"users"));
	}


	@Test(expected = JargonException.class)
	public final void testAjaxUsersSearchByUserGroupNullUserGroup()
			throws Exception {
		UsersController usersController = new UsersController();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAuthenticationToken token = new IRODSAuthenticationToken(
				irodsAccount);
		SecurityContextHolder.getContext().setAuthentication(token);
		IRODSAccessObjectFactoryI irodsAccessObjectFactory = mock(IRODSAccessObjectFactoryI.class);
		usersController.setIrodsAccessObjectFactory(irodsAccessObjectFactory);

		List<User> users = new ArrayList<User>();
		User testUser = new User();
		testUser.setName("test1");
		users.add(testUser);

		StringBuilder sb = new StringBuilder();
		sb.append(RodsGenQueryEnum.COL_USER_GROUP_NAME.getName());
		sb.append(" LIKE '");
		sb.append("test");
		sb.append("%'");

		UserAO userAO = mock(UserAO.class);
		when(irodsAccessObjectFactory.getUserAO(irodsAccount)).thenReturn(
				userAO);
		when(userAO.findWhere(sb.toString())).thenReturn(users);

		usersController.ajaxUsersSearchByUserGroup(null);

	}

	@Test
	public final void testAjaxUserTableDetailsAction() throws Exception {
		UsersController usersController = new UsersController();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAuthenticationToken token = new IRODSAuthenticationToken(
				irodsAccount);
		SecurityContextHolder.getContext().setAuthentication(token);
		IRODSAccessObjectFactoryI irodsAccessObjectFactory = mock(IRODSAccessObjectFactoryI.class);
		usersController.setIrodsAccessObjectFactory(irodsAccessObjectFactory);

		User testUser = new User();
		testUser.setName("test1");
		testUser.setId("1");
		testUser.setInfo("info");
		testUser.setComment("comment");
		testUser.setUserType(UserTypeEnum.RODS_ADMIN);

		UserAO userAO = mock(UserAO.class);
		when(irodsAccessObjectFactory.getUserAO(irodsAccount)).thenReturn(
				userAO);
		when(userAO.findById("1")).thenReturn(testUser);

		ModelAndView mav = usersController.ajaxUsersTableDetails("1");
		TestCase.assertNotNull("mav was null from controller", mav);

		TestCase.assertEquals("wrong view returned", mav.getViewName(),
				"ajax_users_table_details");
		TestCase.assertTrue("no user returned", mav.getModelMap().containsKey(
				"user"));
		TestCase.assertTrue("no user types returned", mav.getModelMap().containsKey("userTypes"));

	}
	
	@Test
	public final void testUserUpdateAction() throws Exception {
		UsersController usersController = new UsersController();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAuthenticationToken token = new IRODSAuthenticationToken(
				irodsAccount);
		SecurityContextHolder.getContext().setAuthentication(token);
		IRODSAccessObjectFactoryI irodsAccessObjectFactory = mock(IRODSAccessObjectFactoryI.class);
		usersController.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		
		User u = new User();
		u.setId("10");
		u.setComment("userComment");
		u.setInfo("userInfo");
		u.setUserDN("userDN");
		u.setUserType(UserTypeEnum.RODS_ADMIN);
		
		UserAO userAO = mock(UserAO.class);
		when(irodsAccessObjectFactory.getUserAO(irodsAccount)).thenReturn(
				userAO);
		
		
		ModelAndView mav = usersController.updateUser(u.getId(), u.getName(), u.getZone(), u.getComment(), u.getInfo(), u.getUserDN(), u.getUserType().getTextValue());
		TestCase.assertNotNull("mav was null from controller", mav);

		TestCase.assertEquals("wrong view returned", mav.getViewName(),
				"ajax_users_table_details");
		TestCase.assertTrue("no user returned", mav.getModelMap().containsKey(
				"user"));
		TestCase.assertTrue("no user types returned", mav.getModelMap().containsKey("userTypes"));

	}
	
	@Test
	public final void testUserUpdateActionNoId() throws Exception {
		UsersController usersController = new UsersController();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAuthenticationToken token = new IRODSAuthenticationToken(
				irodsAccount);
		SecurityContextHolder.getContext().setAuthentication(token);
		IRODSAccessObjectFactoryI irodsAccessObjectFactory = mock(IRODSAccessObjectFactoryI.class);
		usersController.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		
		User u = new User();
		u.setComment("userComment");
		u.setInfo("userInfo");
		u.setUserDN("userDN");
		u.setUserType(UserTypeEnum.RODS_ADMIN);
		
		UserAO userAO = mock(UserAO.class);
		when(irodsAccessObjectFactory.getUserAO(irodsAccount)).thenReturn(
				userAO);
		
		
		ModelAndView mav = usersController.updateUser("", "name" , "userZone", "userComment", "userInfo", "userDN", "userType");
		TestCase.assertNotNull("mav was null from controller", mav);

		TestCase.assertEquals("wrong view returned", mav.getViewName(),
				"ajax_users_table_details");
		TestCase.assertTrue("no user returned", mav.getModelMap().containsKey(
				"user"));
	}

	@Test(expected = JargonException.class)
	public final void testAjaxUserTableDetailsActionNullUser() throws Exception {
		UsersController usersController = new UsersController();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAuthenticationToken token = new IRODSAuthenticationToken(
				irodsAccount);
		SecurityContextHolder.getContext().setAuthentication(token);
		IRODSAccessObjectFactoryI irodsAccessObjectFactory = mock(IRODSAccessObjectFactoryI.class);
		usersController.setIrodsAccessObjectFactory(irodsAccessObjectFactory);

		UserAO userAO = mock(UserAO.class);
		when(irodsAccessObjectFactory.getUserAO(irodsAccount)).thenReturn(
				userAO);
		usersController.ajaxUsersTableDetails(null);
	}

	@Test(expected = JargonException.class)
	public final void testAjaxUserTableDetailsActionBlankUser()
			throws Exception {
		UsersController usersController = new UsersController();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAuthenticationToken token = new IRODSAuthenticationToken(
				irodsAccount);
		SecurityContextHolder.getContext().setAuthentication(token);
		IRODSAccessObjectFactoryI irodsAccessObjectFactory = mock(IRODSAccessObjectFactoryI.class);
		usersController.setIrodsAccessObjectFactory(irodsAccessObjectFactory);

		UserAO userAO = mock(UserAO.class);
		when(irodsAccessObjectFactory.getUserAO(irodsAccount)).thenReturn(
				userAO);
		usersController.ajaxUsersTableDetails("");
	}

	@Test(expected = JargonException.class)
	public final void testIndexActionNoSecurity() throws Exception {
		UsersController usersController = new UsersController();
		SecurityContextHolder.getContext().setAuthentication(null);
		IRODSAccessObjectFactoryI irodsAccessObjectFactory = mock(IRODSAccessObjectFactoryI.class);
		usersController.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		usersController.ajaxUsersTable();
	}
	
	@Test
	public final void testAjaxUsersSearchByUserType() throws Exception {
		UsersController usersController = new UsersController();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAuthenticationToken token = new IRODSAuthenticationToken(
				irodsAccount);
		SecurityContextHolder.getContext().setAuthentication(token);
		IRODSAccessObjectFactoryI irodsAccessObjectFactory = mock(IRODSAccessObjectFactoryI.class);
		usersController.setIrodsAccessObjectFactory(irodsAccessObjectFactory);

		List<User> users = new ArrayList<User>();
		User testUser = new User();
		testUser.setName("test1");
		users.add(testUser);

		StringBuilder sb = new StringBuilder();
		sb.append(RodsGenQueryEnum.COL_USER_TYPE.getName());
		sb.append(" = '");
		sb.append("rodsadmin");
		sb.append("'");

		UserAO userAO = mock(UserAO.class);
		when(irodsAccessObjectFactory.getUserAO(irodsAccount)).thenReturn(
				userAO);
		when(userAO.findWhere(sb.toString())).thenReturn(users);

		ModelAndView mav = usersController.ajaxUsersSearchByUserType("rodsadmin");
		TestCase.assertNotNull("mav was null from controller", mav);

		TestCase.assertEquals("wrong view returned", mav.getViewName(),
				"ajax_users_table");
		TestCase.assertTrue("no users returned", mav.getModelMap().containsKey(
				"users"));
	}
	
	@Test(expected = JargonException.class)
	public final void testAjaxUsersSearchByInvalidUserType() throws Exception {
		UsersController usersController = new UsersController();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAuthenticationToken token = new IRODSAuthenticationToken(
				irodsAccount);
		SecurityContextHolder.getContext().setAuthentication(token);
		IRODSAccessObjectFactoryI irodsAccessObjectFactory = mock(IRODSAccessObjectFactoryI.class);
		usersController.setIrodsAccessObjectFactory(irodsAccessObjectFactory);

		List<User> users = new ArrayList<User>();
		User testUser = new User();
		testUser.setName("test1");
		users.add(testUser);

		StringBuilder sb = new StringBuilder();
		sb.append(RodsGenQueryEnum.COL_USER_TYPE.getName());
		sb.append(" = '");
		sb.append("rodsadmix");
		sb.append("'");

		UserAO userAO = mock(UserAO.class);
		when(irodsAccessObjectFactory.getUserAO(irodsAccount)).thenReturn(
				userAO);
		when(userAO.findWhere(sb.toString())).thenReturn(users);

		usersController.ajaxUsersSearchByUserType("rodsadmix");
		
	}

	@Test(expected = JargonException.class)
	public final void testAjaxUsersSearchByNullUserType() throws Exception {
		UsersController usersController = new UsersController();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAuthenticationToken token = new IRODSAuthenticationToken(
				irodsAccount);
		SecurityContextHolder.getContext().setAuthentication(token);
		IRODSAccessObjectFactoryI irodsAccessObjectFactory = mock(IRODSAccessObjectFactoryI.class);
		usersController.setIrodsAccessObjectFactory(irodsAccessObjectFactory);

		List<User> users = new ArrayList<User>();
		User testUser = new User();
		testUser.setName("test1");
		users.add(testUser);

		StringBuilder sb = new StringBuilder();
		sb.append(RodsGenQueryEnum.COL_USER_TYPE.getName());
		sb.append(" = '");
		sb.append("rodsadmix");
		sb.append("'");

		UserAO userAO = mock(UserAO.class);
		when(irodsAccessObjectFactory.getUserAO(irodsAccount)).thenReturn(
				userAO);
		when(userAO.findWhere(sb.toString())).thenReturn(users);

		usersController.ajaxUsersSearchByUserType(null);
		
	}
	
	@Test(expected = JargonException.class)
	public final void testAjaxUsersSearchByBlankUserType() throws Exception {
		UsersController usersController = new UsersController();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAuthenticationToken token = new IRODSAuthenticationToken(
				irodsAccount);
		SecurityContextHolder.getContext().setAuthentication(token);
		IRODSAccessObjectFactoryI irodsAccessObjectFactory = mock(IRODSAccessObjectFactoryI.class);
		usersController.setIrodsAccessObjectFactory(irodsAccessObjectFactory);

		List<User> users = new ArrayList<User>();
		User testUser = new User();
		testUser.setName("test1");
		users.add(testUser);

		StringBuilder sb = new StringBuilder();
		sb.append(RodsGenQueryEnum.COL_USER_TYPE.getName());
		sb.append(" = '");
		sb.append("rodsadmix");
		sb.append("'");

		UserAO userAO = mock(UserAO.class);
		when(irodsAccessObjectFactory.getUserAO(irodsAccount)).thenReturn(
				userAO);
		when(userAO.findWhere(sb.toString())).thenReturn(users);

		usersController.ajaxUsersSearchByUserType("");
		
	}
	
	@Test
	public final void testAddUserNewAction() throws Exception {
		
		UsersController usersController = new UsersController();
		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);
		IRODSAuthenticationToken token = new IRODSAuthenticationToken(
				irodsAccount);
		SecurityContextHolder.getContext().setAuthentication(token);
		IRODSAccessObjectFactoryI irodsAccessObjectFactory = mock(IRODSAccessObjectFactoryI.class);
		usersController.setIrodsAccessObjectFactory(irodsAccessObjectFactory);
		
		ModelAndView result = usersController.addUserNewAction();
		TestCase.assertEquals("ajax_user_add", result.getViewName());
		
		UserAddForm userAddForm =(UserAddForm) result.getModelMap().get("userAddForm");
		TestCase.assertNotNull(userAddForm);

	}



}
