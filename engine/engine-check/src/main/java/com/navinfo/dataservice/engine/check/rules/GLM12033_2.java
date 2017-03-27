package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchRealimage;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
 * @ClassName: GLM12033_2
 * @author songdongyan
 * @date 2017年3月24日
 * @Description: 线线普通路口实景图的进入线、退出线不能是交叉点内道路或公交车专用道或步行街，经过线不能是公交车专用道、步行街、私道，且进入线、退出线、经过线的种别均不能为步行道路；
 * 修改普通道路实景图后检查：线线普通路口实景图，进入线、退出线不能是交叉口内道路|公交车专用道|步行街|步行道路，经过线不能是公交车专用道|步行街|私道|步行道路
 * 实景图类型编辑后检查：线线普通路口实景图，进入线、退出线不能是交叉口内道路|公交车专用道|步行街|步行道路，经过线不能是公交车专用道|步行街|私道|步行道路
 * 道路属性编辑后检查：线线普通路口实景图，进入线、退出线不能是交叉口内道路|公交车专用道|步行街|步行道路，经过线不能是公交车专用道|步行街|私道|步行道路
 * link种别编辑后检查：线线普通路口实景图，进入线、退出线不能是交叉口内道路|公交车专用道|步行街|步行道路，经过线不能是公交车专用道|步行街|私道|步行道路
 */
