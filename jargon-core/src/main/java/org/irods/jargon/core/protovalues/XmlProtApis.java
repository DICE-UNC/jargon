package org.irods.jargon.core.protovalues;

public enum XmlProtApis {

	NO_API_NUMBER(0), FILE_CREATE_AN(500), FILE_OPEN_AN(501), FILE_WRITE_AN(502), FILE_CLOSE_AN(
			503), FILE_LSEEK_AN(504), FILE_READ_AN(505), FILE_UNLINK_AN(506), FILE_MKDIR_AN(
			507), FILE_CHMOD_AN(508), FILE_RMDIR_AN(509), FILE_STAT_AN(510), FILE_FSTAT_AN(
			511), FILE_FSYNC_AN(512), FILE_STAGE_AN(513), FILE_GET_FS_FREE_SPACE_AN(
			514), FILE_OPENDIR_AN(515), FILE_CLOSEDIR_AN(516), FILE_READDIR_AN(
			517), FILE_PUT_AN(518), FILE_GET_AN(519), FILE_CHKSUM_AN(520), CHK_N_V_PATH_PERM_AN(
			521), FILE_RENAME_AN(522), FILE_TRUNCATE_AN(523), DATA_OBJ_CREATE_AN(
			601), DATA_OBJ_OPEN_AN(602), DATA_OBJ_READ_AN(603), DATA_OBJ_WRITE_AN(
			604), DATA_OBJ_CLOSE_AN(605), DATA_OBJ_PUT_AN(606), DATA_PUT_AN(607), DATA_OBJ_GET_AN(
			608), DATA_GET_AN(609), DATA_OBJ_REPL_AN(610), DATA_COPY_AN(611), DATA_OBJ_LSEEK_AN(
			612), DATA_OBJ_COPY_AN(613), SIMPLE_QUERY_AN(614), DATA_OBJ_UNLINK_AN(
			615), COLL_CREATE_AN(616), RM_COLL_AN(617), REG_COLL_AN(618), REG_DATA_OBJ_AN(
			619), UNREG_DATA_OBJ_AN(620), REG_REPLICA_AN(621), MOD_DATA_OBJ_META_AN(
			622), RULE_EXEC_SUBMIT_AN(623), RULE_EXEC_DEL_AN(624), EXEC_MY_RULE_AN(
			625), OPR_COMPLETE_AN(626), DATA_OBJ_RENAME_AN(627), DATA_OBJ_RSYNC_AN(
			628), DATA_OBJ_CHKSUM_AN(629), PHY_PATH_REG_AN(630), DATA_OBJ_PHYMV_AN(
			631), DATA_OBJ_TRIM_AN(632), OBJ_STAT_AN(633), EXEC_CMD_AN(634), BUN_SUB_CREATE_AN(
			635), BUN_SUB_OPEN_AN(636), BUN_SUB_READ_AN(637), BUN_SUB_WRITE_AN(
			638), BUN_SUB_CLOSE_AN(639), BUN_SUB_UNLINK_AN(640), BUN_SUB_STAT_AN(
			641), BUN_SUB_FSTAT_AN(642), BUN_SUB_LSEEK_AN(643), BUN_SUB_RENAME_AN(
			644), QUERY_SPEC_COLL_AN(645), MOD_COLL_AN(646), BUN_SUB_MKDIR_AN(
			647), BUN_SUB_RMDIR_AN(648), BUN_SUB_OPENDIR_AN(649), BUN_SUB_READDIR_AN(
			650), BUN_SUB_CLOSEDIR_AN(651), DATA_OBJ_TRUNCATE_AN(652), BUN_SUB_TRUNCATE_AN(
			653), GET_MISC_SVR_INFO_AN(700), GENERAL_ADMIN_AN(701), GEN_QUERY_AN(
			702), AUTH_REQUEST_AN(703), GSI_AUTH_REQUEST_AN(711), AUTH_RESPONSE_AN(
			704), AUTH_CHECK_AN(705), MOD_AVU_METADATA_AN(706), MOD_ACCESS_CONTROL_AN(
			707), RULE_EXEC_MOD_AN(708), GET_TEMP_PASSWORD_AN(709), GENERAL_UPDATE_AN(
			710);

	private int apiNumber;

	XmlProtApis(final int apiNumber) {
		this.apiNumber = apiNumber;
	}

	public int getApiNumber() {
		return apiNumber;
	}

