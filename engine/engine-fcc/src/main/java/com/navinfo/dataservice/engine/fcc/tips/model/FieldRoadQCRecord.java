package com.navinfo.dataservice.engine.fcc.tips.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by zhangjunfang on 2017/7/12.
 */
public class FieldRoadQCRecord {
    private String rowkey;
    private String link_pid;
    private String id;
    private String class_top;
    private String class_bottom;
    private String type;
    private String phenomenon;
    private String description;
    private String initial_cause;
    private String root_cause;
    private String check_userid;
    private String check_time;
    private String confirm_userid;
    private int t_lifecycle;
    private int t_status;

    public String getRowkey() {
        return rowkey;
    }

    public void setRowkey(String rowkey) {
        this.rowkey = rowkey;
    }

    public String getLink_pid() {
        return link_pid;
    }

    public void setLink_pid(String link_pid) {
        this.link_pid = link_pid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClass_top() {
        return class_top;
    }

    public void setClass_top(String class_top) {
        this.class_top = class_top;
    }

    public String getClass_bottom() {
        return class_bottom;
    }

    public void setClass_bottom(String class_bottom) {
        this.class_bottom = class_bottom;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPhenomenon() {
        return phenomenon;
    }

    public void setPhenomenon(String phenomenon) {
        this.phenomenon = phenomenon;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInitial_cause() {
        return initial_cause;
    }

    public void setInitial_cause(String initial_cause) {
        this.initial_cause = initial_cause;
    }

    public String getRoot_cause() {
        return root_cause;
    }

    public void setRoot_cause(String root_cause) {
        this.root_cause = root_cause;
    }

    public String getCheck_userid() {
        return check_userid;
    }

    public void setCheck_userid(String check_userid) {
        this.check_userid = check_userid;
    }

    public String getCheck_time() {
        return check_time;
    }

    public void setCheck_time(String check_time) {
        this.check_time = check_time;
    }

    public String getConfirm_userid() {
        return confirm_userid;
    }

    public void setConfirm_userid(String confirm_userid) {
        this.confirm_userid = confirm_userid;
    }

    public int getT_lifecycle() {
        return t_lifecycle;
    }

    public void setT_lifecycle(int t_lifecycle) {
        this.t_lifecycle = t_lifecycle;
    }

    public int getT_status() {
        return t_status;
    }

    public void setT_status(int t_status) {
        this.t_status = t_status;
    }
}
