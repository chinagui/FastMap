package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeForm;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeMesh;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
* @ClassName: GLM03004 
* @author: zhangpengpeng 
* @date: 2016年8月30日
* @Desc: GLM03004.java 种别为“路上点”的点挂接Link数应等于2；
* 注意：点的属性为“路上点（收费站）”时，明确说明是收费站的错误；
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
		//获取RDNode的信息
		for (IRow obj: checkCommand.getGlmList()){
			if (obj instanceof RdNode){
				RdNode rdNode = (RdNode) obj;
				Map<String, Object> changedFields = rdNode.changedFields();
				int kind = 1;
				if(changedFields.containsKey("kind")){
					kind = (int) changedFields.get("kind");
				}
				//路上点
				if (kind == 3){
					//查询node点挂接的link
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
					// 点挂接link数不为2
					if (resultList.size() != 2){
						List<IRow> meshList = rdNode.getMeshes();
						int meshId = 0;
						if (!meshList.isEmpty()){
							RdNodeMesh rdNodeMesh = (RdNodeMesh) meshList.get(0);
							meshId = rdNodeMesh.getMeshId();
						}
						List<IRow> formList = rdNode.getForms();
						for (IRow form: formList){
							RdNodeForm rdNodeForm = (RdNodeForm) form;
							int formOfWay = rdNodeForm.getFormOfWay();
							if (formOfWay == 4){
								//路上点是收费站
								String log = "路上点(收费站)只能挂接2根Link";
								this.setCheckResult(rdNode.getGeometry(), "[RD_NODE,"+rdNode.getPid()+"]", meshId, log);
							}else{
								//非收费站
								this.setCheckResult(rdNode.getGeometry(), "[RD_NODE,"+rdNode.getPid()+"]", meshId);
							}
						}
					}
				}
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		
	}
	
}
