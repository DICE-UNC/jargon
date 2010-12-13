//  Copyright (c) 2008, Regents of the University of California
//  All rights reserved.
//
//  Redistribution and use in source and binary forms, with or without
//  modification, are permitted provided that the following conditions are
//  met:
//
//    * Redistributions of source code must retain the above copyright notice,
//  this list of conditions and the following disclaimer.
//    * Redistributions in binary form must reproduce the above copyright
//  notice, this list of conditions and the following disclaimer in the
//  documentation and/or other materials provided with the distribution.
//    * Neither the name of the University of California, San Diego (UCSD) nor
//  the names of its contributors may be used to endorse or promote products
//  derived from this software without specific prior written permission.
//
//  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
//  IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
//  THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
//  PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
//  EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
//  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
//  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
//  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
//  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
//  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
//
//  FILE
//  IRODSException.java  -  edu.sdsc.grid.io.irods.IRODSException
//
//  CLASS HIERARCHY
//  java.lang.Object
//      |
//      +-java.io.IOException
//          |
//          +-edu.sdsc.grid.io.irods.IRODSException
//
//  PRINCIPAL AUTHOR
//  Lucas Gilbert, SDSC/UCSD
//
//
package edu.sdsc.grid.io.irods;

import java.io.IOException;

/**
 * Handles errors returned by the iRODS server
 * 
 * @author Lucas Gilbert, San Diego Supercomputer Center
 * @since JARGON2.0
 */
public class IRODSException extends IOException {
	/**
	 * IRODS error integer returned by server
	 */
	private int type;

	IRODSException(final String message) {
		super(message);

	}

	IRODSException(final String message, final int type) {
		super(message);
		this.type = type;
	}

	public int getType() {
		return type;
	}

	/**
	 * Only return the socket error
	 */
	int getSocketError() {
		return type - getSimpleType();
	}

	/**
	 * Drop the socket error portion.
	 */
	int getSimpleType() {
		return ((type / 1000)) * 1000;
	}

	/*
	 * rodsErrorTable.h - common header file for rods server and agents
	 * 
	 * error code format: -mmmmnnn where -mmmm000 is an error code defined in
	 * the rodsErrorTable.h to define an error event. e.g.,
	 * 
	 * static final int SYS_SOCK_OPEN_ERR -1000
	 * 
	 * which define an error when a socket open call failed. Here mmmm = 1
	 * 
	 * nnn is the errno associated with the socket open call. So, if the errno
	 * is 34, then the error returned to the user is -1034. We use 3 figures for
	 * nnn because the errno is less than 1000.
	 */

	// 1,000 - 299,000 - system type
	static final int SYS_SOCK_OPEN_ERR = -1000;
	static final int SYS_SOCK_BIND_ERR = -2000;
	static final int SYS_SOCK_ACCEPT_ERR = -3000;
	static final int SYS_HEADER_READ_LEN_ERR = -4000;
	static final int SYS_HEADER_WRITE_LEN_ERR = -5000;
	static final int SYS_HEADER_TPYE_LEN_ERR = -6000;
	static final int SYS_CAUGHT_SIGNAL = -7000;
	static final int SYS_GETSTARTUP_PACK_ERR = -8000;
	static final int SYS_EXCEED_CONNECT_CNT = -9000;
	static final int SYS_USER_NOT_ALLOWED_TO_CONN = -10000;
	static final int SYS_READ_MSG_BODY_INPUT_ERR = -11000;
	static final int SYS_UNMATCHED_API_NUM = -12000;
	static final int SYS_NO_API_PRIV = -13000;
	static final int SYS_API_INPUT_ERR = -14000;
	static final int SYS_PACK_INSTRUCT_FORMAT_ERR = -15000;
	static final int SYS_MALLOC_ERR = -16000;
	static final int SYS_GET_HOSTNAME_ERR = -17000;
	static final int SYS_OUT_OF_FILE_DESC = -18000;
	static final int SYS_FILE_DESC_OUT_OF_RANGE = -19000;
	static final int SYS_UNRECOGNIZED_REMOTE_FLAG = -20000;
	static final int SYS_INVALID_SERVER_HOST = -21000;
	static final int SYS_SVR_TO_SVR_CONNECT_FAILED = -22000;
	static final int SYS_BAD_FILE_DESCRIPTOR = -23000;
	static final int SYS_INTERNAL_NULL_INPUT_ERR = -24000;
	static final int SYS_CONFIG_FILE_ERR = -25000;
	static final int SYS_INVALID_ZONE_NAME = -26000;
	static final int SYS_COPY_LEN_ERR = -27000;
	static final int SYS_PORT_COOKIE_ERR = -28000;
	static final int SYS_KEY_VAL_TABLE_ERR = -29000;
	static final int SYS_INVALID_RESC_TYPE = -30000;
	static final int SYS_INVALID_FILE_PATH = -31000;
	static final int SYS_INVALID_RESC_INPUT = -32000;
	static final int SYS_INVALID_PORTAL_OPR = -33000;
	static final int SYS_PARA_OPR_NO_SUPPORT = -34000;
	static final int SYS_INVALID_OPR_TYPE = -35000;
	static final int SYS_NO_PATH_PERMISSION = -36000;
	static final int SYS_NO_ICAT_SERVER_ERR = -37000;
	static final int SYS_AGENT_INIT_ERR = -38000;
	static final int SYS_PROXYUSER_NO_PRIV = -39000;
	static final int SYS_NO_DATA_OBJ_PERMISSION = -40000;
	static final int SYS_DELETE_DISALLOWED = -41000;
	static final int SYS_OPEN_REI_FILE_ERR = -42000;
	static final int SYS_NO_RCAT_SERVER_ERR = -43000;
	static final int SYS_UNMATCH_PACK_INSTRUCTI_NAME = -44000;
	static final int SYS_SVR_TO_CLI_MSI_NO_EXIST = -45000;
	static final int SYS_COPY_ALREADY_IN_RESC = -46000;

