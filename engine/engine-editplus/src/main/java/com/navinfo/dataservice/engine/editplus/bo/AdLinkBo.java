//package com.navinfo.dataservice.engine.editplus.bo;
//
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Set;
//
//import org.apache.log4j.Logger;
//
//import com.navinfo.dataservice.bizcommons.service.PidUtil;
//
//import com.navinfo.dataservice.commons.log.LoggerRepos;
//import com.navinfo.dataservice.engine.editplus.model.BasicRow;
//import com.navinfo.dataservice.engine.editplus.model.ad.AdLink;
//import com.navinfo.dataservice.engine.editplus.model.ad.AdLinkMesh;
//import com.navinfo.dataservice.engine.editplus.model.obj.BasicObj;
//import com.navinfo.dataservice.engine.editplus.operation.LinkBreakResult;
//import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
//
//import com.vividsolutions.jts.geom.Point;
//
///**
// * @ClassName: BoAdLink
// * @author xiaoxiaowen4127
// * @date 2016年7月15日
// * @Description: BoAdLink.java
// */
//public class AdLinkBo extends AbstractLinkObj {
//	protected Logger log = LoggerRepos.getLogger(this.getClass());
//
//	private AdLink obj;
//	private AdNodeBo snodeBo;
//	private AdNodeBo enodeBo;
//
//	public List<BasicRow> getMeshes() {
//		return obj.getMeshes();
//	}
//
//	public void setMeshes(List<BasicRow> meshes) {
//		if (getObj().checkChildren(getMeshes(), meshes)) {
//			((AdLink) getObj()).setMeshes(meshes);
//		}
//	}
//
//	@Override
//	public LinkBreakResult breakoff(Point point) throws Exception {
//		LinkBreakResult result = super.breakoff(point);
//		// 设置新增的两条link的KIND
//		AdLink newLeftLink = (AdLink) result.getNewLeftLink().getObj();
//		if (newLeftLink.getMeshes() != null
//				&& newLeftLink.getMeshes().size() == 2) {
//			newLeftLink.setKind(0);
//		}
//		AdLink newRightLink = (AdLink) result.getNewRightLink().getObj();
//		if (newRightLink.getMeshes() != null
//				&& newRightLink.getMeshes().size() == 2) {
//			newRightLink.setKind(0);
//		}
//		return result;
//	}
//
//	@Override
//	public AdNodeBo getSnodeBo() {
//		return snodeBo;
//	}
//
//	@Override
//	public void setSnodeBo(AbstractNodeBo snodeBo) throws Exception {
//		if (!(snodeBo instanceof AdNodeBo)) {
//			throw new Exception("不支持的类型");
//		}
//		this.snodeBo = (AdNodeBo) snodeBo;
//		this.setsNodePid(snodeBo.getObj().objPid());
//	}
//
//	@Override
//	public AdNodeBo getEnodeBo() {
//		return enodeBo;
//	}
//
//	@Override
//	public void setEnodeBo(AbstractNodeBo enodeBo) throws Exception {
//		if (!(enodeBo instanceof AdNodeBo)) {
//			throw new Exception("不支持的类型");
//		}
//		this.enodeBo = (AdNodeBo) enodeBo;
//		this.seteNodePid(enodeBo.getObj().objPid());
//	}
//
//	@Override
//	public BasicObj getObj() {
//		// TODO Auto-generated method stub
//		return obj;
//	}
//
//	@Override
//	public void setObj(BasicObj obj) {
//		this.obj = (AdLink) obj;
//	}
//
//	/**
//	 * 主表clone，子表和其他直接引用
//	 */
//	@Override
//	public AdLinkBo copy() throws Exception {
//		AdLinkBo newBo = new AdLinkBo();
//		BasicObj newObj = obj.copyObj(PidUtil.getInstance().applyAdNodePid());
//		newBo.setObj(newObj);
//		return newBo;
//	}
//
//	@Override
//	public AbstractNodeBo createNewNodeBo(double x, double y) throws Exception {
//		AdNodeBo newBo = new AdNodeBo();
//		// TODO
//		// AdNode adNode = NodeOperateUtils.createAdNode(x, y);
//		// newBo.setPo(adNode);
//		return newBo;
//	}
//
//	@Override
//	public void computeMeshes() throws Exception {
//		Set<String> meshes = CompGeometryUtil
//				.geoToMeshesWithoutBreak(getGeometry());
//		if (meshes != null) {
//			List<BasicRow> meshIRows = new ArrayList<BasicRow>();
//			for (String mesh : meshes) {
//				AdLinkMesh adLinkMesh = new AdLinkMesh();
//				adLinkMesh.setLinkPid(obj.objPid());
//				adLinkMesh.setMeshId(Integer.parseInt(mesh));
//				meshIRows.add(adLinkMesh);
//			}
//			obj.setMeshes(meshIRows);
//		}
//	}
//}