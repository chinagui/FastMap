package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.directroute.RdDirectroute;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneTopology;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionDetail;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionVia;
import com.navinfo.dataservice.dao.glm.model.rd.voiceguide.RdVoiceguide;
import com.navinfo.dataservice.dao.glm.model.rd.voiceguide.RdVoiceguideDetail;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/**
 * 交限 html GLM26017 后台 如果交限、语音引导、顺行进入线和退出线挂接在同一点上，而且这个点未登记路口（不属于任何路口），则不允许制作和修改
 * 
 * @author zhangxiaoyi
 * 如果交限、语音引导、顺行进入线和退出线挂接在同一点上，而且这个点未登记路口（不属于任何路口），则不允许制作和修改
 * 新增顺行
 * 新增语音引导
 * 新增卡车交限
 * 修改卡车交限
 * 修改交限
 * 新增交限？？
 */
public class GLM26017 extends baseRule {
	protected Logger log = Logger.getLogger(this.getClass());
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
				checkRdRestriction(restriObj);
			}else if(obj instanceof RdRestrictionDetail) {
				RdRestrictionDetail restriObj = (RdRestrictionDetail) obj;
				checkRdRestrictionDetail(restriObj);
			}
			else if (obj instanceof RdDirectroute) {// 顺行
				RdDirectroute routeObj = (RdDirectroute) obj;
				checkRdDirectroute(routeObj);
			} else if (obj instanceof RdVoiceguide) {// 语音引导
				RdVoiceguide guideObj = (RdVoiceguide) obj;
				checkRdVoiceguide(guideObj);
			}
		}
	}

	/**
	 * @param restriObj
	 * @throws Exception 
	 */
	private void checkRdRestrictionDetail(RdRestrictionDetail rdRestrictionDetail) throws Exception {
		//是否触发检查条件：新增退出线、修改退出线
		int outLinkPid = 0; 
		if(rdRestrictionDetail.status().equals(ObjStatus.INSERT)){
			outLinkPid = rdRestrictionDetail.getOutLinkPid();
		}
		else if(rdRestrictionDetail.status().equals(ObjStatus.UPDATE)){
			if(rdRestrictionDetail.changedFields().containsKey("outLinkPid")){
				outLinkPid = Integer.parseInt(rdRestrictionDetail.changedFields().get("outLinkPid").toString());
			}
		}
		
		if(outLinkPid!=0){
			StringBuilder sb = new StringBuilder();

			sb.append("SELECT 1 FROM RD_RESTRICTION_DETAIL RRD ,RD_RESTRICTION RR,RD_LINK R");
			sb.append(" WHERE RRD.RESTRIC_PID = " + rdRestrictionDetail.getRestricPid());
			sb.append(" AND RR.PID = RRD.RESTRIC_PID");
			sb.append(" AND R.LINK_PID = " + outLinkPid);
			sb.append(" AND (R.S_NODE_PID = RR.NODE_PID OR R.E_NODE_PID = RR.NODE_PID)");
			sb.append(" AND R.U_RECORD <> 2");
			sb.append(" AND RRD.U_RECORD <> 2");
			sb.append(" AND RR.U_RECORD <> 2");
			sb.append(" AND NOT EXISTS (SELECT 1 FROM RD_CROSS_NODE RCN WHERE RCN.NODE_PID = RR.NODE_PID AND RCN.U_RECORD <> 2)");

			String sql = sb.toString();
			log.info("RdRestrictionDetail前检查GLM26017:" + sql);

			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);

			if(resultList.size()>0){
				this.setCheckResult("", "", 0);
			}

		}
		
	}

	/**
	 * @param guideObj
	 * @throws Exception 
	 */
	private void checkRdVoiceguide(RdVoiceguide guideObj) throws Exception {
		Map<String, Object> changedFields = guideObj.changedFields();
		// 新增执行该检查
		if (changedFields != null && !changedFields.isEmpty()) {
			return;
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
			this.setCheckResult(GeoTranslator.transform(link.getGeometry(), 0.00001, 5), "[RD_NODE,"+nodePid+"]", link.getMeshId(),"如果语音引导进入线和退出线挂接在同一点上，而且这个点未登记路口（不属于任何路口），则不允许制作和修改");
			return;
		}
		
	}

	/**
	 * @param routeObj
	 * @throws Exception 
	 */
	private void checkRdDirectroute(RdDirectroute routeObj) throws Exception {
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
		}
		// 进入线和退出线挂接在同一点上，而且这个点未登记路口
		if (hasSameNode && !isCrossNode(nodePid)) {
			this.setCheckResult(GeoTranslator.transform(linkObj.getGeometry(), 0.00001, 5), "[RD_NODE,"+nodePid+"]", linkObj.getMeshId(),"如果顺行进入线和退出线挂接在同一点上，而且这个点未登记路口（不属于任何路口），则不允许制作和修改");
			return;
		}
		
	}

	/**
	 * @param restriObj
	 * @throws Exception 
	 */
	private void checkRdRestriction(RdRestriction restriObj) throws Exception {
		Map<String, Object> changedFields = restriObj.changedFields();
		// 新增执行该检查
		if (changedFields != null && !changedFields.isEmpty()) {
			return;
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
			this.setCheckResult(GeoTranslator.transform(link.getGeometry(), 0.00001, 5), "[RD_NODE,"+nodePid+"]", link.getMeshId(),"如果交限进入线和退出线挂接在同一点上，而且这个点未登记路口（不属于任何路口），则不允许制作和修改");
			return;
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
		//checkCore(checkCommand);
		for(IRow row:checkCommand.getGlmList()){
			//车信关系类型编辑
			if (row instanceof RdLaneTopology){
				RdLaneTopology rdLaneTopology = (RdLaneTopology) row;
				checkRdLaneTopology(rdLaneTopology);
			}
		}
	}
	
	/**
	 * @author Han Shaoming
	 * @param rdLaneConnexity
	 * @throws Exception 
	 */
	private void checkRdLaneTopology(RdLaneTopology rdLaneTopology) throws Exception {
		// TODO Auto-generated method stub
		//修改车信,触发检查
		StringBuilder sb = new StringBuilder();
		
		sb.append("WITH T AS (SELECT RL.S_NODE_PID NODE_PID,RLT.CONNEXITY_PID");
		sb.append(" FROM RD_LANE_TOPOLOGY RLT, RD_LINK RL WHERE RLT.OUT_LINK_PID = RL.LINK_PID AND RLT.U_RECORD <>2");
		sb.append(" AND RL.U_RECORD <>2 AND RLT.TOPOLOGY_ID="+rdLaneTopology.getPid());
		sb.append(" UNION SELECT RL.E_NODE_PID NODE_PID,RLT.CONNEXITY_PID");
		sb.append(" FROM RD_LANE_TOPOLOGY RLT, RD_LINK RL WHERE RLT.OUT_LINK_PID = RL.LINK_PID");
		sb.append(" AND RLT.U_RECORD <>2 AND RL.U_RECORD <>2 AND RLT.TOPOLOGY_ID="+rdLaneTopology.getPid()+")");
		sb.append(" SELECT DISTINCT RLC.PID FROM RD_LANE_CONNEXITY RLC,T");
		sb.append(" WHERE RLC.PID=T.CONNEXITY_PID AND RLC.U_RECORD <> 2 AND NOT EXISTS");
		sb.append(" (SELECT 1 FROM RD_CROSS_NODE CN WHERE CN.NODE_PID = RLC.NODE_PID AND CN.U_RECORD <> 2)");
		
		String sql = sb.toString();
		log.info("RdLaneConnexity后检查GLM26017--sql:" + sql);
		
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);
		
		if(!resultList.isEmpty()){
			String target = "[RD_LANE_CONNEXITY," + resultList.get(0).toString() + "]";
			this.setCheckResult("", target, 0,"如果车信进入线和退出线挂接在同一点上，而且这个点未登记路口（不属于任何路口），则不允许制作和修改");
		}
	}
}
