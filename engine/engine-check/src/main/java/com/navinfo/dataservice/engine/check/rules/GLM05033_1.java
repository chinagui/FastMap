package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchDetail;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
 * @ClassName: GLM05033_1
 * @author songdongyan
 * @date 2017年3月27日
 * @Description: --相同进入线、退出线处存在两个单分歧时，以下情况可以共存，其他情况报Log：
--a)分歧类型为1：方面分歧,模式图代码不为空, 分歧退出线含有“HW对象JCT属性”,且与2：IC分歧共存；
--b)分歧类型为1:方面分歧，模式图代码为空，与3：3D分歧或4：复杂路口模式图（7开头）共存；
 */
public class GLM05033_1  extends baseRule{

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
			// 新增/修改分歧
			if (obj instanceof RdBranch) {
				RdBranch rdBranch = (RdBranch) obj;
				checkRdBranch(rdBranch);
			}
			//道路属性编辑
			else if (obj instanceof RdLinkForm) {
				RdLinkForm rdLinkForm = (RdLinkForm) obj;
				checkRdLinkForm(rdLinkForm);
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
		boolean ckFlg = false;

		if(rdBranchDetail.status().equals(ObjStatus.UPDATE)){
			if(rdBranchDetail.changedFields().containsKey("branchType")){
				ckFlg = true;
			}
			else if(rdBranchDetail.changedFields().containsKey("patternCode")){
				ckFlg = true;
			}
		}

		if(ckFlg){
			checkRdBranch(rdBranchDetail.getBranchPid());
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
	 * @param linkPid
	 * @throws Exception 
	 */
	private void checkRdLink(int linkPid) throws Exception {
		StringBuilder sb = new StringBuilder();

		sb.append(" WITH TMP2 AS                                                               ");
		sb.append("  (SELECT B.BRANCH_PID                                                      ");
		sb.append("     FROM RD_BRANCH B, TMP1                                                 ");
		sb.append("    WHERE (B.IN_LINK_PID = " + linkPid);
		sb.append("      OR B.OUT_LINK_PID = " + linkPid + ")");
		sb.append("      AND B.U_RECORD <> 2),                                                 ");
		sb.append(" TMP11 AS                                                                   ");
		sb.append("  (SELECT D.BRANCH_PID                                                      ");
		sb.append("     FROM RD_BRANCH_DETAIL D, TMP2                                          ");
		sb.append("    WHERE D.BRANCH_PID = TMP2.BRANCH_PID                                    ");
		sb.append("      AND D.BRANCH_TYPE = 1                                                 ");
		sb.append("      AND D.PATTERN_CODE IS NOT NULL                                        ");
		sb.append("      AND D.U_RECORD <> 2),                                                 ");
		sb.append(" TMP12 AS                                                                   ");
		sb.append("  (SELECT D.BRANCH_PID                                                      ");
		sb.append("     FROM RD_BRANCH_DETAIL D, TMP2                                          ");
		sb.append("    WHERE D.BRANCH_PID = TMP2.BRANCH_PID                                    ");
		sb.append("      AND D.BRANCH_TYPE = 2                                                 ");
		sb.append("      AND D.U_RECORD <> 2),                                                 ");
		sb.append(" TMP21 AS                                                                   ");
		sb.append("  (SELECT D.BRANCH_PID                                                      ");
		sb.append("     FROM RD_BRANCH_DETAIL D, TMP2                                          ");
		sb.append("    WHERE D.BRANCH_PID = TMP2.BRANCH_PID                                    ");
		sb.append("      AND D.BRANCH_TYPE = 1                                                 ");
		sb.append("      AND D.PATTERN_CODE IS NULL                                            ");
		sb.append("      AND D.U_RECORD <> 2),                                                 ");
		sb.append(" TMP23 AS                                                                   ");
		sb.append("  (SELECT D.BRANCH_PID                                                      ");
		sb.append("     FROM RD_BRANCH_DETAIL D, TMP2                                          ");
		sb.append("    WHERE D.BRANCH_PID = TMP2.BRANCH_PID                                    ");
		sb.append("      AND D.BRANCH_TYPE = 3                                                 ");
		sb.append("      AND D.U_RECORD <> 2),                                                 ");
		sb.append(" TMP24 AS                                                                   ");
		sb.append("  (SELECT D.BRANCH_PID                                                      ");
		sb.append("     FROM RD_BRANCH_DETAIL D, TMP2                                          ");
		sb.append("    WHERE D.BRANCH_PID = TMP2.BRANCH_PID                                    ");
		sb.append("      AND D.BRANCH_TYPE = 4                                                 ");
		sb.append("      AND D.U_RECORD <> 2),                                                 ");
		sb.append(" TMP3 AS                                                                    ");
		sb.append("  (SELECT BRANCH_PID                                                        ");
		sb.append("     FROM TMP11                                                             ");
		sb.append("   UNION                                                                    ");
		sb.append("   SELECT BRANCH_PID FROM TMP12),                                           ");
		sb.append(" TMP4 AS                                                                    ");
		sb.append("  (SELECT BRANCH_PID                                                        ");
		sb.append("     FROM TMP21                                                             ");
		sb.append("   UNION                                                                    ");
		sb.append("   SELECT BRANCH_PID FROM TMP23),                                           ");
		sb.append(" TMP5 AS                                                                    ");
		sb.append("  (SELECT BRANCH_PID                                                        ");
		sb.append("     FROM TMP21                                                             ");
		sb.append("   UNION                                                                    ");
		sb.append("   SELECT BRANCH_PID FROM TMP24)                                            ");
		sb.append("                                                                            ");
		sb.append(" SELECT 1");
		sb.append("   FROM TMP2 R                                                              ");
		sb.append("  WHERE (EXISTS (SELECT DISTINCT (1) FROM TMP2 HAVING COUNT(1) > 2))        ");
		sb.append("     OR ((EXISTS (SELECT DISTINCT (1) FROM TMP2 HAVING COUNT(1) = 2)) AND   ");
		sb.append("        (NOT EXISTS (SELECT DISTINCT (1) FROM TMP11)) AND                   ");
		sb.append("        (NOT EXISTS (SELECT DISTINCT (1) FROM TMP21)))                      ");
		sb.append(" UNION                                                                      ");
		sb.append(" SELECT 1");
		sb.append("   FROM TMP2 R                                                              ");
		sb.append("  WHERE (EXISTS (SELECT DISTINCT (1) FROM TMP2 HAVING COUNT(1) = 2))        ");
		sb.append("    AND (EXISTS (SELECT DISTINCT (1) FROM TMP11 HAVING COUNT(1) = 1))       ");
		sb.append("    AND (NOT EXISTS (SELECT DISTINCT (1) FROM TMP3 HAVING COUNT(1) = 2))    ");
		sb.append(" UNION                                                                      ");
		sb.append(" SELECT 1");
		sb.append("   FROM TMP2 R                                                              ");
		sb.append("  WHERE (EXISTS (SELECT DISTINCT (1) FROM TMP2 HAVING COUNT(1) = 2))        ");
		sb.append("    AND (EXISTS (SELECT DISTINCT (1) FROM TMP21 HAVING COUNT(1) = 1))       ");
		sb.append("    AND (NOT EXISTS (SELECT DISTINCT (1) FROM TMP4 HAVING COUNT(1) = 2))    ");
		sb.append("    AND (NOT EXISTS (SELECT DISTINCT (1) FROM TMP5 HAVING COUNT(1) = 2))    ");
		
		
		String sql = sb.toString();
		log.info("RdLink GLM05033_1 sql:" + sql);
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if(!resultList.isEmpty()){
			String target = "[RD_LINK," + linkPid + "]";
			this.setCheckResult("", target, 0);
		}
	}

	/**
	 * @param rdBranch
	 * @throws Exception 
	 */
	private void checkRdBranch(RdBranch rdBranch) throws Exception {
		boolean ckFlg = false;

		if(rdBranch.status().equals(ObjStatus.UPDATE)){
			if(rdBranch.changedFields().containsKey("outLinkPid")){
				ckFlg = true;
			}
		}
		else if(rdBranch.status().equals(ObjStatus.INSERT)){
			ckFlg = true;
		}
		
		if(ckFlg){
			checkRdBranch(rdBranch.getPid());
		}
	}

	/**
	 * @param pid
	 * @throws Exception 
	 */
	private void checkRdBranch(int pid) throws Exception {
		StringBuilder sb = new StringBuilder();

		sb.append(" WITH TMP1 AS                                                               ");
		sb.append("  (SELECT IN_LINK_PID, OUT_LINK_PID                                         ");
		sb.append("     FROM RD_BRANCH                                                         ");
		sb.append("    WHERE BRANCH_PID = " + pid);
		sb.append("    AND U_RECORD <> 2),                                                     ");
		sb.append(" TMP2 AS                                                                    ");
		sb.append("  (SELECT B.BRANCH_PID                                                      ");
		sb.append("     FROM RD_BRANCH B, TMP1                                                 ");
		sb.append("    WHERE B.IN_LINK_PID = TMP1.IN_LINK_PID                                  ");
		sb.append("      AND B.OUT_LINK_PID = TMP1.OUT_LINK_PID                                ");
		sb.append("      AND B.U_RECORD <> 2),                                                 ");
		sb.append(" TMP11 AS                                                                   ");
		sb.append("  (SELECT D.BRANCH_PID                                                      ");
		sb.append("     FROM RD_BRANCH_DETAIL D, TMP2                                          ");
		sb.append("    WHERE D.BRANCH_PID = TMP2.BRANCH_PID                                    ");
		sb.append("      AND D.BRANCH_TYPE = 1                                                 ");
		sb.append("      AND D.PATTERN_CODE IS NOT NULL                                        ");
		sb.append("      AND D.U_RECORD <> 2),                                                 ");
		sb.append(" TMP12 AS                                                                   ");
		sb.append("  (SELECT D.BRANCH_PID                                                      ");
		sb.append("     FROM RD_BRANCH_DETAIL D, TMP2                                          ");
		sb.append("    WHERE D.BRANCH_PID = TMP2.BRANCH_PID                                    ");
		sb.append("      AND D.BRANCH_TYPE = 2                                                 ");
		sb.append("      AND D.U_RECORD <> 2),                                                 ");
		sb.append(" TMP21 AS                                                                   ");
		sb.append("  (SELECT D.BRANCH_PID                                                      ");
		sb.append("     FROM RD_BRANCH_DETAIL D, TMP2                                          ");
		sb.append("    WHERE D.BRANCH_PID = TMP2.BRANCH_PID                                    ");
		sb.append("      AND D.BRANCH_TYPE = 1                                                 ");
		sb.append("      AND D.PATTERN_CODE IS NULL                                            ");
		sb.append("      AND D.U_RECORD <> 2),                                                 ");
		sb.append(" TMP23 AS                                                                   ");
		sb.append("  (SELECT D.BRANCH_PID                                                      ");
		sb.append("     FROM RD_BRANCH_DETAIL D, TMP2                                          ");
		sb.append("    WHERE D.BRANCH_PID = TMP2.BRANCH_PID                                    ");
		sb.append("      AND D.BRANCH_TYPE = 3                                                 ");
		sb.append("      AND D.U_RECORD <> 2),                                                 ");
		sb.append(" TMP24 AS                                                                   ");
		sb.append("  (SELECT D.BRANCH_PID                                                      ");
		sb.append("     FROM RD_BRANCH_DETAIL D, TMP2                                          ");
		sb.append("    WHERE D.BRANCH_PID = TMP2.BRANCH_PID                                    ");
		sb.append("      AND D.BRANCH_TYPE = 4                                                 ");
		sb.append("      AND D.U_RECORD <> 2),                                                 ");
		sb.append(" TMP3 AS                                                                    ");
		sb.append("  (SELECT BRANCH_PID                                                        ");
		sb.append("     FROM TMP11                                                             ");
		sb.append("   UNION                                                                    ");
		sb.append("   SELECT BRANCH_PID FROM TMP12),                                           ");
		sb.append(" TMP4 AS                                                                    ");
		sb.append("  (SELECT BRANCH_PID                                                        ");
		sb.append("     FROM TMP21                                                             ");
		sb.append("   UNION                                                                    ");
		sb.append("   SELECT BRANCH_PID FROM TMP23),                                           ");
		sb.append(" TMP5 AS                                                                    ");
		sb.append("  (SELECT BRANCH_PID                                                        ");
		sb.append("     FROM TMP21                                                             ");
		sb.append("   UNION                                                                    ");
		sb.append("   SELECT BRANCH_PID FROM TMP24)                                            ");
		sb.append("                                                                            ");
		sb.append(" SELECT 1");
		sb.append("   FROM TMP2 R                                                              ");
		sb.append("  WHERE (EXISTS (SELECT DISTINCT (1) FROM TMP2 HAVING COUNT(1) > 2))        ");
		sb.append("     OR ((EXISTS (SELECT DISTINCT (1) FROM TMP2 HAVING COUNT(1) = 2)) AND   ");
		sb.append("        (NOT EXISTS (SELECT DISTINCT (1) FROM TMP11)) AND                   ");
		sb.append("        (NOT EXISTS (SELECT DISTINCT (1) FROM TMP21)))                      ");
		sb.append("                                                                            ");
		sb.append(" UNION                                                                      ");
		sb.append(" SELECT 1");
		sb.append("   FROM TMP2 R                                                              ");
		sb.append("  WHERE (EXISTS (SELECT DISTINCT (1) FROM TMP2 HAVING COUNT(1) = 2))        ");
		sb.append("    AND (EXISTS (SELECT DISTINCT (1) FROM TMP11 HAVING COUNT(1) = 1))       ");
		sb.append("    AND (NOT EXISTS (SELECT DISTINCT (1) FROM TMP3 HAVING COUNT(1) = 2))    ");
		sb.append("                                                                            ");
		sb.append(" UNION                                                                      ");
		sb.append(" SELECT 1");
		sb.append("   FROM TMP2 R                                                              ");
		sb.append("  WHERE (EXISTS (SELECT DISTINCT (1) FROM TMP2 HAVING COUNT(1) = 2))        ");
		sb.append("    AND (EXISTS (SELECT DISTINCT (1) FROM TMP21 HAVING COUNT(1) = 1))       ");
		sb.append("    AND (NOT EXISTS (SELECT DISTINCT (1) FROM TMP4 HAVING COUNT(1) = 2))    ");
		sb.append("    AND (NOT EXISTS (SELECT DISTINCT (1) FROM TMP5 HAVING COUNT(1) = 2))    ");
		
		String sql = sb.toString();
		log.info("RdBranch GLM05033_1 sql:" + sql);
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if(!resultList.isEmpty()){
			String target = "[RD_BRANCH," + pid + "]";
			this.setCheckResult("", target, 0);
		}
	}

}
