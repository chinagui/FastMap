package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCrossName;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCrossNode;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeForm;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
 * @ClassName: GLM26033_1
 * @author songdongyan
 * @date 2017年2月28日
 * @Description: GLM26033_1.java
 * 新增路口名称
 * 删除路口名称
 * 路口名称语言代码编辑
 */
public class GLM26033_1 extends baseRule{

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
		for (IRow obj: checkCommand.getGlmList()){
			//新增/删除路口名称RdCrossName，路口名称语言代码编辑
			if (obj instanceof RdCrossName){
				RdCrossName rdCrossName = (RdCrossName) obj;
				checkRdCrossName(rdCrossName,checkCommand.getGlmList());
			}
		}
	}

	/**
	 * @param rdCrossName
	 * @param list 
	 * @throws Exception 
	 */
	private void checkRdCrossName(RdCrossName rdCrossName, List<IRow> list) throws Exception {
		if((rdCrossName.status().equals(ObjStatus.INSERT))||(rdCrossName.status().equals(ObjStatus.DELETE))){

			StringBuilder sb = new StringBuilder();
			
			sb.append("SELECT 1 FROM RD_CROSS_NAME RCN");
			sb.append(" WHERE RCN.LANG_CODE = 'CHI'");
			sb.append("   AND RCN.U_RECORD <> 2");
			sb.append("   AND NOT EXISTS (SELECT 1");
			sb.append("          FROM RD_CROSS_NAME RCNTMP");
			sb.append("         WHERE RCNTMP.NAME_GROUPID = RCN.NAME_GROUPID");
			sb.append("           AND RCNTMP.PID = RCN.PID");
			sb.append("           AND RCNTMP.LANG_CODE = 'ENG'");
			sb.append("           AND RCNTMP.U_RECORD <> 2)");
			sb.append("   AND RCN.PID = " + rdCrossName.getPid());
			sb.append(" UNION ");
			sb.append(" SELECT 1 FROM RD_CROSS_NAME RCN");
			sb.append(" WHERE RCN.LANG_CODE = 'ENG'");
			sb.append("   AND RCN.U_RECORD <> 2");
			sb.append("   AND NOT EXISTS (SELECT 1");
			sb.append("          FROM RD_CROSS_NAME RCNTMP");
			sb.append("         WHERE RCNTMP.NAME_GROUPID = RCN.NAME_GROUPID");
			sb.append("           AND RCNTMP.PID = RCN.PID");
			sb.append("           AND RCNTMP.LANG_CODE = 'CHI'");
			sb.append("           AND RCNTMP.U_RECORD <> 2)");
			sb.append("   AND RCN.PID = " + rdCrossName.getPid());

			String sql = sb.toString();
			log.info("RdCrossName后检查GLM26033_1:" + sql);

			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);

			if(resultList.size()>0){
				String target = "[RD_CROSS," + rdCrossName.getPid() + "]";
				this.setCheckResult("", target, 0);
			}
		}
		//路口名称语言代码编辑
		else if(rdCrossName.status().equals(ObjStatus.UPDATE)){
			if(rdCrossName.changedFields().containsKey("langCode")){
				String langCode = rdCrossName.changedFields().get("langCode").toString();
//				if((langCode.equals("CHI"))||(langCode.equals("ENG"))){
					
					StringBuilder sb = new StringBuilder();
					
					sb.append("SELECT 1 FROM RD_CROSS_NAME RCN");
					sb.append(" WHERE RCN.LANG_CODE = 'CHI'");
					sb.append("   AND RCN.U_RECORD <> 2");
					sb.append("   AND NOT EXISTS (SELECT 1");
					sb.append("          FROM RD_CROSS_NAME RCNTMP");
					sb.append("         WHERE RCNTMP.NAME_GROUPID = RCN.NAME_GROUPID");
					sb.append("           AND RCNTMP.PID = RCN.PID");
					sb.append("           AND RCNTMP.LANG_CODE = 'ENG'");
					sb.append("           AND RCNTMP.U_RECORD <> 2)");
					sb.append("   AND RCN.PID = " + rdCrossName.getPid());
					sb.append(" UNION ");
					sb.append(" SELECT 1 FROM RD_CROSS_NAME RCN");
					sb.append(" WHERE RCN.LANG_CODE = 'ENG'");
					sb.append("   AND RCN.U_RECORD <> 2");
					sb.append("   AND NOT EXISTS (SELECT 1");
					sb.append("          FROM RD_CROSS_NAME RCNTMP");
					sb.append("         WHERE RCNTMP.NAME_GROUPID = RCN.NAME_GROUPID");
					sb.append("           AND RCNTMP.PID = RCN.PID");
					sb.append("           AND RCNTMP.LANG_CODE = 'CHI'");
					sb.append("           AND RCNTMP.U_RECORD <> 2)");
					sb.append("   AND RCN.PID = " + rdCrossName.getPid());

					String sql = sb.toString();
					log.info("RdCrossName后检查GLM26033_1:" + sql);

					DatabaseOperator getObj = new DatabaseOperator();
					List<Object> resultList = new ArrayList<Object>();
					resultList = getObj.exeSelect(this.getConn(), sql);

					if(resultList.size()>0){
						String target = "[RD_CROSS," + rdCrossName.getPid() + "]";
						this.setCheckResult("", target, 0);
					}
//				}
			}
		}
		
	}
	


}
