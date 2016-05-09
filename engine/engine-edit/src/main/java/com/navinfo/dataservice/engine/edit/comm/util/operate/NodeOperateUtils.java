package com.navinfo.dataservice.engine.edit.comm.util.operate;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.service.PidService;
import com.navinfo.dataservice.commons.util.MeshUtils;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNode;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNodeMesh;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeForm;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeMesh;

public class NodeOperateUtils {
	
	public static RdNode createNode(double x, double y) throws Exception {

		RdNode node = new RdNode();

		node.setPid(PidService.getInstance().applyNodePid());

		node.setGeometry(GeoTranslator.transform(GeoTranslator.point2Jts(x, y),100000,0));

		node.setMesh(Integer.parseInt(MeshUtils.lonlat2Mesh(x, y)));

		RdNodeForm form = new RdNodeForm();

		form.setNodePid(node.getPid());
		
		form.setMesh(node.mesh());

		List<IRow> forms = new ArrayList<IRow>();

		forms.add(form);

		node.setForms(forms);

		RdNodeMesh mesh = new RdNodeMesh();
		
		mesh.setNodePid(node.getPid());

		mesh.setMeshId(node.mesh());

		List<IRow> meshes = new ArrayList<IRow>();

		meshes.add(mesh);

		node.setMeshes(meshes);

		return node;
	}

	/**
	 * @author zhaokk
	 * 创建行政区划点公共方法
	 * 1.如果行政区划点在图廓线上 ，生成多个Node对应图幅信息
	 */
	public static AdNode createAdNode(double x, double y) throws Exception {

		AdNode node = new AdNode();
		 //申请pid
		node.setPid(PidService.getInstance().applyAdNodePid());
		//获取点的几何信息
		node.setGeometry(GeoTranslator.transform(GeoTranslator.point2Jts(x, y),100000,0));
		//维护Node图幅信息
		List<String> meshes = MeshUtils.lonlat2MeshIds(x, y);
		for (String mesh :meshes){
			AdNodeMesh nodeMesh = new AdNodeMesh();
			node.setMesh(Integer.parseInt(mesh));
			nodeMesh.setNodePid(node.getPid());
			nodeMesh.setMeshId(node.mesh());
			List<IRow> nodeMeshs = new ArrayList<IRow>();
			nodeMeshs.add(nodeMesh);
			node.setMeshes(nodeMeshs);
		}
		return node;
	}
	
	
	
}
