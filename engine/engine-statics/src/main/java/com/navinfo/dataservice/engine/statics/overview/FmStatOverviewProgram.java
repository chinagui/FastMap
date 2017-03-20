package com.navinfo.dataservice.engine.statics.overview;

import java.sql.Timestamp;

import com.navinfo.dataservice.engine.statics.tools.StatUtil;

public class FmStatOverviewProgram {
	private int programId      ;
	private int percent         ;
	private int diffDate       ;
	private int progress        ;
	private int status          ;
	private int type          ;
	private int collectProgress;
	private int collectPercent ;
	private int dailyProgress  ;
	private int dailyPercent   ;
	private Timestamp statDate;
	private int monthlyProgress;
	private int monthlyPercent ;
	private Timestamp planStartDate;
	private Timestamp planEndDate  ;
	private int planDate ;
	private Timestamp actualStartDate      ;
	private Timestamp actualEndDate        ;
	private Timestamp collectPlanStartDate;
	private Timestamp collectPlanEndDate  ;
	private int collectPlanDate ;
	private Timestamp collectActualStartDate ;
	private Timestamp collectActualEndDate   ;
	private int collectDiffDate ;
	private Timestamp dailyPlanStartDate     ;
	private Timestamp dailyPlanEndDate       ;
	private int dailyPlanDate  ;
	private Timestamp dailyActualStartDate  ;
	private Timestamp dailyActualEndDate    ;
	private int dailyDiffDate ;
	private Timestamp statTime  ;
	private int poiPlanTotal;
	private int roadPlanTotal ;
	private Timestamp monthlyPlanStartDate ;
	private Timestamp monthlyPlanEndDate   ;
	private int monthlyPlanDate ;
	private Timestamp monthlyActualStartDate;
	private Timestamp monthlyActualEndDate  ;
	private int monthlyDiffDate  ;
	
