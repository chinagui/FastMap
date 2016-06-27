package com.navinfo.dataservice.api.statics.model;

import java.io.Serializable;

public class BlockExpectStatInfo implements Serializable {

	private String date;
	
	private double finish;
	
	private int percent;
	
	private double expect;

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public double getFinish() {
		return finish;
	}

	public void setFinish(double finish) {
		this.finish = finish;
	}

	public int getPercent() {
		return percent;
	}

	public void setPercent(int percent) {
		this.percent = percent;
	}

	public double getExpect() {
		return expect;
	}

	public void setExpect(double expect) {
		this.expect = expect;
	}

}
