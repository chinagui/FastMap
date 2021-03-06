package com.navinfo.dataservice.engine.check.rules;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionDetail;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperatorResultWithGeo;

/** 
 * @ClassName: GLM08033
 * @author songdongyan
 * @date 下午3:33:54
 * @Description: GLM08033	路口交限的进入线和退出线不能为交叉口link
 * 新增交限、新增卡车交限：RdRestriction
 * 修改交限、修改卡车交限：RdRestrictionDetail，修改交限类型，交限退出线，或者新增退出线
 */
public class GLM08033 extends baseRule {

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#preCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
						
		for(IRow obj:checkCommand.getGlmList()){
			if(obj instanceof RdRestriction ){
				RdRestriction rdRestriction = (RdRestriction)obj;
				checkRdRestriction(rdRestriction);
			}
			else if(obj instanceof RdRestrictionDetail ){
				RdRestrictionDetail rdRestrictionDetail = (RdRestrictionDetail)obj;
				checkRdRestrictionDetail(rdRestrictionDetail);
			}
		}
				
	}
	/**
	 * @param rdRestrictionDetail
	 * @throws Exception 
	 */
	private void checkRdRestrictionDetail(RdRestrictionDetail rdRestrictionDetail) throws Exception {
		int outLinkPid = 0;
		if(rdRestrictionDetail.status().equals(ObjStatus.INSERT)){
			if(rdRestrictionDetail.getRelationshipType()==1){
				outLinkPid = rdRestrictionDetail.getOutLinkPid();
			}
		}else if(rdRestrictionDetail.status().equals(ObjStatus.UPDATE)){
			//修改了类型或者退出线触发检查
			if(rdRestrictionDetail.changedFields().containsKey("relationshipType")||rdRestrictionDetail.changedFields().containsKey("outLinkPid")){
				int relationshipType = 1;
				if(rdRestrictionDetail.changedFields().containsKey("relationshipType")){
					relationshipType = Integer.parseInt(rdRestrictionDetail.changedFields().get("relationshipType").toString());	
				}else{
					relationshipType = rdRestrictionDetail.getRelationshipType();
				}
				if(relationshipType ==2){
					return;
				}
				if(rdRestrictionDetail.changedFields().containsKey("outLinkPid")){
					outLinkPid = Integer.parseInt(rdRestrictionDetail.changedFields().get("outLinkPid").toString());
				}else{
					outLinkPid = rdRestrictionDetail.getOutLinkPid();
				}
			}
		}
		if(outLinkPid!=0){
			String sql = "select link_pid from rd_link_form "
					+ "where FORM_OF_WAY = 50 AND U_RECORD != 2 and link_pid in =" + outLinkPid;
			log.info("RdRestrictionDetail前检查GLM08033:" + sql);

			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);
			
			if(!resultList.isEmpty()){
				this.setCheckResult("","",0);
			}
		}
	}
	/**
	 * @param rdRestriction
	 * @throws Exception 
	 */
	private void checkRdRestriction(RdRestriction rdRestriction) throws Exception {
		//新增交限/卡车交限
		if(rdRestriction.status().equals(ObjStatus.INSERT)){
			//获取inLinkPid\outLinkPid
			Set<Integer> linkPids = new HashSet<Integer>();
			boolean isCrossRelate=false;				
			for(IRow deObj:rdRestriction.getDetails()){
				if(deObj instanceof RdRestrictionDetail){
					RdRestrictionDetail rdRestrictionDetail = (RdRestrictionDetail)deObj;
					if(rdRestrictionDetail.getRelationshipType()==1){
						isCrossRelate=true;
						linkPids.add(rdRestrictionDetail.getOutLinkPid());
					}
				}
			}
			if(isCrossRelate){
				linkPids.add(rdRestriction.getInLinkPid());
			}
			//为0说明没有符合条件的路口交限，不进行后续查询
			if(linkPids.size()==0){
				return;
			}
			
			String sql = "select link_pid from rd_link_form "
					+ "where FORM_OF_WAY = 50 AND U_RECORD != 2 and link_pid in ("+StringUtils.join(linkPids, ",")+")";
			log.info("RdRestriction前检查GLM08033:" + sql);

			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);
			
			if(!resultList.isEmpty()){
				this.setCheckResult("","",0);
			}
		}							
	}


	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#postCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		for(IRow obj : checkCommand.getGlmList()){
			//道路属性编辑
			if(obj instanceof RdLinkForm ){
				RdLinkForm RdLinkForm=(RdLinkForm) obj;
				checkRdLinkForm(RdLinkForm);
			}
		}
	}
	/**
	 * @param rdLinkForm
	 * @throws Exception 
	 */
	private void checkRdLinkForm(RdLinkForm rdLinkForm) throws Exception {
		boolean checkFlg = false;
		if(rdLinkForm.status().equals(ObjStatus.UPDATE)){
			if(rdLinkForm.changedFields().containsKey("formOfWay")){
				//交叉口内道路
				int formOfWay = Integer.parseInt(rdLinkForm.changedFields().get("formOfWay").toString());
				if(formOfWay==50){
					checkFlg = true;
				}
			}
		}else if(rdLinkForm.status().equals(ObjStatus.INSERT)){
			int formOfWay = rdLinkForm.getFormOfWay();
			if(formOfWay==50){
				checkFlg = true;
			}
		}

		if(checkFlg){
			StringBuilder sb2 = new StringBuilder();

			sb2.append("SELECT 1 FROM RD_RESTRICTION RR,RD_RESTRICTION_DETAIL RRD");
			sb2.append(" WHERE RR.PID = RRD.RESTRIC_PID");
			sb2.append(" AND RR.IN_LINK_PID = " + rdLinkForm.getLinkPid());
			sb2.append(" AND RR.U_RECORD <> 2");
			sb2.append(" AND RRD.RELATIONSHIP_TYPE = 1");
			sb2.append(" AND RRD.U_RECORD <> 2");
			sb2.append(" UNION");
			sb2.append(" SELECT 1 FROM RD_RESTRICTION_DETAIL RRD");
			sb2.append(" WHERE RRD.OUT_LINK_PID = " + rdLinkForm.getLinkPid());
			sb2.append(" AND RRD.U_RECORD <> 2");
			sb2.append(" AND RRD.RELATIONSHIP_TYPE = 1");
			

			String sql2 = sb2.toString();
			log.info("RdLinkForm后检查GLM08033:" + sql2);

			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql2);

			if(resultList.size()>0){
				String target = "[RD_LINK," + rdLinkForm.getLinkPid() + "]";
				this.setCheckResult("", target, 0);
			}
		}
	}

}
