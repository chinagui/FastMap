create or replace view view_ix_poi as
  select poi.*, g.parent_poi_pid
  from ix_poi poi,
       (select p.parent_poi_pid, c.child_poi_pid
          from ix_poi_children c, ix_poi_parent p
         where c.group_id = p.group_id) g
 where poi.pid = g.child_poi_pid(+);




create or replace view view_ad_admin as
  select ad.*, g.region_id_up
  from ad_admin ad,
       (select p.region_id_down, c.region_id_up
          from ad_admin_group c, ad_admin_part p
         where c.group_id = p.group_id) g
 where ad.region_id = g.region_id_down(+);
