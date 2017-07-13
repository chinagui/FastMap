package com.navinfo.dataservice.engine.fcc.tips.model;

/**
 * Created by zhangjunfang on 2017/7/12.
 */
public class FieldRoadQCRecord {
    private String rowkey;
    private String link_pid;
    private String problem_num;
    private String class_top;
    private String class_bottom;
    private String problem_type;
    private String problem_phenomenon;
    private String problem_description;
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

    public String getProblem_num() {
        return problem_num;
    }

    public void setProblem_num(String problem_num) {
        this.problem_num = problem_num;
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

    public String getProblem_type() {
        return problem_type;
    }

    public void setProblem_type(String problem_type) {
        this.problem_type = problem_type;
    }

    public String getProblem_phenomenon() {
        return problem_phenomenon;
    }

    public void setProblem_phenomenon(String problem_phenomenon) {
        this.problem_phenomenon = problem_phenomenon;
    }

    public String getProblem_description() {
        return problem_description;
    }

    public void setProblem_description(String problem_description) {
        this.problem_description = problem_description;
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
