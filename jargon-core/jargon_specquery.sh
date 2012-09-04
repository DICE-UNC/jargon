#!/bin/sh
exec `iadmin asq "select distinct R_USER_MAIN.user_name ,R_USER_MAIN.zone_name, R_TOKN_MAIN.token_name from R_USER_MAIN , R_TOKN_MAIN, R_OBJT_ACCESS, R_COLL_MAIN where R_OBJT_ACCESS.object_id = R_COLL_MAIN.coll_id AND r_COLL_MAIN.coll_name = ? AND R_TOKN_MAIN.token_namespace = 'access_type' AND R_USER_MAIN.user_id = R_OBJT_ACCESS.user_id AND R_OBJT_ACCESS.access_type_id = R_TOKN_MAIN.token_id" ShowCollAcls`

exec `iadmin asq "select alias,sqlStr from R_SPECIFIC_QUERY where alias like ?" listQueryByAliasLike`

exec `iadmin asq "select alias,sqlStr from R_SPECIFIC_QUERY where alias = ?" findQueryByAlias`


