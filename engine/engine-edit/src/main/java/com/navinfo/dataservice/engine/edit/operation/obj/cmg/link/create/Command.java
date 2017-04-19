package com.navinfo.dataservice.engine.edit.operation.obj.cmg.link.create;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.utils.Constant;
import com.vividsolutions.jts.geom.Geometry;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import org.json.JSONException;

/**
 * @Title: Command
 * @Package: com.navinfo.dataservice.engine.edit.operation.obj.cmg.link.create
 * @Description: 组装创建CMG-LINK所需参数
 * @Author: Crayeres
 * @Date: 2017/4/10
 * @Version: V1.0
 */
public class Command extends AbstractCommand {

    /**
     * 日志记录类
     */
    private Logger logger = Logger.getLogger(Command.class);

    /**
     * 参数
     */
    private String requester;

    /**
     * 起始点PID
     */
    private Integer sNodePid;

    /**
     * 结束点PID
     */
    private Integer eNodePid;

    /**
     * 生成线几何
     */
    private Geometry geometry;

    /**
     * 挂接线信息
     */
    private JSONArray catchLinks;

    @Override
    public OperType getOperType() {
        return OperType.CREATE;
    }

    @Override
    public String getRequester() {
        return this.requester;
    }

    @Override
    public ObjType getObjType() {
        return ObjType.CMGBUILDLINK;
    }

    public Command(JSONObject json, String requester) {
        this.requester = requester;

        this.setDbId(json.getInt("dbId"));

        JSONObject data = json.getJSONObject("data");
        this.sNodePid = data.getInt("sNodePid");
        this.eNodePid = data.getInt("eNodePid");
        try {
            this.geometry = GeoTranslator.geojson2Jts(data.getJSONObject("geometry"), 1, Constant.BASE_PRECISION);
        } catch (JSONException e) {
            logger.error("create cmg link parameter error, parameter : [" + requester + "]");
            e.printStackTrace();
        }

        if (data.containsKey("catchLinks")) {
            this.catchLinks = data.getJSONArray("catchLinks");
        } else {
            this.catchLinks = new JSONArray();
        }
    }

    /**
     * Getter method for property <tt>sNodePid</tt>.
     *
     * @return property value of sNodePid
     */
    public Integer getsNodePid() {
        return sNodePid;
    }

    /**
     * Getter method for property <tt>eNodePid</tt>.
     *
     * @return property value of eNodePid
     */
    public Integer geteNodePid() {
        return eNodePid;
    }

    /**
     * Getter method for property <tt>geometry</tt>.
     *
     * @return property value of geometry
     */
    public Geometry getGeometry() {
        return geometry;
    }

    /**
     * Getter method for property <tt>catchLinks</tt>.
     *
     * @return property value of catchLinks
     */
    public JSONArray getCatchLinks() {
        return catchLinks;
    }
}
