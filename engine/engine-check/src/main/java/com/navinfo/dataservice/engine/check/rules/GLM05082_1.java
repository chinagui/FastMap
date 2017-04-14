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
 * @ClassName: GLM05082_1
 * @author songdongyan
 * @date 2017年3月23日
 * @Description: 分歧类型为“4：复杂路口模式图”时，进入link必须为7级及以上的普通道路，不可为高速、城市高速，否则报log，退出道路不可为9级和10级，否则报log
 */
public class GLM05082_1 extends baseRule{

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
			// 新增/修改复杂路口模式图
			if (obj instanceof RdBranch) {
				RdBranch rdBranch = (RdBranch) obj;
				checkRdBranch(rdBranch);
			}
			//link种别编辑
			else if (obj instanceof RdLink) {
				RdLink rdLink = (RdLink) obj;
				checkRdLink(rdLink);
			}
			else if(obj instanceof RdBranchDetail) {
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
		if(rdBranchDetail.status().equals(ObjStatus.UPDATE)){
			if(rdBranchDetail.changedFields().containsKey("branchType")){
				int branchType = Integer.parseInt(rdBranchDetail.changedFields().get("branchType").toString());
				if(branchType == 4){
					StringBuilder sb = new StringBuilder();

					sb.append(" SELECT 1                                           ");
					sb.append("   FROM RD_LINK R, RD_BRANCH B, RD_BRANCH_DETAIL D  ");
					sb.append("  WHERE R.LINK_PID = B.IN_LINK_PID                  ");
					sb.append("    AND (R.KIND IN (1, 2) OR R.KIND > 7)            ");
					sb.append("    AND B.BRANCH_PID = D.BRANCH_PID                 ");
					sb.append("    AND D.BRANCH_TYPE = 4                           ");
					sb.append("    AND R.U_RECORD <> 2                             ");
					sb.append("    AND B.U_RECORD <> 2                             ");
					sb.append("    AND D.U_RECORD <> 2                             ");
					sb.append("    AND B.BRANCH_PID = " + rdBranchDetail.getPid());
					sb.append(" UNION                                              ");
					sb.append(" SELECT 1                                           ");
					sb.append("   FROM RD_LINK R, RD_BRANCH B, RD_BRANCH_DETAIL D  ");
					sb.append("  WHERE R.LINK_PID = B.OUT_LINK_PID                 ");
					sb.append("    AND R.KIND IN (9, 10)                           ");
					sb.append("    AND B.BRANCH_PID = D.BRANCH_PID                 ");
					sb.append("    AND D.BRANCH_TYPE = 4                           ");
					sb.append("    AND R.U_RECORD <> 2                             ");
					sb.append("    AND B.U_RECORD <> 2                             ");
					sb.append("    AND D.U_RECORD <> 2                             ");
					sb.append("    AND B.BRANCH_PID = " + rdBranchDetail.getPid());

					String sql = sb.toString();
					log.info("RdBranch GLM05082_1 sql:" + sql);
					DatabaseOperator getObj = new DatabaseOperator();
					List<Object> resultList = new ArrayList<Object>();
					resultList = getObj.exeSelect(this.getConn(), sql);

					if (!resultList.isEmpty()) {
						this.setCheckResult("", "[RD_BRANCH," + rdBranchDetail.getPid() + "]", 0);
					}
				}
						
			}
		}
	}

