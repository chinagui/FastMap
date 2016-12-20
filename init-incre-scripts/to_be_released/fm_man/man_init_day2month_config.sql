delete from DAY2MONTH_CONFIG ;
insert into DAY2MONTH_CONFIG
  select DAY2MONTH_CONFIG_seq.Nextval, city_id, 'POI', 0, 0, NULL
    from citY
   where city_id < 100000;
COMMIT;
EXIT;