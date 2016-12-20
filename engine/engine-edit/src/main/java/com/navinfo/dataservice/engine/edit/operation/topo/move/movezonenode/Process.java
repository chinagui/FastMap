package com.navinfo.dataservice.engine.edit.operation.topo.move.movezonenode;

import java.sql.Connection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLink;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneNode;
import com.navinfo.dataservice.dao.glm.selector.ad.zone.ZoneFaceSelector;
import com.navinfo.dataservice.dao.glm.selector.ad.zone.ZoneLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.ad.zone.ZoneNodeSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

/**
 * @author zhaokk ZONE点具体执行类
 */
public class Process extends AbstractProcess<Command> {

    public Process(Command command, Result result, Connection conn) throws Exception {
        super();
        this.setCommand(command);
        this.setResult(result);
        this.setConn(conn);
        this.initCheckCommand();
    }

    public Process(AbstractCommand command) throws Exception {
        super(command);
    }

    /*
     * 移动Zone点加载对应的Zone线信息
     */
    public void lockZoneLink() throws Exception {
        ZoneLinkSelector selector = new ZoneLinkSelector(this.getConn());
        List<ZoneLink> links = selector.loadByNodePid(this.getCommand().getNodePid(), true);
        this.getCommand().setLinks(links);
    }

    /*
     * 移动行政区划点加载对应的行政区点线信息
     */
    public void lockZoneNode() throws Exception {
        if (this.getCommand().getZoneNode() == null) {
            ZoneNodeSelector nodeSelector = new ZoneNodeSelector(this.getConn());
            this.getCommand().setZoneNode((ZoneNode) nodeSelector.loadById(this.getCommand().getNodePid(), true));
        }
    }

    /*
     * 移动行政区划点加载对应的行政区点面信息
     */
    public void lockZoneFace() throws Exception {
        ZoneFaceSelector faceSelector = new ZoneFaceSelector(this.getConn());
        this.getCommand().setFaces(faceSelector.loadZoneFaceByNodeId(this.getCommand().getNodePid(), true));

    }

    @Override
    public boolean prepareData() throws Exception {
        this.lockZoneNode();
        if (CollectionUtils.isEmpty(this.getCommand().getLinks())) {
            this.lockZoneLink();
        }
        this.lockZoneFace();
        return false;
    }

    @Override
    public String exeOperation() throws Exception {
        return new Operation(this.getCommand(), this.getConn()).run(this.getResult());

    }

    public String innerRun() throws Exception {
        String msg;
        try {
            this.prepareData();
            String preCheckMsg = this.preCheck();
            if (preCheckMsg != null) {
                throw new Exception(preCheckMsg);
            }
            IOperation operation = new Operation(this.getCommand(), this.getConn());
            msg = operation.run(this.getResult());
            //this.postCheck();
        } catch (Exception e) {
            this.getConn().rollback();
            throw e;
        }
        return msg;
    }
}