	/**
	 * @param rdLink
	 * @throws Exception 
	 */
	private void checkRdLink(RdLink rdLink) throws Exception {
		if(rdLink.status().equals(ObjStatus.UPDATE)){
			if(rdLink.changedFields().containsKey("kind")){
				StringBuilder sb = new StringBuilder();

				sb.append(" SELECT 1                                              ");
				sb.append("   FROM RD_BRANCH B, RD_BRANCH_DETAIL D, RD_LINK L1    ");
				sb.append("  WHERE B.BRANCH_PID = D.BRANCH_PID                    ");
				sb.append("    AND D.BRANCH_TYPE = 4                              ");
				sb.append("    AND B.IN_LINK_PID = " + rdLink.getPid());
				sb.append("    AND B.U_RECORD != 2                                ");
				sb.append("    AND D.U_RECORD != 2                                ");
				sb.append("    AND L1.U_RECORD != 2                               ");
				sb.append("    AND L1.LINK_PID =  B.IN_LINK_PID                   ");
				sb.append("    AND (L1.KIND IN (1, 2) OR L1.KIND > 7)             ");
				sb.append(" UNION ALL                                             ");
				sb.append(" SELECT 1                                              ");
				sb.append("   FROM RD_BRANCH B, RD_BRANCH_DETAIL D, RD_LINK L1    ");
				sb.append("  WHERE B.BRANCH_PID = D.BRANCH_PID                    ");
				sb.append("    AND D.BRANCH_TYPE = 4                              ");
				sb.append("    AND B.U_RECORD != 2                                ");
				sb.append("    AND D.U_RECORD != 2                                ");
				sb.append("    AND L1.U_RECORD != 2                               ");
				sb.append("    AND B.OUT_LINK_PID = " + rdLink.getPid());
				sb.append("    AND L1.LINK_PID =  B.OUT_LINK_PID                  ");
				sb.append("    AND L1.KIND IN (9, 10)                             ");

				String sql = sb.toString();
				log.info("RdLink GLM05082_1 sql:" + sql);
				DatabaseOperator getObj = new DatabaseOperator();
				List<Object> resultList = new ArrayList<Object>();
				resultList = getObj.exeSelect(this.getConn(), sql);

				if (!resultList.isEmpty()) {
					this.setCheckResult("", "[RD_LINK," + rdLink.getPid() + "]", 0);
				}
			}
		}
	}

	/**
	 * @param rdBranch
	 * @throws Exception 
	 */
	private void checkRdBranch(RdBranch rdBranch) throws Exception {
		boolean flg = true;
		//新增
		if(rdBranch.status().equals(ObjStatus.INSERT)){
			for(IRow irow:rdBranch.getDetails()){
				if (irow instanceof RdBranchDetail){
					RdBranchDetail rdBranchDetail = (RdBranchDetail)irow;
					if(rdBranchDetail.getBranchType()==4){
						flg = true;
					}
				}
			}
		}
		//修改
		else if(rdBranch.status().equals(ObjStatus.UPDATE)){
			if(rdBranch.changedFields().containsKey("inLinkPid")){
				flg = true;
			}
			else if(rdBranch.changedFields().containsKey("outLinkPid")){
				flg = true;
			}
		}
		
		if(flg){
			StringBuilder sb = new StringBuilder();

			sb.append(" SELECT 1                                           ");
			sb.append("   FROM RD_LINK R, RD_BRANCH B, RD_BRANCH_DETAIL D  ");
			sb.append("  WHERE R.LINK_PID = B.IN_LINK_PID                  ");
			sb.append("    AND (R.KIND IN (1, 2) OR R.KIND > 7)            ");
			sb.append("    AND B.BRANCH_PID = D.BRANCH_PID                 ");
			sb.append("    AND D.BRANCH_TYPE = 4                           ");
			sb.append("    AND R.U_RECORD <> 2                             ");
			sb.append("    AND B.U_RECORD <> 2                             ");
			sb.append("    AND D.U_RECORD <> 2                             ");
			sb.append("    AND B.BRANCH_PID = " + rdBranch.getPid());
			sb.append(" UNION                                              ");
			sb.append(" SELECT 1                                           ");
			sb.append("   FROM RD_LINK R, RD_BRANCH B, RD_BRANCH_DETAIL D  ");
			sb.append("  WHERE R.LINK_PID = B.OUT_LINK_PID                 ");
			sb.append("    AND R.KIND IN (9, 10)                           ");
			sb.append("    AND B.BRANCH_PID = D.BRANCH_PID                 ");
			sb.append("    AND D.BRANCH_TYPE = 4                           ");
			sb.append("    AND R.U_RECORD <> 2                             ");
			sb.append("    AND B.U_RECORD <> 2                             ");
			sb.append("    AND D.U_RECORD <> 2                             ");
			sb.append("    AND B.BRANCH_PID = " + rdBranch.getPid());

			String sql = sb.toString();
			log.info("RdBranch GLM05082_1 sql:" + sql);
			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);

			if (!resultList.isEmpty()) {
				this.setCheckResult("", "[RD_BRANCH," + rdBranch.getPid() + "]", 0);
			}
		}
	}

}