public class GLM12033_2 extends baseRule{

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
			// 修改分歧关系类型
			if (obj instanceof RdBranch) {
				RdBranch rdBranch = (RdBranch) obj;
				checkRdBranch(rdBranch);
			}
			//link种别编辑
			else if (obj instanceof RdLink) {
				RdLink rdLink = (RdLink) obj;
				checkRdLink(rdLink);
			}
			//道路属性编辑
			else if (obj instanceof RdLinkForm) {
				RdLinkForm rdLinkForm = (RdLinkForm) obj;
				checkRdLinkForm(rdLinkForm);
			}
			//实景图类型编辑
			else if (obj instanceof RdBranchRealimage) {
				RdBranchRealimage rdBranchRealimage = (RdBranchRealimage) obj;
				checkRdBranchRealimage(rdBranchRealimage);
			}
		}
	}

	/**
	 * @param rdBranchRealimage
	 * @throws Exception 
	 */
	private void checkRdBranchRealimage(RdBranchRealimage rdBranchRealimage) throws Exception {
		if(rdBranchRealimage.status().equals(ObjStatus.UPDATE)){
			if(rdBranchRealimage.changedFields().containsKey("imageType")){
				int imageType = Integer.parseInt(rdBranchRealimage.changedFields().get("imageType").toString());
				if(imageType == 1){
					checkRdBranch(rdBranchRealimage.getBranchPid());
				}
			}
		}
		
	}

	/**
	 * @param rdLinkForm
	 * @throws Exception 
	 */
	private void checkRdLinkForm(RdLinkForm rdLinkForm) throws Exception {
		boolean ckFlg = false;
		List<Integer> formOfWayList = new ArrayList<Integer>();
		formOfWayList.add(20);
		formOfWayList.add(22);
		formOfWayList.add(50);
		if(rdLinkForm.status().equals(ObjStatus.UPDATE)){
			if(rdLinkForm.changedFields().containsKey("formOfWay")){
				int formOfWay = Integer.parseInt(rdLinkForm.changedFields().get("formOfWay").toString());
				if(formOfWayList.contains(formOfWay)){
					ckFlg = true;
				}
			}
		}
		else if(rdLinkForm.status().equals(ObjStatus.INSERT)){
			int formOfWay = rdLinkForm.getFormOfWay();
			if(formOfWayList.contains(formOfWay)){
				ckFlg = true;
			}
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
		if(rdLink.status().equals(ObjStatus.UPDATE)){
			if(rdLink.changedFields().containsKey("kind")){
				int kind = Integer.parseInt(rdLink.changedFields().get("kind").toString());
				if(kind == 10){
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

		sb.append(" SELECT DISTINCT '进入路（退出路）具有步行道路' LOG                                                                       ");
		sb.append("   FROM RD_LINK R, RD_BRANCH RB, RD_BRANCH_REALIMAGE RBR                                                                  ");
		sb.append("  WHERE R.KIND = 10                                                                                                       ");
		sb.append("    AND RB.BRANCH_PID = RBR.BRANCH_PID                                                                                    ");
		sb.append("    AND (RB.IN_LINK_PID = R.LINK_PID OR RB.OUT_LINK_PID = R.LINK_PID)                                                     ");
		sb.append("    AND RB.RELATIONSHIP_TYPE = 2                                                                                          ");
		sb.append("    AND RBR.IMAGE_TYPE = 1                                                                                                ");
		sb.append("    AND R.U_RECORD <> 2                                                                                                   ");
		sb.append("    AND RB.U_RECORD <> 2                                                                                                  ");
		sb.append("    AND RBR.U_RECORD <> 2                                                                                                 ");
		sb.append("    AND R.LINK_PID = " + pid);
		sb.append(" UNION                                                                                                                    ");
		sb.append(" SELECT DISTINCT '进入路（退出路）具有' || DECODE(F.FORM_OF_WAY,50,'交叉点内道路', 20,'步行街', 22,'公交专用道', '') LOG  ");
		sb.append("   FROM RD_LINK_FORM F, RD_BRANCH B, RD_BRANCH_REALIMAGE R                                                                ");
		sb.append("  WHERE B.BRANCH_PID = R.BRANCH_PID                                                                                       ");
		sb.append("    AND R.IMAGE_TYPE = 1                                                                                                  ");
		sb.append("    AND B.RELATIONSHIP_TYPE = 2                                                                                           ");
		sb.append("    AND (F.LINK_PID = B.IN_LINK_PID OR F.LINK_PID = B.OUT_LINK_PID)                                                       ");
		sb.append("    AND F.FORM_OF_WAY IN (50, 22, 20)                                                                                     ");
		sb.append("    AND F.LINK_PID = " + pid);
		sb.append("    AND F.U_RECORD <> 2                                                                                                   ");
		sb.append("    AND B.U_RECORD <> 2                                                                                                   ");
		sb.append("    AND R.U_RECORD <> 2                                                                                                   ");
		sb.append(" UNION                                                                                                                    ");
		sb.append(" SELECT DISTINCT '进入路（退出路）具有' || DECODE(F.FORM_OF_WAY,20, '步行街', 22, '公交专用道', '') LOG                  ");
		sb.append("   FROM RD_BRANCH           B,                                                                                            ");
		sb.append("        RD_BRANCH_REALIMAGE R,                                                                                            ");
		sb.append("        RD_BRANCH_VIA       VIA,                                                                                          ");
		sb.append("        RD_LINK_FORM        F                                                                                             ");
		sb.append("  WHERE B.BRANCH_PID = R.BRANCH_PID                                                                                       ");
		sb.append("    AND R.BRANCH_PID = VIA.BRANCH_PID                                                                                     ");
		sb.append("    AND B.RELATIONSHIP_TYPE = 2                                                                                           ");
		sb.append("    AND R.IMAGE_TYPE = 1                                                                                                  ");
		sb.append("    AND F.LINK_PID = VIA.LINK_PID                                                                                         ");
		sb.append("    AND F.FORM_OF_WAY IN (20, 22)                                                                                         ");
		sb.append("    AND F.LINK_PID = " + pid);
		sb.append("    AND B.U_RECORD <> 2                                                                                                   ");
		sb.append("    AND R.U_RECORD <> 2                                                                                                   ");
		sb.append("    AND VIA.U_RECORD <> 2                                                                                                 ");
		sb.append("    AND F.U_RECORD <> 2                                                                                                   ");
		
		String sql = sb.toString();
		log.info("RdLink GLM12033_2 sql:" + sql);
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		Iterator itr = resultList.iterator();
		while(itr.hasNext()){
			String target = "[RD_LINK," + pid + "]";
			this.setCheckResult("", target, 0,itr.next().toString());
		}
	}

	/**
	 * @param rdBranch
	 * @throws Exception 
	 */
	private void checkRdBranch(RdBranch rdBranch) throws Exception {
		if(rdBranch.status().equals(ObjStatus.UPDATE)){
			if(rdBranch.changedFields().containsKey("relationshipType")){
				int relationshipType = Integer.parseInt(rdBranch.changedFields().get("relationshipType").toString());
				if(relationshipType == 2){
					checkRdBranch(rdBranch.getPid());
				}
			}
		}
	}

	/**
	 * @param pid
	 * @throws Exception 
	 */
	private void checkRdBranch(int pid) throws Exception {
		StringBuilder sb = new StringBuilder();

		sb.append(" SELECT DISTINCT F.FORM_OF_WAY                                        ");
		sb.append("   FROM (SELECT B.BRANCH_PID, B.IN_LINK_PID AS LINK_PID               ");
		sb.append("           FROM RD_BRANCH B, RD_BRANCH_REALIMAGE R                    ");
		sb.append("          WHERE B.BRANCH_PID = R.BRANCH_PID                           ");
		sb.append("            AND B.RELATIONSHIP_TYPE = 2                               ");
		sb.append("            AND R.IMAGE_TYPE = 1                                      ");
		sb.append("            AND B.BRANCH_PID = " + pid);
		sb.append("         UNION ALL                                                    ");
		sb.append("         SELECT B.BRANCH_PID, B.OUT_LINK_PID AS LINK_PID              ");
		sb.append("           FROM RD_BRANCH B, RD_BRANCH_REALIMAGE R                    ");
		sb.append("          WHERE B.BRANCH_PID = R.BRANCH_PID                           ");
		sb.append("            AND B.RELATIONSHIP_TYPE = 2                               ");
		sb.append("            AND R.IMAGE_TYPE = 1                                      ");
		sb.append("            AND B.BRANCH_PID = " + pid + ") T, ");
		sb.append("        RD_LINK_FORM F                                                ");
		sb.append("  WHERE T.LINK_PID = F.LINK_PID                                       ");
		sb.append("    AND F.FORM_OF_WAY IN (50, 22, 20)                                 ");
		sb.append(" UNION                                                                ");
		sb.append(" SELECT DISTINCT L.KIND                                               ");
		sb.append("   FROM (SELECT B.BRANCH_PID, B.IN_LINK_PID AS LINK_PID               ");
		sb.append("           FROM RD_BRANCH B, RD_BRANCH_REALIMAGE R                    ");
		sb.append("          WHERE B.BRANCH_PID = R.BRANCH_PID                           ");
		sb.append("            AND B.RELATIONSHIP_TYPE = 2                               ");
		sb.append("            AND R.IMAGE_TYPE = 1                                      ");
		sb.append("            AND B.BRANCH_PID = " + pid);
		sb.append("         UNION ALL                                                    ");
		sb.append("         SELECT B.BRANCH_PID, B.OUT_LINK_PID AS LINK_PID              ");
		sb.append("           FROM RD_BRANCH B, RD_BRANCH_REALIMAGE R                    ");
		sb.append("          WHERE B.BRANCH_PID = R.BRANCH_PID                           ");
		sb.append("            AND B.RELATIONSHIP_TYPE = 2                               ");
		sb.append("            AND R.IMAGE_TYPE = 1                                      ");
		sb.append("            AND B.BRANCH_PID = " + pid);
		sb.append("         UNION ALL                                                    ");
		sb.append("         SELECT VIA.BRANCH_PID, VIA.LINK_PID                          ");
		sb.append("           FROM RD_BRANCH B, RD_BRANCH_REALIMAGE R, RD_BRANCH_VIA VIA ");
		sb.append("          WHERE B.BRANCH_PID = R.BRANCH_PID                           ");
		sb.append("            AND R.BRANCH_PID = VIA.BRANCH_PID                         ");
		sb.append("            AND B.RELATIONSHIP_TYPE = 2                               ");
		sb.append("            AND R.IMAGE_TYPE = 1                                      ");
		sb.append("            AND B.BRANCH_PID = " + pid + ") T, ");
		sb.append("        RD_LINK L                                                     ");
		sb.append("  WHERE T.LINK_PID = L.LINK_PID                                       ");
		sb.append("    AND L.KIND = 10                                                   ");
		sb.append(" UNION                                                                ");
		sb.append(" SELECT DISTINCT F.FORM_OF_WAY                                        ");
		sb.append("   FROM RD_BRANCH           B,                                        ");
		sb.append("        RD_BRANCH_REALIMAGE R,                                        ");
		sb.append("        RD_BRANCH_VIA       VIA,                                      ");
		sb.append("        RD_LINK_FORM        F                                         ");
		sb.append("  WHERE B.BRANCH_PID = R.BRANCH_PID                                   ");
		sb.append("    AND R.BRANCH_PID = VIA.BRANCH_PID                                 ");
		sb.append("    AND B.RELATIONSHIP_TYPE = 2                                       ");
		sb.append("    AND B.BRANCH_PID = " + pid);
		sb.append("    AND R.IMAGE_TYPE = 1                                              ");
		sb.append("    AND F.LINK_PID = VIA.LINK_PID                                     ");
		sb.append("    AND F.FORM_OF_WAY IN ( 20, 22)                                 ");
		
		String sql = sb.toString();
		log.info("RdBranchRealimage GLM12033_2 sql:" + sql);
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		Iterator itr = resultList.iterator();
		while(itr.hasNext()){
			String target = "[RD_BRANCH," + pid + "]";
			String log = "进入路（退出路）具有";
			int formOfWay = (int) itr.next();
			if(formOfWay==20){
				this.setCheckResult("", target, 0,log+"步行街");
			}
			else if(formOfWay==22){
				this.setCheckResult("", target, 0,log+"公交专用道");
			}
			else if(formOfWay==50){
				this.setCheckResult("", target, 0,log+"交叉点内道路");
			}
			else if(formOfWay==10){
				this.setCheckResult("", target, 0,log+"步行道路");
			}
		}
		
	}

}
