package com.navinfo.dataservice.engine.edit.operation.obj.rdslope.depart;

import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.slope.RdSlope;
import com.navinfo.dataservice.dao.glm.selector.rd.slope.RdSlopeSelector;

import java.sql.Connection;
import java.util.*;

/**
 * Created by chaixin on 2016/10/9 0009.
 */
public class Operation {

    private Connection conn;

    public Operation(Connection conn) {
        this.conn = conn;
    }

    /**
     * 维护上下线分离对坡度的影响
     *
     * @param links      分离线
     * @param leftLinks  分离后左线
     * @param rightLinks 分离后右线
     * @param result     结果集
     * @return
     * @throws Exception
     */
    public String updownDepart(List<RdLink> links, Map<Integer, RdLink> leftLinks, Map<Integer, RdLink> rightLinks, Result result) throws Exception {
        RdSlopeSelector selector = new RdSlopeSelector(conn);
        for (RdLink link : links) {
            Set<RdSlope> slopes = new HashSet<>();
            // 1.当已经参与坡度制作的单线link变为上下线分离时，将整组坡度信息删除
            slopes.addAll(selector.loadByOutLink(link.pid(), true));
            slopes.addAll(selector.loadByViaLink(link.pid(), true));
            if (!slopes.isEmpty()) {
                for (RdSlope slope : slopes)
                    result.insertObject(slope, ObjStatus.DELETE, slope.pid());
                slopes.clear();
            }
            // 2.当目标link上的点已经参与制作坡度
            Set<Integer> tmpNodePids = new HashSet<Integer>();
            tmpNodePids.add(link.getsNodePid());
            tmpNodePids.add(link.geteNodePid());
            List<Integer> nodePids = new ArrayList<>(tmpNodePids).subList(1, tmpNodePids.size() - 1);
            slopes.addAll(selector.loadByNodePids(nodePids, true));
            if (!slopes.isEmpty()) {
                for (RdSlope slope : slopes) {
                    if (!result.getDelObjects().contains(slope))
                        result.insertObject(slope, ObjStatus.DELETE, slope.pid());
                }
                slopes.clear();
            }
        }
        return "";
    }

}
