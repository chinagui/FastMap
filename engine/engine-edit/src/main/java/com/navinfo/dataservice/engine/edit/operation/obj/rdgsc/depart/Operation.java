package com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.depart;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGscLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.gsc.RdGscLinkSelector;
import com.navinfo.dataservice.engine.edit.utils.RdGscOperateUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import java.sql.Connection;
import java.util.List;

/**
 * 用于维护节点分离对立交的影响
 * Created by chaixin on 2016/9/20 0020.
 */
public class Operation {

    private Connection conn;

    public Operation(Connection conn) {
        this.conn = conn;
    }

    public void depart(int nodePid, RdLink oldLink, List<RdLink> newLinks, Result result) throws Exception {
        // 加载RdGscLinkSelector
        RdGscLinkSelector selector = new RdGscLinkSelector(this.conn);
        // 查找oldLink上挂接的所有立交
        List<RdGscLink> rdGscLinks = selector.loadByLinkId(oldLink.pid(), true);
        // 当分离不跨图幅时
        if (newLinks.size() == 1) {
            Coordinate[] linkGeo = newLinks.get(0).getGeometry().getCoordinates();
            for (RdGscLink rdGscLink : rdGscLinks) {
                // 当形状点为起点、终点时不处理
                if (rdGscLink.getStartEnd() != 0) throw new Exception("有立交不允许删除,移动,修行操作,请先解除立交");
                // 重新计算形状点号并更新
                Coordinate coor = oldLink.getGeometry().getCoordinates()[rdGscLink.getShpSeqNum()];
                Geometry gscGeo = GeoTranslator.transform(GeoTranslator.point2Jts(coor.x, coor.y), 0.00001, 5);
                RdGscOperateUtils.calShpSeqNum(rdGscLink, gscGeo, linkGeo);
                result.insertObject(rdGscLink, ObjStatus.UPDATE, rdGscLink.getPid());
            }
            // 当分离后线跨越图幅
        } else if (newLinks.size() > 1) {
            for (RdGscLink rdGscLink : rdGscLinks) {
                // 当形状点为起点、终点时不处理
                if (rdGscLink.getStartEnd() != 0) throw new Exception("有立交不允许删除,移动,修行操作,请先解除立交");
                Coordinate coor = oldLink.getGeometry().getCoordinates()[rdGscLink.getShpSeqNum()];
                Geometry gscGeo = GeoTranslator.point2Jts(coor.x, coor.y);
                // 判断形状点在新生成的哪条link上
                // 重新计算形状点号并更新
                for (RdLink link : newLinks) {
                    if (link.getGeometry().distance(gscGeo) <= 1) {
                        RdGscOperateUtils.calShpSeqNum(rdGscLink, gscGeo, link.getGeometry().getCoordinates());
                        result.insertObject(rdGscLink, ObjStatus.UPDATE, rdGscLink.getPid());
                        break;
                    }
                }
            }
        }

    }
}
