package org.irods.jargon.core.protovalues;

public enum ErrorEnum {

	/**
	 * Error Codes
	 */
	SYS_SOCK_OPEN_ERR(-1000), SYS_SOCK_BIND_ERR(-2000), SYS_SOCK_ACCEPT_ERR(-3000), SYS_HEADER_READ_LEN_ERR(-4000),
	SYS_HEADER_WRITE_LEN_ERR(-5000), SYS_HEADER_TPYE_LEN_ERR(-6000), SYS_CAUGHT_SIGNAL(-7000),
	SYS_GETSTARTUP_PACK_ERR(-8000), SYS_EXCEED_CONNECT_CNT(-9000), SYS_USER_NOT_ALLOWED_TO_CONN(-10000),
	SYS_READ_MSG_BODY_INPUT_ERR(-11000), SYS_UNMATCHED_API_NUM(-12000), SYS_NO_API_PRIV(-13000),
	SYS_API_INPUT_ERR(-14000), SYS_PACK_INSTRUCT_FORMAT_ERR(-15000), SYS_MALLOC_ERR(-16000),
	SYS_GET_HOSTNAME_ERR(-17000), SYS_OUT_OF_FILE_DESC(-18000), SYS_FILE_DESC_OUT_OF_RANGE(-19000),
	SYS_UNRECOGNIZED_REMOTE_FLAG(-20000), SYS_INVALID_SERVER_HOST(-21000), SYS_SVR_TO_SVR_CONNECT_FAILED(-22000),
	SYS_BAD_FILE_DESCRIPTOR(-23000), SYS_INTERNAL_NULL_INPUT_ERR(-24000), SYS_CONFIG_FILE_ERR(-25000),
	SYS_INVALID_ZONE_NAME(-26000), SYS_COPY_LEN_ERR(-27000), SYS_PORT_COOKIE_ERR(-28000), SYS_KEY_VAL_TABLE_ERR(-29000),
	SYS_INVALID_RESC_TYPE(-30000), SYS_INVALID_FILE_PATH(-31000), SYS_INVALID_RESC_INPUT(-32000),
	SYS_INVALID_PORTAL_OPR(-33000), SYS_PARA_OPR_NO_SUPPORT(-34000), SYS_INVALID_OPR_TYPE(-35000),
	SYS_NO_PATH_PERMISSION(-36000), SYS_NO_ICAT_SERVER_ERR(-37000), SYS_AGENT_INIT_ERR(-38000),
	SYS_PROXYUSER_NO_PRIV(-39000), SYS_NO_DATA_OBJ_PERMISSION(-40000), SYS_DELETE_DISALLOWED(-41000),
	SYS_OPEN_REI_FILE_ERR(-42000), SYS_NO_RCAT_SERVER_ERR(-43000), SYS_UNMATCH_PACK_INSTRUCTI_NAME(-44000),
	SYS_SVR_TO_CLI_MSI_NO_EXIST(-45000), SYS_COPY_ALREADY_IN_RESC(-46000), SYS_RECONN_OPR_MISMATCH(-47000),
	SYS_INPUT_PERM_OUT_OF_RANGE(-48000), SYS_FORK_ERROR(-49000), SYS_PIPE_ERROR(-50000),
	SYS_EXEC_CMD_STATUS_SZ_ERROR(-51000), SYS_PATH_IS_NOT_A_FILE(-52000), SYS_UNMATCHED_SPEC_COLL_TYPE(-53000),
	SYS_TOO_MANY_QUERY_RESULT(-54000), SYS_SPEC_COLL_NOT_IN_CACHE(-55000), SYS_SPEC_COLL_OBJ_NOT_EXIST(-56000),
	SYS_REG_OBJ_IN_SPEC_COLL(-57000), SYS_DEST_SPEC_COLL_SUB_EXIST(-58000), SYS_SRC_DEST_SPEC_COLL_CONFLICT(-59000),
	SYS_UNKNOWN_SPEC_COLL_CLASS(-60000), SYS_MOUNT_MOUNTED_COLL_ERR(-73000), COLLECTION_NOT_MOUNTED(-74000),
	USER_AUTH_STRING_EMPTY(-60000), COLLECTION_NOT_EMPTY(-79000), FEDERATED_ZONE_NOT_AVAILABLE(-92111),
	SYS_RESC_DOES_NOT_EXIST(-78000), INVALID_INPUT_PARAM(-130000), SYS_REPLICA_INACCESSIBLE(-168000),
	USER_AUTH_SCHEME_ERR(-300000), USER_RODS_HOST_EMPTY(-302000), USER_RODS_HOSTNAME_ERR(-303000),
	USER_SOCK_OPEN_ERR(-304000), USER_SOCK_CONNECT_ERR(-305000), USER_STRLEN_TOOLONG(-306000),
	USER_API_INPUT_ERR(-307000), USER_PACKSTRUCT_INPUT_ERR(-308000), USER_NO_SUPPORT_ERR(-309000),
	USER_FILE_DOES_NOT_EXIST(-310000), USER_FILE_TOO_LARGE(-311000), OVERWITE_WITHOUT_FORCE_FLAG(-312000),
	UNMATCHED_KEY_OR_INDEX(-313000), USER_CHKSUM_MISMATCH(-314000), USER_BAD_KEYWORD_ERR(-315000),
	USER__NULL_INPUT_ERR(-316000), USER_INPUT_PATH_ERR(-317000), USER_INPUT_OPTION_ERR(-318000),
	USER_INVALID_USERNAME_FORMAT(-319000), USER_DIRECT_RESC_INPUT_ERR(-320000), USER_NO_RESC_INPUT_ERR(-321000),
	USER_PARAM_LABEL_ERR(-322000), USER_PARAM_TYPE_ERR(-323000), BASE64_BUFFER_OVERFLOW(-324000),
	BASE64_INVALID_PACKET(-325000), USER_MSG_TYPE_NO_SUPPORT(-326000), USER_RSYNC_NO_MODE_INPUT_ERR(-337000),
	USER_OPTION_INPUT_ERR(-338000), SAME_SRC_DEST_PATHS_ERR(-339000), USER_RESTART_FILE_INPUT_ERR(-340000),
	RESTART_OPR_FAILED(-341000), BAD_EXEC_CMD_PATH(-342000), EXEC_CMD_OUTPUT_TOO_LARGE(-343000),
	EXEC_CMD_ERROR(-344000), BAD_INPUT_DESC_INDEX(-345000), USER_PATH_EXCEEDS_MAX(-346000),
	USER_SOCK_CONNECT_TIMEDOUT(-347000), FILE_INDEX_LOOKUP_ERR(-500000), UNIX_FILE_OPEN_ERR(-510000),
	UNIX_FILE_CREATE_ERR(-511000), UNIX_FILE_READ_ERR(-512000), UNIX_FILE_WRITE_ERR(-513000),
	UNIX_FILE_CLOSE_ERR(-514000), UNIX_FILE_UNLINK_ERR(-515000), UNIX_FILE_STAT_ERR(-516000),
	UNIX_FILE_FSTAT_ERR(-517000), UNIX_FILE_LSEEK_ERR(-518000), UNIX_FILE_FSYNC_ERR(-519000),
	UNIX_FILE_MKDIR_ERR(-520000), UNIX_FILE_MKDIR2_ERR(-520002), UNIX_FILE_RMDIR_ERR(-521000),
	UNIX_FILE_OPENDIR_ERR(-522000), UNIX_FILE_CLOSEDIR_ERR(-523000), UNIX_FILE_READDIR_ERR(-524000),
	UNIX_FILE_STAGE_ERR(-525000), UNIX_FILE_GET_FS_FREESPACE_ERR(-526000), UNIX_FILE_CHMOD_ERR(-527000),
	UNIX_FILE_RENAME_ERR(-528000), UNIX_FILE_TRUNCATE_ERR(-529000), CATALOG_NOT_CONNECTED(-801000),
	CAT_ENV_ERR(-802000), CAT_CONNECT_ERR(-803000), CAT_DISCONNECT_ERR(-804000), CAT_CLOSE_ENV_ERR(-805000),
	CAT_SQL_ERR(-806000), CAT_GET_ROW_ERR(-807000), CAT_NO_ROWS_FOUND(-808000),
	CATALOG_ALREADY_HAS_ITEM_BY_THAT_NAME(-809000), CAT_INVALID_RESOURCE_TYPE(-810000),
	CAT_INVALID_RESOURCE_CLASS(-811000), CAT_INVALID_RESOURCE_NET_ADDR(-812000),
	CAT_INVALID_RESOURCE_VAULT_PATH(-813000), CAT_UNKNOWN_COLLECTION(-814000), CAT_INVALID_DATA_TYPE(-815000),
	CAT_INVALID_ARGUMENT(-816000), CAT_UNKNOWN_FILE(-817000), CAT_NO_ACCESS_PERMISSION(-818000),
	CAT_SUCCESS_BUT_WITH_NO_INFO(-819000), CAT_INVALID_USER_TYPE(-820000), CAT_COLLECTION_NOT_EMPTY(-821000),
	CAT_TOO_MANY_TABLES(-822000), CAT_UNKNOWN_TABLE(-823000), CAT_NOT_OPEN(-824000), CAT_FAILED_TO_LINK_TABLES(-825000),
	CAT_INVALID_AUTHENTICATION(-826000), CAT_INVALID_USER(-827000), CAT_INVALID_ZONE(-828000),
	CAT_INVALID_GROUP(-829000), CAT_INSUFFICIENT_PRIVILEGE_LEVEL(-830000), CAT_INVALID_RESOURCE(-831000),
	CAT_INVALID_CLIENT_USER(-832000), CAT_NAME_EXISTS_AS_COLLECTION(-833000), CAT_NAME_EXISTS_AS_DATAOBJ(-834000),
	CAT_RESOURCE_NOT_EMPTY(-835000), CAT_NOT_A_DATAOBJ_AND_NOT_A_COLLECTION(-836000), CAT_RECURSIVE_MOVE(-837000),
	CAT_LAST_REPLICA(-838000), CAT_OCI_ERROR(-839000), CAT_PASSWORD_EXPIRED(-840000), SPECIFIC_QUERY_EXCEPTION(-853000),
	CAT_HOSTNAME_INVALID(-855000), CAT_TICKET_INVALID(-890000), FILE_OPEN_ERR(-900000), FILE_READ_ERR(-901000),
	FILE_WRITE_ERR(-902000), PASSWORD_EXCEEDS_MAX_SIZE(-903000), ENVIRONMENT_VAR_HOME_NOT_DEFINED(-904000),
	UNABLE_TO_STAT_FILE(-905000), AUTH_FILE_NOT_ENCRYPTED(-906000), AUTH_FILE_DOES_NOT_EXIST(-907000),
	UNLINK_FAILED(-908000), NO_PASSWORD_ENTERED(-909000), PAM_AUTH_ERROR(-993000),
	OBJPATH_EMPTY_IN_STRUCT_ERR(-1000000), RESCNAME_EMPTY_IN_STRUCT_ERR(-1001000),
	DATATYPE_EMPTY_IN_STRUCT_ERR(-1002000), DATASIZE_EMPTY_IN_STRUCT_ERR(-1003000),
	CHKSUM_EMPTY_IN_STRUCT_ERR(-1004000), VERSION_EMPTY_IN_STRUCT_ERR(-1005000), FILEPATH_EMPTY_IN_STRUCT_ERR(-1006000),
	REPLNUM_EMPTY_IN_STRUCT_ERR(-1007000), REPLSTATUS_EMPTY_IN_STRUCT_ERR(-1008000),
	DATAOWNER_EMPTY_IN_STRUCT_ERR(-1009000), DATAOWNERZONE_EMPTY_IN_STRUCT_ERR(-1010000),
	DATAEXPIRY_EMPTY_IN_STRUCT_ERR(-1011000), DATACOMMENTS_EMPTY_IN_STRUCT_ERR(-1012000),
	DATACREATE_EMPTY_IN_STRUCT_ERR(-1013000), DATAMODIFY_EMPTY_IN_STRUCT_ERR(-1014000),
	DATAACCESS_EMPTY_IN_STRUCT_ERR(-1015000), DATAACCESSINX_EMPTY_IN_STRUCT_ERR(-1016000), NO_RULE_FOUND_ERR(-1017000),
	NO_MORE_RULES_ERR(-1018000), UNMATCHED_ACTION_ERR(-1019000), RULES_FILE_READ_ERROR(-1020000),
	ACTION_ARG_COUNT_MISMATCH(-1021000), MAX_NUM_OF_ARGS_IN_ACTION_EXCEEDED(-1022000),
	UNKNOWN_PARAM_IN_RULE_ERR(-1023000), DESTRESCNAME_EMPTY_IN_STRUCT_ERR(-1024000),
	BACKUPRESCNAME_EMPTY_IN_STRUCT_ERR(-1025000), DATAID_EMPTY_IN_STRUCT_ERR(-1026000),
	COLLID_EMPTY_IN_STRUCT_ERR(-1027000), RESCGROUPNAME_EMPTY_IN_STRUCT_ERR(-1028000),
	STATUSSTRING_EMPTY_IN_STRUCT_ERR(-1029000), DATAMAPID_EMPTY_IN_STRUCT_ERR(-1030000),
	USERNAMECLIENT_EMPTY_IN_STRUCT_ERR(-1031000), RODSZONECLIENT_EMPTY_IN_STRUCT_ERR(-1032000),
	USERTYPECLIENT_EMPTY_IN_STRUCT_ERR(-1033000), HOSTCLIENT_EMPTY_IN_STRUCT_ERR(-1034000),
	AUTHSTRCLIENT_EMPTY_IN_STRUCT_ERR(-1035000), USERAUTHSCHEMECLIENT_EMPTY_IN_STRUCT_ERR(-1036000),
	USERINFOCLIENT_EMPTY_IN_STRUCT_ERR(-1037000), USERCOMMENTCLIENT_EMPTY_IN_STRUCT_ERR(-1038000),
	USERCREATECLIENT_EMPTY_IN_STRUCT_ERR(-1039000), USERMODIFYCLIENT_EMPTY_IN_STRUCT_ERR(-1040000),
	USERNAMEPROXY_EMPTY_IN_STRUCT_ERR(-1041000), RODSZONEPROXY_EMPTY_IN_STRUCT_ERR(-1042000),
	USERTYPEPROXY_EMPTY_IN_STRUCT_ERR(-1043000), HOSTPROXY_EMPTY_IN_STRUCT_ERR(-1044000),
	AUTHSTRPROXY_EMPTY_IN_STRUCT_ERR(-1045000), USERAUTHSCHEMEPROXY_EMPTY_IN_STRUCT_ERR(-1046000),
	USERINFOPROXY_EMPTY_IN_STRUCT_ERR(-1047000), USERCOMMENTPROXY_EMPTY_IN_STRUCT_ERR(-1048000),
	USERCREATEPROXY_EMPTY_IN_STRUCT_ERR(-1049000), USERMODIFYPROXY_EMPTY_IN_STRUCT_ERR(-1050000),
	COLLNAME_EMPTY_IN_STRUCT_ERR(-1051000), COLLPARENTNAME_EMPTY_IN_STRUCT_ERR(-1052000),
	COLLOWNERNAME_EMPTY_IN_STRUCT_ERR(-1053000), COLLOWNERZONE_EMPTY_IN_STRUCT_ERR(-1054000),
	COLLEXPIRY_EMPTY_IN_STRUCT_ERR(-1055000), COLLCOMMENTS_EMPTY_IN_STRUCT_ERR(-1056000),
	COLLCREATE_EMPTY_IN_STRUCT_ERR(-1057000), COLLMODIFY_EMPTY_IN_STRUCT_ERR(-1058000),
	COLLACCESS_EMPTY_IN_STRUCT_ERR(-1059000), COLLACCESSINX_EMPTY_IN_STRUCT_ERR(-1060000),
	COLLMAPID_EMPTY_IN_STRUCT_ERR(-1062000), COLLINHERITANCE_EMPTY_IN_STRUCT_ERR(-1063000),
	RESCZONE_EMPTY_IN_STRUCT_ERR(-1065000), RESCLOC_EMPTY_IN_STRUCT_ERR(-1066000),
	RESCTYPE_EMPTY_IN_STRUCT_ERR(-1067000), RESCTYPEINX_EMPTY_IN_STRUCT_ERR(-1068000),
	RESCCLASS_EMPTY_IN_STRUCT_ERR(-1069000), RESCCLASSINX_EMPTY_IN_STRUCT_ERR(-1070000),
	RESCVAULTPATH_EMPTY_IN_STRUCT_ERR(-1071000), NUMOPEN_ORTS_EMPTY_IN_STRUCT_ERR(-1072000),
	PARAOPR_EMPTY_IN_STRUCT_ERR(-1073000), RESCID_EMPTY_IN_STRUCT_ERR(-1074000),
	GATEWAYADDR_EMPTY_IN_STRUCT_ERR(-1075000), RESCMAX_BJSIZE_EMPTY_IN_STRUCT_ERR(-1076000),
	FREESPACE_EMPTY_IN_STRUCT_ERR(-1077000), FREESPACETIME_EMPTY_IN_STRUCT_ERR(-1078000),
	FREESPACETIMESTAMP_EMPTY_IN_STRUCT_ERR(-1079000), RESCINFO_EMPTY_IN_STRUCT_ERR(-1080000),
	RESCCOMMENTS_EMPTY_IN_STRUCT_ERR(-1081000), RESCCREATE_EMPTY_IN_STRUCT_ERR(-1082000),
	RESCMODIFY_EMPTY_IN_STRUCT_ERR(-1083000), INPUT_ARG_NOT_WELL_FORMED_ERR(-1084000),
	INPUT_ARG_OUT_OF_ARGC_RANGE_ERR(-1085000), INSUFFICIENT_INPUT_ARG_ERR(-1086000),
	INPUT_ARG_DOES_NOT_MATCH_ERR(-1087000), RETRY_WITHOUT_RECOVERY_ERR(-1088000), CUT_ACTION_PROCESSED_ERR(-1089000),
	ACTION_FAILED_ERR(-1090000), FAIL_ACTION_ENCOUNTERED_ERR(-1091000), VARIABLE_NAME_TOO_LONG_ERR(-1092000),
	UNKNOWN_VARIABLE_MAP_ERR(-1093000), UNDEFINED_VARIABLE_MAP_ERR(-1094000), NULL_VALUE_ERR(-1095000),
	DVARMAP_FILE_READ_ERROR(-1096000), NO_RULE_OR_MSI_FUNCTION_FOUND_ERR(-1097000), FILE_CREATE_ERROR(-1098000),
	FMAP_FILE_READ_ERROR(-1099000), DATE_FORMAT_ERR(-1100000), RULE_FAILED_ERR(-1101000),
	NO_MICROSERVICE_FOUND_ERR(-1102000), INVALID_REGEXP(-1103000), INVALID_OBJECT_NAME(-1104000),
	INVALID_OBJECT_TYPE(-1105000), NO_VALUES_FOUND(-1106000), NO_COLUMN_NAME_FOUND(-1107000),
	RULE_ENGINE_ERROR(-1201000), RULE_ENGINE_SYNTAX_ERROR(-1211000), KEY_NOT_FOUND(-1800000),
	KEY_TYPE_MISMATCH(-1801000), CHILD_EXISTS(-1802000), HIERARCHY_ERROR(-1803000), CHILD_NOT_FOUND(-1804000),
	NO_NEXT_RESOURCE_FOUND(-1805000), NO_PDMO_DEFINED(-1806000), INVALID_LOCATION(-1807000), PLUGIN_ERROR(-1808000),
	INVALID_RESC_CHILD_CONTEXT(-1809000), INVALID_FILE_OBJECT(-1810000), INVALID_OPERATION(-1811000),
	CHILD_HAS_PARENT(-1812000), FILE_NOT_IN_VAULT(-1813000), DIRECT_ARCHIVE_ACCESS(-1814000),
	ADVANCED_NEGOTIATION_NOT_SUPPORTED(-1815000), DIRECT_CHILD_ACCESS(-1816000), INVALID_DYNAMIC_CAST(-1817000),
	INVALID_ACCESS_TO_IMPOSTOR_RESOURCE(-1818000), INVALID_LEXICAL_CAST(-1819000),
	CONTROL_PLANE_MESSAGE_ERROR(-1820000), REPLICA_NOT_IN_RESC(-18210000), INVALID_ANY_CAST(-1822000),
	BAD_FUNCTION_CALL(-1823000), CLIENT_NEGOTIATION_ERROR(-1824000), SERVER_NEGOTIATION_ERROR(-1825000);

