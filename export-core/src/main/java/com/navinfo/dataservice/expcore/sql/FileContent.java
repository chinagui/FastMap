package com.navinfo.dataservice.expcore.sql;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;

/**
 * User: liuqing
 * Date: 2010-10-9
 * Time: 9:27:16
 */
public class FileContent {
    List<Element> rootElements;
    List<Element> assembledRootElements = new ArrayList<Element>();
    List<String> excludeElements = new ArrayList<String>();
    List<String> includeElements = new ArrayList<String>();


    public FileContent(List<Element> rootElements) {
        this.rootElements = rootElements;
    }


    public void addAssembledElement(Element e) {
        assembledRootElements.add(e);
    }

    public void addExcludeElements(List<Element> e) {
        for (Element excludeElement : e) {
            String sqlId = excludeElement.attributeValue("sqlId").trim();
            excludeElements.add(sqlId);
        }
    }


    public List<Element> getAssembledRootElements() {
        return assembledRootElements;
    }

    public void setAssembledRootElements(List<Element> assembledRootElements) {
        this.assembledRootElements = assembledRootElements;
    }


    public boolean excludeSql(String sqlId) {
        return excludeElements.contains(sqlId);
    }

    public void setRootElements(List<Element> rootElements) {
        this.rootElements = rootElements;
    }

    public List<Element> getRootElements() {
        return rootElements;
    }
}
