package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchDetail;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
 * @ClassName: GLM05101
 * @author songdongyan
 * @date 2017年3月27日
 * @Description: 检查对象:普通道路方面分歧（分歧类型为方面分歧且模式图号码为空）
检查原则：进入线是8级及8级以下道路等级，则报log
 */
public class GLM05101 extends baseRule{

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
			//道路属性编辑
			else if (obj instanceof RdLink) {
				RdLink rdLink = (RdLink) obj;
				checkRdLink(rdLink);
			}
			//分歧详细信息
			else if (obj instanceof RdBranchDetail) {
				RdBranchDetail rdBranchDetail = (RdBranchDetail) obj;
				checkRdBranchDetail(rdBranchDetail);
			}
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
			if(branchType==1){
				flg = true;
			}
		}
		else if(rdBranchDetail.changedFields().containsKey("patternCode")){
			if(rdBranchDetail.changedFields().get("patternCode")==null){
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
					if((rdBranchDetail.getBranchType()==1)&&(rdBranchDetail.getPatternCode()==null)){
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

		sb.append(" SELECT 1                                               ");
		sb.append("   FROM RD_BRANCH RB, RD_BRANCH_DETAIL RBD, RD_LINK RL  ");
		sb.append("  WHERE RB.BRANCH_PID = " + pid);
		sb.append("    AND RB.BRANCH_PID = RBD.BRANCH_PID                  ");
		sb.append("    AND RBD.BRANCH_TYPE = 1                             ");
		sb.append("    AND RB.U_RECORD <> 2                                ");
		sb.append("    AND RBD.U_RECORD <> 2                               ");
		sb.append("    AND RL.U_RECORD <> 2                                ");
		sb.append("    AND RB.IN_LINK_PID = RL.LINK_PID                    ");
		sb.append("    AND RBD.PATTERN_CODE IS NULL                        ");
		sb.append("    AND RL.KIND >= 8                                    ");
		
		
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


	/**
	 * @param rdLink
	 * @throws Exception 
	 */
	private void checkRdLink(RdLink rdLink) throws Exception {
		if(rdLink.status().equals(ObjStatus.UPDATE)){
			if(rdLink.changedFields().containsKey("kind")){
				int kind = (int)rdLink.changedFields().get("kind");
				if(kind <= 8){
					checkRdLink(rdLink.getPid());
				}
			}
		}
	}

	/**
	 * @param pid
	 * @throws Exception 
	 */
	private void checkRdLink(int pid) throws Exception {
		StringBuilder sb = new StringBuilder();

		sb.append(" SELECT 1                                               ");
		sb.append("   FROM RD_BRANCH RB, RD_BRANCH_DETAIL RBD, RD_LINK RL  ");
		sb.append("  WHERE RL.LINK_PID = " + pid);
		sb.append("    AND RB.BRANCH_PID = RBD.BRANCH_PID                  ");
		sb.append("    AND RBD.BRANCH_TYPE = 1                             ");
		sb.append("    AND RB.U_RECORD <> 2                                ");
		sb.append("    AND RBD.U_RECORD <> 2                               ");
		sb.append("    AND RL.U_RECORD <> 2                                ");
		sb.append("    AND RB.IN_LINK_PID = RL.LINK_PID                    ");
		sb.append("    AND RBD.PATTERN_CODE IS NULL                        ");
		sb.append("    AND RL.KIND >= 8                                    ");
		
		
		String sql = sb.toString();
		log.info("RdLink GLM05101 sql:" + sql);
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if(!resultList.isEmpty()){
			String target = "[RD_LINK," + pid + "]";
			this.setCheckResult("", target, 0);
		}
	}
}
