package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeForm;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
* @ClassName: GLM03004 
* @author: zhangpengpeng 
* @date: 2016年8月30日
* @Desc: GLM03004.java 种别为“路上点”的点挂接Link数应等于2；
* 注意：点的属性为“路上点（收费站）”时，明确说明是收费站的错误；
* node种别编辑 服务端后检查:
* 新增LINK,分离节点,平滑修形 服务端后检查:
*/
public class GLM03004 extends baseRule{
	public GLM03004(){	
	}
	
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		for(IRow row:checkCommand.getGlmList()){
			//node种别编辑
			if (row instanceof RdNode){
				RdNode rdNode = (RdNode) row;
				this.checkRdNode(rdNode);
			}
			//新增LINK,分离节点,平滑修形
			else if (row instanceof RdLink){
				RdLink rdLink = (RdLink) row;
				this.checkRdLink(rdLink);
			}
		}
	}
	
	/**
	 * @author Han Shaoming
	 * @param rdNodeForm
	 * @throws Exception 
	 */
	private void checkRdNode(RdNode rdNode) throws Exception {
		// TODO Auto-generated method stub
		boolean checkFlag = false;
		if(rdNode.status().equals(ObjStatus.UPDATE)){
			Map<String, Object> changedFields = rdNode.changedFields();
			if(!changedFields.isEmpty()){
				if(changedFields.containsKey("kind")){
					int kind = (int) changedFields.get("kind");
					if(kind == 3){
						checkFlag = true;
					}
				}
			}
		}
		if(checkFlag){
			boolean check = this.check(rdNode.getPid());
			
			if(check){
				List<IRow> formList = rdNode.getForms();
				for (IRow form: formList){
					RdNodeForm rdNodeForm = (RdNodeForm) form;
					int formOfWay = rdNodeForm.getFormOfWay();
					if (formOfWay == 4){
						//路上点是收费站
						this.setCheckResult(rdNode.getGeometry(), "[RD_NODE,"+rdNode.getPid()+"]", 0, "路上点(收费站)只能挂接2根Link");
					}else{
						//非收费站
						this.setCheckResult(rdNode.getGeometry(), "[RD_NODE,"+rdNode.getPid()+"]",0, "路上点只能挂接2根Link");
					}
				}
			}
		}
	}

	/**
	 * @author Han Shaoming
	 * @param rdLink
	 * @throws Exception 
	 */
	private void checkRdLink(RdLink rdLink) throws Exception {
		// TODO Auto-generated method stub
		Map<String, Object> changedFields = rdLink.changedFields();
		Set<Integer> nodePids = new HashSet<Integer>();
		//新增LINK
		if(ObjStatus.INSERT.equals(rdLink.status())){
			nodePids.add(rdLink.getsNodePid());
			nodePids.add(rdLink.geteNodePid());
		}
		//分离节点,平滑修形
		if(ObjStatus.UPDATE.equals(rdLink.status())){
			Integer sNodePid = null;
			Integer eNodePid = null;
			if(changedFields.containsKey("sNodePid")){
				nodePids.add(rdLink.getsNodePid());
				sNodePid = (Integer) changedFields.get("sNodePid");
				if(sNodePid != null){
					nodePids.add(sNodePid);
				}
			}
			if(changedFields.containsKey("eNodePid")){
				nodePids.add(rdLink.geteNodePid());
				eNodePid = (Integer) changedFields.get("eNodePid");
				if(eNodePid != null){
					nodePids.add(eNodePid);
				}
			}
		}
		for (Integer nodePid : nodePids) {
			boolean check = this.check(nodePid);

			if(check){
				StringBuilder sb = new StringBuilder();
				
				sb.append("SELECT RNF.NODE_PID FROM RD_NODE_FORM RNF");
				sb.append(" WHERE RNF.NODE_PID = "+nodePid);
				sb.append(" AND RNF.FORM_OF_WAY = 4 AND RNF.U_RECORD <> 2");
				String sql = sb.toString();
				log.info("后检查GLM03004判断node是否为收费站--sql:" + sql);
				
				DatabaseOperator getObj = new DatabaseOperator();
				List<Object> resultList = new ArrayList<Object>();
				resultList = getObj.exeSelect(this.getConn(), sql);
				
				if(!resultList.isEmpty()){
					String target = "[RD_LINK," + rdLink.getPid() + "]";
					this.setCheckResult("", target, 0,"路上点(收费站)只能挂接2根Link");
				}else{
					String target = "[RD_LINK," + rdLink.getPid() + "]";
					this.setCheckResult("", target, 0,"路上点只能挂接2根Link");
				}
			}
		}
	}
	
	/**
	 * @author Han Shaoming
	 * @param rdNodeForm
	 * @throws Exception 
	 */
	private boolean check(int nodePid) throws Exception {
		// TODO Auto-generated method stub
		boolean flag = false;
		 
		StringBuilder sb = new StringBuilder();
        sb.append("SELECT L.LINK_PID FROM RD_NODE RD, RD_LINK L");
        sb.append(" WHERE (RD.NODE_PID = L.S_NODE_PID OR RD.NODE_PID = L.E_NODE_PID)");
        sb.append(" AND RD.NODE_PID = "+nodePid);
        sb.append(" AND RD.KIND = 3 AND RD.U_RECORD != 2 AND L.U_RECORD != 2");
		String sql = sb.toString();
		
		DatabaseOperator getObj=new DatabaseOperator();
		List<Object> resultList=new ArrayList<Object>();
		resultList=getObj.exeSelect(this.getConn(), sql);
		// 点挂接link数不为2
		if (resultList.size() != 2){
			flag = true;
		}
		return flag;
	}
	
}
