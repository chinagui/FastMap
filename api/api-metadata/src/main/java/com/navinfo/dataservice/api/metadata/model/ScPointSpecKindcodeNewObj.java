package com.navinfo.dataservice.api.metadata.model;

import java.io.Serializable;

public class ScPointSpecKindcodeNewObj implements Serializable{
	//POI_KIND, RATING, TOPCITY
	private String poiKind;
	private int rating;
	private int topcity;
	public int getTopcity() {
		return topcity;
	}
	public void setTopcity(int topcity) {
		this.topcity = topcity;
	}
	public int getRating() {
		return rating;
	}
	public void setRating(int rating) {
		this.rating = rating;
	}
	public String getPoiKind() {
		return poiKind;
	}
	public void setPoiKind(String poiKind) {
		this.poiKind = poiKind;
	}
}
