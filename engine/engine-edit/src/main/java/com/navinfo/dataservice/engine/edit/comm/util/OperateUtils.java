package com.navinfo.dataservice.engine.edit.comm.util;

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

public class OperateUtils {
	
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
	
	public static AdNode createAdNode(double x, double y) throws Exception {

		AdNode node = new AdNode();

		node.setPid(PidService.getInstance().applyAdNodePid());

		node.setGeometry(GeoTranslator.transform(GeoTranslator.point2Jts(x, y),100000,0));

		node.setMesh(Integer.parseInt(MeshUtils.lonlat2Mesh(x, y)));
		
		AdNodeMesh nodeMesh = new AdNodeMesh();
		nodeMesh.setNodePid(node.getPid());
		nodeMesh.setMeshId(node.mesh());
		List<IRow> nodeMeshs = new ArrayList<IRow>();
		nodeMeshs.add(nodeMesh);
		node.setMeshes(nodeMeshs);
		return node;
	}
	
}
