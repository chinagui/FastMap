package com.navinfo.dataservice.control.model;


/** 
* @ClassName:  IxPoiHotel 
* @author zl
* @date 2017-06-20
* @Description: TODO
*/
public class UploadIxPoiHotel{
	private int rating ;
	private String creditCards ;
	private String description ;
	private String checkInTime ;
	private String checkOutTime ;
	private int roomCount;
	private String roomType ;
	private String roomPrice;
	private int breakfast ;
	private String service;
	private int parking ;
	private String openHour ;
	private String rowId ;
	
	
	public int getRoomCount() {
		return roomCount;
	}
	public void setRoomCount(int roomCount) {
		this.roomCount = roomCount;
	}
	public int getRating() {
		return rating;
	}
	public void setRating(int rating) {
		this.rating = rating;
	}
	public String getCreditCards() {
		return creditCards;
	}
	public void setCreditCards(String creditCards) {
		this.creditCards = creditCards;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getCheckInTime() {
		return checkInTime;
	}
	public void setCheckInTime(String checkInTime) {
		this.checkInTime = checkInTime;
	}
	public String getCheckOutTime() {
		return checkOutTime;
	}
	public void setCheckOutTime(String checkOutTime) {
		this.checkOutTime = checkOutTime;
	}
	public String getRoomType() {
		return roomType;
	}
	public void setRoomType(String roomType) {
		this.roomType = roomType;
	}
	public String getRoomPrice() {
		return roomPrice;
	}
	public void setRoomPrice(String roomPrice) {
		this.roomPrice = roomPrice;
	}
	public int getBreakfast() {
		return breakfast;
	}
	public void setBreakfast(int breakfast) {
		this.breakfast = breakfast;
	}
	public String getService() {
		return service;
	}
	public void setService(String service) {
		this.service = service;
	}
	public int getParking() {
		return parking;
	}
	public void setParking(int parking) {
		this.parking = parking;
	}
	public String getOpenHour() {
		return openHour;
	}
	public void setOpenHour(String openHour) {
		this.openHour = openHour;
	}
	public String getRowId() {
		return rowId;
	}
	public void setRowId(String rowId) {
		this.rowId = rowId;
	}
	
	
}
