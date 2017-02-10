package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeForm;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
* @ClassName: GLM03069 
* @author: zhangpengpeng 
* @date: 2016年9月12日
* @Desc: GLM03069.java 图廓点只能挂接两条不同图幅的Link，
* 既只能有两条Link以该图廓点为起终点，且Link属于不同的图幅
* node属性编辑 服务端后检查:
* 新增LINK,分离节点,平滑修形 服务端后检查:
*/
public class GLM03069 extends baseRule{
	public GLM03069(){
	}
	
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
	}
	
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		for(IRow row:checkCommand.getGlmList()){
			//node属性编辑
			if (row instanceof RdNodeForm){
				RdNodeForm rdNodeForm = (RdNodeForm) row;
				this.checkRdNodeForm(rdNodeForm);
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
	private void checkRdNodeForm(RdNodeForm rdNodeForm) throws Exception {
		// TODO Auto-generated method stub
		boolean checkFlag = false;
		if(rdNodeForm.status().equals(ObjStatus.UPDATE)){
			Map<String, Object> changedFields = rdNodeForm.changedFields();
			if(!changedFields.isEmpty()){
				if(changedFields.containsKey("formOfWay")){
					int formOfWay = (int) changedFields.get("formOfWay");
					if(formOfWay == 2){
						checkFlag = true;
					}
				}
			}
		}else if (rdNodeForm.status().equals(ObjStatus.INSERT)){
			int formOfWay = rdNodeForm.getFormOfWay();
			if(formOfWay == 2){
				checkFlag = true;
			}
		}
		if(checkFlag){
			boolean check = this.check(rdNodeForm.getNodePid());
			
			if(check){
				String target = "[RD_NODE," + rdNodeForm.getNodePid() + "]";
				this.setCheckResult("", target, 0);
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
				String target = "[RD_LINK," + rdLink.getPid() + "]";
				this.setCheckResult("", target, 0);
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
        sb.append("select rn.LINK_PID from RD_LINK rn where (rn.E_NODE_PID= ");
        sb.append(nodePid);
        sb.append(" or rn.S_NODE_PID= ");
        sb.append(nodePid);
        sb.append(" ) and rn.U_RECORD <> 2");
		String sql = sb.toString();
		
		DatabaseOperator getObj=new DatabaseOperator();
		List<Object> resultList=new ArrayList<Object>();
		resultList=getObj.exeSelect(this.getConn(), sql);
		
		if (resultList.size() == 2){
			//挂接2根link
			StringBuilder sb1 = new StringBuilder();
			sb1.append("select distinct rn.MESH_ID from RD_LINK rn where RN.U_RECORD != 2 AND rn.LINK_PID IN ( ");
			sb1.append(Integer.parseInt(resultList.get(0).toString()));
			sb1.append(" , ");
			sb1.append(Integer.parseInt(resultList.get(1).toString()));
			sb1.append(" )");
			String sql1 = sb1.toString();
			
			DatabaseOperator getObj1=new DatabaseOperator();
			List<Object> resultList1=new ArrayList<Object>();
			resultList1=getObj1.exeSelect(this.getConn(), sql1);
			if (resultList1.size() != 2){
				flag = true;
			}
		}else if (resultList.size() != 2){
			flag = true;
		}
		return flag;
	}
}
