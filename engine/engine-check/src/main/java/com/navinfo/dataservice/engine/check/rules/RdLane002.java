package com.navinfo.dataservice.engine.check.rules;

import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneTopology;
import com.navinfo.dataservice.engine.check.core.baseRule;
/**
 * 车信	html	RDLANE002	后台	
 * 线线车信必须有经过线
 * @author zhangxiaoyi
 *
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
				Map<String, Object> changedFields=laneObj.changedFields();
				//新增执行该检查
				if(changedFields!=null && !changedFields.isEmpty()){continue;}
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
			}}
	}
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
