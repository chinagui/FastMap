-- Create table
create table TRANSLATE_LOG
(
  id                 VARCHAR2(32),
  file_name          VARCHAR2(500),
  user_id            NUMBER,
  download_path      VARCHAR2(500),
  job_id             NUMBER,
  download_file_name VARCHAR2(500),
  file_size          NUMBER
);