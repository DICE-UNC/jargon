package org.irods.jargon.lingo.mvc.controllers;

import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.protovalues.UserTypeEnum;
import org.irods.jargon.core.pub.UserAO;
import org.irods.jargon.core.pub.domain.User;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.irods.jargon.lingo.exceptions.LingoException;
import org.irods.jargon.lingo.mvc.controllers.forms.UserAddForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@RequestMapping("/users/**")
@Controller
public class UsersController extends AbstractLingoController {

	// FIXME: change throws to remove JargonException
	
	private Logger log = LoggerFactory.getLogger(UsersController.class);

	@RequestMapping("/users")
	public ModelAndView indexAction() throws JargonException, LingoException {
		log.debug("user controller action");
		
		checkControllerInjectedContracts();

		ModelAndView mav = new ModelAndView();
		mav.setViewName("users");
		
		// user types are used to build form select
		List<String> userTypes = UserTypeEnum.getUserTypeList();
		
		mav.addObject("userTypes", userTypes);

		log.debug("returning mav from usersController");
		return mav;
	}

	@RequestMapping("/users/ajax_usertable")
	public ModelAndView ajaxUsersTable() throws JargonException, LingoException {
		log.debug("ajax user controller action");
				
		checkControllerInjectedContracts();

		ModelAndView mav = new ModelAndView();
		mav.setViewName("ajax_users_table");
		
		IRODSAccount irodsAccount = this.getAuthenticatedIRODSAccount();
		log.debug("logged in as {}", irodsAccount.toString());

		log.debug("getting user ao");
		UserAO userAO = getIrodsAccessObjectFactory().getUserAO(irodsAccount);
		log.debug("got user AO, getting users list");
		List<User> users = userAO.findAll();
		for (User user : users) {
			log.debug("got user:" + user);
		}

		getIrodsAccessObjectFactory().closeSession();

		log.debug("session closed...");
		mav.addObject("users", users);
		log.debug("returning mav from usersController for ajax call");
		return mav;
	}

	@RequestMapping("/users/ajax_user_search_user_name")
	public ModelAndView ajaxUsersSearchByUserName(
			@RequestParam("userName") String userName) throws JargonException, LingoException {
		
		if (userName == null) {
			throw new JargonException("null user name for search");
		}
		
		checkControllerInjectedContracts();

		log.debug("ajaxUsersSearchByUserName() controller action for userName: {}", userName);
		ModelAndView mav = new ModelAndView();
		mav.setViewName("ajax_users_table");
		IRODSAccount irodsAccount = this.getAuthenticatedIRODSAccount();
		log.debug("logged in as {}", irodsAccount.toString());

		log.debug("getting user ao");
		UserAO userAO = getIrodsAccessObjectFactory().getUserAO(irodsAccount);
		log.debug("got user AO, getting users list");
		
		StringBuilder sb = new StringBuilder();
		sb.append(RodsGenQueryEnum.COL_USER_NAME.getName());
		sb.append(" LIKE '");
		sb.append(userName.trim());
		sb.append("%'");
		
		List<User> users = userAO.findWhere(sb.toString());
		for (User user : users) {
			log.debug("got user:" + user);
		}

		getIrodsAccessObjectFactory().closeSession();

		log.debug("session closed...");
		mav.addObject("users", users);
		log.debug("returning mav from usersController for ajax call");
		return mav;
	}
	
	@RequestMapping("/users/ajax_user_search_user_type")
	public ModelAndView ajaxUsersSearchByUserType(
			@RequestParam("userType") String userType) throws JargonException, LingoException {
		
		if (userType == null) {
			throw new JargonException("null user type for search");
		}
		
		checkControllerInjectedContracts();
		
		UserTypeEnum userTypeEnum = UserTypeEnum.findTypeByString(userType);
		if (userTypeEnum.equals(UserTypeEnum.RODS_UNKNOWN)) {
			throw new JargonException("unknown user type:" + userType.trim());
		}
		
		log.debug("ajaxUsersSearchByUserType() controller action for userType: {}", userType);
		ModelAndView mav = new ModelAndView();
		mav.setViewName("ajax_users_table");
		IRODSAccount irodsAccount = this.getAuthenticatedIRODSAccount();
		log.debug("logged in as {}", irodsAccount.toString());

		log.debug("getting user ao");
		UserAO userAO = getIrodsAccessObjectFactory().getUserAO(irodsAccount);
		log.debug("got user AO, getting users list");
		
		StringBuilder sb = new StringBuilder();
		sb.append(RodsGenQueryEnum.COL_USER_TYPE .getName());
		sb.append(" = '");
		sb.append(userType.trim());
		sb.append("'");
		
		List<User> users = userAO.findWhere(sb.toString());
		for (User user : users) {
			log.debug("got user:" + user);
		}

		getIrodsAccessObjectFactory().closeSession();

		log.debug("session closed...");
		mav.addObject("users", users);
		log.debug("returning mav from usersController for ajax call");
		return mav;
	}
	
