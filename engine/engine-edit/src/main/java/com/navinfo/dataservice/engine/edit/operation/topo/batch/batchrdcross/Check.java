package com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdcross;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCrossNode;

import java.util.List;

/**
 * Created by chaixin on 2016/10/14 0014.
 */
public class Check {
    public void isHasRdCross(RdCross cross, List<RdCross> crossList, List<Integer> nodes) throws Exception {
        StringBuffer result = new StringBuffer("道路点");
        boolean flag = false;
        for (RdCross c : crossList) {
            if (c.pid() != cross.pid()) {
                if (!flag) flag = true;
                for (IRow row : c.getNodes()) {
                    Integer nodePid = ((RdCrossNode) row).getNodePid();
                    if (nodes.contains(nodePid)) {
                        result.append(nodePid).append("、");
                    }
                }
            }
        }
        String excep = result.substring(0, result.length() - 1);
        excep += "已存在路口，请重新选择.";
        if (flag) {
            throw new Exception(excep);
        }
    }
}
