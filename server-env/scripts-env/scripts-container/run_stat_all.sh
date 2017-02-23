#!/bin/bash
#Program:
#    This run bat of sh from gdb to nds.haha!
#20160909 zdz
export LANG=en_US.UTF-8
pwdDir=$PWD

echo stats_collect_road,stats_edit_daily_road
commCD="cd ${pwdDir}/robot_init"
$commCD
sh ./stats_collect_road.sh
sh ./stats_edit_daily_road.sh

commCD="cd ${pwdDir}"
$commCD
sh ./run_stat.sh