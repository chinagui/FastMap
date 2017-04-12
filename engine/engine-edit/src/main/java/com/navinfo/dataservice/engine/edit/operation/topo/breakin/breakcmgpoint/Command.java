package com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakcmgpoint;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildface;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildlink;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildnode;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.obj.cmg.node.CmgnodeUtil;
import com.navinfo.dataservice.engine.edit.utils.Constant;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @Title: Command
 * @Package: com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakcmgpoint
 * @Description: 组装打断CMG-LINK所需参数
 * @Author: Crayeres
 * @Date: 2017/4/10
 * @Version: V1.0
 */
public class Command extends AbstractCommand {

    /**
     * 请求参数
     */
    private String requester;

    /**
     * 打断点对象
     */
    private CmgBuildnode cmgnode;

    /**
     * 连续打断点信息
     */
    private JSONArray breakNodes;

    /**
     * 被打断线对象
     */
    private CmgBuildlink cmglink;

    /**
     * 打断后生成的CMG-LINK
     */
    private List<CmgBuildlink> newCmglinks = new ArrayList<>();

    /**
     * 被打断线所关联面对象
     */
    private List<CmgBuildface> cmgfaces;

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
        return ObjType.CMGBUILDNODE;
    }

    public Command( JSONObject json, String requester) {
        this.requester = requester;
        this.setDbId(json.getInt("dbId"));

        this.cmglink.setPid(json.getInt("objId"));

        JSONObject data = json.getJSONObject("data");
        if (data.containsKey("breakNodePid")) {
            this.cmgnode.setPid(data.getInt("breakNodePid"));
        }
        if (data.containsKey("breakNodes")) {
           this.breakNodes = data.getJSONArray("breakNodes");
        } else {
            this.cmgnode.setGeometry(new GeometryFactory().createPoint(
                    new Coordinate(CmgnodeUtil.reviseItude(data.getDouble("longitude"))
                            , CmgnodeUtil.reviseItude(data.getDouble("longitude")))));
        }
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
     * Getter method for property <tt>breakNodes</tt>.
     *
     * @return property value of breakNodes
     */
    public JSONArray getBreakNodes() {
        return breakNodes;
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

    /**
     * Getter method for property <tt>cmgnode</tt>.
     *
     * @return property value of cmgnode
     */
    public CmgBuildnode getCmgnode() {
        return cmgnode;
    }

    /**
     * Setter method for property <tt>cmgnode</tt>.
     *
     * @param cmgnode value to be assigned to property cmgnode
     */
    public void setCmgnode(CmgBuildnode cmgnode) {
        this.cmgnode = cmgnode;
    }

    /**
     * Getter method for property <tt>newCmglinks</tt>.
     *
     * @return property value of newCmglinks
     */
    public List<CmgBuildlink> getNewCmglinks() {
        return newCmglinks;
    }

    /**
     * Setter method for property <tt>newCmglinks</tt>.
     *
     * @param newCmglinks value to be assigned to property newCmglinks
     */
    public void setNewCmglinks(List<CmgBuildlink> newCmglinks) {
        this.newCmglinks = newCmglinks;
    }
}
