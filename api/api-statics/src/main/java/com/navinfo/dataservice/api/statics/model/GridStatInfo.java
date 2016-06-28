package com.navinfo.dataservice.api.statics.model;

import java.io.Serializable;

public class GridStatInfo implements Serializable {

	private String gridId;

	private double totalPoi;

	private double finishPoi;

	private int percentPoi;
	
	private double totalRoad;

	private double finishRoad;

	private int percentRoad;
	
	private String statDate;
	
	public String getStatDate() {
		return statDate;
	}

	public void setStatDate(String statDate) {
		this.statDate = statDate;
	}

	public String getGridId() {
		return gridId;
	}

	public void setGridId(String gridId) {
		this.gridId = gridId;
	}

	public double getTotalPoi() {
		return totalPoi;
	}

	public void setTotalPoi(double totalPoi) {
		this.totalPoi = totalPoi;
	}

	public double getFinishPoi() {
		return finishPoi;
	}

	public void setFinishPoi(double finishPoi) {
		this.finishPoi = finishPoi;
	}

	public int getPercentPoi() {
		return percentPoi;
	}

	public void setPercentPoi(int percentPoi) {
		this.percentPoi = percentPoi;
	}

	public double getTotalRoad() {
		return totalRoad;
	}

	public void setTotalRoad(double totalRoad) {
		this.totalRoad = totalRoad;
	}

	public double getFinishRoad() {
		return finishRoad;
	}

	public void setFinishRoad(double finishRoad) {
		this.finishRoad = finishRoad;
	}

	public int getPercentRoad() {
		return percentRoad;
	}

	public void setPercentRoad(int percentRoad) {
		this.percentRoad = percentRoad;
	}

}