	int i;

	ErrorEnum(final int i) {
		this.i = i;
	}

	public int getInt() {
		return i;
	}

	public static ErrorEnum valueOfString(final String i) {
		return valueOf(Integer.parseInt(i));
	}

	public static ErrorEnum valueOf(final int i) {

		switch (i) {
		/**
		 * Error Codes
		 */
		case -1000:
			return SYS_SOCK_OPEN_ERR;
		case -2000:
			return SYS_SOCK_BIND_ERR;
		case -3000:
			return SYS_SOCK_ACCEPT_ERR;
		case -4000:
			return SYS_HEADER_READ_LEN_ERR;
		case -5000:
			return SYS_HEADER_WRITE_LEN_ERR;
		case -6000:
			return SYS_HEADER_TPYE_LEN_ERR;
		case -7000:
			return SYS_CAUGHT_SIGNAL;
		case -8000:
			return SYS_GETSTARTUP_PACK_ERR;
		case -9000:
			return SYS_EXCEED_CONNECT_CNT;
		case -10000:
			return SYS_USER_NOT_ALLOWED_TO_CONN;
		case -11000:
			return SYS_READ_MSG_BODY_INPUT_ERR;
		case -12000:
			return SYS_UNMATCHED_API_NUM;
		case -13000:
			return SYS_NO_API_PRIV;
		case -14000:
			return SYS_API_INPUT_ERR;
		case -15000:
			return SYS_PACK_INSTRUCT_FORMAT_ERR;
		case -16000:
			return SYS_MALLOC_ERR;
		case -17000:
			return SYS_GET_HOSTNAME_ERR;
		case -18000:
			return SYS_OUT_OF_FILE_DESC;
		case -19000:
			return SYS_FILE_DESC_OUT_OF_RANGE;
		case -20000:
			return SYS_UNRECOGNIZED_REMOTE_FLAG;
		case -21000:
			return SYS_INVALID_SERVER_HOST;
		case -22000:
			return SYS_SVR_TO_SVR_CONNECT_FAILED;
		case -23000:
			return SYS_BAD_FILE_DESCRIPTOR;
		case -24000:
			return SYS_INTERNAL_NULL_INPUT_ERR;
		case -25000:
			return SYS_CONFIG_FILE_ERR;
		case -26000:
			return SYS_INVALID_ZONE_NAME;
		case -27000:
			return SYS_COPY_LEN_ERR;
		case -28000:
			return SYS_PORT_COOKIE_ERR;
		case -29000:
			return SYS_KEY_VAL_TABLE_ERR;
		case -30000:
			return SYS_INVALID_RESC_TYPE;
		case -31000:
			return SYS_INVALID_FILE_PATH;
		case -32000:
			return SYS_INVALID_RESC_INPUT;
		case -33000:
			return SYS_INVALID_PORTAL_OPR;
		case -34000:
			return SYS_PARA_OPR_NO_SUPPORT;
		case -35000:
			return SYS_INVALID_OPR_TYPE;
		case -36000:
			return SYS_NO_PATH_PERMISSION;
		case -37000:
			return SYS_NO_ICAT_SERVER_ERR;
		case -38000:
			return SYS_AGENT_INIT_ERR;
		case -39000:
			return SYS_PROXYUSER_NO_PRIV;
		case -40000:
			return SYS_NO_DATA_OBJ_PERMISSION;
		case -41000:
			return SYS_DELETE_DISALLOWED;
		case -42000:
			return SYS_OPEN_REI_FILE_ERR;
		case -43000:
			return SYS_NO_RCAT_SERVER_ERR;
		case -44000:
			return SYS_UNMATCH_PACK_INSTRUCTI_NAME;
		case -45000:
			return SYS_SVR_TO_CLI_MSI_NO_EXIST;
		case -46000:
			return SYS_COPY_ALREADY_IN_RESC;
		case -47000:
			return SYS_RECONN_OPR_MISMATCH;
		case -48000:
			return SYS_INPUT_PERM_OUT_OF_RANGE;
		case -49000:
			return SYS_FORK_ERROR;
		case -50000:
			return SYS_PIPE_ERROR;
		case -51000:
			return SYS_EXEC_CMD_STATUS_SZ_ERROR;
		case -52000:
			return SYS_PATH_IS_NOT_A_FILE;
		case -53000:
			return SYS_UNMATCHED_SPEC_COLL_TYPE;
		case -54000:
			return SYS_TOO_MANY_QUERY_RESULT;
		case -55000:
			return SYS_SPEC_COLL_NOT_IN_CACHE;
		case -56000:
			return SYS_SPEC_COLL_OBJ_NOT_EXIST;
		case -57000:
			return SYS_REG_OBJ_IN_SPEC_COLL;
		case -58000:
			return SYS_DEST_SPEC_COLL_SUB_EXIST;
		case -59000:
			return SYS_SRC_DEST_SPEC_COLL_CONFLICT;
		case -60000:
			return SYS_UNKNOWN_SPEC_COLL_CLASS;
		case -73000:
			return SYS_MOUNT_MOUNTED_COLL_ERR;
		case -74000:
			return COLLECTION_NOT_MOUNTED;
		case -78000:
			return SYS_RESC_DOES_NOT_EXIST;
		case -79000:
			return COLLECTION_NOT_EMPTY;
		case -92111:
			return FEDERATED_ZONE_NOT_AVAILABLE;
		case -130000:
			return INVALID_INPUT_PARAM;
		case -168000:
			return SYS_REPLICA_INACCESSIBLE;
		case -300000:
			return USER_AUTH_SCHEME_ERR;
		case -301000:
			return USER_AUTH_STRING_EMPTY;
		case -302000:
			return USER_RODS_HOST_EMPTY;
		case -303000:
			return USER_RODS_HOSTNAME_ERR;
		case -304000:
			return USER_SOCK_OPEN_ERR;
		case -305000:
			return USER_SOCK_CONNECT_ERR;
		case -306000:
			return USER_STRLEN_TOOLONG;
		case -307000:
			return USER_API_INPUT_ERR;
		case -308000:
			return USER_PACKSTRUCT_INPUT_ERR;
		case -309000:
			return USER_NO_SUPPORT_ERR;
		case -310000:
			return USER_FILE_DOES_NOT_EXIST;
		case -311000:
			return USER_FILE_TOO_LARGE;
		case -312000:
			return OVERWITE_WITHOUT_FORCE_FLAG;
		case -313000:
			return UNMATCHED_KEY_OR_INDEX;
		case -314000:
			return USER_CHKSUM_MISMATCH;
		case -315000:
			return USER_BAD_KEYWORD_ERR;
		case -316000:
			return USER__NULL_INPUT_ERR;
		case -317000:
			return USER_INPUT_PATH_ERR;
		case -318000:
			return USER_INPUT_OPTION_ERR;
		case -319000:
			return USER_INVALID_USERNAME_FORMAT;
		case -320000:
			return USER_DIRECT_RESC_INPUT_ERR;
		case -321000:
			return USER_NO_RESC_INPUT_ERR;
		case -322000:
			return USER_PARAM_LABEL_ERR;
		case -323000:
			return USER_PARAM_TYPE_ERR;
		case -324000:
			return BASE64_BUFFER_OVERFLOW;
		case -325000:
			return BASE64_INVALID_PACKET;
		case -326000:
			return USER_MSG_TYPE_NO_SUPPORT;
		case -337000:
			return USER_RSYNC_NO_MODE_INPUT_ERR;
		case -338000:
			return USER_OPTION_INPUT_ERR;
		case -339000:
			return SAME_SRC_DEST_PATHS_ERR;
		case -340000:
			return USER_RESTART_FILE_INPUT_ERR;
		case -341000:
			return RESTART_OPR_FAILED;
		case -342000: // TODO Auto-generated constructor stub

			return BAD_EXEC_CMD_PATH;
		case -343000:
			return EXEC_CMD_OUTPUT_TOO_LARGE;
		case -344000:
			return EXEC_CMD_ERROR;
		case -345000:
			return BAD_INPUT_DESC_INDEX;
		case -346000:
			return USER_PATH_EXCEEDS_MAX;
		case -347000:
			return USER_SOCK_CONNECT_TIMEDOUT;
		case -500000:
			return FILE_INDEX_LOOKUP_ERR;
		case -510000:
			return UNIX_FILE_OPEN_ERR;
		case -511000:
			return UNIX_FILE_CREATE_ERR;
		case -512000:
			return UNIX_FILE_READ_ERR;
		case -513000:
			return UNIX_FILE_WRITE_ERR;
		case -514000:
			return UNIX_FILE_CLOSE_ERR;
		case -515000:
			return UNIX_FILE_UNLINK_ERR;
		case -516000:
			return UNIX_FILE_STAT_ERR;
		case -517000:
			return UNIX_FILE_FSTAT_ERR;
		case -518000:
			return UNIX_FILE_LSEEK_ERR;
		case -519000:
			return UNIX_FILE_FSYNC_ERR;
		case -520000:
			return UNIX_FILE_MKDIR_ERR;
		case -520002:
			return UNIX_FILE_MKDIR2_ERR;
		case -521000:
			return UNIX_FILE_RMDIR_ERR;
		case -522000:
			return UNIX_FILE_OPENDIR_ERR;
		case -522002:
			return UNIX_FILE_OPENDIR_ERR;
		case -523000:
			return UNIX_FILE_CLOSEDIR_ERR;
		case -524000:
			return UNIX_FILE_READDIR_ERR;
		case -525000:
			return UNIX_FILE_STAGE_ERR;
		case -526000:
			return UNIX_FILE_GET_FS_FREESPACE_ERR;
		case -527000:
			return UNIX_FILE_CHMOD_ERR;
		case -528000:
			return UNIX_FILE_RENAME_ERR;
		case -529000:
			return UNIX_FILE_TRUNCATE_ERR;
		case -801000:
			return CATALOG_NOT_CONNECTED;
		case -802000:
			return CAT_ENV_ERR;
		case -803000:
			return CAT_CONNECT_ERR;
		case -804000:
			return CAT_DISCONNECT_ERR;
		case -805000:
			return CAT_CLOSE_ENV_ERR;
		case -806000:
			return CAT_SQL_ERR;
		case -807000:
			return CAT_GET_ROW_ERR;
		case -808000:
			return CAT_NO_ROWS_FOUND;
		case -809000:
			return CATALOG_ALREADY_HAS_ITEM_BY_THAT_NAME;
		case -810000:
			return CAT_INVALID_RESOURCE_TYPE;
		case -811000:
			return CAT_INVALID_RESOURCE_CLASS;
		case -812000:
			return CAT_INVALID_RESOURCE_NET_ADDR;
		case -813000:
			return CAT_INVALID_RESOURCE_VAULT_PATH;
		case -814000:
			return CAT_UNKNOWN_COLLECTION;
		case -815000:
			return CAT_INVALID_DATA_TYPE;
		case -816000:
			return CAT_INVALID_ARGUMENT;
		case -817000:
			return CAT_UNKNOWN_FILE;
		case -818000:
			return CAT_NO_ACCESS_PERMISSION;
		case -819000:
			return CAT_SUCCESS_BUT_WITH_NO_INFO;
		case -820000:
			return CAT_INVALID_USER_TYPE;
		case -821000:
			return CAT_COLLECTION_NOT_EMPTY;
		case -822000:
			return CAT_TOO_MANY_TABLES;
		case -823000:
			return CAT_UNKNOWN_TABLE;
		case -824000:
			return CAT_NOT_OPEN;
		case -825000:
			return CAT_FAILED_TO_LINK_TABLES;
		case -826000:
			return CAT_INVALID_AUTHENTICATION;
		case -827000:
			return CAT_INVALID_USER;
		case -828000:
			return CAT_INVALID_ZONE;
		case -829000:
			return CAT_INVALID_GROUP;
		case -830000:
			return CAT_INSUFFICIENT_PRIVILEGE_LEVEL;
		case -831000:
			return CAT_INVALID_RESOURCE;
		case -832000:
			return CAT_INVALID_CLIENT_USER;
		case -833000:
			return CAT_NAME_EXISTS_AS_COLLECTION;
		case -834000:
			return CAT_NAME_EXISTS_AS_DATAOBJ;
		case -835000:
			return CAT_RESOURCE_NOT_EMPTY;
		case -836000:
			return CAT_NOT_A_DATAOBJ_AND_NOT_A_COLLECTION;
		case -837000:
			return CAT_RECURSIVE_MOVE;
		case -838000:
			return CAT_LAST_REPLICA;
		case -839000:
			return CAT_OCI_ERROR;
		case -840000:
			return CAT_PASSWORD_EXPIRED;
		case -853000:
			return SPECIFIC_QUERY_EXCEPTION;
		case -890000:
			return CAT_TICKET_INVALID;
		case -900000:
			return FILE_OPEN_ERR;
		case -901000:
			return FILE_READ_ERR;
		case -902000:
			return FILE_WRITE_ERR;
		case -903000:
			return PASSWORD_EXCEEDS_MAX_SIZE;
		case -904000:
			return ENVIRONMENT_VAR_HOME_NOT_DEFINED;
		case -905000:
			return UNABLE_TO_STAT_FILE;
		case -906000:
			return AUTH_FILE_NOT_ENCRYPTED;
		case -907000:
			return AUTH_FILE_DOES_NOT_EXIST;
		case -908000:
			return UNLINK_FAILED;
		case -909000:
			return NO_PASSWORD_ENTERED;
		case -993000:
			return PAM_AUTH_ERROR;
		case -1000000:
			return OBJPATH_EMPTY_IN_STRUCT_ERR;
		case -1001000:
			return RESCNAME_EMPTY_IN_STRUCT_ERR;
		case -1002000:
			return DATATYPE_EMPTY_IN_STRUCT_ERR;
		case -1003000:
			return DATASIZE_EMPTY_IN_STRUCT_ERR;
		case -1004000:
			return CHKSUM_EMPTY_IN_STRUCT_ERR;
		case -1005000:
			return VERSION_EMPTY_IN_STRUCT_ERR;
		case -1006000:
			return FILEPATH_EMPTY_IN_STRUCT_ERR;
		case -1007000:
			return REPLNUM_EMPTY_IN_STRUCT_ERR;
		case -1008000:
			return REPLSTATUS_EMPTY_IN_STRUCT_ERR;
		case -1009000:
			return DATAOWNER_EMPTY_IN_STRUCT_ERR;
		case -1010000:
			return DATAOWNERZONE_EMPTY_IN_STRUCT_ERR;
		case -1011000:
			return DATAEXPIRY_EMPTY_IN_STRUCT_ERR;
		case -1012000:
			return DATACOMMENTS_EMPTY_IN_STRUCT_ERR;
		case -1013000:
			return DATACREATE_EMPTY_IN_STRUCT_ERR;
		case -1014000:
			return DATAMODIFY_EMPTY_IN_STRUCT_ERR;
		case -1015000:
			return DATAACCESS_EMPTY_IN_STRUCT_ERR;
		case -1016000:
			return DATAACCESSINX_EMPTY_IN_STRUCT_ERR;
		case -1017000:
			return NO_RULE_FOUND_ERR;
		case -1018000:
			return NO_MORE_RULES_ERR;
		case -1019000:
			return UNMATCHED_ACTION_ERR;
		case -1020000:
			return RULES_FILE_READ_ERROR;
		case -1021000:
			return ACTION_ARG_COUNT_MISMATCH;
		case -1022000:
			return MAX_NUM_OF_ARGS_IN_ACTION_EXCEEDED;
		case -1023000:
			return UNKNOWN_PARAM_IN_RULE_ERR;
		case -1024000:
			return DESTRESCNAME_EMPTY_IN_STRUCT_ERR;
		case -1025000:
			return BACKUPRESCNAME_EMPTY_IN_STRUCT_ERR;
		case -1026000:
			return DATAID_EMPTY_IN_STRUCT_ERR;
		case -1027000:
			return COLLID_EMPTY_IN_STRUCT_ERR;
		case -1028000:
			return RESCGROUPNAME_EMPTY_IN_STRUCT_ERR;
		case -1029000:
			return STATUSSTRING_EMPTY_IN_STRUCT_ERR;
		case -1030000:
			return DATAMAPID_EMPTY_IN_STRUCT_ERR;
		case -1031000:
			return USERNAMECLIENT_EMPTY_IN_STRUCT_ERR;
		case -1032000:
			return RODSZONECLIENT_EMPTY_IN_STRUCT_ERR;
		case -1033000:
			return USERTYPECLIENT_EMPTY_IN_STRUCT_ERR;
		case -1034000:
			return HOSTCLIENT_EMPTY_IN_STRUCT_ERR;
		case -1035000:
			return AUTHSTRCLIENT_EMPTY_IN_STRUCT_ERR;
		case -1036000:
			return USERAUTHSCHEMECLIENT_EMPTY_IN_STRUCT_ERR;
		case -1037000:
			return USERINFOCLIENT_EMPTY_IN_STRUCT_ERR;
		case -1038000:
			return USERCOMMENTCLIENT_EMPTY_IN_STRUCT_ERR;
		case -1039000:
			return USERCREATECLIENT_EMPTY_IN_STRUCT_ERR;
		case -1040000:
			return USERMODIFYCLIENT_EMPTY_IN_STRUCT_ERR;
		case -1041000:
			return USERNAMEPROXY_EMPTY_IN_STRUCT_ERR;
		case -1042000:
			return RODSZONEPROXY_EMPTY_IN_STRUCT_ERR;
		case -1043000:
			return USERTYPEPROXY_EMPTY_IN_STRUCT_ERR;
		case -1044000:
			return HOSTPROXY_EMPTY_IN_STRUCT_ERR;
		case -1045000:
			return AUTHSTRPROXY_EMPTY_IN_STRUCT_ERR;
		case -1046000:
			return USERAUTHSCHEMEPROXY_EMPTY_IN_STRUCT_ERR;
		case -1047000:
			return USERINFOPROXY_EMPTY_IN_STRUCT_ERR;
		case -1048000:
			return USERCOMMENTPROXY_EMPTY_IN_STRUCT_ERR;
		case -1049000:
			return USERCREATEPROXY_EMPTY_IN_STRUCT_ERR;
		case -1050000:
			return USERMODIFYPROXY_EMPTY_IN_STRUCT_ERR;
		case -1051000:
			return COLLNAME_EMPTY_IN_STRUCT_ERR;
		case -1052000:
			return COLLPARENTNAME_EMPTY_IN_STRUCT_ERR;
		case -1053000:
			return COLLOWNERNAME_EMPTY_IN_STRUCT_ERR;
		case -1054000:
			return COLLOWNERZONE_EMPTY_IN_STRUCT_ERR;
		case -1055000:
			return COLLEXPIRY_EMPTY_IN_STRUCT_ERR;
		case -1056000:
			return COLLCOMMENTS_EMPTY_IN_STRUCT_ERR;
		case -1057000:
			return COLLCREATE_EMPTY_IN_STRUCT_ERR;
		case -1058000:
			return COLLMODIFY_EMPTY_IN_STRUCT_ERR;
		case -1059000:
			return COLLACCESS_EMPTY_IN_STRUCT_ERR;
		case -1060000:
			return COLLACCESSINX_EMPTY_IN_STRUCT_ERR;
		case -1062000:
			return COLLMAPID_EMPTY_IN_STRUCT_ERR;
		case -1063000:
			return COLLINHERITANCE_EMPTY_IN_STRUCT_ERR;
		case -1065000:
			return RESCZONE_EMPTY_IN_STRUCT_ERR;
		case -1066000:
			return RESCLOC_EMPTY_IN_STRUCT_ERR;
		case -1067000:
			return RESCTYPE_EMPTY_IN_STRUCT_ERR;
		case -1068000:
			return RESCTYPEINX_EMPTY_IN_STRUCT_ERR;
		case -1069000:
			return RESCCLASS_EMPTY_IN_STRUCT_ERR;
		case -1070000:
			return RESCCLASSINX_EMPTY_IN_STRUCT_ERR;
		case -1071000:
			return RESCVAULTPATH_EMPTY_IN_STRUCT_ERR;
		case -1072000:
			return NUMOPEN_ORTS_EMPTY_IN_STRUCT_ERR;
		case -1073000:
			return PARAOPR_EMPTY_IN_STRUCT_ERR;
		case -1074000:
			return RESCID_EMPTY_IN_STRUCT_ERR;
		case -1075000:
			return GATEWAYADDR_EMPTY_IN_STRUCT_ERR;
		case -1076000:
			return RESCMAX_BJSIZE_EMPTY_IN_STRUCT_ERR;
		case -1077000:
			return FREESPACE_EMPTY_IN_STRUCT_ERR;
		case -1078000:
			return FREESPACETIME_EMPTY_IN_STRUCT_ERR;
		case -1079000:
			return FREESPACETIMESTAMP_EMPTY_IN_STRUCT_ERR;
		case -1080000:
			return RESCINFO_EMPTY_IN_STRUCT_ERR;
		case -1081000:
			return RESCCOMMENTS_EMPTY_IN_STRUCT_ERR;
		case -1082000:
			return RESCCREATE_EMPTY_IN_STRUCT_ERR;
		case -1083000:
			return RESCMODIFY_EMPTY_IN_STRUCT_ERR;
		case -1084000:
			return INPUT_ARG_NOT_WELL_FORMED_ERR;
		case -1085000:
			return INPUT_ARG_OUT_OF_ARGC_RANGE_ERR;
		case -1086000:
			return INSUFFICIENT_INPUT_ARG_ERR;
		case -1087000:
			return INPUT_ARG_DOES_NOT_MATCH_ERR;
		case -1088000:
			return RETRY_WITHOUT_RECOVERY_ERR;
		case -1089000:
			return CUT_ACTION_PROCESSED_ERR;
		case -1090000:
			return ACTION_FAILED_ERR;
		case -1091000:
			return FAIL_ACTION_ENCOUNTERED_ERR;
		case -1092000:
			return VARIABLE_NAME_TOO_LONG_ERR;
		case -1093000:
			return UNKNOWN_VARIABLE_MAP_ERR;
		case -1094000:
			return UNDEFINED_VARIABLE_MAP_ERR;
		case -1095000:
			return NULL_VALUE_ERR;
		case -1096000:
			return DVARMAP_FILE_READ_ERROR;
		case -1097000:
			return NO_RULE_OR_MSI_FUNCTION_FOUND_ERR;
		case -1098000:
			return FILE_CREATE_ERROR;
		case -1099000:
			return FMAP_FILE_READ_ERROR;
		case -1100000:
			return DATE_FORMAT_ERR;
		case -1101000:
			return RULE_FAILED_ERR;
		case -1102000:
			return NO_MICROSERVICE_FOUND_ERR;
		case -1103000:
			return INVALID_REGEXP;
		case -1104000:
			return INVALID_OBJECT_NAME;
		case -1105000:
			return INVALID_OBJECT_TYPE;
		case -1106000:
			return NO_VALUES_FOUND;
		case -1107000:
			return NO_COLUMN_NAME_FOUND;
		case -1201000:
			return RULE_ENGINE_ERROR;
		case -1211000:
			return RULE_ENGINE_SYNTAX_ERROR;
		case -1800000:
			return KEY_NOT_FOUND;
		case -1801000:
			return KEY_TYPE_MISMATCH;
		case -1802000:
			return CHILD_EXISTS;
		case -1803000:
			return HIERARCHY_ERROR;
		case -1804000:
			return CHILD_NOT_FOUND;
		case -1805000:
			return NO_NEXT_RESOURCE_FOUND;
		case -1806000:
			return NO_PDMO_DEFINED;
		case -1807000:
			return INVALID_LOCATION;
		case -1808000:
			return PLUGIN_ERROR;
		case -1809000:
			return INVALID_RESC_CHILD_CONTEXT;
		case -1810000:
			return INVALID_FILE_OBJECT;
		case -1811000:
			return INVALID_OPERATION;
		case -1812000:
			return CHILD_HAS_PARENT;
		case -1813000:
			return FILE_NOT_IN_VAULT;
		case -1814000:
			return DIRECT_ARCHIVE_ACCESS;
		case -1815000:
			return ADVANCED_NEGOTIATION_NOT_SUPPORTED;
		case -1816000:
			return DIRECT_CHILD_ACCESS;
		case -1817000:
			return INVALID_DYNAMIC_CAST;
		case -1818000:
			return INVALID_ACCESS_TO_IMPOSTOR_RESOURCE;
		case -1819000:
			return INVALID_LEXICAL_CAST;
		case -1820000:
			return CONTROL_PLANE_MESSAGE_ERROR;
		case -1821000:
			return REPLICA_NOT_IN_RESC;
		case -1822000:
			return INVALID_ANY_CAST;
		case -1823000:
			return BAD_FUNCTION_CALL;
		case -1824000:
			return CLIENT_NEGOTIATION_ERROR;
		case -1825000:
			return SERVER_NEGOTIATION_ERROR;
		default:
			throw new IllegalArgumentException("unexpected int: " + i);
		}
	}
}
