BEGIN
  DBMS_SCHEDULER.create_job (
    job_name        => 'INCREMENT_MOVE_DATA_JOB',
    job_type        => 'PLSQL_BLOCK',
    job_action      => 'BEGIN DATA_LOG_APP.INCREMENT_MOVE_DATA; END;',
    start_date      => SYSTIMESTAMP,
    repeat_interval => 'freq=daily; byhour=6; byminute=0; bysecond=0;',
    end_date        => NULL,
    enabled         => TRUE,
    comments        => 'Job defined entirely by the CREATE JOB procedure.');
END;
/
