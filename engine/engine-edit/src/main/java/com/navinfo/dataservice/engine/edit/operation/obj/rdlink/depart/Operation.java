package com.navinfo.dataservice.engine.edit.operation.obj.rdlink.depart;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkRtic;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

/**
 * Created by chaixin on 2016/10/8 0008.
 */
public class Operation {
    private Connection conn;

    public Operation(Connection conn) {
        this.conn = conn;
    }

    /**
     * 维护上下线分离对RTIC的影响
     *
     * @param links      分离线
     * @param leftLinks  分离后左线
     * @param rightLinks 分离后右线
     * @param result     结果集
     * @return
     * @throws Exception
     */
    public String updownDepart(List<RdLink> links, Map<Integer, RdLink> leftLinks, Map<Integer, RdLink> rightLinks, Result result) throws Exception {
        for (RdLink link : links) {
            int direct = link.getDirect();
			if (1 == direct) {
				this.updateTwoDirection(link.getRtics(),
						leftLinks.get(link.pid()), rightLinks.get(link.pid()),
						result);
			} else if (2 == direct || 3 == direct) {
				this.updateSingleDirection(link.getRtics(),
						rightLinks.get(link.pid()), result);
			}
        }
        return "";
    }

    // 处理单方向link
    private void updateSingleDirection(List<IRow> rtics, RdLink link, Result result) {
        for (IRow row : rtics) {        	
        	
			RdLinkRtic insertRtic = new RdLinkRtic();

			insertRtic.copy(row);

			insertRtic.setLinkPid(link.pid());
        	
            result.insertObject(insertRtic, ObjStatus.INSERT, link.pid());
        }
    }

    // 处理双方向link
    private void updateTwoDirection(List<IRow> rtics, RdLink leftLink, RdLink rightLink, Result result) {
        for (IRow row : rtics) {
            RdLinkRtic rtic = (RdLinkRtic) row;
            int updownFlag = rtic.getUpdownFlag();
            if (1 == updownFlag) {
                rtic.setLinkPid(rightLink.pid());
            } else if (2 == updownFlag) {
                rtic.setUpdownFlag(1);
                rtic.setLinkPid(leftLink.pid());
            }
            result.insertObject(rtic, ObjStatus.INSERT, rtic.getLinkPid());
        }
    }
}
