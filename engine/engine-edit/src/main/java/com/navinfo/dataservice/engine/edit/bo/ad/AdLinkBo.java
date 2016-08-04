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
import com.navinfo.dataservice.dao.pidservice.PidService;
import com.navinfo.dataservice.engine.edit.bo.BoFactory;
import com.navinfo.dataservice.engine.edit.bo.BreakResult;
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
	private List<AdLinkMesh> meshes;
	private AdNodeBo sNode;
	private AdNodeBo eNode;

	@Override
	public BreakResult breakoff(Point point) throws Exception {
		BreakResult result = super.breakoff(point);

		result.setPrimaryPid(po.getPid());
		log.info("2 删除要打断的行政区划线信息");
		result.insertObject(po, ObjStatus.DELETE, po.pid());
		result.setTargetLinkBo(this);

		log.debug("3 生成打断点的信息");
		AdNode node = NodeOperateUtils.createAdNode(point.getX(), point.getY());
		result.insertObject(node, ObjStatus.INSERT, node.pid());
		int breakNodePid = node.pid();
		log.debug("3.1 打断点的pid = " + breakNodePid);

		log.debug("4 组装 第一条link 的信息");
		AdLink slink = this.addLinkBySourceLink(result.getNewLeftGeometry(),
				po.getsNodePid(), breakNodePid, po);
		result.setNewLeftLink((LinkBo) BoFactory.getInstance().create(slink));
		result.insertObject(slink, ObjStatus.INSERT, slink.pid());
		log.debug("4.1 生成第一条link信息 pid = " + slink.getPid());

		log.debug("5 组装 第一条link 的信息");
		AdLink elink = this.addLinkBySourceLink(result.getNewRightGeometry(),
				breakNodePid, po.geteNodePid(), po);
		result.setNewRightLink((LinkBo) BoFactory.getInstance().create(elink));
		result.insertObject(elink, ObjStatus.INSERT, elink.pid());
		log.debug("5.1 生成第二条link信息 pid = " + elink.getPid());

		return null;
	}

	public AdLink getAdLink() {
		return po;
	}

	public void setAdLink(AdLink adLink) {
		this.po = adLink;
	}

	public List<AdLinkMesh> getMeshes() {
		if (null == this.meshes) {
			if (this.po.getPid() == 0) {
				this.meshes = new ArrayList<AdLinkMesh>();
			} else {
				//能够唯一确定关联字段的可以使用this
				this.meshes = PoFactory.getInstance().list(conn,
						AdLinkMesh.class, this.po,
						isLock);
			}
		}

		return this.meshes;
	}

	public void setMeshes(List<AdLinkMesh> meshes) {
		this.meshes = meshes;
	}

	public AdNodeBo getsNode() throws Exception {
		if (null == sNode) {
			AdLink queryPo = new AdLink();
			queryPo.setsNodePid(this.po.getsNodePid());
			IObj po = PoFactory.getInstance().get(conn, AdNode.class,
					queryPo, isLock);
			sNode = (AdNodeBo) BoFactory.getInstance().create(po);
		}
		return sNode;
	}

	public void setsNode(AdNodeBo sNode) {
		this.sNode = sNode;
	}

	public AdNodeBo geteNode() throws Exception {
		if (null == eNode) {
			AdLink queryPo = new AdLink();
			queryPo.seteNodePid(this.po.geteNodePid());
			IObj po = PoFactory.getInstance().get(conn, AdNode.class,
					queryPo, isLock);
			eNode = (AdNodeBo) BoFactory.getInstance().create(po);
		}
		return eNode;
	}

	public void seteNode(AdNodeBo eNode) {
		this.eNode = eNode;
	}

	@Override
	public void setPo(IObj po) {
		this.po = (AdLink) po;
		this.geometry = this.po.getGeometry();
	}

	private AdLink addLinkBySourceLink(Geometry g, int sNodePid, int eNodePid,
			AdLink sourcelink) throws Exception {
		AdLink link = new AdLink();
		link.copy(sourcelink);
		Set<String> meshes = CompGeometryUtil.geoToMeshesWithoutBreak(g);
		link.setPid(PidService.getInstance().applyAdLinkPid());
		if (meshes.size() == 2) {
			link.setKind(0);
		}
		Iterator<String> it = meshes.iterator();
		List<IRow> meshIRows = new ArrayList<IRow>();
		while (it.hasNext()) {
			meshIRows.add(getLinkChildren(link, Integer.parseInt(it.next())));
		}
		link.setMeshes(meshIRows);
		double linkLength = GeometryUtils.getLinkLength(g);
		link.setLength(linkLength);
		link.setGeometry(GeoTranslator.transform(g, 100000, 0));
		link.setsNodePid(sNodePid);
		link.seteNodePid(eNodePid);
		return link;
	}

	private AdLinkMesh getLinkChildren(AdLink link, int meshId) {
		AdLinkMesh mesh = new AdLinkMesh();
		mesh.setLinkPid(link.getPid());
		mesh.setMesh(meshId);
		mesh.setMeshId(meshId);
		return mesh;
	}

	@Override
	public IObj getPo() {
		// TODO Auto-generated method stub
		return po;
	}
}