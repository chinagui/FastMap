package com.navinfo.dataservice.engine.edit.bo.ad;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLinkMesh;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNode;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdNodeSelector;
import com.navinfo.dataservice.dao.pidservice.PidService;
import com.navinfo.dataservice.engine.edit.bo.AbstractBo;
import com.navinfo.dataservice.engine.edit.bo.BoFactory;
import com.navinfo.dataservice.engine.edit.bo.LinkBreakResult;
import com.navinfo.dataservice.engine.edit.bo.NodeBo;
import com.navinfo.dataservice.engine.edit.bo.LinkBo;
import com.navinfo.dataservice.engine.edit.bo.PoFactory;
import com.navinfo.dataservice.engine.edit.utils.NodeOperateUtils;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

/**
 * @ClassName: BoAdLink
 * @author xiaoxiaowen4127
 * @date 2016年7月15日
 * @Description: BoAdLink.java
 */
public class AdLinkBo extends LinkBo {
	protected Logger log = LoggerRepos.getLogger(this.getClass());
	
	private AdLink po;
	private AdNodeBo snodeBo;
	private AdNodeBo enodeBo;
	@Override
	public LinkBreakResult breakoff(Point point) throws Exception {
		LinkBreakResult result = super.breakoff(point);
		//设置新增的两条link的KIND
		AdLink newLeftLink = (AdLink)result.getNewLeftLink().getPo();
		if (newLeftLink.getMeshes()!=null&&newLeftLink.getMeshes().size() == 2) {
			newLeftLink.setKind(0);
		}
		AdLink newRightLink = (AdLink)result.getNewRightLink().getPo();
		if (newRightLink.getMeshes()!=null&&newRightLink.getMeshes().size() == 2) {
			newRightLink.setKind(0);
		}
		return result;
	}

	@Override
	public AdNodeBo getSnodeBo(){
		return snodeBo;
	}
	@Override
	public AdNodeBo loadSnodeBo()throws Exception{
		if (null == snodeBo) {
			AdNodeSelector adNodeSel = new AdNodeSelector(conn);
			AdNode adNode = (AdNode)adNodeSel.loadById(po.getsNodePid(), true);
			snodeBo = (AdNodeBo) BoFactory.getInstance().create(adNode);
		}
		return snodeBo;
	}
	@Override
	public void setSnodeBo(NodeBo snodeBo)throws Exception {
		if(!(snodeBo instanceof AdNodeBo)){
			throw new Exception("不支持的类型");
		}
		this.snodeBo = (AdNodeBo)snodeBo;
		this.po.setsNodePid(snodeBo.getPo().pid());
	}
	@Override
	public AdNodeBo getEnodeBo(){
		return enodeBo;
	}
	@Override
	public AdNodeBo loadEnodeBo() throws Exception {
		if (null == enodeBo) {
			AdNodeSelector adNodeSel = new AdNodeSelector(conn);
			AdNode adNode = (AdNode)adNodeSel.loadById(po.geteNodePid(), true);
			enodeBo = (AdNodeBo) BoFactory.getInstance().create(adNode);
		}
		return enodeBo;
	}
	@Override
	public void setEnodeBo(NodeBo enodeBo) throws Exception{
		if(!(enodeBo instanceof AdNodeBo)){
			throw new Exception("不支持的类型");
		}
		this.enodeBo = (AdNodeBo)enodeBo;
		this.po.seteNodePid(enodeBo.getPo().pid());
	}

	@Override
	public IObj getPo() {
		// TODO Auto-generated method stub
		return po;
	}

	@Override
	public void setPo(IObj po) {
		this.po = (AdLink) po;
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.edit.bo.LinkBo#getGeometry()
	 */
	@Override
	public Geometry getGeometry() {
		return po.getGeometry();
	}
	@Override
	public void setGeometry(Geometry geo){
		this.po.setGeometry(geo);
		this.po.setLength(GeometryUtils.getLinkLength(geo));
		
	}

	/**
	 * 主表clone，子表和其他直接引用
	 */
	@Override
	public AdLinkBo copy() throws Exception{
		AdLinkBo newBo = new AdLinkBo();
		AdLink newPo = new AdLink();
		newPo.copy(po);
		newPo.setPid(PidService.getInstance().applyAdNodePid());
		newBo.setPo(newPo);
		return null;
	}
	
	@Override
	public NodeBo createNewNodeBo(double x,double y)throws Exception{
		AdNodeBo newBo = new AdNodeBo();
		AdNode adNode = NodeOperateUtils.createAdNode(x, y);
		newBo.setPo(adNode);
		return newBo;
	}
	
	@Override
	public void computeMeshes()throws Exception{
		Set<String> meshes = CompGeometryUtil.geoToMeshesWithoutBreak(getGeometry());
		if(meshes!=null){
			List<IRow> meshIRows = new ArrayList<IRow>();
			for(String mesh:meshes){
				AdLinkMesh adLinkMesh = new AdLinkMesh();
				adLinkMesh.setLinkPid(po.getPid());
				adLinkMesh.setMesh(Integer.parseInt(mesh));
				meshIRows.add(adLinkMesh);
			}
			po.setMeshes(meshIRows);
		}
	}
}