	public static XmlProtApis valueOf(final int i) {
		switch (i) {
		case 0:
			return NO_API_NUMBER;
		case 500:
			return FILE_CREATE_AN;
		case 501:
			return FILE_OPEN_AN;
		case 502:
			return FILE_WRITE_AN;
		case 503:
			return FILE_CLOSE_AN;
		case 504:
			return FILE_LSEEK_AN;
		case 505:
			return FILE_READ_AN;
		case 506:
			return FILE_UNLINK_AN;
		case 507:
			return FILE_MKDIR_AN;
		case 508:
			return FILE_CHMOD_AN;
		case 509:
			return FILE_RMDIR_AN;
		case 510:
			return FILE_STAT_AN;
		case 511:
			return FILE_FSTAT_AN;
		case 512:
			return FILE_FSYNC_AN;
		case 513:
			return FILE_STAGE_AN;
		case 514:
			return FILE_GET_FS_FREE_SPACE_AN;
		case 515:
			return FILE_OPENDIR_AN;
		case 516:
			return FILE_CLOSEDIR_AN;
		case 517:
			return FILE_READDIR_AN;
		case 518:
			return FILE_PUT_AN;
		case 519:
			return FILE_GET_AN;
		case 520:
			return FILE_CHKSUM_AN;
		case 521:
			return CHK_N_V_PATH_PERM_AN;
		case 522:
			return FILE_RENAME_AN;
		case 523:
			return FILE_TRUNCATE_AN;
		case 601:
			return DATA_OBJ_CREATE_AN;
		case 602:
			return DATA_OBJ_OPEN_AN;
		case 603:
			return DATA_OBJ_READ_AN;
		case 604:
			return DATA_OBJ_WRITE_AN;
		case 605:
			return DATA_OBJ_CLOSE_AN;
		case 606:
			return DATA_OBJ_PUT_AN;
		case 607:
			return DATA_PUT_AN;
		case 608:
			return DATA_OBJ_GET_AN;
		case 609:
			return DATA_GET_AN;
		case 610:
			return DATA_OBJ_REPL_AN;
		case 611:
			return DATA_COPY_AN;
		case 612:
			return DATA_OBJ_LSEEK_AN;
		case 613:
			return DATA_OBJ_COPY_AN;
		case 614:
			return SIMPLE_QUERY_AN;
		case 615:
			return DATA_OBJ_UNLINK_AN;
		case 616:
			return COLL_CREATE_AN;
		case 617:
			return RM_COLL_AN;
		case 618:
			return REG_COLL_AN;
		case 619:
			return REG_DATA_OBJ_AN;
		case 620:
			return UNREG_DATA_OBJ_AN;
		case 621:
			return REG_REPLICA_AN;
		case 622:
			return MOD_DATA_OBJ_META_AN;
		case 623:
			return RULE_EXEC_SUBMIT_AN;
		case 624:
			return RULE_EXEC_DEL_AN;
		case 625:
			return EXEC_MY_RULE_AN;
		case 626:
			return OPR_COMPLETE_AN;
		case 627:
			return DATA_OBJ_RENAME_AN;
		case 628:
			return DATA_OBJ_RSYNC_AN;
		case 629:
			return DATA_OBJ_CHKSUM_AN;
		case 630:
			return PHY_PATH_REG_AN;
		case 631:
			return DATA_OBJ_PHYMV_AN;
		case 632:
			return DATA_OBJ_TRIM_AN;
		case 633:
			return OBJ_STAT_AN;
		case 634:
			return EXEC_CMD_AN;
		case 635:
			return BUN_SUB_CREATE_AN;
		case 636:
			return BUN_SUB_OPEN_AN;
		case 637:
			return BUN_SUB_READ_AN;
		case 638:
			return BUN_SUB_WRITE_AN;
		case 639:
			return BUN_SUB_CLOSE_AN;
		case 640:
			return BUN_SUB_UNLINK_AN;
		case 641:
			return BUN_SUB_STAT_AN;
		case 642:
			return BUN_SUB_FSTAT_AN;
		case 643:
			return BUN_SUB_LSEEK_AN;
		case 644:
			return BUN_SUB_RENAME_AN;
		case 645:
			return QUERY_SPEC_COLL_AN;
		case 646:
			return MOD_COLL_AN;
		case 647:
			return BUN_SUB_MKDIR_AN;
		case 648:
			return BUN_SUB_RMDIR_AN;
		case 649:
			return BUN_SUB_OPENDIR_AN;
		case 650:
			return BUN_SUB_READDIR_AN;
		case 651:
			return BUN_SUB_CLOSEDIR_AN;
		case 652:
			return DATA_OBJ_TRUNCATE_AN;
		case 653:
			return BUN_SUB_TRUNCATE_AN;
		case 700:
			return GET_MISC_SVR_INFO_AN;
		case 701:
			return GENERAL_ADMIN_AN;
		case 702:
			return GEN_QUERY_AN;
		case 703:
			return AUTH_REQUEST_AN;
		case 704:
			return AUTH_RESPONSE_AN;
		case 705:
			return AUTH_CHECK_AN;
		case 706:
			return MOD_AVU_METADATA_AN;
		case 707:
			return MOD_ACCESS_CONTROL_AN;
		case 708:
			return RULE_EXEC_MOD_AN;
		case 709:
			return GET_TEMP_PASSWORD_AN;
		case 710:
			return GENERAL_UPDATE_AN;

		default:
			throw new IllegalArgumentException("unexpected int: " + i);
		}
	}
}