	// 300,000 - 499,000 - user input type error
	static final int USER_AUTH_SCHEME_ERR = -300000;
	static final int USER_AUTH_STRING_EMPTY = -301000;
	static final int USER_RODS_HOST_EMPTY = -302000;
	static final int USER_RODS_HOSTNAME_ERR = -303000;
	static final int USER_SOCK_OPEN_ERR = -304000;
	static final int USER_SOCK_CONNECT_ERR = -305000;
	static final int USER_STRLEN_TOOLONG = -306000;
	static final int USER_API_INPUT_ERR = -307000;
	static final int USER_PACKSTRUCT_INPUT_ERR = -308000;
	static final int USER_NO_SUPPORT_ERR = -309000;
	static final int USER_FILE_DOES_NOT_EXIST = -310000;
	static final int USER_FILE_TOO_LARGE = -311000;
	public static final int OVERWITE_WITHOUT_FORCE_FLAG = -312000;
	static final int UNMATCHED_KEY_OR_INDEX = -313000;
	static final int USER_CHKSUM_MISMATCH = -314000;
	static final int USER_BAD_KEYWORD_ERR = -315000;
	static final int USER__NULL_INPUT_ERR = -316000;
	static final int USER_INPUT_PATH_ERR = -317000;
	static final int USER_INPUT_OPTION_ERR = -318000;
	static final int USER_INVALID_USERNAME_FORMAT = -319000;
	static final int USER_DIRECT_RESC_INPUT_ERR = -320000;
	static final int USER_NO_RESC_INPUT_ERR = -321000;
	static final int USER_PARAM_LABEL_ERR = -322000;
	static final int USER_PARAM_TYPE_ERR = -323000;
	static final int BASE64_BUFFER_OVERFLOW = -324000;
	static final int BASE64_INVALID_PACKET = -325000;
	static final int USER_MSG_TYPE_NO_SUPPORT = -326000;
	static final int USER_RSYNC_NO_MODE_INPUT_ERR = -337000;
	static final int USER_OPTION_INPUT_ERR = -338000;
	static final int SAME_SRC_DEST_PATHS_ERR = -339000;

	// 500,000 to 800,000 - file driver error
	static final int FILE_INDEX_LOOKUP_ERR = -500000;
	static final int UNIX_FILE_OPEN_ERR = -510000;
	static final int UNIX_FILE_CREATE_ERR = -511000;
	static final int UNIX_FILE_READ_ERR = -512000;
	static final int UNIX_FILE_WRITE_ERR = -513000;
	static final int UNIX_FILE_CLOSE_ERR = -514000;
	static final int UNIX_FILE_UNLINK_ERR = -515000;
	static final int UNIX_FILE_STAT_ERR = -516000;
	static final int UNIX_FILE_FSTAT_ERR = -517000;
	static final int UNIX_FILE_LSEEK_ERR = -518000;
	static final int UNIX_FILE_FSYNC_ERR = -519000;
	static final int UNIX_FILE_MKDIR_ERR = -520000;
	static final int UNIX_FILE_RMDIR_ERR = -521000;
	static final int UNIX_FILE_OPENDIR_ERR = -522000;
	static final int UNIX_FILE_CLOSEDIR_ERR = -523000;
	static final int UNIX_FILE_READDIR_ERR = -524000;
	static final int UNIX_FILE_STAGE_ERR = -525000;
	static final int UNIX_FILE_GET_FS_FREESPACE_ERR = -526000;
	static final int UNIX_FILE_CHMOD_ERR = -527000;
	static final int UNIX_FILE_RENAME_ERR = -528000;

