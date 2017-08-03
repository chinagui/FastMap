package com.navinfo.dataservice.engine.meta.translates.model;

import java.sql.ResultSet;

/**
 * @Title: Chi2EngKeyword
 * @Package: com.navinfo.dataservice.engine.meta.translates.model
 * @Description: sc_point_chi2eng_keyword
 * @Author: Crayeres
 * @Date: 7/26/2017
 * @Version: V1.0
 */
public class Chi2EngKeyword {

    private Long id;

    private String chikeywords;

    private String engkeywords;

    private int priority;

    private String kind;

    private String source;

    private String memo;

    /**
     * Getter method for property <tt>id</tt>.
     *
     * @return property value of id
     */
    public Long getId() {
        return id;
    }

    /**
     * Setter method for property <tt>id</tt>.
     *
     * @param id value to be assigned to property id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Getter method for property <tt>chikeywords</tt>.
     *
     * @return property value of chikeywords
     */
    public String getChikeywords() {
        return chikeywords;
    }

    /**
     * Setter method for property <tt>chikeywords</tt>.
     *
     * @param chikeywords value to be assigned to property chikeywords
     */
    public void setChikeywords(String chikeywords) {
        this.chikeywords = chikeywords;
    }

    /**
     * Getter method for property <tt>engkeywords</tt>.
     *
     * @return property value of engkeywords
     */
    public String getEngkeywords() {
        return engkeywords;
    }

    /**
     * Setter method for property <tt>engkeywords</tt>.
     *
     * @param engkeywords value to be assigned to property engkeywords
     */
    public void setEngkeywords(String engkeywords) {
        this.engkeywords = engkeywords;
    }

    /**
     * Getter method for property <tt>priority</tt>.
     *
     * @return property value of priority
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Setter method for property <tt>priority</tt>.
     *
     * @param priority value to be assigned to property priority
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     * Getter method for property <tt>kind</tt>.
     *
     * @return property value of kind
     */
    public String getKind() {
        return kind;
    }

    /**
     * Setter method for property <tt>kind</tt>.
     *
     * @param kind value to be assigned to property kind
     */
    public void setKind(String kind) {
        this.kind = kind;
    }

    /**
     * Getter method for property <tt>source</tt>.
     *
     * @return property value of source
     */
    public String getSource() {
        return source;
    }

    /**
     * Setter method for property <tt>source</tt>.
     *
     * @param source value to be assigned to property source
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * Getter method for property <tt>memo</tt>.
     *
     * @return property value of memo
     */
    public String getMemo() {
        return memo;
    }

    /**
     * Setter method for property <tt>memo</tt>.
     *
     * @param memo value to be assigned to property memo
     */
    public void setMemo(String memo) {
        this.memo = memo;
    }
}
