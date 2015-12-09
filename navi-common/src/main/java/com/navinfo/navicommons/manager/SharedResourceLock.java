package com.navinfo.navicommons.manager;

/**
 * User: liuqing
 * Date: 2010-9-3
 * Time: 13:41:02
 */
public class SharedResourceLock {

    public static final int EXP=0;
    public static final int EXP_VIEW=1;
    public static final int DIFF=2;
    public static final int CHANGE_VIEW=3;
    public static final int IMP=4;
    public static final int CHECK=5;

    private Integer type;
    private boolean used;
    private Integer id;
    private String value;

    public SharedResourceLock() {
        
    }

    public SharedResourceLock(String value) {
        this.value = value;

    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(Integer userdValue) {
        //1为使用
        this.used = (userdValue == 1);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }


}