	// 800,000 to 900,000 - Catalog library errors
	static final int CATALOG_NOT_CONNECTED = -801000;
	static final int CAT_ENV_ERR = -802000;
	static final int CAT_CONNECT_ERR = -803000;
	static final int CAT_DISCONNECT_ERR = -804000;
	static final int CAT_CLOSE_ENV_ERR = -805000;
	static final int CAT_SQL_ERR = -806000;
	static final int CAT_GET_ROW_ERR = -807000;
	public static final int CAT_NO_ROWS_FOUND = -808000;
	static final int CATALOG_ALREADY_HAS_ITEM_BY_THAT_NAME = -809000;
	static final int CAT_INVALID_RESOURCE_TYPE = -810000;
	static final int CAT_INVALID_RESOURCE_CLASS = -811000;
	static final int CAT_INVALID_RESOURCE_NET_ADDR = -812000;
	static final int CAT_INVALID_RESOURCE_VAULT_PATH = -813000;
	static final int CAT_UNKNOWN_COLLECTION = -814000;
	static final int CAT_INVALID_DATA_TYPE = -815000;
	static final int CAT_INVALID_ARGUMENT = -816000;
	static final int CAT_UNKNOWN_FILE = -817000;
	static final int CAT_NO_ACCESS_PERMISSION = -818000;
	public static final int CAT_SUCCESS_BUT_WITH_NO_INFO = -819000;
	static final int CAT_INVALID_USER_TYPE = -820000;
	static final int CAT_COLLECTION_NOT_EMPTY = -821000;
	static final int CAT_TOO_MANY_TABLES = -822000;
	static final int CAT_UNKNOWN_TABLE = -823000;
	static final int CAT_NOT_OPEN = -824000;
	static final int CAT_FAILED_TO_LINK_TABLES = -825000;
	static final int CAT_INVALID_AUTHENTICATION = -826000;
	static final int CAT_INVALID_USER = -827000;
	static final int CAT_INVALID_ZONE = -828000;
	static final int CAT_INVALID_GROUP = -829000;
	static final int CAT_INSUFFICIENT_PRIVILEGE_LEVEL = -830000;
	static final int CAT_INVALID_RESOURCE = -831000;
	static final int CAT_INVALID_CLIENT_USER = -832000;
	static final int CAT_NAME_EXISTS_AS_COLLECTION = -833000;
	static final int CAT_NAME_EXISTS_AS_DATAOBJ = -834000;
	static final int CAT_RESOURCE_NOT_EMPTY = -835000;
	static final int CAT_NOT_A_DATAOBJ_AND_NOT_A_COLLECTION = -836000;
	static final int CAT_RECURSIVE_MOVE = -837000;
	static final int CAT_LAST_REPLICA = -838000;

	// 900,000 to 999,000 - Misc errors (used by obf library)
	static final int FILE_OPEN_ERR = -900000;
	static final int FILE_READ_ERR = -901000;
	static final int FILE_WRITE_ERR = -902000;
	static final int PASSWORD_EXCEEDS_MAX_SIZE = -903000;
	static final int ENVIRONMENT_VAR_HOME_NOT_DEFINED = -904000;
	static final int UNABLE_TO_STAT_FILE = -905000;
	static final int AUTH_FILE_NOT_ENCRYPTED = -906000;
	static final int AUTH_FILE_DOES_NOT_EXIST = -907000;
	static final int UNLINK_FAILED = -908000;
	static final int NO_PASSWORD_ENTERED = -909000;

