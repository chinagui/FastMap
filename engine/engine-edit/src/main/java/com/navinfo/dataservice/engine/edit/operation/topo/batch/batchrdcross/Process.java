package com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdcross;

import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.selector.rd.cross.RdCrossSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chaixin on 2016/10/12 0012.
 */
public class Process extends AbstractProcess<Command> {
    public Process() {
    }

    public Process(AbstractCommand command) throws Exception {
        super(command);
    }

//    @Override
//    public String preCheck() throws Exception {
//        RdCrossSelector selector = new RdCrossSelector(super.getConn());
//        List<RdCross> crossList = selector.loadRdCrossByNodeOrLink(super.getCommand().getNodePids(), new ArrayList<Integer>(), true);
//        Check check = new Check();
//        check.isHasRdCross(super.getCommand().getRdCross(), crossList, super.getCommand().getNodePids());
//        return super.preCheck();
//    }

    @Override
    public boolean prepareData() throws Exception {
        // 加载RdCross数据
        RdCrossSelector selector = new RdCrossSelector(super.getConn());
        RdCross cross = (RdCross) selector.loadById(super.getCommand().getPid(), true);
        super.getCommand().setRdCross(cross);
        return super.prepareData();
    }

    @Override
    public String exeOperation() throws Exception {
        return new Operation(super.getCommand(), super.getConn()).run(super.getResult());
    }
}
