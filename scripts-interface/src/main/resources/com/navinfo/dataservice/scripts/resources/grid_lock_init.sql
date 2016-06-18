insert /*+append */into grid_lock(grid_id,region_id,handle_region_id,lock_object,lock_status)
select grid_id,region_id,region_id as handle_region_id,1 as lock_object ,0 as lock_status from grid
union all 
select grid_id,region_id,region_id as handle_region_id,2 as lock_object ,0 as lock_status  from grid;