/**
 * 
 */
package org.irods.jargon.core.utils;

import org.irods.jargon.core.packinstr.Tag;

/**
 * Constants used for IRODS/Jargon
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public final class IRODSConstants {

	/**
	 * Approximate maximum number of bytes transfered by each thread during a
	 * parallel transfer.
	 */
	public static final int TRANSFER_THREAD_SIZE = 6000000;

	public static final int SYS_CLI_TO_SVR_COLL_STAT_REPLY = 99999997;
	public static final int SYS_CLI_TO_SVR_COLL_STAT_SIZE = 10;

	/**
	 * Maximum threads to open for a parallel transfer. More than this usually
	 * won't help, might even be slower.
	 */
	public static final int MAX_THREAD_NUMBER = 16;

	/**
	 * 16 bit char
	 */
	public static final int CHAR_LENGTH = 2;

	/**
	 * 16 bit short
	 */
	public static final int SHORT_LENGTH = 2;

	/**
	 * 32 bit integer
	 */
	public static final int INT_LENGTH = 4;

	/**
	 * 64 bit long
	 */
	public static final int LONG_LENGTH = 8;

	/**
	 * Maximum password length. Used in challenge response.
	 */
	public static final int MAX_PASSWORD_LENGTH = 50;
	/**
	 * Standard challenge length. Used in challenge response.
	 */
	public static final int CHALLENGE_LENGTH = 64;

	/**
	 * Max number of SQL attributes a query can return?
	 */
	public static final int MAX_SQL_ATTR = 50;

	// Various iRODS message types, in include/rodsDef.h
	public static final String RODS_CONNECT = "RODS_CONNECT";
	public static final String RODS_VERSION = "RODS_VERSION";
	public static final String RODS_API_REQ = "RODS_API_REQ";
	public static final String RODS_DISCONNECT = "RODS_DISCONNECT";
	public static final String RODS_REAUTH = "RODS_REAUTH";
	public static final String RODS_API_REPLY = "RODS_API_REPLY";

	// Various iRODS message types, in include/api/dataObjInpOut.h
	// definition for oprType in dataObjInp_t, portalOpr_t and l1desc_t
	public static final int DONE_OPR = 9999;
	public static final int PUT_OPR = 1;
	public static final int GET_OPR = 2;
	public static final int SAME_HOST_COPY_OPR = 3;
	public static final int COPY_TO_LOCAL_OPR = 4;
	public static final int COPY_TO_REM_OPR = 5;
	public static final int REPLICATE_OPR = 6;
	public static final int REPLICATE_DEST = 7;
	public static final int REPLICATE_SRC = 8;
	public static final int COPY_DEST = 9;
	public static final int COPY_SRC = 10;
	public static final int RENAME_DATA_OBJ = 11;
	public static final int RENAME_COLL = 12;
	public static final int MOVE_OPR = 13;
	public static final int RSYNC_OPR = 14;
	public static final int PHYMV_OPR = 15;
	public static final int PHYMV_SRC = 16;
	public static final int PHYMV_DEST = 17;

	/* from apiNumber.h - header file for API number assignment */
	/* 500 - 599 - Internal File I/O API calls */
	public static final int FILE_CREATE_AN = 500;
	public static final int FILE_OPEN_AN = 501;
	public static final int FILE_WRITE_AN = 502;
	public static final int FILE_CLOSE_AN = 503;
	public static final int FILE_LSEEK_AN = 504;
	public static final int FILE_READ_AN = 505;
	public static final int FILE_UNLINK_AN = 506;
	public static final int FILE_MKDIR_AN = 507;
	public static final int FILE_CHMOD_AN = 508;
	public static final int FILE_RMDIR_AN = 509;
	public static final int FILE_STAT_AN = 510;
	public static final int FILE_FSTAT_AN = 511;
	public static final int FILE_FSYNC_AN = 512;
	public static final int FILE_STAGE_AN = 513;
	public static final int FILE_GET_FS_FREE_SPACE_AN = 514;
	public static final int FILE_OPENDIR_AN = 515;
	public static final int FILE_CLOSEDIR_AN = 516;
	public static final int FILE_READDIR_AN = 517;
	public static final int FILE_PUT_AN = 518;
	public static final int FILE_CHKSUM_AN = 520;
	public static final int CHK_N_V_PATH_PERM_AN = 521;
	public static final int FILE_RENAME_AN = 522;
	public static final int FILE_TRUNCATE_AN = 523;
	public static final int FILE_STAGE_TO_CACHE_AN = 524;
	public static final int FILE_SYNC_TO_ARCH_AN = 525;

	/* 600 - 699 - Object File I/O API calls */
	public static final int DATA_OBJ_CREATE_AN = 601;
	public static final int DATA_OBJ_OPEN_AN = 602;
	public static final int DATA_OBJ_READ_AN = 603;
	public static final int DATA_OBJ_WRITE_AN = 604;
	public static final int DATA_OBJ_CLOSE_AN = 605;
	public static final int DATA_OBJ_PUT_AN = 606;
	public static final int DATA_PUT_AN = 607;
	public static final int DATA_OBJ_GET_AN = 608;
	public static final int DATA_GET_AN = 609;
	public static final int DATA_OBJ_REPL_AN = 610;
	public static final int DATA_COPY_AN = 611;
	public static final int DATA_OBJ_LSEEK_AN = 612;
	public static final int DATA_OBJ_COPY_AN = 613;
	public static final int SIMPLE_QUERY_AN = 614;
	public static final int DATA_OBJ_UNLINK_AN = 615;
	public static final int COLL_CREATE_AN = 616;
	public static final int REG_DATA_OBJ_AN = 619;
	public static final int UNREG_DATA_OBJ_AN = 620;
	public static final int REG_REPLICA_AN = 621;
	public static final int MOD_DATA_OBJ_META_AN = 622;
	public static final int RULE_EXEC_SUBMIT_AN = 623;
	public static final int RULE_EXEC_DEL_AN = 624;
	public static final int EXEC_MY_RULE_AN = 625;
	public static final int OPR_COMPLETE_AN = 626;
	public static final int DATA_OBJ_RENAME_AN = 627;
	public static final int DATA_OBJ_RSYNC_AN = 628;
	public static final int DATA_OBJ_CHKSUM_AN = 629;
	public static final int PHY_PATH_REG_AN = 630;
	public static final int DATA_OBJ_PHYMV_AN = 631;
	public static final int DATA_OBJ_TRIM_AN = 632;
	public static final int OBJ_STAT_AN = 633;
	public static final int EXEC_CMD_AN = 634;
	public static final int SUB_STRUCT_FILE_CREATE_AN = 635;
	public static final int SUB_STRUCT_FILE_OPEN_AN = 636;
	public static final int SUB_STRUCT_FILE_READ_AN = 637;
	public static final int SUB_STRUCT_FILE_WRITE_AN = 638;
	public static final int SUB_STRUCT_FILE_CLOSE_AN = 639;
	public static final int SUB_STRUCT_FILE_UNLINK_AN = 640;
	public static final int SUB_STRUCT_FILE_STAT_AN = 641;
	public static final int SUB_STRUCT_FILE_FSTAT_AN = 642;
	public static final int SUB_STRUCT_FILE_LSEEK_AN = 643;
	public static final int SUB_STRUCT_FILE_RENAME_AN = 644;
	public static final int QUERY_SPEC_COLL_AN = 645;
	public static final int SUB_STRUCT_FILE_MKDIR_AN = 647;
	public static final int SUB_STRUCT_FILE_RMDIR_AN = 648;
	public static final int SUB_STRUCT_FILE_OPENDIR_AN = 649;
	public static final int SUB_STRUCT_FILE_READDIR_AN = 650;
	public static final int SUB_STRUCT_FILE_CLOSEDIR_AN = 651;
	public static final int DATA_OBJ_TRUNCATE_AN = 652;
	public static final int SUB_STRUCT_FILE_TRUNCATE_AN = 653;
	public static final int GET_XMSG_TICKET_AN = 654;
	public static final int SEND_XMSG_AN = 655;
	public static final int RCV_XMSG_AN = 656;
	public static final int SUB_STRUCT_FILE_GET_AN = 657;
	public static final int SUB_STRUCT_FILE_PUT_AN = 658;
	public static final int SYNC_MOUNTED_COLL_AN = 659;
	public static final int STRUCT_FILE_SYNC_AN = 660;
	public static final int CLOSE_COLLECTION_AN = 661;
	public static final int RM_COLL_AN = 663;
	public static final int STRUCT_FILE_EXTRACT_AN = 664;
	public static final int STRUCT_FILE_EXT_AND_REG_AN = 665;
	public static final int STRUCT_FILE_BUNDLE_AN = 666;
	public static final int CHK_OBJ_PERM_AND_STAT_AN = 667;
	public static final int GET_REMOTE_ZONE_RESC_AN = 668;
	public static final int DATA_OBJ_OPEN_AND_STAT_AN = 669;
	public static final int L3_FILE_GET_SINGLE_BUF_AN = 670;
	public static final int L3_FILE_PUT_SINGLE_BUF_AN = 671;
	public static final int DATA_OBJ_CREATE_AND_STAT_AN = 672;

	public static final int COLL_REPL_AN = 677;
	public static final int OPEN_COLLECTION_AN = 678;
	public static final int MOD_COLL_AN = 680;
	public static final int RM_COLL_OLD_AN = 682;
	public static final int REG_COLL_AN = 683;

	/* 700 - 799 - Metadata API calls */
	public static final int GET_MISC_SVR_INFO_AN = 700;
	public static final int GENERAL_ADMIN_AN = 701;
	public static final int GEN_QUERY_AN = 702;
	public static final int AUTH_REQUEST_AN = 703;
	public static final int AUTH_RESPONSE_AN = 704;
	public static final int AUTH_CHECK_AN = 705;
	public static final int MOD_AVU_METADATA_AN = 706;
	public static final int MOD_ACCESS_CONTROL_AN = 707;
	public static final int RULE_EXEC_MOD_AN = 708;
	public static final int GET_TEMP_PASSWORD_AN = 709;
	public static final int GENERAL_UPDATE_AN = 710;
	public static final int GSI_AUTH_REQUEST_AN = 711;
	public static final int READ_COLLECTION_AN = 713;
	public static final int USER_ADMIN_AN = 714;
	public static final int GENERAL_ROW_INSERT_AN = 715;
	public static final int GENERAL_ROW_PURGE_AN = 716;
	public static final int KRB_AUTH_REQUEST_AN = 717;

	// iRODS communication types
	public static final char OPEN_START_TAG = Tag.OPEN_START_TAG;
	public static final char CLOSE_START_TAG = Tag.CLOSE_START_TAG;
	public static final String OPEN_END_TAG = Tag.OPEN_END_TAG;
	public static final char CLOSE_END_TAG = Tag.CLOSE_END_TAG;

	// typical header tags
	public static final String type = "type";
	public static final String msgLen = "msgLen";
	public static final String errorLen = "errorLen";
	public static final String bsLen = "bsLen";
	public static final String intInfo = "intInfo";

	// leaf tags
	public static final String irodsProt = "irodsProt";
	public static final String reconnFlag = "reconnFlag";
	public static final String connectCnt = "connectCnt";
	public static final String proxyUser = "proxyUser";
	public static final String proxyRcatZone = "proxyRcatZone";
	public static final String clientUser = "clientUser";
	public static final String clientRcatZone = "clientRcatZone";
	public static final String relVersion = "relVersion";
	public static final String apiVersion = "apiVersion";
	public static final String option = "option";
	public static final String status = "status";
	public static final String challenge = "challenge";
	public static final String response = "response";
	public static final String username = "username";
	public static final String objPath = "objPath";
	public static final String createMode = "createMode";
	public static final String openFlags = "openFlags";
	public static final String offset = "offset";
	public static final String dataSize = "dataSize";
	public static final String numThreads = "numThreads";
	public static final String oprType = "oprType";
	public static final String ssLen = "ssLen";
	public static final String objSize = "objSize";
	public static final String objType = "objType";
	public static final String numCopies = "numCopies";
	public static final String dataId = "dataId";
	public static final String chksum = "chksum";
	public static final String ownerName = "ownerName";
	public static final String ownerZone = "ownerZone";
	public static final String createTime = "createTime";
	public static final String modifyTime = "modifyTime";
	public static final String inx = "inx";
	public static final String maxRows = "maxRows";
	public static final String continueInx = "continueInx";
	public static final String partialStartIndex = "partialStartIndex";
	public static final String ivalue = "ivalue";
	public static final String svalue = "svalue";
	public static final String iiLen = "iiLen";
	public static final String isLen = "isLen";
	public static final String keyWord = "keyWord";
	public static final String rowCnt = "rowCnt";
	public static final String attriCnt = "attriCnt";
	public static final String attriInx = "attriInx";
	public static final String reslen = "reslen";
	public static final String queryValue = "value";
	public static final String collName = "collName";
	public static final String recursiveFlag = "recursiveFlag";
	public static final String accessLevel = "accessLevel";
	public static final String userName = "userName";
	public static final String zone = "zone";
	public static final String path = "path";
	public static final String L1_DESC_INX = "l1descInx";
	public static final String len = "len";
	public static final String fileInx = "fileInx";
	public static final String whence = "whence";
	public static final String dataObjInx = "dataObjInx";
	public static final String bytesWritten = "bytesWritten";
	public static final String msg = "msg";
	public static final String myRule = "myRule";
	public static final String outParamDesc = "outParamDesc";
	public static final String hostAddr = "hostAddr";
	public static final String rodsZone = "rodsZone";
	public static final String port = "port";
	public static final String ServerDN = "ServerDN";
	public static final String flags = "flags";
	public static final String collection = "collection";
	public static final String cmd = "cmd";
	public static final String cmdArgv = "cmdArgv";
	public static final String execAddr = "execAddr";
	public static final String hintPath = "hintPath";
	public static final String addPathToArgv = "addPathToArgv";
	public static final String options = "options";
	public static final String portNum = "portNum";
	public static final String cookie = "cookie";
	public static final String buflen = "buflen";
	public static final String buf = "buf";

	// Complex tags
	public static final String CollOprStat_PI = "CollOprStat_PI";
	public static final String MsgHeader_PI = "MsgHeader_PI";
	public static final String StartupPack_PI = "StartupPack_PI";
	public static final String Version_PI = "Version_PI";

	public static final String authRequestOut_PI = "authRequestOut_PI";
	public static final String authResponseInp_PI = "authResponseInp_PI";

	public static final String gsiAuthRequestOut_PI = "gsiAuthRequestOut_PI";

	public static final String DataObjInp_PI = "DataObjInp_PI";
	public static final String GenQueryInp_PI = "GenQueryInp_PI";
	public static final String ModAVUMetadataInp_PI = "ModAVUMetadataInp_PI";
	public static final String InxIvalPair_PI = "InxIvalPair_PI";
	public static final String InxValPair_PI = "InxValPair_PI";
	public static final String KeyValPair_PI = "KeyValPair_PI";
	public static final String RodsObjStat_PI = "RodsObjStat_PI";
	public static final String SqlResult_PI = "SqlResult_PI";
	public static final String DataObjCopyInp_PI = "DataObjCopyInp_PI";
	public static final String ExecCmd_PI = "ExecCmd_PI";
	public static final String PortList_PI = "PortList_PI";
	public static final String StructFileExtAndRegInp_PI = "StructFileExtAndRegInp_PI";

	public static final String CollInp_PI = "CollInp_PI";
	// new function after iRODS201
	public static final String CollInpNew_PI = "CollInpNew_PI";

	public static final String modAccessControlInp_PI = "modAccessControlInp_PI";
	public static final String dataObjReadInp_PI = "dataObjReadInp_PI";
	public static final String dataObjWriteInp_PI = "dataObjWriteInp_PI";
	public static final String fileLseekInp_PI = "fileLseekInp_PI";
	public static final String dataObjCloseInp_PI = "dataObjCloseInp_PI";
	public static final String BinBytesBuf_PI = "BinBytesBuf_PI";

	public static final String RErrMsg_PI = "RErrMsg_PI";

	// rules related tags
	public static final String ExecMyRuleInp_PI = "ExecMyRuleInp";
	public static final String RHostAddr_PI = "RHostAddr_PI";
	public static final String MsParamArray_PI = "MsParamArray_PI";
	public static final String MsParam_PI = "MsParam_PI";
	public static final String paramLen = "paramLen";
	public static final String label = "label";
	public static final String dummyInt = "dummyInt";

	// admin tags
	public static final String generalAdminInp_PI = "generalAdminInp_PI";
	public static final String simpleQueryInp_PI = "simpleQueryInp_PI";
	public static final String simpleQueryOut_PI = "simpleQueryOut_PI";
	public static final String sql = "sql";
	public static final String control = "control";
	public static final String form = "form";
	public static final String maxBufSize = "maxBufSize";
	public static final String outBuf = "outBuf";
	public static final String arg0 = "arg0";
	public static final String arg1 = "arg1";
	public static final String arg2 = "arg2";
	public static final String arg3 = "arg3";
	public static final String arg4 = "arg4";
	public static final String arg5 = "arg5";
	public static final String arg6 = "arg6";
	public static final String arg7 = "arg7";
	public static final String arg8 = "arg8";
	public static final String arg9 = "arg9";

	private IRODSConstants() {

	}

}
