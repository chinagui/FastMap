package com.navinfo.dataservice.commons.job;

import java.util.Iterator;
import java.util.List;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;

import com.navinfo.dataservice.commons.util.StringUtils;

/** 
 * @ClassName: AbstractJobRequest 
 * @author Xiao Xiaowen 
 * @date 2016-1-15 下午2:18:48 
 * @Description: TODO
 */
public abstract class AbstractJobRequest {
	protected Logger log = Logger.getLogger(this.getClass());
	protected String gdbVersion;
	
	public abstract void validate()throws JobRuntimeException;
	
	public abstract void setAttrValue(String attName,String attValue)throws JobRuntimeException;
	

	public String getGdbVersion() {
		return gdbVersion;
	}

	public void setGdbVersion(String gdbVersion) {
		this.gdbVersion = gdbVersion;
	}

	public void parseByXmlConfig(Document rootDoc) throws JobRuntimeException{
		if(rootDoc==null) {
			log.warn("注意：未传入的解析xml对象，导出的config未被初始化");
		}
		List<Element> attrsList = rootDoc.getRootElement().elements();
		for(Element attrs: attrsList){
			List<Element> attrList = attrs.elements();
			for(Element att:attrList){
				String attName = att.attributeValue("name");
				String attValue = att.attributeValue("value");
				if(StringUtils.isEmpty(attName)||StringUtils.isEmpty(attValue)){
					log.warn("注意：导出配置的xml中存在name或者value为空的attr node，已经被忽略。");
					continue;
				}
				setAttrValue(attName,attValue);
			}
		}
	}
	public void parseByJsonConfig(JSONObject json)throws JobRuntimeException{
		if(json==null) {
			log.warn("注意：未传入的解析json对象，导出的config未被初始化");
		}
		for(Iterator it = json.keys();it.hasNext();){
			String attName = (String)it.next();
			String attValue = (String)json.get(attName);
			if(StringUtils.isEmpty(attName)||StringUtils.isEmpty(attValue)){
				log.warn("注意：导出配置的json中存在name或者value为空的属性，已经被忽略。");
				continue;
			}
			setAttrValue(attName,attValue);
		}
	}
}
