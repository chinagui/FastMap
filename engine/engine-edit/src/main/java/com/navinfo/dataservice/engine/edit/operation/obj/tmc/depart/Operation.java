package com.navinfo.dataservice.engine.edit.operation.obj.tmc.depart;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdTmclocation;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdTmclocationLink;

/**
 * 上下线分离维护TMC信息
 * 
 * @ClassName: Operation
 * @author Zhang Xiaolong
 * @date 2016年12月20日 上午10:46:25
 * @Description: TODO
 */
public class Operation {
	private Connection conn;

	public Operation(Connection conn) {
		this.conn = conn;
	}

	/**
	 * 维护上下线分离对TMC的影响
	 *
	 * @param links
	 *            分离线
	 * @param leftLinks
	 *            分离后左线
	 * @param rightLinks
	 *            分离后右线
	 * @param noTargetLinks
	 * @param result
	 *            结果集
	 * @return
	 * @throws Exception
	 */
	public String updownDepart(List<RdLink> links, Map<Integer, RdLink> leftLinks, Map<Integer, RdLink> rightLinks,
			Map<Integer, RdLink> noTargetLinks, Result result) throws Exception {
		for (RdLink link : links) {
			updateTmcLocation(link.getTmclocations(), link, leftLinks.get(link.getPid()), rightLinks.get(link.getPid()),
					result);
		}
		return "";
	}

	/**
	 * 上下线分离更新tmc匹配关系
	 * 
	 * @param tmcLocations
	 * @param originLink
	 * @param leftLink
	 * @param rightLink
	 * @param result
	 */
	private void updateTmcLocation(List<IRow> tmcLocationLinks, RdLink originLink, RdLink leftLink, RdLink rightLink,
			Result result) {
		int originLinkPid = originLink.getPid();

		//创建link时已经把子表信息复制到新link中，清空后再操作
		leftLink.getTmclocations().clear();
		rightLink.getTmclocations().clear();

		for (IRow row : tmcLocationLinks) {
			RdTmclocationLink link = (RdTmclocationLink) row;

			// 找到原link的tmc信息，赋值给分离后与TMC方向相同的link
			if (link.getLinkPid() == originLinkPid) {

				// 如果tmc的同行方向为顺方向，自动维护右侧分离线
				if (link.getDirect() == 1) {
					link.changedFields().put("linkPid", rightLink.getPid());
					result.insertObject(link, ObjStatus.UPDATE, link.getGroupId());
				}

				// 如果tmc的同行方向为逆方向，自动维护左侧分离线,方向关系维护为
				else if (link.getDirect() == 2) {
					link.changedFields().put("linkPid", leftLink.getPid());
					link.changedFields().put("direct", 1);
					result.insertObject(link, ObjStatus.UPDATE, link.getGroupId());
				}
			}
		}
	}

}
