package com.navinfo.dataservice.engine.edit.operation.topo.depart.departcmgnode;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildlink;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildnode;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.obj.cmg.node.CmgnodeUtil;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import net.sf.json.JSONObject;

import java.util.List;

/**
 * @Title: Command
 * @Package: com.navinfo.dataservice.engine.edit.operation.topo.depart.departcmgnode
 * @Description: ${TODO}
 * @Author: Crayeres
 * @Date: 2017/4/13
 * @Version: V1.0
 */
public class Command extends AbstractCommand {

    /**
     * 请求参数
     */
    private String requester;

    /**
     * 分离CMG-NODE
     */
    private CmgBuildnode cmgnode = new CmgBuildnode();

    /**
     * 分离点挂接CMG-LINK
     */
    private List<CmgBuildlink> cmglinks;

    /**
     * 分离CMG-LINK
     */
    private CmgBuildlink cmglink = new CmgBuildlink();

    /**
     * 分离后坐标
     */
    private Point point;

    /**
     * 挂节点
     */
    private int catchNodePid;

    /**
     * 挂接线
     */
    private int catchLinkPid;

    @Override
    public OperType getOperType() {
        return OperType.DEPART;
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

        cmgnode.setPid(json.getInt("objId"));
        JSONObject data = json.getJSONObject("data");

        catchNodePid = data.getInt("catchNodePid");
        catchLinkPid = data.getInt("catchLinkPid");
        cmglink.setPid(data.getInt("linkPid"));

        if (data.containsKey("longitude") && data.containsKey("latitude")) {
            point = new GeometryFactory().createPoint(new Coordinate(CmgnodeUtil.reviseItude(data.getDouble("longitude")),
                    CmgnodeUtil.reviseItude(data.getDouble("latitude"))));
        }
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
     * Getter method for property <tt>cmglinks</tt>.
     *
     * @return property value of cmglinks
     */
    public List<CmgBuildlink> getCmglinks() {
        return cmglinks;
    }

    /**
     * Setter method for property <tt>cmglinks</tt>.
     *
     * @param cmglinks value to be assigned to property cmglinks
     */
    public void setCmglinks(List<CmgBuildlink> cmglinks) {
        this.cmglinks = cmglinks;
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
     * Getter method for property <tt>point</tt>.
     *
     * @return property value of point
     */
    public Point getPoint() {
        return point;
    }

    /**
     * Getter method for property <tt>catchNodePid</tt>.
     *
     * @return property value of catchNodePid
     */
    public int getCatchNodePid() {
        return catchNodePid;
    }

    /**
     * Getter method for property <tt>catchLinkPid</tt>.
     *
     * @return property value of catchLinkPid
     */
    public int getCatchLinkPid() {
        return catchLinkPid;
    }
}
