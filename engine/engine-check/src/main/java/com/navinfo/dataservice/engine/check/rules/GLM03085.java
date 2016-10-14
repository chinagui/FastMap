package com.navinfo.dataservice.engine.check.rules;

import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeForm;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeMesh;
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
			if (obj instanceof RdNode){
				RdNode rdNode = (RdNode)obj;
				double rdNodeX = rdNode.getGeometry().getCoordinate().x;
				double rdNodeY = rdNode.getGeometry().getCoordinate().y;
				//判断点是不是在图廓线上
				if (MeshUtils.isPointAtMeshBorderWith100000(rdNodeX, rdNodeY)){
					List<IRow> fromList = rdNode.getForms();
					for (IRow form: fromList){
						RdNodeForm rdNodeForm = (RdNodeForm) form;
						int formOfWay = rdNodeForm.getFormOfWay();
						//点的形态不是图廓点
						if (formOfWay != 2){
							List<IRow> meshList = rdNode.getMeshes();
							if (!meshList.isEmpty()){
								RdNodeMesh rdNodeMesh = (RdNodeMesh)meshList.get(0);
								int meshId = rdNodeMesh.getMeshId();
								this.setCheckResult(rdNode.getGeometry(), "[RD_NODE,"+rdNode.getPid()+"]", meshId);
							}
						}
					}
				}
			}
		}
	}

}