	@RequestMapping("/users/ajax_user_search_user_group")
	public ModelAndView ajaxUsersSearchByUserGroup(
			@RequestParam("userGroup") String userGroup) throws JargonException, LingoException {
		
		if (userGroup == null) {
			throw new JargonException("null user name for search");
		}
		
		checkControllerInjectedContracts();
		
		log.debug("ajaxUsersSearchByUserGroup() controller action for userGroup: {}", userGroup);
		ModelAndView mav = new ModelAndView();
		mav.setViewName("ajax_users_table");
		IRODSAccount irodsAccount = this.getAuthenticatedIRODSAccount();
		log.debug("logged in as {}", irodsAccount.toString());

		log.debug("getting user ao");
		UserAO userAO = getIrodsAccessObjectFactory().getUserAO(irodsAccount);
		log.debug("got user AO, getting users list");
		
		StringBuilder sb = new StringBuilder();
		sb.append(RodsGenQueryEnum.COL_USER_GROUP_NAME.getName());
		sb.append(" LIKE '");
		sb.append(userGroup.trim());
		sb.append("%'");
				
		List<User> users = userAO.findWhere(sb.toString());
		for (User user : users) {
			log.debug("got user:" + user);
		}

		getIrodsAccessObjectFactory().closeSession();

		log.debug("session closed...");
		mav.addObject("users", users);
		log.debug("returning mav from usersController for ajax call");
		return mav;
	}
	
	
	
	@RequestMapping("/users/ajax_user_table_details/{userId}")
	public ModelAndView ajaxUsersTableDetails(
			@PathVariable("userId") String userId) throws JargonException, LingoException {

		if (userId == null || userId.length() == 0) {
			String message = "no user id parameter provided";
			log.error(message);
			throw new JargonException(message);
		}

		if (log.isInfoEnabled()) {
			log.info("ajax user details action for" + userId);
		}
		
		checkControllerInjectedContracts();


		ModelAndView mav = new ModelAndView();
		mav.setViewName("ajax_users_table_details");

		IRODSAccount irodsAccount = this.getAuthenticatedIRODSAccount();
		log.debug("logged in as {}", irodsAccount.toString());
		
		log.debug("getting user ao");
		UserAO userAO = getIrodsAccessObjectFactory().getUserAO(irodsAccount);

		User user = null;

		try {
			user = userAO.findById(userId);
		} catch (DataNotFoundException e) {
			StringBuilder errorMessage = new StringBuilder();
			errorMessage.append("user not found for id:");
			errorMessage.append(userId);
			String errorMessageString = errorMessage.toString();
			log.error(errorMessageString);
			throw new JargonException(errorMessageString);
		}

		getIrodsAccessObjectFactory().closeSession();

		log.debug("session closed...");
		mav.addObject("user", user);
		mav.addObject("userTypes", UserTypeEnum.getUserTypeList());
		log
				.debug("returning mav from usersController for ajax call with user details");
		return mav;
	}
	
	@RequestMapping(value = "/users/update_user", method= RequestMethod.POST)
	public ModelAndView updateUser(
			@RequestParam("id") String userId, 
			@RequestParam("name") String userName, 
			@RequestParam("zone") String userZone,
			@RequestParam("comment") String userComment,
			@RequestParam("info") String userInfo,
			@RequestParam("userDN") String userDN,
			@RequestParam("userType.textValue") String userType) throws JargonException, DataNotFoundException, LingoException {

		if (log.isInfoEnabled()) {
			log.info("user update action for id:" + userId);
		}
		
		checkControllerInjectedContracts();

		ModelAndView mav = new ModelAndView();
		mav.setViewName("ajax_users_table_details");

		IRODSAccount irodsAccount = this.getAuthenticatedIRODSAccount();
		log.debug("logged in as {}", irodsAccount.toString());
		
		log.debug("getting user ao");
		UserAO userAO = getIrodsAccessObjectFactory().getUserAO(irodsAccount);

		User user = new User();
		user.setId(userId);
		user.setName(userName);
		user.setComment(userComment);
		user.setInfo(userInfo);
		user.setUserDN(userDN);
		user.setUserType(UserTypeEnum.findTypeByString(userType));
		user.setZone(userZone);
		
		log.info("updating user: {}", user.toString());
		userAO.updateUser(user);
		
		user = userAO.findById(user.getId());
		
		getIrodsAccessObjectFactory().closeSession();

		log.debug("session closed...");
		List<String> userTypes = UserTypeEnum.getUserTypeList();
		mav.addObject("user", user);
		mav.addObject("userTypes", userTypes);

		log
				.debug("returning mav from usersController for ajax call with user details");
		return mav;
	}
	
	/**
	 * This method responds to a GET operation, and will initialize to add a
	 * series by creating and pre-populating reference data for the
	 * <code>SeriesForm</code>
	 * 
	 * @return
	 * @throws JargonException
	 */
	@RequestMapping(value = "/users/add", method = RequestMethod.GET)
	public ModelAndView addUserNewAction() throws LingoException {
		log.debug("executing  addSeriesNewAction");
		checkControllerInjectedContracts();
		final ModelAndView mav = new ModelAndView();
		mav.setViewName("ajax_user_add");
		mav.addObject("userAddForm", new UserAddForm());
		
		IRODSAccount irodsAccount;
		try {
			irodsAccount = this.getAuthenticatedIRODSAccount();
		} catch (JargonException e) {
			log.error("error getting irodsAccount", e);
			throw new LingoException(e);
		}
		log.debug("got irods account");
		log.debug("returning mav from Controller for view: {}", mav
				.getViewName());
		return mav;
		
	}

	

}
