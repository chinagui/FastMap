package com.navinfo.dataservice.commons.fileConvert;

import java.util.Set;

public class Block {
	private String name ;
	private String city ;
	private String province ;
	private String county ;
	private String area ;
	private String number ;
	private String blockCode ;
	private String job1;
	private String job2;
	private String workProperty;
	
	
	private Set<String> gridIds;
	//private Object geometry ;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	
	public String getProvince() {
		return province;
	}
	public void setProvince(String province) {
		this.province = province;
	}
	

	public String getCounty() {
		return county;
	}
	public void setCounty(String county) {
		this.county = county;
	}
	public String getArea() {
		return area;
	}
	public void setArea(String area) {
		this.area = area;
	}
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}	
	public String getBlockCode() {
		return blockCode;
	}
	public void setBlockCode(String blockCode) {
		this.blockCode = blockCode;
	}
	public Set<String> getGridIds() {
		return gridIds;
	}
	public void setGridIds(Set<String> gridIds) {
		this.gridIds = gridIds;
	}
	
	public String getJob1() {
		return job1;
	}
	public void setJob1(String job1) {
		this.job1 = job1;
	}
	public String getJob2() {
		return job2;
	}
	public void setJob2(String job2) {
		this.job2 = job2;
	}
	public String getWorkProperty() {
		return workProperty;
	}
	public void setWorkProperty(String workProperty) {
		this.workProperty = workProperty;
	}
}
