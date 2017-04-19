package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeForm;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/**
 * @ClassName GLM03082
 * @author Han Shaoming
 * @date 2017年3月23日 上午10:00:51
 * @Description TODO
 * Node具有“门牌号码点”形态时，种别必须为“路上点”，否则报log
 * node属性编辑	服务端后检查
 * node种别编辑	服务端后检查
 */
public class GLM03082 extends baseRule {

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		for(IRow row:checkCommand.getGlmList()){
			//node种别编辑
			if(row instanceof RdNode){
				RdNode rdNode = (RdNode)row;
				checkRdNode(rdNode);
			}
			//node属性编辑
			if (row instanceof RdNodeForm){
				RdNodeForm rdNodeForm = (RdNodeForm) row;
				this.checkRdNodeForm(rdNodeForm);
			}
		}
	}
	
	/**
	 * @author Han Shaoming
	 * @param rdNode
	 * @throws Exception 
	 */
	private void checkRdNode(RdNode rdNode) throws Exception {
		boolean checkFlag = false;
		if(rdNode.status().equals(ObjStatus.UPDATE)){
			Map<String, Object> changedFields = rdNode.changedFields();
			if(!changedFields.isEmpty()){
				if(changedFields.containsKey("kind")){
					int kind = (int) changedFields.get("kind");
					if(kind != 3){
						checkFlag = true;
					}
				}
			}
		}
		if(checkFlag){
			StringBuilder sb = new StringBuilder();
			 
			sb.append("SELECT F.NODE_PID FROM RD_NODE_FORM F");
			sb.append(" WHERE F.NODE_PID="+rdNode.getPid());
			sb.append(" AND F.FORM_OF_WAY = 16 AND F.U_RECORD != 2");
			String sql = sb.toString();
			log.info("RdNode后检查GLM03082--sql:" + sql);
			
			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);
			
			if(!resultList.isEmpty()){
				String target = "[RD_NODE," + rdNode.getPid() + "]";
				this.setCheckResult("", target, 0);
			}
		}
	}

	/**
	 * @author Han Shaoming
	 * @param rdNodeForm
	 * @throws Exception 
	 */
	private void checkRdNodeForm(RdNodeForm rdNodeForm) throws Exception {
		boolean checkFlag = false;
		if(rdNodeForm.status().equals(ObjStatus.UPDATE)){
			Map<String, Object> changedFields = rdNodeForm.changedFields();
			if(!changedFields.isEmpty()){
				if(changedFields.containsKey("formOfWay")){
					int formOfWay = (int) changedFields.get("formOfWay");
					if(formOfWay == 16){
					checkFlag = true;
					}
				}
			}
		}else if (rdNodeForm.status().equals(ObjStatus.INSERT)){
			int formOfWay = rdNodeForm.getFormOfWay();
			if(formOfWay == 16){
				checkFlag = true;
			}
		}
		if(checkFlag){
			StringBuilder sb = new StringBuilder();
			  
			sb.append("SELECT N.NODE_PID FROM RD_NODE N");
			sb.append(" WHERE N.NODE_PID="+rdNodeForm.getNodePid());
			sb.append(" AND N.KIND <> 3 AND N.U_RECORD != 2");
			String sql = sb.toString();
			log.info("RdNode后检查GLM03082--sql:" + sql);
			
			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);
			
			if(!resultList.isEmpty()){
				String target = "[RD_NODE," + rdNodeForm.getNodePid() + "]";
				this.setCheckResult("", target, 0);
			}
		}
	}

}
