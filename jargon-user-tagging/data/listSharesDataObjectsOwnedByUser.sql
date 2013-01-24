SELECT "r_data_main"."data_id", "r_data_main"."coll_id", "r_data_main"."data_name",
  "r_data_main"."data_owner_name", "r_data_main"."data_owner_zone",
  "r_meta_main"."meta_id", "r_meta_main"."meta_attr_name", "r_meta_main"."meta_attr_value",
  "r_meta_main"."meta_attr_unit"
  FROM "r_data_main", "r_meta_main"
  WHERE "r_data_main"."data_id" = "r_meta_main"."meta_id"
