package com.navinfo.dataservice.monitor.agent.model;

import java.io.Serializable;

public class StatInfo implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String metric;
	private String endpoint;
	private long timestemp;
	private int step = 60;
	private double value;
	private String counterType = "GAUGE";
	private String tags;
	
	public String getMetric() {
		return metric;
	}
	public void setMetric(String metric) {
		this.metric = metric;
	}
	public String getEndpoint() {
		return endpoint;
	}
	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}
	public long getTimestemp() {
		return timestemp;
	}
	public void setTimestemp(long timestemp) {
		this.timestemp = timestemp;
	}
	public int getStep() {
		return step;
	}
	public void setStep(int step) {
		this.step = step;
	}
	public double getValue() {
		return value;
	}
	public void setValue(double value) {
		this.value = value;
	}
	public String getCounterType() {
		return counterType;
	}
	public void setCounterType(String counterType) {
		this.counterType = counterType;
	}
	public String getTags() {
		return tags;
	}
	public void setTags(String tags) {
		this.tags = tags;
	}

	
}
