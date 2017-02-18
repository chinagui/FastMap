package com.navinfo.dataservice.engine.edit.operation.topo.delete.deletezonenode;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneFace;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLink;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneNode;
import com.navinfo.dataservice.dao.glm.selector.ad.zone.ZoneFaceSelector;
import com.navinfo.dataservice.dao.glm.selector.ad.zone.ZoneLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.ad.zone.ZoneNodeSelector;
import com.navinfo.dataservice.dao.log.LogWriter;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;
import com.navinfo.dataservice.engine.edit.operation.OperatorFactory;

/**
 * @author zhaokk ZONE点删除操作类
 */

public class Process extends AbstractProcess<Command> {

    public Process(AbstractCommand command) throws Exception {
        super(command);
        // TODO Auto-generated constructor stub
    }

    protected Logger log = Logger.getLogger(this.getClass());

    /*
     * 加载行ZONE点对应的ZONE线
     */
    public void lockZoneLink() throws Exception {

        ZoneLinkSelector selector = new ZoneLinkSelector(this.getConn());
        List<ZoneLink> links = selector.loadByNodePid(this.getCommand().getNodePid(), true);
        List<Integer> linkPids = new ArrayList<Integer>();
        for (ZoneLink link : links) {
            linkPids.add(link.getPid());
        }
        this.getCommand().setLinks(links);

        this.getCommand().setLinkPids(linkPids);
    }

    /*
     * 加载行Zone点对应的Zone点
     */
    public void lockZoneNode() throws Exception {

        ZoneNodeSelector selector = new ZoneNodeSelector(this.getConn());

        ZoneNode node = (ZoneNode) selector.loadById(this.getCommand().getNodePid(), true);

        this.getCommand().setNode(node);

    }

    /*
     * 加载ZONE点对应的行政区盲端节点
     */
    public void lockEndZoneNode() throws Exception {

        ZoneNodeSelector selector = new ZoneNodeSelector(this.getConn());

        List<Integer> nodePids = new ArrayList<Integer>();

        nodePids.add(this.getCommand().getNodePid());

        List<ZoneNode> nodes = new ArrayList<ZoneNode>();

        for (Integer linkPid : this.getCommand().getLinkPids()) {

            List<ZoneNode> list = selector.loadEndZoneNodeByLinkPid(linkPid, true);

            for (ZoneNode node : list) {
                int nodePid = node.getPid();

                if (nodePids.contains(nodePid)) {
                    continue;
                }

                nodePids.add(node.getPid());

                nodes.add(node);
            }

        }

        this.getCommand().setNodes(nodes);

        this.getCommand().setNodePids(nodePids);
    }

    /*
     * 加载Zone点对应的ZONE线
     */
    public void lockZoneFace() throws Exception {

        ZoneFaceSelector selector = new ZoneFaceSelector(this.getConn());

        List<ZoneFace> faces = new ArrayList<ZoneFace>();

        for (Integer linkPid : this.getCommand().getLinkPids()) {

            List<ZoneFace> list = selector.loadZoneFaceByLinkId(linkPid, true);

            for (ZoneFace face : list) {
                faces.add(face);

            }
        }
        this.getCommand().setFaces(faces);
    }

    @Override
    public boolean prepareData() throws Exception {
        this.lockZoneNode();
        this.lockZoneLink();
        this.lockEndZoneNode();
        this.lockZoneFace();
        return true;
    }

    @Override
    public boolean recordData() throws Exception {

        LogWriter lw = new LogWriter(this.getConn());

        lw.generateLog(this.getCommand(), this.getResult());

        OperatorFactory.recordData(this.getConn(), this.getResult());

        lw.recordLog(this.getCommand(), this.getResult());

        return true;
    }

    @Override
    public String exeOperation() throws Exception {
        // 删除ZONE点有关ZONE点、线具体操作
        IOperation op = new OpTopo(this.getCommand());
        op.run(this.getResult());
        // 同一点关系
        OpRefRdSameNode opRefRdSameNode = new OpRefRdSameNode(getConn());
        opRefRdSameNode.run(getResult(), this.getCommand());

        updataRelationObj();
        // 删除ZONE点有关ZONE面具体操作
        IOperation opAdFace = new OpRefZoneFace(this.getCommand(), getConn());
        return opAdFace.run(this.getResult());
    }

    /**
     * 维护关联要素
     *
     * @throws Exception
     */
    private void updataRelationObj() throws Exception {

        OpRefRelationObj opRefRelationObj = new OpRefRelationObj(getConn());

        opRefRelationObj.handleSameLink(this.getResult(), this.getCommand());
    }
}
