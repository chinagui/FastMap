package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.directroute.RdDirectroute;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneTopology;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionDetail;
import com.navinfo.dataservice.dao.glm.model.rd.voiceguide.RdVoiceguide;
import com.navinfo.dataservice.dao.glm.model.rd.voiceguide.RdVoiceguideDetail;
import com.navinfo.dataservice.dao.glm.selector.rd.directroute.RdDirectrouteSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.laneconnexity.RdLaneConnexitySelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.restrict.RdRestrictionSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.voiceguide.RdVoiceguideSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.CoordinateSequenceComparator;
import com.vividsolutions.jts.geom.CoordinateSequenceFilter;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryComponentFilter;
import com.vividsolutions.jts.geom.GeometryFilter;
import com.vividsolutions.jts.geom.util.GeometryTransformer;

/**
 * 交限 html GLM26017 后台 如果交限、语音引导、顺行进入线和退出线挂接在同一点上，而且这个点未登记路口（不属于任何路口），则不允许制作和修改
 * 
 * @author zhangxiaoyi
 * 
 */
public class GLM26017 extends baseRule {

	public GLM26017() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		checkCore(checkCommand);
	}
	
	private void checkCore(CheckCommand checkCommand) throws Exception {
		for (IRow obj : checkCommand.getGlmList()) {
			// create的时候只有主表对象，其中包含的内容涵盖子表内容，可直接用
			if (obj instanceof RdRestriction) {// 交限
				RdRestriction restriObj = (RdRestriction) obj;
				Map<String, Object> changedFields = restriObj.changedFields();
				// 新增执行该检查
				if (changedFields != null && !changedFields.isEmpty()) {
					continue;
				}
				List<Integer> outLinkPidList = new ArrayList<Integer>();
				for (IRow detail : restriObj.getDetails()) {
					outLinkPidList.add(((RdRestrictionDetail) detail)
							.getOutLinkPid());
				}
				RdLinkSelector linkSelector = new RdLinkSelector(getConn());
				List<IRow> linkObjList = linkSelector.loadByIds(outLinkPidList,
						false, false);
				boolean hasSameNode = false;
				int nodePid = restriObj.getNodePid();
				RdLink link=new RdLink();
				for (IRow linkTmp : linkObjList) {
					RdLink linkObj = (RdLink) linkTmp;
					if (nodePid == linkObj.getsNodePid()
							|| nodePid == linkObj.geteNodePid()) {
						hasSameNode = true;
						link=linkObj;
						break;
					}
				}
				// 进入线和退出线挂接在同一点上，而且这个点未登记路口
				if (hasSameNode && !isCrossNode(nodePid)) {
					this.setCheckResult(GeoTranslator.transform(link.getGeometry(), 0.00001, 5), "[RD_NODE,"+nodePid+"]", link.getMeshId(),"交限进入线和退出线挂接在同一点上，而且这个点未登记路口");
					return;
				}
			} else if (obj instanceof RdDirectroute) {// 顺行
				RdDirectroute routeObj = (RdDirectroute) obj;
				Map<String, Object> changedFields = routeObj.changedFields();
				// 获取退出线,新增/修改
				int outLinkPid = routeObj.getOutLinkPid();
				if (!changedFields.isEmpty()) {
					if (changedFields.containsKey("outLinkPid")) {
						outLinkPid = (int) changedFields.get("outLinkPid");
					}
				}

				RdLinkSelector linkSelector = new RdLinkSelector(getConn());
				RdLink linkObj = (RdLink) linkSelector.loadByIdOnlyRdLink(
						outLinkPid, false);
				boolean hasSameNode = false;
				int nodePid = routeObj.getNodePid();
				if (nodePid == linkObj.getsNodePid()
						|| nodePid == linkObj.geteNodePid()) {
					hasSameNode = true;
					break;
				}
				// 进入线和退出线挂接在同一点上，而且这个点未登记路口
				if (hasSameNode && !isCrossNode(nodePid)) {
					this.setCheckResult(GeoTranslator.transform(linkObj.getGeometry(), 0.00001, 5), "[RD_NODE,"+nodePid+"]", linkObj.getMeshId(),"顺行进入线和退出线挂接在同一点上，而且这个点未登记路口");
					return;
				}
			} else if (obj instanceof RdVoiceguide) {// 语音引导
				RdVoiceguide guideObj = (RdVoiceguide) obj;
				Map<String, Object> changedFields = guideObj.changedFields();
				// 新增执行该检查
				if (changedFields != null && !changedFields.isEmpty()) {
					continue;
				}
				List<Integer> outLinkPidList = new ArrayList<Integer>();
				for (IRow detail : guideObj.getDetails()) {
					outLinkPidList.add(((RdVoiceguideDetail) detail)
							.getOutLinkPid());
				}
				RdLinkSelector linkSelector = new RdLinkSelector(getConn());
				List<IRow> linkObjList = linkSelector.loadByIds(outLinkPidList,
						false, false);
				boolean hasSameNode = false;
				int nodePid = guideObj.getNodePid();
				RdLink link=new RdLink();
				for (IRow linkTmp : linkObjList) {
					RdLink linkObj = (RdLink) linkTmp;
					if (nodePid == linkObj.getsNodePid()
							|| nodePid == linkObj.geteNodePid()) {
						hasSameNode = true;
						link=linkObj;
						break;
					}
				}
				// 进入线和退出线挂接在同一点上，而且这个点未登记路口
				if (hasSameNode && !isCrossNode(nodePid)) {
					this.setCheckResult(GeoTranslator.transform(link.getGeometry(), 0.00001, 5), "[RD_NODE,"+nodePid+"]", link.getMeshId(),"语音引导进入线和退出线挂接在同一点上，而且这个点未登记路口");
					return;
				}
			}
		}
	}

	/**
	 * 判断点是否制作了路口
	 * 
	 * @param nodePid
	 * @return 路口点 true；非路口点 false
	 * @throws Exception
	 */
	private boolean isCrossNode(int nodePid) throws Exception {
		String sql = "SELECT 1 FROM RD_CROSS_NODE CN WHERE CN.U_RECORD!=2 AND CN.NODE_PID = "
				+ nodePid;
		DatabaseOperator operator = new DatabaseOperator();
		List<Object> result = operator.exeSelect(getConn(), sql);
		if (result != null && result.size() > 0) {
			return true;
		}
		return false;
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		checkCore(checkCommand);
	}
}
