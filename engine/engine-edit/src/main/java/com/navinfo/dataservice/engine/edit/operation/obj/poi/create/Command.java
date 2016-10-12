package com.navinfo.dataservice.engine.edit.operation.obj.poi.create;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

/**
 * @author 赵凯凯
 * @version V1.0
 * @Title: Command.java
 * @Description: 前台创建poi request数据封装类
 * @date 2016年6月15日 下午2:26:38
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
    // 引导坐标
    private double xguide;

    // 种别代码
    private String kindCode;

    // 名称内容
    private String name;

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

    public String getKindCode() {
        return kindCode;
    }

    public String getName() {
        return name;
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
        return OperType.CREATE;
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
        if (data.containsKey("linkPid")) {
            this.setLinkPid(data.getInt("linkPid"));
        } else {
            this.setLinkPid(0);
        }
        // 获取经纬度
        this.setLatitude(Math.round(data.getDouble("latitude") * 100000) / 100000.0);
        this.setLongitude(Math.round(data.getDouble("longitude") * 100000) / 100000.0);
        if (data.containsKey("x_guide")) {
            // 获取引导坐标
            this.setXguide(Math.round(data.getDouble("x_guide") * 100000) / 100000.0);
        } else {
            this.setXguide(0);
        }
        if (data.containsKey("y_guide")) {
            this.setYguide(Math.round(data.getDouble("y_guide") * 100000) / 100000.0);
        } else {
            this.setYguide(0);
        }
        if (data.containsKey("kind_code")) {
            this.kindCode = data.getString("kind_code");
        }
        if (data.containsKey("name")) {
            this.name = data.getString("name");
        }
    }

}
