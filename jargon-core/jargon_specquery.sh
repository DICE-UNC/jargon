#!/bin/sh

iadmin asq "select alias,sqlStr from R_SPECIFIC_QUERY where alias like ?" listQueryByAliasLike

iadmin asq "select alias,sqlStr from R_SPECIFIC_QUERY where alias = ?" findQueryByAlias

iadmin asq "SELECT c.parent_coll_name, c.coll_name, c.create_ts, c.modify_ts, c.coll_id, c.coll_owner_name, c.coll_owner_zone,c.coll_type, u.user_name, u.zone_name, a.access_type_id, u.user_id FROMr_coll_main c JOIN r_objt_access a ON c.coll_id = a.object_id JOIN r_user_main u ON a.user_id = u.user_id WHERE c.parent_coll_name = ? LIMIT ? OFFSET ?" ilsLACollections

iadmin asq "SELECT s.coll_name, s.data_name, s.create_ts, s.modify_ts, s.data_id, s.data_size, s.data_repl_num, s.data_owner_name, s.data_owner_zone, u.user_name, u.user_id, a.access_type_id,  u.user_type_name, u.zone_name FROM ( SELECT c.coll_name, d.data_name, d.create_ts, d.modify_ts, d.data_id, d.data_repl_num, d.data_size, d.data_owner_name, d.data_owner_zone FROM r_coll_main c JOIN r_data_main d ON c.coll_id = d.coll_id  WHERE c.coll_name = ?  ORDER BY d.data_name) s JOIN r_objt_access a ON s.data_id = a.object_id JOIN r_user_main u ON a.user_id = u.user_id LIMIT ? OFFSET ?" ilsLADataObjects

iadmin asq "select distinct R_USER_MAIN.user_name ,r_data_access.user_id ,r_data_access.access_type_id ,R_USER_MAIN.user_type_name,R_USER_MAIN.zone_name ,r_group_main.user_name  from  R_USER_MAIN , R_OBJT_ACCESS r_data_access , R_USER_MAIN r_group_main , R_DATA_MAIN , R_COLL_MAIN , R_USER_GROUP  where R_COLL_MAIN.coll_name  = ?  AND R_DATA_MAIN.data_name  = ?  AND R_USER_MAIN.user_name  = ? AND R_COLL_MAIN.coll_id = R_DATA_MAIN.coll_id  AND R_DATA_MAIN.data_id = r_data_access.object_id  AND R_USER_MAIN.user_id = r_data_access.user_id  AND R_USER_GROUP.group_user_id = r_group_main.user_id  AND R_USER_MAIN.user_id = R_USER_GROUP.user_id" listUserACLForDataObjViaGroup
