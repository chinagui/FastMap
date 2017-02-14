package com.navinfo.dataservice.api.man.model;

import java.sql.Timestamp;

public class Program {
	private int programId;
	private int cityId;
	private String inforId;
	private int latest;
	private int createUserId;
	private String name;
	private int type;
	private int status;
	private String descp;
	private Timestamp createDate;
	private Timestamp planStartDate;
	private Timestamp planEndDate;
	private Timestamp collectPlanStartDate;
	private Timestamp collectPlanEndDate;
	private Timestamp dayEditPlanStartDate;
	private Timestamp dayEditPlanEndDate;
	private Timestamp monthEditPlanStartDate;
	private Timestamp monthEditPlanEndDate;
	private Timestamp producePlanStartDate;
	private Timestamp producePlanEndDate;
	private int lot;
	public int getProgramId() {
		return programId;
	}
	public void setProgramId(int programId) {
		this.programId = programId;
	}
	public int getCityId() {
		return cityId;
	}
	public void setCityId(int cityId) {
		this.cityId = cityId;
	}
	public String getInforId() {
		return inforId;
	}
	public void setInforId(String inforId) {
		this.inforId = inforId;
	}
	public int getLatest() {
		return latest;
	}
	public void setLatest(int latest) {
		this.latest = latest;
	}
	public int getCreateUserId() {
		return createUserId;
	}
	public void setCreateUserId(int createUserId) {
		this.createUserId = createUserId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getDescp() {
		return descp;
	}
	public void setDescp(String descp) {
		this.descp = descp;
	}
	public Timestamp getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Timestamp createDate) {
		this.createDate = createDate;
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
	public Timestamp getDayEditPlanStartDate() {
		return dayEditPlanStartDate;
	}
	public void setDayEditPlanStartDate(Timestamp dayEditPlanStartDate) {
		this.dayEditPlanStartDate = dayEditPlanStartDate;
	}
	public Timestamp getDayEditPlanEndDate() {
		return dayEditPlanEndDate;
	}
	public void setDayEditPlanEndDate(Timestamp dayEditPlanEndDate) {
		this.dayEditPlanEndDate = dayEditPlanEndDate;
	}
	public Timestamp getMonthEditPlanStartDate() {
		return monthEditPlanStartDate;
	}
	public void setMonthEditPlanStartDate(Timestamp monthEditPlanStartDate) {
		this.monthEditPlanStartDate = monthEditPlanStartDate;
	}
	public Timestamp getMonthEditPlanEndDate() {
		return monthEditPlanEndDate;
	}
	public void setMonthEditPlanEndDate(Timestamp monthEditPlanEndDate) {
		this.monthEditPlanEndDate = monthEditPlanEndDate;
	}
	public Timestamp getProducePlanStartDate() {
		return producePlanStartDate;
	}
	public void setProducePlanStartDate(Timestamp producePlanStartDate) {
		this.producePlanStartDate = producePlanStartDate;
	}
	public Timestamp getProducePlanEndDate() {
		return producePlanEndDate;
	}
	public void setProducePlanEndDate(Timestamp producePlanEndDate) {
		this.producePlanEndDate = producePlanEndDate;
	}
	public int getLot() {
		return lot;
	}
	public void setLot(int lot) {
		this.lot = lot;
	}
}
