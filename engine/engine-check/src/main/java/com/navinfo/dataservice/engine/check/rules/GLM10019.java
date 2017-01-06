package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.slope.RdSlope;
import com.navinfo.dataservice.dao.glm.model.rd.slope.RdSlopeVia;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.graph.LinksContainClosedLoop;

/**
 * @ClassName: GLM10019
 * @author songdongyan
 * @date 2016年8月24日
 * @Description: 同一个坡度的所有相关link均不能形成闭合环
 */
public class GLM10019 extends baseRule {

	/**
	 * 
	 */
	public GLM10019() {
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.navinfo.dataservice.engine.check.core.baseRule#preCheck(com.navinfo
	 * .dataservice.dao.check.CheckCommand)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		for (IRow obj : checkCommand.getGlmList()) {
			// 获取新建RdBranch信息
			if (obj instanceof RdSlope) {
				RdSlope rdSlope = (RdSlope) obj;
				// 新增信息
				// 接续线信息
				RdLinkSelector rdLinkSelector = new RdLinkSelector(
						this.getConn());
				List<IRow> viaLinks = new ArrayList<IRow>();
				if (rdSlope.getSlopeVias() != null) {
					for (IRow row : rdSlope.getSlopeVias()) {
						RdSlopeVia slopeVia = (RdSlopeVia) row;
						viaLinks.add((RdLink) rdLinkSelector
								.loadByIdOnlyRdLink(slopeVia.getLinkPid(),
										false));
					}
				}
				// 获取退出线信息
				int outLinkPid = rdSlope.getLinkPid();

				// 修改信息
				Map<String, Object> changedFields = rdSlope.changedFields();
				if (!changedFields.isEmpty()) {
					if (changedFields.containsKey("linkPid")) {
						outLinkPid = (int) changedFields.get("linkPid");
					}
					if (changedFields.containsKey("slopeVias")) {
						viaLinks.clear();
						for (IRow row : (List<IRow>) changedFields
								.get("slopeVias")) {
							RdSlopeVia slopeVia = (RdSlopeVia) row;
							viaLinks.add((RdLink) rdLinkSelector
									.loadByIdOnlyRdLink(slopeVia.getLinkPid(),
											false));
						}
					}
				}

				RdLink outLink = (RdLink) rdLinkSelector.loadByIdOnlyRdLink(
						outLinkPid, false);
				viaLinks.add(outLink);

				LinksContainClosedLoop linksContainClosedLoop = new LinksContainClosedLoop(
						viaLinks);
				if (linksContainClosedLoop.containClosedLoop()) {
					this.setCheckResult("", "", 0);
					return;
				}
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.navinfo.dataservice.engine.check.core.baseRule#postCheck(com.navinfo
	 * .dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub

	}

}
