package com.navinfo.dataservice.FosEngine.edit.operation.obj.rdlink.create;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.FosEngine.edit.model.IRow;
import com.navinfo.dataservice.FosEngine.edit.model.ObjStatus;
import com.navinfo.dataservice.FosEngine.edit.model.Result;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLink;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLinkForm;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLinkSpeedlimit;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.node.RdNode;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.node.RdNodeForm;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.node.RdNodeMesh;
import com.navinfo.dataservice.FosEngine.edit.operation.IOperation;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.service.PidService;
import com.navinfo.dataservice.commons.util.MeshUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class Operation implements IOperation {

	private Command command;

	public Operation(Command command) {
		this.command = command;

	}
	
	@Override
	public String run(Result result) throws Exception {
		
		String msg = null;
		
		Geometry geometry = GeoTranslator.geojson2Jts(command.getGeometry(), 100000, 0);
		
		RdLink link = new RdLink();
		
		link.setPid(PidService.getInstance().applyLinkPid());
		
		result.setPrimaryPid(link.getPid());
		
		link.setGeometry(geometry);
		
		link.setOriginLinkPid(link.getPid());
		
		link.setWidth(55);
		
		Coordinate[] coords = geometry.getCoordinates();
				
		if (0 == command.getsNodePid()){
			
			Coordinate point = coords[0];
			
			RdNode node = createNode(point.x, point.y);
			
			link.setsNodePid(node.getPid());
			
			result.insertObject(node, ObjStatus.INSERT);
		}
		else{
			link.setsNodePid(command.getsNodePid());
		}
		
		if (0 == command.geteNodePid()){
			
			Coordinate point = coords[coords.length-1];
			
			RdNode node = createNode(point.x, point.y);
			
			link.seteNodePid(node.getPid());
			
			result.insertObject(node, ObjStatus.INSERT);
		}
		else{
			link.seteNodePid(command.geteNodePid());
		}
		
		setLinkChildren(link);
		
		result.insertObject(link, ObjStatus.INSERT);
		
		return msg;
	}
	
	/**
	 * 维护link的子表
	 * @param link
	 */
	private void setLinkChildren(RdLink link){
		
		RdLinkForm form = new RdLinkForm();
		
		form.setLinkPid(link.getPid());
		
		List<IRow> forms = new ArrayList<IRow>();
		
		forms.add(form);
		
		link.setForms(forms);
		
		RdLinkSpeedlimit speedlimit = new RdLinkSpeedlimit();
		
		speedlimit.setLinkPid(link.getPid());
		
		List<IRow> speedlimits = new ArrayList<IRow>();
		
		speedlimits.add(speedlimit);
		
		link.setSpeedlimits(speedlimits);
		
	}
	
	/**
	 * 创建一个rdnode
	 * @param x 经度
	 * @param y 纬度
	 * @return rdnode
	 * @throws Exception
	 */
	private RdNode createNode(double x , double y) throws Exception{
		
		RdNode node = new RdNode();
		
		node.setPid(PidService.getInstance().applyNodePid());
		
		node.setGeometry(GeoTranslator.point2Jts(x, y));
		
		RdNodeForm form = new RdNodeForm();
		
		form.setNodePid(node.getPid());
		
		List<IRow> forms = new ArrayList<IRow>();
		
		forms.add(form);
		
		node.setForms(forms);
		
		RdNodeMesh mesh = new RdNodeMesh();
		
		mesh.setNodePid(node.getPid());
		
		mesh.setMeshId(Integer.valueOf(MeshUtils.lonlat2Mesh(x/100000.0, y/100000.0)));
		
		List<IRow> meshes = new ArrayList<IRow>();
		
		meshes.add(mesh);
		
		node.setMeshes(meshes);
		
		return node;
	}
	
}
