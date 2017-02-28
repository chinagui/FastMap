package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCrossNode;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeForm;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
 * @ClassName: GLM26045
 * @author songdongyan
 * @date 2017年2月28日
 * @Description: GLM26045.java
 * 新增路口后检查
 * 修改路口后检查
 * NODE属性编辑后检查
 */
public class GLM26045 extends baseRule{

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
			//新增路口RdCross
			if (obj instanceof RdCross){
				RdCross rdCross = (RdCross) obj;
				checkRdCross(rdCross);
			}
			//修改路口RdCrossNode
			else if (obj instanceof RdCrossNode){
				RdCrossNode rdCrossNode = (RdCrossNode) obj;
				checkRdCrossNode(rdCrossNode);
			}
			//NODE属性编辑
			else if (obj instanceof RdNodeForm){
				RdNodeForm rdNodeForm = (RdNodeForm) obj;
				checkRdNodeForm(rdNodeForm);
			}
		}
	}

	/**
	 * @param rdNodeForm
	 * @throws Exception 
	 */
	private void checkRdNodeForm(RdNodeForm rdNodeForm) throws Exception {
		boolean checjFlg = false;
		if(rdNodeForm.status().equals(ObjStatus.INSERT)){
			if(rdNodeForm.getFormOfWay()==2){
				checjFlg = true;
			}
		}
		
		else if(rdNodeForm.status().equals(ObjStatus.UPDATE)){
			if(rdNodeForm.changedFields().containsKey("formOfWay")){
				int formOfWay = Integer.parseInt(rdNodeForm.changedFields().get("formOfWay").toString());
				if(formOfWay==2){
					checjFlg = true;
				}
			}
		}
		
		if(checjFlg){
			StringBuilder sb = new StringBuilder();
			
			sb.append("SELECT RCN.PID FROM RD_CROSS_NODE RCN, RD_NODE_FORM RNF");
			sb.append(" WHERE RCN.NODE_PID = RNF.NODE_PID");
			sb.append("   AND RNF.FORM_OF_WAY = 2");
			sb.append("   AND RCN.NODE_PID = " + rdNodeForm.getNodePid());
			sb.append("   AND RCN.U_RECORD <> 2");
			sb.append("   AND RNF.U_RECORD <> 2");

			String sql = sb.toString();
			log.info("RdNodeForm后检查GLM26045:" + sql);

			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);

			if(resultList.size()>0){
				String target = "[RD_NODE," + rdNodeForm.getNodePid() + "]";
				this.setCheckResult("", target, 0);
			}
		}	
	}

	/**
	 * @param rdCrossNode
	 * @throws Exception 
	 */
	private void checkRdCrossNode(RdCrossNode rdCrossNode) throws Exception {
		if(rdCrossNode.status().equals(ObjStatus.INSERT)){
			StringBuilder sb = new StringBuilder();
			
			sb.append("SELECT 1 FROM RD_CROSS_NODE RCN, RD_NODE_FORM RNF");
			sb.append(" WHERE RCN.NODE_PID = RNF.NODE_PID");
			sb.append("   AND RNF.FORM_OF_WAY = 2");
			sb.append("   AND RCN.NODE_PID = " + rdCrossNode.getNodePid());
			sb.append("   AND RCN.U_RECORD <> 2");
			sb.append("   AND RNF.U_RECORD <> 2");

			String sql = sb.toString();
			log.info("RdCross后检查GLM26045:" + sql);

			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);

			if(resultList.size()>0){
				String target = "[RD_CROSS," + rdCrossNode.getPid() + "]";
				this.setCheckResult("", target, 0);
			}
		}	
		
	}

	/**
	 * @param rdCross
	 * @throws Exception 
	 */
	private void checkRdCross(RdCross rdCross) throws Exception {
		if(rdCross.status().equals(ObjStatus.INSERT)){
			StringBuilder sb = new StringBuilder();
			
			sb.append("SELECT 1 FROM RD_CROSS_NODE RCN, RD_NODE_FORM RNF");
			sb.append(" WHERE RCN.NODE_PID = RNF.NODE_PID");
			sb.append("   AND RNF.FORM_OF_WAY = 2");
			sb.append("   AND RCN.PID = " + rdCross.getPid());
			sb.append("   AND RCN.U_RECORD <> 2");
			sb.append("   AND RNF.U_RECORD <> 2");

			String sql = sb.toString();
			log.info("RdCross后检查GLM26045:" + sql);

			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);

			if(resultList.size()>0){
				String target = "[RD_CROSS," + rdCross.getPid() + "]";
				this.setCheckResult("", target, 0);
			}
		}	
	}


}
