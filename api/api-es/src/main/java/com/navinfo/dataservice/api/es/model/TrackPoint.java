package com.navinfo.dataservice.api.es.model;

import net.sf.json.JSONObject;

public class TrackPoint {
    private String id;
    private String a_recordTime;
    private JSONObject a_geometry;
    private int a_linkId;
    private int a_user;

    public TrackPoint(){

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public JSONObject getA_geometry() {
        return a_geometry;
    }

    public void setA_geometry(JSONObject a_geometry) {
        this.a_geometry = a_geometry;
    }

    public int getA_linkId() {
        return a_linkId;
    }

    public void setA_linkId(int a_linkId) {
        this.a_linkId = a_linkId;
    }

    public int getA_user() {
        return a_user;
    }

    public void setA_user(int a_user) {
        this.a_user = a_user;
    }

    public JSONObject toJSON(){
        JSONObject json = JSONObject.fromObject(this);
        json.remove("id");
        return json;
    }

    public String getA_recordTime() {
        return a_recordTime;
    }

    public void setA_recordTime(String a_recordTime) {
        this.a_recordTime = a_recordTime;
    }
}
