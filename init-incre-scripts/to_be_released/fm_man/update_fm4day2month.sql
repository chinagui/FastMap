-- glm upgrade
ALTER TABLE FM_DAY2MONTH_SYNC RENAME COLUMN CITY_ID TO REGION_ID;

COMMIT;
EXIT;