package com.navinfo.dataservice.engine.edit.operation.obj.rdlink.update;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.dao.glm.iface.AlertObject;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

import net.sf.json.JSONObject;

public class Process extends AbstractProcess<Command> {

    public Process(AbstractCommand command) throws Exception {
        super(command);
        // TODO Auto-generated constructor stub
    }

    public Process(AbstractCommand command, Result result, Connection conn) throws Exception {
        super(command, result, conn);
    }

    private RdLink updateLink;

    @Override
    public boolean prepareData() throws Exception {
        RdLinkSelector linkSelector = new RdLinkSelector(this.getConn());
        if (null == getCommand().getLinkPids())
            this.updateLink = (RdLink) linkSelector.loadById(this.getCommand().getLinkPid(), true);
        else {
            for (IRow row : linkSelector.loadByIds(getCommand().getLinkPids(), true, true))
                getCommand().getLinks().add((RdLink) row);
        }
        return false;
    }

    @Override
    public String run() throws Exception {

        try {
            if (!this.getCommand().isInfect()) {
                this.getConn().setAutoCommit(false);

                prepareData();

                Operation operation = new Operation(this.getCommand(), updateLink, this.getConn());
                operation.run(this.getResult());
                
                String preCheckMsg = this.preCheck();
                if (preCheckMsg != null) {
                    throw new Exception(preCheckMsg);
                }

                recordData();

                postCheck();

                this.getConn().commit();
            } else {
                String preCheckMsg = this.preCheck();
                if (preCheckMsg != null) {
                    throw new Exception(preCheckMsg);
                }
                prepareData();
                Map<String, List<AlertObject>> infects = new HashMap<String, List<AlertObject>>();
                //修改link方向
                Operation operation = new Operation();
                List<AlertObject> updateLinkDataList = operation.getUpdateRdLinkAlertData(updateLink, this.getCommand().getUpdateContent());
                if (CollectionUtils.isNotEmpty(updateLinkDataList)) {
                    infects.put("修改link方向", updateLinkDataList);
                }
                com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.update.Operation trafficOperation = new com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.update.Operation(this.getConn());
                List<AlertObject> deleteTrafficAlertDataList = trafficOperation.getUpdateLinkDirectInfectData(updateLink, this.getCommand().getUpdateContent());
                if (CollectionUtils.isNotEmpty(deleteTrafficAlertDataList)) {
                    infects.put("修改link方向删除link上原有信号灯", deleteTrafficAlertDataList);
                }

                List<AlertObject> updateCrossAlertDataList = trafficOperation.getUpdateLinkDirectInfectCross(updateLink, this.getCommand().getUpdateContent());

                if (CollectionUtils.isNotEmpty(updateCrossAlertDataList)) {
                    infects.put("请注意，修改道路方向，可能需要对下列路口维护信号灯信息", updateCrossAlertDataList);
                }
                this.getConn().commit();

                return JSONObject.fromObject(infects).toString();
            }

        } catch (Exception e) {

            this.getConn().rollback();

            throw e;
        } finally {
            try {
                this.getConn().close();
            } catch (Exception e) {

            }
        }

        return null;
    }

    @Override
    public String exeOperation() throws Exception {
        return null;
    }

    public String innerRun() throws Exception {
        String msg;
        try {
            this.prepareData();

            IOperation operation = new Operation(this.getCommand(), this.updateLink, getConn());

            msg = operation.run(this.getResult());
            
            String preCheckMsg = this.preCheck();

            if (preCheckMsg != null) {
                throw new Exception(preCheckMsg);
            }

            //this.postCheck();

        } catch (Exception e) {

            this.getConn().rollback();

            throw e;
        }

        return msg;
    }
}
