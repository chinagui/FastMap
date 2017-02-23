source ./*.conf
sqlplus $pid_super_username/$pid_super_password @./create_pid_user.sql $pid_username $pid_password
sqlplus $pid_username/$pid_password @./create_type_function.sql
sqlplus $pid_username/$pid_password @./global_id_man.sql
sqlplus $pid_username/$pid_password @./init_segments.sql $pid_start
sqlplus $fmsys_url @./insert_fm_sys_config.sql $pid_server_ip $pid_server_port $pid_server_sid $pid_username $pid_password