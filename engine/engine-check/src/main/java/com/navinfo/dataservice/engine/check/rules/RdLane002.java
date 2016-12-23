package com.navinfo.dataservice.engine.check.rules;

import java.util.List;
import java.util.Map;

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
				}
			}
		}
		//修改联通关系
		else if(rdLaneTopology.status().equals(ObjStatus.UPDATE)){
			int viaNum = rdLaneTopology.getVias().size();
//			List<String> viaLinks = new ArrayList<String>();
//			String sql = "SELECT LISTAGG(V.LINK_PID,',') WITHIN GROUP (ORDER BY V.LINK_PID)"
//					+ " FROM RD_LANE_VIA V"
//					+ " WHERE V.TOPOLOGY_ID = " + rdLaneTopology.getPid()
//					+ " AND V.U_RECORD <> 2 ";
//			DatabaseOperator getObj = new DatabaseOperator();
//			List<Object> resultList = new ArrayList<Object>();
//			resultList = getObj.exeSelect(this.getConn(), sql);
//			
//			if(resultList.size()>0){
//				viaLinks = Arrays.asList(resultList.get(0).toString().split(","));
//			}
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
