package com.navinfo.dataservice.engine.check.rules;

import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionDetail;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionVia;
import com.navinfo.dataservice.engine.check.core.baseRule;
/**
 * @author zhangxiaoyi
 * 路口交限里不允许有经过线信息
 * 新增交限服务端前检查 :RdRestriction(insert)
 * 修改交限服务端前检查 :RdRestrictionDetail(insert,update),遍历CheckCommand内RdRestrictionVia统计数量
 * 新增卡车交限服务端前检查:RdRestriction(insert)
 * 修改卡车交限服务端前检查:RdRestrictionDetail(insert,update),遍历CheckCommand内RdRestrictionVia统计数量
 *
 */
public class GLM08031 extends baseRule {

	public GLM08031() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		for(IRow obj:checkCommand.getGlmList()){
			//新增交限/卡车交限
			if(obj instanceof RdRestriction ){
				RdRestriction restriObj=(RdRestriction) obj;
				if(restriObj.status().equals(ObjStatus.INSERT)){
					checkRdRestriction(restriObj);
				}
			}
			//修改交限/卡车交限
			else if(obj instanceof RdRestrictionDetail){
				RdRestrictionDetail rdRestrictionDetail=(RdRestrictionDetail) obj;
				checkRdRestrictionDetail(rdRestrictionDetail,checkCommand);
			}
		}
	}

	/**
	 * @param rdRestrictionDetail
	 * @param checkCommand 
	 */
	private void checkRdRestrictionDetail(RdRestrictionDetail rdRestrictionDetail, CheckCommand checkCommand) {
		int relationshipType = 1;
		//新增/修改交限退出线
		if(!rdRestrictionDetail.status().equals(ObjStatus.DELETE)){
			if(rdRestrictionDetail.changedFields().containsKey("relationshipType")){
				relationshipType = Integer.parseInt(rdRestrictionDetail.changedFields().get("relationshipType").toString());
			}else{
				relationshipType = rdRestrictionDetail.getRelationshipType();
			}
		}
		if(relationshipType==1){
			int viaNum = rdRestrictionDetail.getVias().size();
			for(IRow obj:checkCommand.getGlmList()){
				if(obj instanceof RdRestrictionVia){
					RdRestrictionVia rdRestrictionVia = (RdRestrictionVia)obj;
					if(rdRestrictionVia.getDetailId() == rdRestrictionDetail.getPid()){
						if(rdRestrictionVia.status().equals(ObjStatus.DELETE)){
							viaNum --;
						}else if(rdRestrictionVia.status().equals(ObjStatus.INSERT)){
							viaNum ++;
						}
					}
				}
			}
			if(viaNum!=0){
				this.setCheckResult("", "", 0);
				return;
			}
		}
		
	}

	/**
	 * @param restriObj
	 */
	private void checkRdRestriction(RdRestriction restriObj) {
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

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
