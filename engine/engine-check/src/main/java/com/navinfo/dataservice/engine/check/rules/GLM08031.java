package com.navinfo.dataservice.engine.check.rules;

import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionDetail;
import com.navinfo.dataservice.engine.check.core.baseRule;
/**
 * 交限	html	GLM08031	后台	路口交限里不允许有经过线信息
 * @author zhangxiaoyi
 *
 */
public class GLM08031 extends baseRule {

	public GLM08031() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		for(IRow obj:checkCommand.getGlmList()){
			//新增交限检查
			if(obj instanceof RdRestriction ){
				RdRestriction restriObj=(RdRestriction) obj;
				List<IRow> details=restriObj.getDetails();
				for(IRow objTmp:details){
					RdRestrictionDetail detailObj=(RdRestrictionDetail) objTmp;
					if(detailObj.getRelationshipType()==1){
						List<IRow> vias=detailObj.getVias();
						if(vias!=null&&vias.size()>0){
							//if(true){return;}
							this.setCheckResult("", "", 0);
							return;
						}
					}
				}
			}
		}
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