	// 1,000,000 to 1,500,000 - Rule Engine errors
	static final int OBJPATH_EMPTY_IN_STRUCT_ERR = -1000000;
	static final int RESCNAME_EMPTY_IN_STRUCT_ERR = -1001000;
	static final int DATATYPE_EMPTY_IN_STRUCT_ERR = -1002000;
	static final int DATASIZE_EMPTY_IN_STRUCT_ERR = -1003000;
	static final int CHKSUM_EMPTY_IN_STRUCT_ERR = -1004000;
	static final int VERSION_EMPTY_IN_STRUCT_ERR = -1005000;
	static final int FILEPATH_EMPTY_IN_STRUCT_ERR = -1006000;
	static final int REPLNUM_EMPTY_IN_STRUCT_ERR = -1007000;
	static final int REPLSTATUS_EMPTY_IN_STRUCT_ERR = -1008000;
	static final int DATAOWNER_EMPTY_IN_STRUCT_ERR = -1009000;
	static final int DATAOWNERZONE_EMPTY_IN_STRUCT_ERR = -1010000;
	static final int DATAEXPIRY_EMPTY_IN_STRUCT_ERR = -1011000;
	static final int DATACOMMENTS_EMPTY_IN_STRUCT_ERR = -1012000;
	static final int DATACREATE_EMPTY_IN_STRUCT_ERR = -1013000;
	static final int DATAMODIFY_EMPTY_IN_STRUCT_ERR = -1014000;
	static final int DATAACCESS_EMPTY_IN_STRUCT_ERR = -1015000;
	static final int DATAACCESSINX_EMPTY_IN_STRUCT_ERR = -1016000;
	static final int NO_RULE_FOUND_ERR = -1017000;
	static final int NO_MORE_RULES_ERR = -1018000;
	static final int UNMATCHED_ACTION_ERR = -1019000;
	static final int RULES_FILE_READ_ERROR = -1020000;
	static final int ACTION_ARG_COUNT_MISMATCH = -1021000;
	static final int MAX_NUM_OF_ARGS_IN_ACTION_EXCEEDED = -1022000;
	static final int UNKNOWN_PARAM_IN_RULE_ERR = -1023000;
	static final int DESTRESCNAME_EMPTY_IN_STRUCT_ERR = -1024000;
	static final int BACKUPRESCNAME_EMPTY_IN_STRUCT_ERR = -1025000;
	static final int DATAID_EMPTY_IN_STRUCT_ERR = -1026000;
	static final int COLLID_EMPTY_IN_STRUCT_ERR = -1027000;
	static final int RESCGROUPNAME_EMPTY_IN_STRUCT_ERR = -1028000;
	static final int STATUSSTRING_EMPTY_IN_STRUCT_ERR = -1029000;
	static final int DATAMAPID_EMPTY_IN_STRUCT_ERR = -1030000;
	static final int USERNAMECLIENT_EMPTY_IN_STRUCT_ERR = -1031000;
	static final int RODSZONECLIENT_EMPTY_IN_STRUCT_ERR = -1032000;
	static final int USERTYPECLIENT_EMPTY_IN_STRUCT_ERR = -1033000;
	static final int HOSTCLIENT_EMPTY_IN_STRUCT_ERR = -1034000;
	static final int AUTHSTRCLIENT_EMPTY_IN_STRUCT_ERR = -1035000;
	static final int USERAUTHSCHEMECLIENT_EMPTY_IN_STRUCT_ERR = -1036000;
	static final int USERINFOCLIENT_EMPTY_IN_STRUCT_ERR = -1037000;
	static final int USERCOMMENTCLIENT_EMPTY_IN_STRUCT_ERR = -1038000;
	static final int USERCREATECLIENT_EMPTY_IN_STRUCT_ERR = -1039000;
	static final int USERMODIFYCLIENT_EMPTY_IN_STRUCT_ERR = -1040000;
	static final int USERNAMEPROXY_EMPTY_IN_STRUCT_ERR = -1041000;
	static final int RODSZONEPROXY_EMPTY_IN_STRUCT_ERR = -1042000;
	static final int USERTYPEPROXY_EMPTY_IN_STRUCT_ERR = -1043000;
	static final int HOSTPROXY_EMPTY_IN_STRUCT_ERR = -1044000;
	static final int AUTHSTRPROXY_EMPTY_IN_STRUCT_ERR = -1045000;
	static final int USERAUTHSCHEMEPROXY_EMPTY_IN_STRUCT_ERR = -1046000;
	static final int USERINFOPROXY_EMPTY_IN_STRUCT_ERR = -1047000;
	static final int USERCOMMENTPROXY_EMPTY_IN_STRUCT_ERR = -1048000;
	static final int USERCREATEPROXY_EMPTY_IN_STRUCT_ERR = -1049000;
	static final int USERMODIFYPROXY_EMPTY_IN_STRUCT_ERR = -1050000;
	static final int COLLNAME_EMPTY_IN_STRUCT_ERR = -1051000;
	static final int COLLPARENTNAME_EMPTY_IN_STRUCT_ERR = -1052000;
	static final int COLLOWNERNAME_EMPTY_IN_STRUCT_ERR = -1053000;
	static final int COLLOWNERZONE_EMPTY_IN_STRUCT_ERR = -1054000;
	static final int COLLEXPIRY_EMPTY_IN_STRUCT_ERR = -1055000;
	static final int COLLCOMMENTS_EMPTY_IN_STRUCT_ERR = -1056000;
	static final int COLLCREATE_EMPTY_IN_STRUCT_ERR = -1057000;
	static final int COLLMODIFY_EMPTY_IN_STRUCT_ERR = -1058000;
	static final int COLLACCESS_EMPTY_IN_STRUCT_ERR = -1059000;
	static final int COLLACCESSINX_EMPTY_IN_STRUCT_ERR = -1060000;
	static final int COLLMAPID_EMPTY_IN_STRUCT_ERR = -1062000;
	static final int COLLINHERITANCE_EMPTY_IN_STRUCT_ERR = -1063000;
	static final int RESCZONE_EMPTY_IN_STRUCT_ERR = -1065000;
	static final int RESCLOC_EMPTY_IN_STRUCT_ERR = -1066000;
	static final int RESCTYPE_EMPTY_IN_STRUCT_ERR = -1067000;
	static final int RESCTYPEINX_EMPTY_IN_STRUCT_ERR = -1068000;
	static final int RESCCLASS_EMPTY_IN_STRUCT_ERR = -1069000;
	static final int RESCCLASSINX_EMPTY_IN_STRUCT_ERR = -1070000;
	static final int RESCVAULTPATH_EMPTY_IN_STRUCT_ERR = -1071000;
	static final int NUMOPEN_ORTS_EMPTY_IN_STRUCT_ERR = -1072000;
	static final int PARAOPR_EMPTY_IN_STRUCT_ERR = -1073000;
	static final int RESCID_EMPTY_IN_STRUCT_ERR = -1074000;
	static final int GATEWAYADDR_EMPTY_IN_STRUCT_ERR = -1075000;
	static final int RESCMAX_BJSIZE_EMPTY_IN_STRUCT_ERR = -1076000;
	static final int FREESPACE_EMPTY_IN_STRUCT_ERR = -1077000;
	static final int FREESPACETIME_EMPTY_IN_STRUCT_ERR = -1078000;
	static final int FREESPACETIMESTAMP_EMPTY_IN_STRUCT_ERR = -1079000;
	static final int RESCINFO_EMPTY_IN_STRUCT_ERR = -1080000;
	static final int RESCCOMMENTS_EMPTY_IN_STRUCT_ERR = -1081000;
	static final int RESCCREATE_EMPTY_IN_STRUCT_ERR = -1082000;
	static final int RESCMODIFY_EMPTY_IN_STRUCT_ERR = -1083000;
	static final int INPUT_ARG_NOT_WELL_FORMED_ERR = -1084000;
	static final int INPUT_ARG_OUT_OF_ARGC_RANGE_ERR = -1085000;
	static final int INSUFFICIENT_INPUT_ARG_ERR = -1086000;
	static final int INPUT_ARG_DOES_NOT_MATCH_ERR = -1087000;
	static final int RETRY_WITHOUT_RECOVERY_ERR = -1088000;
	static final int CUT_ACTION_PROCESSED_ERR = -1089000;
	static final int ACTION_FAILED_ERR = -1090000;
	static final int FAIL_ACTION_ENCOUNTERED_ERR = -1091000;
	static final int VARIABLE_NAME_TOO_LONG_ERR = -1092000;
	static final int UNKNOWN_VARIABLE_MAP_ERR = -1093000;
	static final int UNDEFINED_VARIABLE_MAP_ERR = -1094000;
	static final int NULL_VALUE_ERR = -1095000;
	static final int DVARMAP_FILE_READ_ERROR = -1096000;
	static final int NO_RULE_OR_MSI_FUNCTION_FOUND_ERR = -1097000;
	static final int FILE_CREATE_ERROR = -1098000;
	static final int FMAP_FILE_READ_ERROR = -1099000;
	static final int DATE_FORMAT_ERR = -1100000;
	static final int RULE_FAILED_ERR = -1101000;
	static final int NO_MICROSERVICE_FOUND_ERR = -1102000;
	static final int INVALID_REGEXP = -1103000;
	static final int INVALID_OBJECT_NAME = -1104000;
	static final int INVALID_OBJECT_TYPE = -1105000;
	static final int NO_VALUES_FOUND = -1106000;

	// The following are handler protocol type msg. These are not real error
	static final int SYS_NULL_INPUT = -99999996;
	static final int SYS_HANDLER_DONE_WITH_ERROR = -99999997;
	static final int SYS_HANDLER_DONE_NO_ERROR = -99999998;
	static final int SYS_NO_HANDLER_REPLY_MSG = -99999999;

	// RODS_ERROR_TABLE_H
}