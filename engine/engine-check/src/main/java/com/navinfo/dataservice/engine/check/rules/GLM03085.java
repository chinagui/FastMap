package com.navinfo.dataservice.engine.check.rules;

import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeForm;
import com.navinfo.dataservice.dao.glm.selector.rd.node.RdNodeSelector;
import com.navinfo.navicommons.geo.computation.MeshUtils;

/** 
* @ClassName: GLM03085 
* @author: zhangpengpeng 
* @date: 2016年8月30日
* @Desc: GLM03085.java 图廓线上的点的形态必须是“图廓点”
*/
public class GLM03085 extends baseRule {
	public GLM03085(){	
	}
	
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		
		for (IRow obj: checkCommand.getGlmList()){
			//获取RdNode信息
			if (obj instanceof RdNodeForm){
				RdNodeForm rdNodeForm = (RdNodeForm)obj;
				int nodePid = rdNodeForm.getNodePid();
				//查询主表
				RdNodeSelector nodeSelector=new RdNodeSelector(this.getConn());
				String sql = "SELECT * FROM RD_NODE WHERE NODE_PID="+nodePid;
				List<RdNode> rdNodes = nodeSelector.loadBySql(sql, false);
				if(!rdNodes.isEmpty()){
					RdNode rdNode=rdNodes.get(0);
					double rdNodeX = rdNode.getGeometry().getCoordinate().x;
					double rdNodeY = rdNode.getGeometry().getCoordinate().y;
					//判断点是不是在图廓线上
					if (MeshUtils.isPointAtMeshBorderWith100000(rdNodeX, rdNodeY)){
						boolean checkFlag = false;
						if(rdNodeForm.status().equals(ObjStatus.UPDATE)){
							Map<String, Object> changedFields = rdNodeForm.changedFields();
							if(!changedFields.isEmpty()){
								if(changedFields.containsKey("formOfWay")){
									int formOfWay = (int) changedFields.get("formOfWay");
									if(formOfWay != 2){
										checkFlag = true;
									}
								}
							}
						}else if (rdNodeForm.status().equals(ObjStatus.INSERT)){
							int formOfWay = rdNodeForm.getFormOfWay();
							if(formOfWay != 2){
								checkFlag = true;
							}
						}
						if(checkFlag){
							this.setCheckResult("", "[RD_NODE,"+nodePid+"]", 0);
						}
						/*Map<String, Object> changedFields = rdNodeForm.changedFields();
						int formOfWay = (int) changedFields.get("formOfWay");
						//点的形态不是图廓点
						if (formOfWay != 2){
							this.setCheckResult("", "[RD_NODE,"+nodePid+"]", 0);
						}*/
					}
				}
			}
		}
	}

}
