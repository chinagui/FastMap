package com.navinfo.dataservice.api.statics.model;

import java.io.Serializable;

/** 
 * @ClassName: SubtaskStatInfo
 * @author songdongyan
 * @date 2016年9月14日
 * @Description: SubtaskStatInfo.java
 */
public class SubtaskStatInfo implements Serializable {

	/**
	 * 
	 */
	public SubtaskStatInfo() {
		// TODO Auto-generated constructor stub
	}
	
	private int subtaskId;
	private int percent;
	private int progress;
	private int diffDate;

	private double totalPoi;
	private double finishPoi;
	private double workingPoi;
	
	private double totalRoad;
	private double finishRoad;
	private double workingRoad;
	
	public SubtaskStatInfo(int subtaskId
			,int percent
			,double totalPoi
			,double finishPoi
			,double workingPoi
			,double totalRoad
			,double finishRoad
			,double workingRoad
			) {
		this.subtaskId = subtaskId;
		this.percent = percent;
		this.totalPoi = totalPoi;
		this.finishPoi = finishPoi;
		this.workingPoi = workingPoi;
		this.totalRoad = totalRoad;
		this.finishRoad = finishRoad;
		this.workingRoad = workingRoad;

	}
	
	/**
	 * @return the subtaskId
	 */
	public int getSubtaskId() {
		return subtaskId;
	}
	/**
	 * @param subtaskId the subtaskId to set
	 */
	public void setSubtaskId(int subtaskId) {
		this.subtaskId = subtaskId;
	}
	/**
	 * @return the percent
	 */
	public int getPercent() {
		return percent;
	}
	/**
	 * @param percent the percent to set
	 */
	public void setPercent(int percent) {
		this.percent = percent;
	}
	/**
	 * @return the totalPoi
	 */
	public double getTotalPoi() {
		return totalPoi;
	}
	/**
	 * @param totalPoi the totalPoi to set
	 */
	public void setTotalPoi(double totalPoi) {
		this.totalPoi = totalPoi;
	}
	/**
	 * @return the finishPoi
	 */
	public double getFinishPoi() {
		return finishPoi;
	}
	/**
	 * @param finishPoi the finishPoi to set
	 */
	public void setFinishPoi(double finishPoi) {
		this.finishPoi = finishPoi;
	}
	/**
	 * @return the workingPoi
	 */
	public double getWorkingPoi() {
		return workingPoi;
	}
	/**
	 * @param workingPoi the workingPoi to set
	 */
	public void setWorkingPoi(double workingPoi) {
		this.workingPoi = workingPoi;
	}
	/**
	 * @return the totalRoad
	 */
	public double getTotalRoad() {
		return totalRoad;
	}
	/**
	 * @param totalRoad the totalRoad to set
	 */
	public void setTotalRoad(double totalRoad) {
		this.totalRoad = totalRoad;
	}
	/**
	 * @return the finishRoad
	 */
	public double getFinishRoad() {
		return finishRoad;
	}
	/**
	 * @param finishRoad the finishRoad to set
	 */
	public void setFinishRoad(double finishRoad) {
		this.finishRoad = finishRoad;
	}
	/**
	 * @return the workingRoad
	 */
	public double getWorkingRoad() {
		return workingRoad;
	}
	/**
	 * @param workingRoad the workingRoad to set
	 */
	public void setWorkingRoad(double workingRoad) {
		this.workingRoad = workingRoad;
	}

	/**
	 * @return the progress
	 */
	public int getProgress() {
		return progress;
	}

	/**
	 * @param progress the progress to set
	 */
	public void setProgress(int progress) {
		this.progress = progress;
	}

	/**
	 * @return the diffDate
	 */
	public int getDiffDate() {
		return diffDate;
	}

	/**
	 * @param diffDate the diffDate to set
	 */
	public void setDiffDate(int diffDate) {
		this.diffDate = diffDate;
	}
	

}
