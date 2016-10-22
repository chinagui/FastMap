package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeForm;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
* @ClassName: GLM03069 
* @author: zhangpengpeng 
* @date: 2016年9月12日
* @Desc: GLM03069.java 图廓点只能挂接两条不同图幅的Link，
* 既只能有两条Link以该图廓点为起终点，且Link属于不同的图幅
*/
public class GLM03069 extends baseRule{
	public GLM03069(){
	}
	
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
	}
	
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		for(IRow obj: checkCommand.getGlmList()){
			if (obj instanceof RdNode){
				RdNode rdNode = (RdNode) obj;
				List<IRow> forms = rdNode.getForms();
				boolean isBorderNode = false;
				for (IRow form: forms){
					RdNodeForm rdNodeForm = (RdNodeForm) form;
					if (rdNodeForm.getFormOfWay() == 2){
						isBorderNode = true;
						break;
					}
				}
				if (isBorderNode){
					//图廓点
					int rdNodePid = rdNode.getPid();
					StringBuilder sb = new StringBuilder();
			        sb.append("select rn.LINK_PID from RD_LINK rn where (rn.E_NODE_PID= ");
			        sb.append(rdNodePid);
			        sb.append(" or rn.S_NODE_PID= ");
			        sb.append(rdNodePid);
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
							this.setCheckResult("", "[RD_NODE,"+rdNode.getPid()+"]", 0);
						}
					}else if (resultList.size() > 2){
						String log = "图廓点挂接了2个以上link";
						this.setCheckResult("", "[RD_NODE,"+rdNode.getPid()+"]", 0, log);
					}
				}
			}
		}
	}
}
