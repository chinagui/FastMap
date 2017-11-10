comment on COLUMN subtask.quality_method is
'质检方式，1 现场2室内';
update subtask set quality_method=1 where quality_method=3;

commit; 
exit;