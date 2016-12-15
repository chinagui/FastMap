package com.navinfo.dataservice.engine.edit.operation.topo.repair.repairrdlink;

import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.List;
import java.util.Map;

import org.json.JSONException;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.vividsolutions.jts.geom.Geometry;

/***
 * RDLINK修行
 *
 * @author zhaokk
 *
 */
public class Command extends AbstractCommand {

    private String requester;

    private int linkPid;

    private Geometry linkGeom;

    private JSONArray catchInfos;

    private RdLink updateLink;

    private String operationType = "";

    private Map<RdNode, List<RdLink>> nodeLinkRelation;

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    private List<RdGsc> gscList;

    public List<RdGsc> getGscList() {
        return gscList;
    }

    public void setGscList(List<RdGsc> gscList) {
        this.gscList = gscList;
    }

    public int getLinkPid() {
        return linkPid;
    }

    public Geometry getLinkGeom() {
        return linkGeom;
    }


    public void setLinkGeom(Geometry linkGeom) {
        this.linkGeom = linkGeom;
    }

    public JSONArray getCatchInfos() {
        return catchInfos;
    }

    public void setCatchInfos(JSONArray catchInfos) {
        this.catchInfos = catchInfos;
    }

    public RdLink getUpdateLink() {
        return updateLink;
    }

    public void setUpdateLink(RdLink updateLink) {
        this.updateLink = updateLink;
    }

    public Map<RdNode, List<RdLink>> getNodeLinkRelation() {
        return nodeLinkRelation;
    }

    public void setNodeLinkRelation(Map<RdNode, List<RdLink>> nodeLinkRelation) {
        this.nodeLinkRelation = nodeLinkRelation;
    }

    @Override
    public OperType getOperType() {

        return OperType.REPAIR;
    }

    @Override
    public String getRequester() {

        return requester;
    }

    @Override
    public ObjType getObjType() {

        return ObjType.RDLINK;
    }

    public Command(JSONObject json, String requester) throws JSONException {

        this.requester = requester;

        this.setDbId(json.getInt("dbId"));

        this.linkPid = json.getInt("objId");

        JSONObject data = json.getJSONObject("data");

        JSONObject geometry = data.getJSONObject("geometry");

        this.linkGeom = (GeoTranslator.geojson2Jts(
                geometry, 1, 5));
        //修行挂接信息
        if (data.containsKey("catchInfos")) {
            this.catchInfos = data.getJSONArray("catchInfos");
        }

    }

}
