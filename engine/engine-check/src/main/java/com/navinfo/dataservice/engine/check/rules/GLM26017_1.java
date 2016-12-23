package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.directroute.RdDirectroute;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneTopology;
import com.navinfo.dataservice.dao.glm.model.rd.voiceguide.RdVoiceguide;
import com.navinfo.dataservice.dao.glm.model.rd.voiceguide.RdVoiceguideDetail;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/**
 * @ClassName GLM26017_1
 * @author Han Shaoming
 * @date 2016年12月23日 下午4:43:34
 * @Description TODO
 * 关系类型为“路口”的车信、交限、顺行、路口语音引导关系信息，应制作到登记了路口的点上
 */
public class GLM26017_1 extends baseRule {

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		for(IRow row:checkCommand.getGlmList()){
			//关系类型编辑（语音引导详细信息表）
			if(row instanceof RdVoiceguide){
				RdVoiceguide rdVoiceguide = (RdVoiceguide)row;
				int pid = rdVoiceguide.getPid();
				checkRdVoiceguide(pid);
			}else if(row instanceof RdVoiceguideDetail){
				RdVoiceguideDetail rdVoiceguideDetail = (RdVoiceguideDetail)row;
				int pid = rdVoiceguideDetail.getVoiceguidePid();
				checkRdVoiceguide(pid);
			}
			//关系类型编辑（顺行表）
			else if(row instanceof RdDirectroute){
				RdDirectroute rdDirectroute = (RdDirectroute)row;
				checkRdDirectroute(rdDirectroute);
			}
		}
	}

	

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		for(IRow row:checkCommand.getGlmList()){
			//车信关系类型编辑
			if (row instanceof RdLaneTopology){
				RdLaneTopology rdLaneTopology = (RdLaneTopology) row;
				checkRdLaneTopology(rdLaneTopology);
			}
			//关系类型编辑（语音引导详细信息表）
			else if(row instanceof RdVoiceguide){
				RdVoiceguide rdVoiceguide = (RdVoiceguide)row;
				int pid = rdVoiceguide.getPid();
				checkRdVoiceguide(pid);
			}else if(row instanceof RdVoiceguideDetail){
				RdVoiceguideDetail rdVoiceguideDetail = (RdVoiceguideDetail)row;
				int pid = rdVoiceguideDetail.getVoiceguidePid();
				checkRdVoiceguide(pid);
			}
			//关系类型编辑（顺行表）
			else if(row instanceof RdDirectroute){
				RdDirectroute rdDirectroute = (RdDirectroute)row;
				checkRdDirectroute(rdDirectroute);
			} 
		}
	}
	
	/**
	 * @author Han Shaoming
	 * @param pid
	 * @throws Exception 
	 */
	private void checkRdVoiceguide(int pid) throws Exception {
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder();
		
		sb.append("SELECT V.PID FROM RD_VOICEGUIDE V, RD_VOICEGUIDE_DETAIL VD");
		sb.append(" WHERE V.PID = VD.VOICEGUIDE_PID AND VD.RELATIONSHIP_TYPE = 1 AND V.U_RECORD <> 2");
		sb.append(" AND VD.U_RECORD <> 2 AND V.PID = "+pid+" AND NOT EXISTS");
		sb.append(" (SELECT 1 FROM RD_CROSS_NODE CN WHERE CN.NODE_PID = V.NODE_PID AND CN.U_RECORD <> 2)");
		
		String sql = sb.toString();
		log.info("RdVoiceguide检查GLM26017_1--sql:" + sql);
		
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);
		
		if(!resultList.isEmpty()){
			String target = "[RD_VOICEGUIDE," + (long)resultList.get(0) + "]";
			this.setCheckResult("", target, 0,"关系类型为“路口”的语音引导关系信息，应制作到登记了路口的点上");
		}
	}

	/**
	 * @author Han Shaoming
	 * @param rdDirectroute
	 * @throws Exception 
	 */
	private void checkRdDirectroute(RdDirectroute rdDirectroute) throws Exception {
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder();
		
		sb.append("SELECT D.PID FROM RD_DIRECTROUTE D");
		sb.append(" WHERE D.RELATIONSHIP_TYPE = 1 AND D.U_RECORD <> 2");
		sb.append(" AND D.PID="+rdDirectroute.getPid()+" AND NOT EXISTS");
		sb.append(" (SELECT 1 FROM RD_CROSS_NODE CN WHERE CN.NODE_PID = D.NODE_PID AND CN.U_RECORD <> 2)");
		
		String sql = sb.toString();
		log.info("RdDirectroute检查GLM26017_1--sql:" + sql);
		
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);
		
		if(!resultList.isEmpty()){
			String target = "[RD_DIRECTROUTE," + rdDirectroute.getPid() + "]";
			this.setCheckResult("", target, 0,"关系类型为“路口”的顺行关系信息，应制作到登记了路口的点上");
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
		if(ObjStatus.UPDATE.equals(rdLaneTopology.status())){
			StringBuilder sb = new StringBuilder();
			
			sb.append("SELECT C.PID FROM RD_LANE_CONNEXITY C, RD_LANE_TOPOLOGY T");
			sb.append(" WHERE C.PID = T.CONNEXITY_PID AND T.RELATIONSHIP_TYPE = 1 AND C.U_RECORD <>2");
			sb.append(" AND T.U_RECORD <>2 AND T.TOPOLOGY_ID="+rdLaneTopology.getPid());
			sb.append(" AND NOT EXISTS");
			sb.append(" (SELECT 1 FROM RD_CROSS_NODE CN WHERE CN.NODE_PID = C.NODE_PID AND CN.U_RECORD <> 2)");
			
			String sql = sb.toString();
			log.info("RdLaneConnexity后检查GLM26017_1--sql:" + sql);
			
			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);
			
			if(!resultList.isEmpty()){
				String target = "[RD_LANE_CONNEXITY," + (long)resultList.get(0) + "]";
				this.setCheckResult("", target, 0,"关系类型为“路口”的车信关系信息，应制作到登记了路口的点上");
			}
		}
	}

}
