
-- Create/Recreate indexes
create index IDX_TMP_AIP_MT on TEMP_AU_IX_POI_MUL_TASK (PID);
-- Create/Recreate indexes
create index IDX_TMP_AIP_UT on TEMP_AU_IX_POI_UNIQ_TASK (PID);
-- Create/Recreate indexes
create index IDX_TMP_AP_MODLOG_ADID on TEMP_AU_POI_MODIFY_LOG (AUDATA_ID);
create index IDX_TMP_AP_MODLOG_PID on TEMP_AU_POI_MODIFY_LOG (PID);
-- Create/Recreate indexes
create index IDX_TMP_HIS_IX_POI on TEMP_HIS_IX_POI (PID);
-- Create/Recreate indexes
create index idx_au_ipn_pid on AU_IX_POI_NAME (poi_pid,AUDATA_ID);
create index IX_AIP_PS on AU_IX_POI (PID, STATE, ATT_OPRSTATUS);
-- Create/Recreate indexes
create index idx_ipa on IX_POI_ADDRESS (poi_pid);
create index IDX_IPN_PID on IX_POI_NAME (POI_PID);
-- Create/Recreate indexes
create index IDX_TMP_HIS_IX_ANNOTATION on TEMP_HIS_IX_ANNOTATION (PID);
create index IDX_TMP_HIS_IX_POINTADDRESS on TEMP_HIS_IX_POINTADDRESS (PID);
-- Create/Recreate indexes
create index IDX_TMP_PT_MODLOG_ADID on TEMP_AU_PT_POI_MODIFY_LOG (AUDATA_ID);
create index IDX_TMP_PT_MODLOG_PID on TEMP_AU_PT_POI_MODIFY_LOG (PID);
create index IDX_TEMP_HIS_PT_POI on TEMP_HIS_PT_POI (PID);
create index IDX_TEMP_PT_PLATFORM on TEMP_PT_PLATFORM (PID);

-- Create/Recreate indexes
create index IDX_TEMP_HIS_PT_PLATFORM on TEMP_HIS_PT_PLATFORM (PID);

-- Create/Recreate indexes
create index IDX_TMP_PLT_MODLOG_ADID on TEMP_AU_PT_PLATFORM_MODIFY_LOG (AUDATA_ID);
create index IDX_TMP_PLT_MODLOG_PID on TEMP_AU_PT_PLATFORM_MODIFY_LOG (PID);


-- Create/Recreate indexes
create index IDX_TMP_AIA_MT on TEMP_AU_IX_ANNO_MUL_TASK (PID);

-- Create/Recreate indexes
create index IDX_TMP_AIA_UT on temp_au_ix_anno_uniq_task (PID);

create index IDX_TMP_AIPA_MT on temp_au_ix_point_mul_task (PID);

create index IDX_TMP_aptcom_MT on temp_au_ptcom_mul_task (PID);

-- Create/Recreate indexes
create index IDX_TMP_ptline_MT on temp_au_ptline_mul_task (PID);

-- Create/Recreate indexes
create index IDX_TMP_ptplt_MT on temp_au_pt_plt_mul_task (PID);

-- Create/Recreate indexes
create index IDX_TMP_ptpoi_MT on temp_au_ptpoi_mul_task (PID);

-- Create/Recreate indexes
create index IDX_TMP_ANN_MODLOG_ADID on TEMP_AU_ANN_MODIFY_LOG (AUDATA_ID);
create index IDX_TMP_ANN_MODLOG_PID on TEMP_AU_ANN_MODIFY_LOG (PID);

create index IDX_TMP_HIS_IX_POI_E on TEMP_HIS_IX_POI_EXT (PID);

-- Create/Recreate indexes
create index IDX_TMP_AIP_GP on TEMP_AU_IX_POI_GRP (AUDATA_ID, PID);
create index IDX_TMP_AMD_IP on TEMP_AU_MUL_DEL_IX_POI (PID);


create index IDX_TEMP_HIS_IX_POI_NAME on TEMP_HIS_IX_POI_NAME (POI_PID, LANG_CODE,NAME_CLASS);

create index IDX_TEMP_HIS_IX_POI_PARENT on TEMP_HIS_IX_POI_PARENT (GROUP_ID);

create index IDX_TEMP_IX_POI_PARENT_MG on temp_ix_poi_parent_mg (GROUP_ID);

-- Create/Recreate indexes
create index IDX_temp_mul_ix_poi_mg on temp_mul_ix_poi_mg (AUDATA_ID, PID);


create index IDX_TEMP_IX_POI_ADDRESS_MG on TEMP_IX_POI_ADDRESS_MG (NAME_ID);

create index IDX_TEMP_RESTAURANT_MG on TEMP_IX_POI_RESTAURANT_MG (RESTAURANT_ID);


-- Create/Recreate indexes
create index IDX_TMP_AIA_GP on temp_au_ix_anot_grp (AUDATA_ID, PID);

create index IDX_AU_IX_ANOT on AU_IX_ANNOTATION (pid, geo_oprstatus, att_oprstatus, modify_flag);


-- Create/Recreate indexes
create index IDX_temp_his_pt_line on temp_his_pt_line (PID);

-- Create/Recreate indexes
create index IDX_temp_ptline_ext on temp_ptline_ext (PID);

-- Create/Recreate indexes
create index IDX_TEMP_PT_LINE_NAME on TEMP_PT_LINE_NAME (NAME_ID);

-- Create/Recreate indexes
create index IDX_temp_pt_line_name_mg on temp_pt_line_name_mg (NAME_ID);


-- Create/Recreate indexes
create index IDX_TMP_PTLINE_MODLOG_ADID on TEMP_AU_PT_LINE_MODIFY_LOG (AUDATA_ID);
create index IDX_TMP_PTLINE_MODLOG_PID on TEMP_AU_PT_LINE_MODIFY_LOG (PID);

-- Create/Recreate indexes
create index IDX_temp_pt_eta_line on temp_pt_eta_line (PID);

create index idx_temp_auetaline_mod_log on TEMP_AU_PT_ETA_LINE_MOD_LOG (pid, audata_id);

-- Create/Recreate indexes
create index IDX_TMP_PT_ETA_STOP on TEMP_PT_ETA_STOP (POI_PID);

-- Create/Recreate indexes
create index IDX_TMP_PTetastop_MODLOG_ADID on temp_au_pt_eta_stop_mod_log (AUDATA_ID,PID);


create index IDX_TMP_PT_ETA_ACCESS on TEMP_PT_ETA_ACCESS (POI_PID);

-- Create/Recreate indexes
create index IDX_TMP_ETAACCESS_MODLOG_ADID on temp_au_pt_eta_access_mod_log (AUDATA_ID,PID);


create index idx_poi_flag_code on ix_poi_flag (flag_code);

create index IDX_TMP_AMS_MT on TEMP_AU_MARK_SPEEDLMT_MULTASK (PID);
create index IDX_TMP_AMS_GP on TEMP_AU_MARK_SPEEDLIMIT_GRP (MARK_ID, PID);

create index IDX_TMP_AMS_LOG_MARKID on TEMP_AU_MARK_SPEEDLIMIT_LOG (MARK_ID);
create index IDX_TMP_AMS_LOG_PID on TEMP_AU_MARK_SPEEDLIMIT_LOG (PID);
create index IDX_TMP_HIS_RD_SPEEDLIMIT on TEMP_HIS_RD_SPEEDLIMIT (PID);
create index IDX_TMP_NEW_MARK_DIRECT on TEMP_NEW_MARK_DIRECT (MARK_ID);
