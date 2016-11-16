/**
 * 
 */
package com.navinfo.dataservice.engine.meta.tmc.model;

/** 
* @ClassName: TmcArea 
* @author Zhang Xiaolong
* @date 2016年11月15日 下午3:29:19 
* @Description: TODO
*/
public class TmcArea {
	private int tmcId;
	
	private String name;
	
	private int uperTmcId;
	
	private int cid;

	public int getTmcId() {
		return tmcId;
	}

	public void setTmcId(int tmcId) {
		this.tmcId = tmcId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getUperTmcId() {
		return uperTmcId;
	}

	public void setUperTmcId(int uperTmcId) {
		this.uperTmcId = uperTmcId;
	}

	public int getCid() {
		return cid;
	}

	public void setCid(int cid) {
		this.cid = cid;
	}
}
