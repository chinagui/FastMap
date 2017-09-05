package com.navinfo.dataservice.engine.fcc.tips.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangjunfang on 2017/5/19.
 */
public class TipsTrack {
    private int t_lifecycle = 0;
    private int t_command = 0;
    private String t_date = "";
    private List<TrackInfo> t_trackInfo = new ArrayList<>();
    private int t_tipStatus;
    private int t_dEditStatus;
    private int t_dEditMeth;
    private int t_mEditStatus;
    private int t_mEditMeth;
    private String t_dataDate = "";

    public int getT_lifecycle() {
        return t_lifecycle;
    }

    public void setT_lifecycle(int t_lifecycle) {
        this.t_lifecycle = t_lifecycle;
    }

    public int getT_command() {
        return t_command;
    }

    public void setT_command(int t_command) {
        this.t_command = t_command;
    }

    public String getT_date() {
        return t_date;
    }

    public void setT_date(String t_date) {
        this.t_date = t_date;
    }

    public List<TrackInfo> getT_trackInfo() {
        return t_trackInfo;
    }

    public void setT_trackInfo(List<TrackInfo> t_trackInfo) {
        this.t_trackInfo = t_trackInfo;
    }

    public int getT_tipStatus() {
        return t_tipStatus;
    }

    public void setT_tipStatus(int t_tipStatus) {
        this.t_tipStatus = t_tipStatus;
    }

    public int getT_dEditStatus() {
        return t_dEditStatus;
    }

    public void setT_dEditStatus(int t_dEditStatus) {
        this.t_dEditStatus = t_dEditStatus;
    }

    public int getT_dEditMeth() {
        return t_dEditMeth;
    }

    public void setT_dEditMeth(int t_dEditMeth) {
        this.t_dEditMeth = t_dEditMeth;
    }

    public int getT_mEditStatus() {
        return t_mEditStatus;
    }

    public void setT_mEditStatus(int t_mEditStatus) {
        this.t_mEditStatus = t_mEditStatus;
    }

    public int getT_mEditMeth() {
        return t_mEditMeth;
    }

    public void setT_mEditMeth(int t_mEditMeth) {
        this.t_mEditMeth = t_mEditMeth;
    }
    
    

    /**
	 * @return the t_dataDate
	 */
	public String getT_dataDate() {
		return t_dataDate;
	}

	/**
	 * @param t_dataDate the t_dataDate to set
	 */
	public void setT_dataDate(String t_dataDate) {
		this.t_dataDate = t_dataDate;
	}

	public void addTrackInfo(int stage, String date, int handler) {
        TrackInfo trackInfo = new TrackInfo();
        trackInfo.setStage(stage);
        trackInfo.setDate(date);
        trackInfo.setHandler(handler);
        this.getT_trackInfo().add(trackInfo);
    }

    public static class TrackInfo {
        private int stage = 0;
        private String date = "";
        private int handler = 0;

        public int getStage() {
            return stage;
        }

        public void setStage(int stage) {
            this.stage = stage;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public int getHandler() {
            return handler;
        }

        public void setHandler(int handler) {
            this.handler = handler;
        }
    }
}