	public int getProgramId() {
		return programId;
	}
	public void setProgramId(int programId) {
		this.programId = programId;
	}
	public int getPercent() {
		return (this.collectPercent+this.dailyPercent+this.monthlyPercent)/3;
	}
	public int getDiffDate() {
		if(this.actualEndDate==null){
			return StatUtil.daysOfTwo(this.statDate,this.planEndDate);
		}else{
			return StatUtil.daysOfTwo(this.actualStartDate,this.actualEndDate);
		}
	}
	public int getProgress() {
		if(this.collectProgress==2||this.dailyProgress==2||this.monthlyProgress==2){
			return 2;
		}
		return 1;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public int getCollectProgress() {
		return collectProgress;
	}
	public void setCollectProgress(int collectProgress) {
		this.collectProgress = collectProgress;
	}
	public int getCollectPercent() {
		return collectPercent;
	}
	public void setCollectPercent(int collectPercent) {
		this.collectPercent = collectPercent;
	}
	public int getDailyProgress() {
		return dailyProgress;
	}
	public void setDailyProgress(int dailyProgress) {
		this.dailyProgress = dailyProgress;
	}
	public int getDailyPercent() {
		return dailyPercent;
	}
	public void setDailyPercent(int dailyPercent) {
		this.dailyPercent = dailyPercent;
	}
	public Timestamp getStatDate() {
		return new Timestamp(System.currentTimeMillis());
	}
	public int getMonthlyProgress() {
		return monthlyProgress;
	}
	public void setMonthlyProgress(int monthlyProgress) {
		this.monthlyProgress = monthlyProgress;
	}
	public int getMonthlyPercent() {
		return monthlyPercent;
	}
	public void setMonthlyPercent(int monthlyPercent) {
		this.monthlyPercent = monthlyPercent;
	}
	public Timestamp getPlanStartDate() {
		return planStartDate;
	}
	public void setPlanStartDate(Timestamp planStartDate) {
		this.planStartDate = planStartDate;
	}
	public Timestamp getPlanEndDate() {
		return planEndDate;
	}
	public void setPlanEndDate(Timestamp planEndDate) {
		this.planEndDate = planEndDate;
	}
	public int getPlanDate() {
		return StatUtil.daysOfTwo(this.planStartDate,this.planEndDate);
	}
	public Timestamp getActualStartDate() {
		return this.planStartDate;
	}
	public Timestamp getActualEndDate() {
		return actualEndDate;
	}
	public void setActualEndDate(Timestamp actualEndDate) {
		this.actualEndDate = actualEndDate;
	}
	public Timestamp getCollectPlanStartDate() {
		return collectPlanStartDate;
	}
	public void setCollectPlanStartDate(Timestamp collectPlanStartDate) {
		this.collectPlanStartDate = collectPlanStartDate;
	}
	public Timestamp getCollectPlanEndDate() {
		return collectPlanEndDate;
	}
	public void setCollectPlanEndDate(Timestamp collectPlanEndDate) {
		this.collectPlanEndDate = collectPlanEndDate;
	}
	public int getCollectPlanDate() {
		return StatUtil.daysOfTwo(this.collectPlanStartDate,this.collectPlanEndDate);
	}
	public Timestamp getCollectActualStartDate() {
		return this.collectPlanStartDate;
	}
	public Timestamp getCollectActualEndDate() {
		return collectActualEndDate;
	}
	public void setCollectActualEndDate(Timestamp collectActualEndDate) {
		this.collectActualEndDate = collectActualEndDate;
	}
	public int getCollectDiffDate() {
		if(this.collectActualEndDate==null){
			return StatUtil.daysOfTwo(this.statDate,this.collectPlanEndDate);
		}else{
			return StatUtil.daysOfTwo(this.collectActualStartDate,this.collectActualEndDate);
		}
	}
	public Timestamp getDailyPlanStartDate() {
		return dailyPlanStartDate;
	}
	public void setDailyPlanStartDate(Timestamp dailyPlanStartDate) {
		this.dailyPlanStartDate = dailyPlanStartDate;
	}
	public Timestamp getDailyPlanEndDate() {
		return dailyPlanEndDate;
	}
	public void setDailyPlanEndDate(Timestamp dailyPlanEndDate) {
		this.dailyPlanEndDate = dailyPlanEndDate;
	}
	public int getDailyPlanDate() {
		return StatUtil.daysOfTwo(this.dailyPlanStartDate,this.dailyPlanEndDate);
	}
	public Timestamp getDailyActualStartDate() {
		return this.dailyPlanStartDate;
	}
	public Timestamp getDailyActualEndDate() {
		return dailyActualEndDate;
	}
	public void setDailyActualEndDate(Timestamp dailyActualEndDate) {
		this.dailyActualEndDate = dailyActualEndDate;
	}
	public int getDailyDiffDate() {
		if(this.dailyActualEndDate==null){
			return StatUtil.daysOfTwo(this.statDate,this.dailyPlanEndDate);
		}else{
			return StatUtil.daysOfTwo(this.dailyActualStartDate,this.dailyActualEndDate);
		}
	}
	public Timestamp getStatTime() {
		return new Timestamp(System.currentTimeMillis());
	}
	public int getPoiPlanTotal() {
		return poiPlanTotal;
	}
	public void setPoiPlanTotal(int poiPlanTotal) {
		this.poiPlanTotal = poiPlanTotal;
	}
	public int getRoadPlanTotal() {
		return roadPlanTotal;
	}
	public void setRoadPlanTotal(int roadPlanTotal) {
		this.roadPlanTotal = roadPlanTotal;
	}
	public Timestamp getMonthlyPlanStartDate() {
		return monthlyPlanStartDate;
	}
	public void setMonthlyPlanStartDate(Timestamp monthlyPlanStartDate) {
		this.monthlyPlanStartDate = monthlyPlanStartDate;
	}
	public Timestamp getMonthlyPlanEndDate() {
		return monthlyPlanEndDate;
	}
	public void setMonthlyPlanEndDate(Timestamp monthlyPlanEndDate) {
		this.monthlyPlanEndDate = monthlyPlanEndDate;
	}
	public int getMonthlyPlanDate() {
		return StatUtil.daysOfTwo(this.monthlyPlanStartDate,this.monthlyPlanEndDate);
	}
	public Timestamp getMonthlyActualStartDate() {
		return this.monthlyPlanStartDate;
	}
	public Timestamp getMonthlyActualEndDate() {
		return monthlyActualEndDate;
	}
	public void setMonthlyActualEndDate(Timestamp monthlyActualEndDate) {
		this.monthlyActualEndDate = monthlyActualEndDate;
	}
	public int getMonthlyDiffDate() {
		if(this.monthlyActualEndDate==null){
			return StatUtil.daysOfTwo(this.statDate,this.monthlyPlanEndDate);
		}else{
			return StatUtil.daysOfTwo(this.monthlyActualStartDate,this.monthlyActualEndDate);
		}
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
}
