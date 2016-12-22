package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleteadlink;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFace;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNode;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdFaceSelector;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdNodeSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {

    public Process(AbstractCommand command) throws Exception {
        super(command);

    }

    public void lockAdLink() throws Exception {

        AdLinkSelector selector = new AdLinkSelector(this.getConn());

        AdLink link = (AdLink) selector.loadById(this.getCommand().getLinkPid(), true);

        this.getCommand().setLink(link);
    }

    // 锁定盲端节点
    public void lockAdNode() throws Exception {

        AdNodeSelector selector = new AdNodeSelector(this.getConn());

        List<AdNode> nodes = selector.loadEndAdNodeByLinkPid(this.getCommand().getLinkPid(), false);

        List<Integer> nodePids = new ArrayList<Integer>();

        for (AdNode node : nodes) {
            nodePids.add(node.getPid());
        }
        this.getCommand().setNodes(nodes);

        this.getCommand().setNodePids(nodePids);
    }

    // 锁定盲端节点
    public void lockAdFace() throws Exception {
        AdFaceSelector selector = new AdFaceSelector(this.getConn());
        List<AdFace> faces = selector.loadAdFaceByLinkId(this.getCommand().getLinkPid(), true);
        this.getCommand().setFaces(faces);
    }

    @Override
    public boolean prepareData() throws Exception {

        // 获取该link对象
        lockAdLink();

        if (this.getCommand().getLink() == null) {

            throw new Exception("指定删除的LINK不存在！");
        }

        lockAdNode();
        lockAdFace();
        return true;
    }

    @Override
    public String exeOperation() throws Exception {
        // 删除行政区划线有关行政区划点、线具体操作
        IOperation op = new OpTopo(this.getCommand());
        op.run(this.getResult());
        // 同一点关系
        OpRefRdSameNode opRefRdSameNode = new OpRefRdSameNode(getConn());
        opRefRdSameNode.run(getResult(), this.getCommand().getNodePids());

        updataRelationObj();
        // 删除行政区划线有关行政区划面具体操作
        IOperation opAdFace = new OpRefAdFace(this.getCommand(), getConn());
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
