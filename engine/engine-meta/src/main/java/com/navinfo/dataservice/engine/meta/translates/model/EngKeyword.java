package com.navinfo.dataservice.engine.meta.translates.model;

/**
 * @Title: EngKeyword
 * @Package: com.navinfo.dataservice.engine.meta.translates.model
 * @Description: ${TODO}
 * @Author: Crayeres
 * @Date: 7/27/2017
 * @Version: V1.0
 */
public class EngKeyword {

    private Long id;

    private String specWords;

    private String combinedWords;

    private String selectedWords;

    private String priority;

    private String result;

    private String engWords;

    private int type;

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
     * Getter method for property <tt>specWords</tt>.
     *
     * @return property value of specWords
     */
    public String getSpecWords() {
        return specWords;
    }

    /**
     * Setter method for property <tt>specWords</tt>.
     *
     * @param specWords value to be assigned to property specWords
     */
    public void setSpecWords(String specWords) {
        this.specWords = specWords;
    }

    /**
     * Getter method for property <tt>combinedWords</tt>.
     *
     * @return property value of combinedWords
     */
    public String getCombinedWords() {
        return combinedWords;
    }

    /**
     * Setter method for property <tt>combinedWords</tt>.
     *
     * @param combinedWords value to be assigned to property combinedWords
     */
    public void setCombinedWords(String combinedWords) {
        this.combinedWords = combinedWords;
    }

    /**
     * Getter method for property <tt>selectedWords</tt>.
     *
     * @return property value of selectedWords
     */
    public String getSelectedWords() {
        return selectedWords;
    }

    /**
     * Setter method for property <tt>selectedWords</tt>.
     *
     * @param selectedWords value to be assigned to property selectedWords
     */
    public void setSelectedWords(String selectedWords) {
        this.selectedWords = selectedWords;
    }

    /**
     * Getter method for property <tt>priority</tt>.
     *
     * @return property value of priority
     */
    public String getPriority() {
        return priority;
    }

    /**
     * Setter method for property <tt>priority</tt>.
     *
     * @param priority value to be assigned to property priority
     */
    public void setPriority(String priority) {
        this.priority = priority;
    }

    /**
     * Getter method for property <tt>result</tt>.
     *
     * @return property value of result
     */
    public String getResult() {
        return result;
    }

    /**
     * Setter method for property <tt>result</tt>.
     *
     * @param result value to be assigned to property result
     */
    public void setResult(String result) {
        this.result = result;
    }

    /**
     * Getter method for property <tt>engWords</tt>.
     *
     * @return property value of engWords
     */
    public String getEngWords() {
        return engWords;
    }

    /**
     * Setter method for property <tt>engWords</tt>.
     *
     * @param engWords value to be assigned to property engWords
     */
    public void setEngWords(String engWords) {
        this.engWords = engWords;
    }

    /**
     * Getter method for property <tt>type</tt>.
     *
     * @return property value of type
     */
    public int getType() {
        return type;
    }

    /**
     * Setter method for property <tt>type</tt>.
     *
     * @param type value to be assigned to property type
     */
    public void setType(int type) {
        this.type = type;
    }
}
