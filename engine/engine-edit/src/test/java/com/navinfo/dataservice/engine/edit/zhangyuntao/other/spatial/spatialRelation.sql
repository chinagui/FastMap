select *
  from rd_link t
 where t.u_record != 2
   and sdo_relate(t.geometry,
                  sdo_geometry('POLYGON((116.46726 40.08326,116.46621 40.0831,116.46644 40.08244,116.46759 40.08236,116.46726 40.08326))',
                               8307),
                  'mask=inside') = 'TRUE';

select *
  from lu_face t
 where t.u_record != 2
   and sdo_relate(t.geometry,
                  sdo_geometry('LINESTRING(116.46726 40.08326,116.46621 40.0831)',
                               8307),
                  'mask=covers') = 'TRUE';

select *
  from lu_face t
 where t.u_record != 2
   and sdo_relate(t.geometry,
                  sdo_geometry('LINESTRING(116.46699 40.08309,116.46714 40.08249)',
                               8307),
                  'mask=contains') = 'TRUE';

select *
  from lu_face t
 where t.u_record != 2
   and sdo_relate(t.geometry,
                  sdo_geometry('LINESTRING(116.46699 40.08309,116.46714 40.08249)',
                               8307),
                  'mask=contains+covers') = 'TRUE';
                  
ERROR:select *
  from lu_face t
 where t.u_record != 2
   and sdo_relate(t.geometry,
                  sdo_geometry('LINESTRING(116.46621 40.0831,116.46644 40.08244, 116.46666 40.08255)',
                               8307),
                  'mask=contains+covers') = 'TRUE';
                  
select *
  from lu_face t
 where t.u_record != 2
   and sdo_relate(t.geometry,
                  sdo_geometry('LINESTRING(116.46621 40.0831,116.46644 40.08244,116.46787 40.08266)',
                               8307),
                  'mask=contains+covers') = 'TRUE';