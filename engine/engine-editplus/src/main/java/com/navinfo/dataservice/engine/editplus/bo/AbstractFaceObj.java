package com.navinfo.dataservice.engine.editplus.bo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.engine.editplus.model.AbstractFace;
import com.navinfo.dataservice.engine.editplus.model.AbstractFaceTopo;
import com.navinfo.dataservice.engine.editplus.model.BasicRow;
import com.navinfo.dataservice.engine.editplus.model.obj.BasicObj;
import com.navinfo.dataservice.engine.editplus.operation.OperationResult;
import com.navinfo.dataservice.engine.editplus.operation.OperationType;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Geometry;

public abstract class AbstractFaceObj extends BasicObj{

	public AbstractFaceObj(BasicRow mainrow) {
		super(mainrow);
		// TODO Auto-generated constructor stub
	}


	protected Map<Long,AbstractLinkBo> topoLinkBoMap;
	
	public Geometry getGeometry(){
		return ((AbstractFace)getObj()).getGeometry();
	}
	public void setGeometry(Geometry geo){
		if(getObj().checkValue("GEOMETRY", getGeometry(), geo)){
			((AbstractFace)getObj()).setGeometry(geo);
		}
		double area = GeometryUtils.getCalculateArea(geo);
		setArea(area);
		double perimeter = GeometryUtils.getLinkLength(geo);
		setPerimeter(perimeter);
		
	}
	public void setGeometryAll(Geometry geo){
		setGeometry(geo);
		double area = GeometryUtils.getCalculateArea(geo);
		setArea(area);
		double perimeter = GeometryUtils.getLinkLength(geo);
		setPerimeter(perimeter);
		
	}
	public double getArea(){
		return ((AbstractFace)getObj()).getArea();
	}
	public void setArea(double area){
		if(getObj().checkValue("AREA", getArea(), area)){
			((AbstractFace)getObj()).setArea(area);
		}
		
	}
	public double getPerimeter(){
		return ((AbstractFace)getObj()).getPerimeter();
	}
	public void setPerimeter(double perimeter){
		if(getObj().checkValue("PERIMETER", getPerimeter(), perimeter)){
			((AbstractFace)getObj()).setPerimeter(perimeter);
		}
	}
	public int getMeshId(){
		return ((AbstractFace)getObj()).getMeshId();
	}
	public void setMeshId(int meshId){
		if(getObj().checkValue("MESH_ID", getMeshId(), meshId)){
			((AbstractFace)getObj()).setMeshId(meshId);
		}
	}
	public List<AbstractFaceTopo> getTopos(){
		return ((AbstractFace)getObj()).getTopos();
	}
	public void setTopos(List<AbstractFaceTopo> topos){
		if(getObj().checkChildren(getTopos(), topos)){
			((AbstractFace)getObj()).setTopos(topos);
		}
	}
	public Map<Long,AbstractLinkBo> getTopoLinkBoMap() {
		return topoLinkBoMap;
	}
	public void setTopoLinkBoMap(Map<Long,AbstractLinkBo> topoLinkBoMap) {
		this.topoLinkBoMap = topoLinkBoMap;
	}
	public abstract AbstractFaceTopo createTopo(long linkPid);
	public int getValidTopoLength(){
		int count=0;
		for(AbstractFaceTopo t:getTopos()){
			if(!t.getOpType().equals(OperationType.DELETE)){
				count++;
			}
		}
		return count;
	}
	
/* operation */
	public OperationResult breakoff(AbstractLinkBo oldLink, AbstractLinkBo newLeftLink,
			AbstractLinkBo newRightLink) throws Exception {
		OperationResult result = new OperationResult();
		List<AbstractFaceTopo> topos = new ArrayList<AbstractFaceTopo>();
		for (AbstractFaceTopo topo : getTopos()) {
			if (topo.getLinkPid() == oldLink.getPid()) {
				topo.setOpType(OperationType.DELETE);//Po层数据是不能删除的，只能打状态
				topoLinkBoMap.remove(oldLink.getPid());//Bo层数据可以删除
			}else{
				topo.setOpType(OperationType.UPDATE);
			}
		}
		topos.add(createTopo(newLeftLink.getPid()));
		topoLinkBoMap.put(newLeftLink.getPid(), newLeftLink);
		topos.add(createTopo(newRightLink.getPid()));
		topoLinkBoMap.put(newRightLink.getPid(), newRightLink);

//		if (topos.size() < 1) {
//			throw new Exception("重新维护面的形状:发现面没有组成link");
//		}
		reorderTopos();
		return result;
	}
	
	
	public void reorderTopos(){
		//1.调整topo的seq
		//...
		//2.根据调整好的topo生成新的face的Geometry
		//...
	}
	
}
