package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchDetail;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
 * @ClassName: GLM05097
 * @author songdongyan
 * @date 2017年3月27日
 * @Description: GLM05097.java
 */
public class GLM05097 extends baseRule{

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#preCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#postCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		for (IRow obj : checkCommand.getGlmList()) {
			// 新增分歧
			if (obj instanceof RdBranch) {
				RdBranch rdBranch = (RdBranch) obj;
				checkRdBranch(rdBranch);
			}
			//分歧详细信息
			else if (obj instanceof RdBranchDetail) {
				RdBranchDetail rdBranchDetail = (RdBranchDetail) obj;
				checkRdBranchDetail(rdBranchDetail);
			}
			//道路属性编辑
			else if (obj instanceof RdLink) {
				RdLink rdLink = (RdLink) obj;
				checkRdLink(rdLink);
			}
			//道路形态编辑
			else if (obj instanceof RdLinkForm) {
				RdLinkForm rdLinkForm = (RdLinkForm) obj;
				checkRdLinkForm(rdLinkForm);
			}
		}
	}

	/**
	 * @param rdLinkForm
	 * @throws Exception 
	 */
	private void checkRdLinkForm(RdLinkForm rdLinkForm) throws Exception {
		boolean ckFlg = false;

		if(rdLinkForm.status().equals(ObjStatus.UPDATE)){
			if(rdLinkForm.changedFields().containsKey("formOfWay")){
				ckFlg = true;
			}
		}
		else if(rdLinkForm.status().equals(ObjStatus.INSERT)){
			ckFlg = true;
		}
		
		if(ckFlg){
			checkRdLink(rdLinkForm.getLinkPid());
		}
	}



	/**
	 * @param rdLink
	 * @throws Exception 
	 */
	private void checkRdLink(RdLink rdLink) throws Exception {
		boolean flg = false;
		if(rdLink.status().equals(ObjStatus.UPDATE)){
			if(rdLink.changedFields().containsKey("kind")){
				int kind = Integer.parseInt(rdLink.changedFields().get("kind").toString());
				if((kind!=1)&&(kind!=2)){
					if((rdLink.getKind()==2)||(rdLink.getKind()==1)){
						flg = true;
					}
				}
			}
			else if(rdLink.changedFields().containsKey("multiDigitized")){
				int multiDigitized = Integer.parseInt(rdLink.changedFields().get("multiDigitized").toString());
				if(multiDigitized==0){
					flg = true;
				}
			}
			//道路方向：单向变双向
			else if(rdLink.changedFields().containsKey("direct")){
				int direct = Integer.parseInt(rdLink.changedFields().get("direct").toString());
				if((direct!=3)&&(direct!=2)){
					if((rdLink.getDirect()==3)||(rdLink.getDirect()==2)){
						flg = true;
					}
				}
			}
			
			if(flg){
				checkRdLink(rdLink.getPid());
			}
		}
	}

	/**
	 * @param linkPid
	 * @throws Exception 
	 */
	private void checkRdLink(int linkPid) throws Exception {
		StringBuilder sb = new StringBuilder();

		sb.append(" SELECT 1                                                               ");
		sb.append("   FROM RD_BRANCH RB, RD_BRANCH_DETAIL RBD, RD_LINK RL, RD_LINK_FORM RLF");
		sb.append("  WHERE RL.LINK_PID = " + linkPid);
		sb.append("    AND RB.BRANCH_PID = RBD.BRANCH_PID                                  ");
		sb.append("    AND RBD.BRANCH_TYPE = 2                                             ");
		sb.append("    AND RL.LINK_PID = RLF.LINK_PID                                      ");
		sb.append("    AND RB.IN_LINK_PID = RL.LINK_PID                                    ");
		sb.append("    AND RB.U_RECORD <> 2                                                ");
		sb.append("    AND RBD.U_RECORD <> 2                                               ");
		sb.append("    AND RL.U_RECORD <> 2                                                ");
		sb.append("    AND RLF.U_RECORD <> 2                                               ");
		sb.append("    AND (RL.KIND NOT IN (1, 2) OR RL.MULTI_DIGITIZED <> 1 OR            ");
		sb.append("        RLF.FORM_OF_WAY <> 14 OR RL.DIRECT NOT IN (2, 3))               ");
		
		String sql = sb.toString();
		log.info("RdLink GLM05101 sql:" + sql);
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if(!resultList.isEmpty()){
			String target = "[RD_LINK," + linkPid + "]";
			this.setCheckResult("", target, 0);
		}
	}
	/**
	 * @param rdBranchDetail
	 * @throws Exception 
	 */
	private void checkRdBranchDetail(RdBranchDetail rdBranchDetail) throws Exception {
		boolean flg = false;
		if(rdBranchDetail.changedFields().containsKey("branchType")){
			int branchType = Integer.parseInt(rdBranchDetail.changedFields().get("branchType").toString());
			if(branchType==2){
				flg = true;
			}
		}
		if(flg){
			checkRdBranch(rdBranchDetail.getBranchPid());
		}
	}

	/**
	 * @param rdBranch
	 * @throws Exception 
	 */
	private void checkRdBranch(RdBranch rdBranch) throws Exception {
		boolean flg = false;
		if(rdBranch.status().equals(ObjStatus.INSERT)){
			for(IRow irow:rdBranch.getDetails()){
				if (irow instanceof RdBranchDetail){
					RdBranchDetail rdBranchDetail = (RdBranchDetail)irow;
					if((rdBranchDetail.getBranchType()==2)){
						flg = true;
					}
				}
			}
		}

		if(flg){
			checkRdBranch(rdBranch.getPid());
		}
	}

	/**
	 * @param pid
	 * @throws Exception 
	 */
	private void checkRdBranch(int pid) throws Exception {
		StringBuilder sb = new StringBuilder();

		sb.append(" SELECT 1                                                               ");
		sb.append("   FROM RD_BRANCH RB, RD_BRANCH_DETAIL RBD, RD_LINK RL, RD_LINK_FORM RLF");
		sb.append("  WHERE RB.BRANCH_PID = " + pid);
		sb.append("    AND RB.BRANCH_PID = RBD.BRANCH_PID                                  ");
		sb.append("    AND RBD.BRANCH_TYPE = 2                                             ");
		sb.append("    AND RL.LINK_PID = RLF.LINK_PID                                      ");
		sb.append("    AND RB.IN_LINK_PID = RL.LINK_PID                                    ");
		sb.append("    AND RB.U_RECORD <> 2                                                ");
		sb.append("    AND RBD.U_RECORD <> 2                                               ");
		sb.append("    AND RL.U_RECORD <> 2                                                ");
		sb.append("    AND RLF.U_RECORD <> 2                                               ");
		sb.append("    AND (RL.KIND NOT IN (1, 2) OR RL.MULTI_DIGITIZED <> 1 OR            ");
		sb.append("        RLF.FORM_OF_WAY <> 14 OR RL.DIRECT NOT IN (2, 3))               ");
		
		
		String sql = sb.toString();
		log.info("RdBranch GLM05101 sql:" + sql);
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if(!resultList.isEmpty()){
			String target = "[RD_BRANCH," + pid + "]";
			this.setCheckResult("", target, 0);
		}
	}

}
