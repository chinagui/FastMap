package com.navinfo.dataservice.engine.fcc.tips.model;

/**
 * Created by gaojian on 2016/8/22.
 */
public class TipsIndexModel {
    private String id;
    private String wkt;
    private int stage;
    private String t_operateDate;
    private String t_date;
    private int t_lifecycle;
    private int t_command;
    private int handler;
    private int s_sourceCode;
    private String s_sourceType;
    private String g_location;
    private String g_guide;
    private String deep;
    private String feedback;
    private int s_reliability;
    private String _version_;
    private String custom;
    private String tipdiff;
    private int s_qTaskId;
    private int s_mTaskId;
    private int s_qSubTaskId;
    private int s_mSubTaskId;
    private int t_tipStatus;
    private int t_dEditStatus;
    private int t_dEditMeth;
    private int t_mEditStatus;
    private int t_mEditMeth;
    private String wktLocation;
    private String relate_links = "";
    private String relate_nodes = "";
    private String s_project = "";


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getWkt() {
        return wkt;
    }

    public void setWkt(String wkt) {
        this.wkt = wkt;
    }

    public int getStage() {
        return stage;
    }

    public void setStage(int stage) {
        this.stage = stage;
    }

    public String getT_operateDate() {
        return t_operateDate;
    }

    public void setT_operateDate(String t_operateDate) {
        this.t_operateDate = t_operateDate;
    }

    public String getT_date() {
        return t_date;
    }

    public void setT_date(String t_date) {
        this.t_date = t_date;
    }

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

    public int getHandler() {
        return handler;
    }

    public void setHandler(int handler) {
        this.handler = handler;
    }

    public int getS_sourceCode() {
        return s_sourceCode;
    }

    public void setS_sourceCode(int s_sourceCode) {
        this.s_sourceCode = s_sourceCode;
    }

    public String getS_sourceType() {
        return s_sourceType;
    }

    public void setS_sourceType(String s_sourceType) {
        this.s_sourceType = s_sourceType;
    }

    public String getG_location() {
        return g_location;
    }

    public void setG_location(String g_location) {
        this.g_location = g_location;
    }

    public String getG_guide() {
        return g_guide;
    }

    public void setG_guide(String g_guide) {
        this.g_guide = g_guide;
    }

    public String getDeep() {
        return deep;
    }

    public void setDeep(String deep) {
        this.deep = deep;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public int getS_reliability() {
        return s_reliability;
    }

    public void setS_reliability(int s_reliability) {
        this.s_reliability = s_reliability;
    }

    public String get_version_() {
        return _version_;
    }

    public void set_version_(String _version_) {
        this._version_ = _version_;
    }

    public String getCustom() {
        return custom;
    }

    public void setCustom(String custom) {
        this.custom = custom;
    }

    public String getTipdiff() {
        return tipdiff;
    }

    public void setTipdiff(String tipdiff) {
        this.tipdiff = tipdiff;
    }

    public int getS_qTaskId() {
        return s_qTaskId;
    }

    public void setS_qTaskId(int s_qTaskId) {
        this.s_qTaskId = s_qTaskId;
    }

    public int getS_mTaskId() {
        return s_mTaskId;
    }

    public void setS_mTaskId(int s_mTaskId) {
        this.s_mTaskId = s_mTaskId;
    }

    public int getS_qSubTaskId() {
        return s_qSubTaskId;
    }

    public void setS_qSubTaskId(int s_qSubTaskId) {
        this.s_qSubTaskId = s_qSubTaskId;
    }

    public int getS_mSubTaskId() {
        return s_mSubTaskId;
    }

    public void setS_mSubTaskId(int s_mSubTaskId) {
        this.s_mSubTaskId = s_mSubTaskId;
    }

    public String getWktLocation() {
        return wktLocation;
    }

    public void setWktLocation(String wktLocation) {
        this.wktLocation = wktLocation;
    }

    public String getRelate_links() {
        return relate_links;
    }

    public void setRelate_links(String relate_links) {
        this.relate_links = relate_links;
    }

    public String getRelate_nodes() {
        return relate_nodes;
    }

    public void setRelate_nodes(String relate_nodes) {
        this.relate_nodes = relate_nodes;
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

    public String getS_project() {
        return s_project;
    }

    public void setS_project(String s_project) {
        this.s_project = s_project;
    }

    public void setSource(TipsSource source) {
        this.s_sourceCode = source.getS_sourceCode();
        this.s_sourceType = source.getS_sourceType();
        this.s_reliability = source.getS_reliability();
        this.s_qTaskId = source.getS_qTaskId();
        this.s_qSubTaskId = source.getS_qSubTaskId();
        this.s_mTaskId = source.getS_mTaskId();
        this.s_mSubTaskId = source.getS_mSubTaskId();
    }

    public void setTrack(TipsTrack track) {
        this.t_command = track.getT_command();
        this.t_dEditMeth = track.getT_dEditMeth();
        this.t_dEditStatus = track.getT_dEditStatus();
        this.t_lifecycle = track.getT_lifecycle();
        this.t_tipStatus = track.getT_tipStatus();
        this.t_mEditMeth = track.getT_mEditMeth();
        this.t_mEditStatus = track.getT_mEditStatus();
    }
}
