package com.navinfo.dataservice.engine.edit.edit.operation.obj.poi.move;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;

/**
 * 
* @Title: Command.java 
* @Description: 前台移动poi request数据封装类
* @author 赵凯凯
* @date 2016年6月15日 下午2:26:38 
* @version V1.0
 */
public class Command extends AbstractCommand implements ICommand {

	private String requester;

	/**
	 * 引导线RDLink的pid
	 */
	private Integer linkPid;

	/**
	 * 经度
	 */
	private double longitude;

	/**
	 * 维度
	 */
	

	private double latitude;
	private IxPoi ixPoi;
	public IxPoi getIxPoi() {
		return ixPoi;
	}
   
	public void setIxPoi(IxPoi ixPoi) {
		this.ixPoi = ixPoi;
	}
	private int  pid;
	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}
	//引导坐标
	private double xguide;
	public double getXguide() {
		return xguide;
	}

	public void setXguide(double xguide) {
		this.xguide = xguide;
	}

	public double getYguide() {
		return yguide;
	}

	public void setYguide(double yguide) {
		this.yguide = yguide;
	}

	private double yguide;

	public Integer getLinkPid() {
		return linkPid;
	}

	public void setLinkPid(Integer linkPid) {
		this.linkPid = linkPid;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	@Override
	public OperType getOperType() {
		return OperType.MOVE;
	}

	@Override
	public ObjType getObjType() {
		return ObjType.IXPOI;
	}

	@Override
	public String getRequester() {
		return requester;
	}

	public Command(JSONObject json, String requester) {
		this.requester = requester;
		this.setDbId(json.getInt("dbId"));
		JSONObject data = json.getJSONObject("data");
		this.setLinkPid(data.getInt("linkPid"));
		//获取经纬度
		
        this.setLatitude(Math.round(data.getDouble("latitude")*100000)/100000.0);
        this.setLongitude(Math.round(data.getDouble("longitude")*100000)/100000.0);
        //获取引导坐标
		this.setXguide(Math.round(data.getDouble("x_guide")*100000)/100000.0);
		this.setYguide(Math.round(data.getDouble("y_guide")*100000)/100000.0);
	}

}
