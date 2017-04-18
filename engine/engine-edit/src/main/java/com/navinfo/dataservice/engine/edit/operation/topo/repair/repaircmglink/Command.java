package com.navinfo.dataservice.engine.edit.operation.topo.repair.repaircmglink;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildface;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildlink;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.utils.Constant;
import com.vividsolutions.jts.geom.Geometry;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.json.JSONException;

import java.util.List;

/**
 * @Title: Command
 * @Package: com.navinfo.dataservice.engine.edit.operation.topo.repair.repaircmglink
 * @Description: ${TODO}
 * @Author: Crayeres
 * @Date: 2017/4/17
 * @Version: V1.0
 */
public class Command extends AbstractCommand {

    /**
     * 请求参数
     */
    private String requester;

    /**
     * 待修改对象
     */
    private CmgBuildlink cmglink = new CmgBuildlink();

    /**
     * 关联CMG-FACE
     */
    private List<CmgBuildface> cmgfaces;

    /**
     * 修形后几何
     */
    private Geometry geometry;

    /**
     * 挂接信息
     */
    private JSONArray catchInfos;

    /**
     * @return 操作类型
     */
    @Override
    public OperType getOperType() {
        return OperType.REPAIR;
    }

    /**
     * @return 请求参数
     */
    @Override
    public String getRequester() {
        return this.requester;
    }

    /**
     * @return 操作对象类型
     */
    @Override
    public ObjType getObjType() {
        return ObjType.CMGBUILDLINK;
    }

    public Command(JSONObject json,String requester) throws JSONException {
        this.requester = requester;
        this.setDbId(json.getInt("dbId"));

        JSONObject data = json.getJSONObject("data");
        cmglink.setPid(json.getInt("objId"));
        geometry = GeoTranslator.geojson2Jts(data.getJSONObject("geometry"), 1, Constant.BASE_PRECISION);

        if (data.containsKey("catchInfos")) {
            catchInfos = data.getJSONArray("catchInfos");
        }
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
     * Getter method for property <tt>catchInfos</tt>.
     *
     * @return property value of catchInfos
     */
    public JSONArray getCatchInfos() {
        return catchInfos;
    }

    /**
     * Getter method for property <tt>cmglink</tt>.
     *
     * @return property value of cmglink
     */
    public CmgBuildlink getCmglink() {

        return cmglink;
    }

    /**
     * Setter method for property <tt>cmglink</tt>.
     *
     * @param cmglink value to be assigned to property cmglink
     */
    public void setCmglink(CmgBuildlink cmglink) {
        this.cmglink = cmglink;
    }

    /**
     * Getter method for property <tt>cmgfaces</tt>.
     *
     * @return property value of cmgfaces
     */
    public List<CmgBuildface> getCmgfaces() {
        return cmgfaces;
    }

    /**
     * Setter method for property <tt>cmgfaces</tt>.
     *
     * @param cmgfaces value to be assigned to property cmgfaces
     */
    public void setCmgfaces(List<CmgBuildface> cmgfaces) {
        this.cmgfaces = cmgfaces;
    }
}
