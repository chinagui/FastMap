package com.navinfo.dataservice.FosEngine.photos;

import java.util.List;

/**
 * 照片模型
 */
public class Photo {

	private String rowkey;

	private String a_uuid;

	private int a_uploadUser;

	private String a_uploadDate;

	private double a_latitude;

	private double a_longitude;

	private String a_title;

	private String a_subtitle;

	private int a_sourceId;

	private double a_direction;

	private String a_shootDate;

	private String a_deviceNum;

	private int a_type;

	private int a_content;

	private List<String> a_tag;

	public String getRowkey() {
		return rowkey;
	}

	public void setRowkey(String rowkey) {
		this.rowkey = rowkey;
	}

	public String getA_uuid() {
		return a_uuid;
	}

	public void setA_uuid(String a_uuid) {
		this.a_uuid = a_uuid;
	}

	public int getA_uploadUser() {
		return a_uploadUser;
	}

	public void setA_uploadUser(int a_uploadUser) {
		this.a_uploadUser = a_uploadUser;
	}

	public String getA_uploadDate() {
		return a_uploadDate;
	}

	public void setA_uploadDate(String a_uploadDate) {
		this.a_uploadDate = a_uploadDate;
	}

	public double getA_latitude() {
		return a_latitude;
	}

	public void setA_latitude(double a_latitude) {
		this.a_latitude = a_latitude;
	}

	public double getA_longitude() {
		return a_longitude;
	}

	public void setA_longitude(double a_longitude) {
		this.a_longitude = a_longitude;
	}

	public String getA_title() {
		return a_title;
	}

	public void setA_title(String a_title) {
		this.a_title = a_title;
	}

	public String getA_subtitle() {
		return a_subtitle;
	}

	public void setA_subtitle(String a_subtitle) {
		this.a_subtitle = a_subtitle;
	}

	public int getA_sourceId() {
		return a_sourceId;
	}

	public void setA_sourceId(int a_sourceId) {
		this.a_sourceId = a_sourceId;
	}

	public String getA_shootDate() {
		return a_shootDate;
	}

	public void setA_shootDate(String a_shootDate) {
		this.a_shootDate = a_shootDate;
	}

	public String getA_deviceNum() {
		return a_deviceNum;
	}

	public void setA_deviceNum(String a_deviceNum) {
		this.a_deviceNum = a_deviceNum;
	}

	public int getA_type() {
		return a_type;
	}

	public void setA_type(int a_type) {
		this.a_type = a_type;
	}

	public int getA_content() {
		return a_content;
	}

	public void setA_content(int a_content) {
		this.a_content = a_content;
	}

	public List<String> getA_tag() {
		return a_tag;
	}

	public void setA_tag(List<String> a_tag) {
		this.a_tag = a_tag;
	}

	public double getA_direction() {
		return a_direction;
	}

	public void setA_direction(double a_direction) {
		this.a_direction = a_direction;
	}

	public String convert2Brief() {
		return "{\"rowkey\":" + rowkey + ",\"a_longitude\":" + a_longitude
				+ ",\"a_latitude\":" + a_latitude + "}";
	}
}
