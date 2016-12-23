package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.alibaba.dubbo.common.logger.Logger;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneTopology;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
 * @ClassName: RELATING_CHECK_CROSS_RELATION_MUST_CONNECTED1
 * @author songdongyan
 * @date 2016年12月21日
 * @Description: 如果车信进入线和退出线挂接在同一点上，而且这个点未登记路口（不属于任何路口），则不允许制作和修改
 * 同一组车信，同一进入点可能即直接挂接一个退出link,又不直接挂接一个线线退出link.
 * 一组车信，只要有与进入node直接挂接的退出link，并且该node没有制作路口，则报log
 * 新增车信服务端前检查RdLaneConnexity
 * 修改车信服务端前检查:修改车信RdLaneTopology
 * 
 */
public class RELATING_CHECK_CROSS_RELATION_MUST_CONNECTED1 extends baseRule{

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#preCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		for(IRow obj : checkCommand.getGlmList()){
			//新增车信
			if (obj instanceof RdLaneConnexity){
				RdLaneConnexity rdLaneConnexity = (RdLaneConnexity)obj;
				checkRdLaneConnexity(rdLaneConnexity,checkCommand.getOperType());
			}
			//修改车信退出线
			else if (obj instanceof RdLaneTopology){
				RdLaneTopology rdLaneTopology = (RdLaneTopology)obj;
				checkRdLaneTopology(rdLaneTopology,checkCommand.getOperType());
			}
		}
		
	}

	/**
	 * @param rdLaneTopology
	 * @param operType
	 * @throws Exception 
	 */
	private void checkRdLaneTopology(RdLaneTopology rdLaneTopology, OperType operType) throws Exception {
		//修改车信退出线
		int outLinkPid = rdLaneTopology.getOutLinkPid();
		int pid = rdLaneTopology.getConnexityPid();
		StringBuilder sb = new StringBuilder();

		sb.append("SELECT 1 FROM RD_LANE_CONNEXITY C, RD_LINK L");
		sb.append(" WHERE C.PID =" + pid);
		sb.append(" AND L.LINK_PID = " + outLinkPid);
		sb.append(" AND (L.S_NODE_PID = C.NODE_PID OR L.E_NODE_PID = C.NODE_PID)");
		sb.append(" AND C.U_RECORD <> 2");
		sb.append(" AND L.U_RECORD <> 2");
		sb.append(" AND NOT EXISTS (SELECT 1 FROM RD_CROSS_NODE N");
		sb.append(" WHERE N.NODE_PID = C.NODE_PID");
		sb.append(" AND N.U_RECORD <> 2)");
		

		String sql = sb.toString();
		log.info("RdLaneTopology前检查RELATING_CHECK_CROSS_RELATION_MUST_CONNECTED1:" + sql);

		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if(resultList.size()>0){
			this.setCheckResult("", "", 0);
		}
		
	}

	/**
	 * @param rdLaneConnexity
	 * @param operType 
	 * @throws Exception 
	 */
	private void checkRdLaneConnexity(RdLaneConnexity rdLaneConnexity, OperType operType) throws Exception {
		//新增车信RdLaneConnexity
		if(rdLaneConnexity.status().equals(ObjStatus.INSERT)){
			int inNodePid = rdLaneConnexity.getNodePid();
			Set<Integer> outLinkPids = new HashSet<Integer>();
			for(IRow rdLaneTopology:rdLaneConnexity.getTopos()){
				outLinkPids.add(((RdLaneTopology) rdLaneTopology).getOutLinkPid());
			}
			StringBuilder sb = new StringBuilder();

			sb.append("SELECT 1 FROM RD_LINK R");
			sb.append(" WHERE R.LINK_PID IN (" + StringUtils.join(outLinkPids.toArray(),",") + ")");
			sb.append(" AND R.U_RECORD <> 2");
			sb.append(" AND (R.S_NODE_PID = " + inNodePid);
			sb.append(" OR R.E_NODE_PID = " + inNodePid +")");
			sb.append(" AND NOT EXISTS (SELECT 1 FROM RD_CROSS_NODE N");
			sb.append(" WHERE N.NODE_PID = " + inNodePid);
			sb.append(" AND N.U_RECORD <> 2)");
			

			String sql = sb.toString();
			log.info("RdLaneConnexity前检查RELATING_CHECK_CROSS_RELATION_MUST_CONNECTED1:" + sql);

			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);

			if(resultList.size()>0){
				this.setCheckResult("", "", 0);
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#postCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
