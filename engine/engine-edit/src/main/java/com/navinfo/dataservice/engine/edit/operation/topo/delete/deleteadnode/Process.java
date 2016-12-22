package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleteadnode;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFace;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNode;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdFaceSelector;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdNodeSelector;
import com.navinfo.dataservice.dao.log.LogWriter;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;
import com.navinfo.dataservice.engine.edit.operation.OperatorFactory;

/**
 * @author zhaokk 行政区划点删除操作类
 */

public class Process extends AbstractProcess<Command> {

    public Process(AbstractCommand command) throws Exception {
        super(command);
        // TODO Auto-generated constructor stub
    }

    protected Logger log = Logger.getLogger(this.getClass());

    /*
     * 加载行政区划点对应的行政区划线
     */
    public void lockAdLink() throws Exception {

        AdLinkSelector selector = new AdLinkSelector(this.getConn());
        List<AdLink> links = selector.loadByNodePid(this.getCommand().getNodePid(), true);
        List<Integer> linkPids = new ArrayList<Integer>();
        for (AdLink link : links) {
            linkPids.add(link.getPid());
        }
        this.getCommand().setLinks(links);

        this.getCommand().setLinkPids(linkPids);
    }

    /*
     * 加载行政区划点对应的行政区点
     */
    public void lockAdNode() throws Exception {

        AdNodeSelector selector = new AdNodeSelector(this.getConn());

        AdNode node = (AdNode) selector.loadById(this.getCommand().getNodePid(), true);

        this.getCommand().setNode(node);

    }

    /*
     * 加载行政区划点对应的行政区盲端节点
     */
    public void lockEndAdNode() throws Exception {

        AdNodeSelector selector = new AdNodeSelector(this.getConn());

        List<Integer> nodePids = new ArrayList<Integer>();

        nodePids.add(this.getCommand().getNodePid());

        List<AdNode> nodes = new ArrayList<AdNode>();

        for (Integer linkPid : this.getCommand().getLinkPids()) {

            List<AdNode> list = selector.loadEndAdNodeByLinkPid(linkPid, true);

            for (AdNode node : list) {
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
     * 加载行政区划点对应的行政区划线
     */
    public void lockAdFace() throws Exception {

        AdFaceSelector selector = new AdFaceSelector(this.getConn());

        List<AdFace> faces = new ArrayList<AdFace>();

        for (Integer linkPid : this.getCommand().getLinkPids()) {

            List<AdFace> list = selector.loadAdFaceByLinkId(linkPid, true);

            for (AdFace face : list) {
                faces.add(face);

            }
        }
        this.getCommand().setFaces(faces);
    }

    @Override
    public boolean prepareData() throws Exception {

        // 获取该adnode对象
        lockAdNode();

        if (this.getCommand().getNode() == null) {

            throw new Exception("指定删除的RDNODE不存在！");
        }

        lockAdLink();

        lockEndAdNode();
        lockAdFace();
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
        // 删除行政区划点有关行政区划点、线具体操作
        IOperation op = new OpTopo(this.getCommand());
        op.run(this.getResult());
        // 同一点关系
        OpRefRdSameNode opRefRdSameNode = new OpRefRdSameNode(getConn());
        opRefRdSameNode.run(getResult(), this.getCommand());

        updataRelationObj();
        // 删除行政区划点有关行政区划面具体操作
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
