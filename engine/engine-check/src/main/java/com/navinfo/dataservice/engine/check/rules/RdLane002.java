package com.navinfo.dataservice.engine.check.rules;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneTopology;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneVia;
import com.navinfo.dataservice.engine.check.core.baseRule;
/**
 * 车信	html	RDLANE002	后台	
 * 线线车信必须有经过线
 * @author zhangxiaoyi
 *新增车信服务端前检查RdLaneConnexity
 *修改车信服务端前检查RdLaneTopology
 */
public class RdLane002 extends baseRule {

	public RdLane002() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		for(IRow obj : checkCommand.getGlmList()){
			//create的时候只有主表对象，其中包含的内容涵盖子表内容，可直接用
			if (obj instanceof RdLaneConnexity){//交限
				RdLaneConnexity laneObj=(RdLaneConnexity) obj;
				checkRdLaneConnexity(laneObj);
			}
			//修改车信
			else if(obj instanceof RdLaneTopology){
				RdLaneTopology rdLaneTopology = (RdLaneTopology)obj;
				checkRdLaneTopology(rdLaneTopology,checkCommand);
			}
			//修改车信
			else if(obj instanceof RdLaneVia){
				RdLaneVia rdLaneVia = (RdLaneVia)obj;
				checkRdLaneVia(rdLaneVia,checkCommand);
			}
		}
	}
	/**
	 * @param rdLaneVia
	 * @param checkCommand 
	 * @throws SQLException 
	 */
	private void checkRdLaneVia(RdLaneVia rdLaneVia, CheckCommand checkCommand) throws SQLException {
		if(rdLaneVia.status().equals(ObjStatus.DELETE)){
			StringBuilder sb = new StringBuilder();

			sb.append("SELECT COUNT(1) NUM FROM RD_LANE_TOPOLOGY T,RD_LANE_VIA V ");
			sb.append("WHERE T.TOPOLOGY_ID = " + rdLaneVia.getTopologyId());
			sb.append("AND T.TOPOLOGY_ID = V.TOPOLOGY_ID");
			sb.append("AND T.U_RECORD <> 2");
			sb.append("AND V.U_RECORD <> 2");
			sb.append("AND V.LINK_PID <> " + rdLaneVia.getLinkPid());

			String sql = sb.toString();
			log.info("RdLaneVia前检查RdLane002:" + sql);
			
			int num = 0;
			PreparedStatement pstmt = this.getConn().prepareStatement(sql);	
			ResultSet resultSet = pstmt.executeQuery();
			
			if (resultSet.next()){
				num = resultSet.getInt("NUM");
			} 
			resultSet.close();
			pstmt.close();
			
			if(num==0){
				for(IRow objInnerLoop : checkCommand.getGlmList()){
					if(objInnerLoop instanceof RdLaneTopology){
						RdLaneTopology rdLaneTopology = (RdLaneTopology)objInnerLoop;
						if(rdLaneTopology.getPid() == rdLaneVia.getTopologyId()){
							if((!rdLaneTopology.status().equals(ObjStatus.DELETE))&&(rdLaneTopology.getRelationshipType()==2)){
								this.setCheckResult("", "", 0);
								return;
							}
						}

					}
				}
			}
		}
	}

	/**
	 * @param rdLaneTopology
	 * @param checkCommand 
	 * @throws Exception 
	 */
	private void checkRdLaneTopology(RdLaneTopology rdLaneTopology, CheckCommand checkCommand) throws Exception {
		//新增联通关系
		if(rdLaneTopology.status().equals(ObjStatus.INSERT)){
			if(rdLaneTopology.getRelationshipType()==2){
				int viaNum = 0;
				for(IRow objInnerLoop : checkCommand.getGlmList()){
					if(objInnerLoop instanceof RdLaneVia){
						RdLaneVia rdLaneVia = (RdLaneVia)objInnerLoop;
						//排除删除的经过线
						if(rdLaneVia.status().equals(ObjStatus.DELETE)){
							continue;
						}
						if(rdLaneVia.getTopologyId()==rdLaneTopology.getPid()){
							viaNum ++;
						}
					}
				}
				if(viaNum==0){
					this.setCheckResult("", "", 0);
					return;
				}
			}
		}
		//修改联通关系
		else if(rdLaneTopology.status().equals(ObjStatus.UPDATE)){
			int viaNum = rdLaneTopology.getVias().size();
			if(rdLaneTopology.getRelationshipType()==2){
				for(IRow objInnerLoop : checkCommand.getGlmList()){
					if(objInnerLoop instanceof RdLaneVia){
						RdLaneVia rdLaneVia = (RdLaneVia)objInnerLoop;
						//删除的经过线
						if(rdLaneVia.status().equals(ObjStatus.DELETE)){
							if(rdLaneVia.getTopologyId()==rdLaneTopology.getPid()){
								viaNum --;
							}
							continue;
						}
						//新增的经过线
						if(rdLaneVia.status().equals(ObjStatus.INSERT)){
							if(rdLaneVia.getTopologyId()==rdLaneTopology.getPid()){
								viaNum ++;
							}
							continue;
						}
					}
				}
				
				if(viaNum==0){
					this.setCheckResult("", "", 0);
					return;
				}
			}
		}
		
	}

	/**
	 * @param laneObj
	 */
	private void checkRdLaneConnexity(RdLaneConnexity laneObj) {
		Map<String, Object> changedFields=laneObj.changedFields();
		//新增执行该检查
		if(changedFields!=null && !changedFields.isEmpty()){return;}
		for(IRow topo:laneObj.getTopos()){
			RdLaneTopology topoObj=(RdLaneTopology) topo;
			if(topoObj.getRelationshipType()==2){
				List<IRow> vias=topoObj.getVias();
				if(vias==null||vias.size()==0){
					this.setCheckResult("", "", 0);
					return;
				}
			}
		}
		
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
