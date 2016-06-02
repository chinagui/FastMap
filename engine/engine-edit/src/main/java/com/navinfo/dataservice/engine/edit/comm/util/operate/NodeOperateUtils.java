package com.navinfo.dataservice.engine.edit.comm.util.operate;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNode;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNodeMesh;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeForm;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeMesh;
import com.navinfo.dataservice.dao.pidservice.PidService;
import com.navinfo.navicommons.geo.computation.MeshUtils;

public class NodeOperateUtils {

	public static RdNode createNode(double x, double y) throws Exception {

		RdNode node = new RdNode();

		node.setPid(PidService.getInstance().applyNodePid());

		node.setGeometry(GeoTranslator.transform(GeoTranslator.point2Jts(x, y), 100000, 0));

		List<IRow> meshes = new ArrayList<IRow>();

		for (String meshId : MeshUtils.point2Meshes(x, y)) {
			RdNodeMesh mesh = new RdNodeMesh();

			mesh.setNodePid(node.getPid());

			mesh.setMeshId(Integer.parseInt(meshId));

			meshes.add(mesh);
		}
		
		node.setMeshes(meshes);

		RdNodeForm form = new RdNodeForm();

		form.setNodePid(node.getPid());

		if(meshes.size()>1)
		{
			//图郭点
			form.setFormOfWay(2);
		}

		List<IRow> forms = new ArrayList<IRow>();

		forms.add(form);

		node.setForms(forms);

		return node;
	}

	/**
	 * @author zhaokk 创建行政区划点公共方法 1.如果行政区划点在图廓线上 ，生成多个Node对应图幅信息
	 */
	public static AdNode createAdNode(double x, double y) throws Exception {

		AdNode node = new AdNode();
		// 申请pid
		node.setPid(PidService.getInstance().applyAdNodePid());
		// 获取点的几何信息
		node.setGeometry(GeoTranslator.transform(GeoTranslator.point2Jts(x, y), 100000, 0));
		// 维护Node图幅信息
		// 判断是否图廓点
		if (MeshUtils.isPointAtMeshBorder(x, y)) {
			node.setForm(1);
		}
		for (String mesh : MeshUtils.point2Meshes(x, y)) {
			AdNodeMesh nodeMesh = new AdNodeMesh();
			nodeMesh.setNodePid(node.getPid());
			nodeMesh.setMeshId(Integer.parseInt(mesh));
			List<IRow> nodeMeshs = new ArrayList<IRow>();
			nodeMeshs.add(nodeMesh);
			node.setMeshes(nodeMeshs);
		}
		return node;
	}

}
