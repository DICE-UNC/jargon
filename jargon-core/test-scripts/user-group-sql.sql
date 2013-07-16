SELECT "public"."r_user_group"."group_user_id", "public"."r_user_group"."user_id",
  "public"."r_user_main"."user_id", "public"."r_user_main"."user_name"
  FROM
       "public"."r_user_group" JOIN "public"."r_user_main" ON "public"."r_user_group"."user_id" = "public"."r_user_main"."user_id" JOIN "public"."r_objt_access" JOIN "public"."r_data_main" ON "public"."r_objt_access"."object_id" = "public"."r_data_main"."data_id" ON "public"."r_user_main"."user_id" = "public"."r_objt_access"."user_id"
