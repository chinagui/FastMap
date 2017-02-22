package com.navinfo.dataservice.engine.edit.operation.topo.repair.repairrdlink;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.gsc.RdGscSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;
import com.navinfo.dataservice.engine.edit.operation.parameterCheck.DepartCheck;
import com.navinfo.dataservice.engine.edit.utils.RdGscOperateUtils;

public class Process extends AbstractProcess<Command> {

    public Process(AbstractCommand command) throws Exception {
        super(command);
    }

    private Check check = new Check();

    @Override
    public boolean prepareData() throws Exception {

        int linkPid = this.getCommand().getLinkPid();

        this.getCommand().setUpdateLink((RdLink) new RdLinkSelector(this.getConn()).loadById(linkPid, true));

        // 查询需要修行的线上是否存在立交
        RdGscSelector gscSelector = new RdGscSelector(this.getConn());

        List<RdGsc> gscList = gscSelector.loadRdGscLinkByLinkPid(linkPid, "RD_LINK", true);

        this.getCommand().setGscList(gscList);

        return false;
    }


    private void parameterCheck() throws Exception {

        DepartCheck departCheck = new DepartCheck(this.getConn());

        RdLinkSelector rdLinkSelector = new RdLinkSelector(this.getConn());

        if (this.getCommand().getCatchInfos() != null && this.getCommand().getCatchInfos().size() > 0) {

            List<Integer> nodePids = new ArrayList<Integer>();

            for (int i = 0; i < this.getCommand().getCatchInfos().size(); i++) {
                JSONObject obj = this.getCommand().getCatchInfos().getJSONObject(i);
                // 分离移动的node
                nodePids.add(obj.getInt("nodePid"));
            }

            for (int nodePid : nodePids) {

                List<Integer> linkPids = rdLinkSelector.loadLinkPidByNodePid(nodePid, false);

                if (linkPids.size() > 1) {
                    departCheck.checkIsSameNode(nodePid, "RD_NODE");
                }
            }

            if (nodePids.size() > 0) {
                departCheck.checkIsVia(this.getCommand().getLinkPid());
            }
        }
    }

    @Override
    public String preCheck() throws Exception {

        // check.checkIsVia(this.getConn(), this.getCommand().getLinkPid());
        check.checkShapePointDistance(GeoTranslator.jts2Geojson(this.getCommand().getLinkGeom()));
        // 分离节点检查CRFI
        check.checkCRFI(getConn(), getCommand());
        // 分离节点检查顺行
        check.checkRdDirectRAndLaneC(getConn(), getCommand());
        return super.preCheck();
    }

    @Override
    public String exeOperation() throws Exception {

        parameterCheck();

        RdGscOperateUtils.checkIsMoveGscPoint(GeoTranslator.jts2Geojson(this.getCommand().getLinkGeom()), this
                .getConn(), this.getCommand().getLinkPid(), "RD_LINK");
        return new Operation(this.getConn(), this.getCommand()).run(this.getResult());
    }

}
