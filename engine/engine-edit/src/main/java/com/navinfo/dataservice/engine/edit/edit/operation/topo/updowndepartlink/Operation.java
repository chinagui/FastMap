package com.navinfo.dataservice.engine.edit.edit.operation.topo.updowndepartlink;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.text.StyledEditorKit.BoldAction;

import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkIntRtic;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkLimit;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkRtic;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.node.RdNodeSelector;
import com.navinfo.dataservice.engine.edit.comm.util.operate.NodeOperateUtils;
import com.navinfo.dataservice.engine.edit.comm.util.operate.RdLinkOperateUtils;
import com.navinfo.navicommons.geo.computation.CompLineUtil;
import com.sun.research.ws.wadl.Link;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

/**
 * @author zhaokk 上下线分离具体实现操作类
 */
public class Operation implements IOperation {
	protected Logger log = Logger.getLogger(this.getClass());
	private Command command;
	private Connection conn;

	public Operation(Command command, Connection conn) {

		this.command = command;
		this.conn = conn;
	}

	/*
	 * 上下线分离 执行类
	 */
	@Override
	public String run(Result result) throws Exception {

		// 1.创建分离后生成links
		this.createDepartLinks(result);
		// 2.删除soucelinks
		this.deleteLinks(result);
		return null;
	}

	/*
	 * 删除上下线分离的source links和关系
	 */
	private void deleteLinks(Result result) throws Exception {
		for (int linkPid : command.getLinkPids()) {
			JSONObject deleteJson = new JSONObject();
			// 要移动点的project_id
			deleteJson.put("projectId", command.getProjectId());
			deleteJson.put("objId", linkPid);
			com.navinfo.dataservice.engine.edit.edit.operation.topo.deletelink.Command deletecommand = new com.navinfo.dataservice.engine.edit.edit.operation.topo.deletelink.Command(
					deleteJson, command.getRequester());
			com.navinfo.dataservice.engine.edit.edit.operation.topo.deletelink.Process process = new com.navinfo.dataservice.engine.edit.edit.operation.topo.deletelink.Process(
					deletecommand, result, conn);
			process.innerRun();
		}
	}

	/*
	 * 创建上下分离RDLINk
	 */
	private void createDepartLinks(Result result) throws Exception {
		List<RdLink> links = command.getLinks();
		LineString[] lineStrings = new LineString[links.size()];
		// 组装LineString
		
		for (int i = 0; i < links.size(); i++) {
			lineStrings[i] = (new GeometryFactory().createLineString(GeoTranslator.transform(links
					.get(i).getGeometry(),0.00001,5).getCoordinates()));
		}
		// 调用分离后生成的上下线
		// 生成的线按照顺序存放在List<LineString> 前一半是左线 后一半是右线
		// 传入起点和终点Point
		Point sPoint = new GeometryFactory().createPoint(this
				.getStartAndEndNode(links, 0).getGeometry().getCoordinate());
		RdNode snode = this.getStartAndEndNode(links, 0);
		RdNode eNode = this.getStartAndEndNode(links, 1);
		LineString[] lines = CompLineUtil.separate(sPoint, lineStrings,
				command.getDistance());

		// 生成分离后左线
		Map<Geometry, RdNode> map = new HashMap<Geometry, RdNode>();
		map.put(snode.getGeometry(), snode);
		map.put(eNode.getGeometry(), eNode);
		List<RdLink> upLists = new ArrayList<RdLink>();
		List<RdLink> downLists = new ArrayList<RdLink>();
		for (int i = 0; i < command.getLinkPids().size(); i++) {
			RdLink departLink = new RdLink();
			departLink.setGeometry(lines[i]);
			RdLink currentLink = command.getLinks().get(i);
			RdLink nextLink = null;
			if (i == command.getLinkPids().size() - 1) {
				nextLink = currentLink;
			}else{
				nextLink = command.getLinks().get(i+1);
			}
			
			downLists.addAll(this.createUpRdLink(departLink, result,
					currentLink, nextLink, map));
		}// 生成分离后右线
		for (int i = command.getLinkPids().size(); i < lines.length; i++) {
			RdLink departLink = new RdLink();
			departLink.setGeometry(lines[i]);
			RdLink currentLink = command.getLinks().get(
					i - command.getLinkPids().size());
			RdLink nextLink = null;
			if (i == lines.length - 1) {
				nextLink = currentLink;
			}else{
				nextLink= command.getLinks().get(
						i - command.getLinkPids().size()+1);
			}
			upLists.addAll(this.createDownRdLink(departLink, result,
					currentLink, nextLink, map));
		}
		// 属性维护
		this.RelationLink(upLists, result, 0);
		this.RelationLink(downLists, result, 1);

	}
	// 上下线属性维护
	// upDownFlag 新生成线标志0上线(左线) 1下线(右线)
	private void RelationLink(List<RdLink> links, Result result, int upDownFlag) {

		for (RdLink link : links) {
			// 1.属性维护
			this.relationNatureForlink(link);
			// 2.限制信息
			this.relationLimitForLink(link);
			// 3.限速信息
			// 上下线分离后的新link都清空原先link的速度限制值
			link.getSpeedlimits().clear();
			// 4同一线关系维护
			// 5同一点关系维护
			// 6 对RTIC信息的维护
			this.relationRticForLink(link, upDownFlag);
			// 7对TMC的维护
			// 8对线门牌处理
			// 9对详细车道信息的维护
			// 10关联link的维护
			// 11上下线分离目标link
			// 12邮编索引的维护
			// 13上下线分离后对点限速关联link的维护
			// 14上下线分离后对电子眼关联link的维护
			// 15对点-线-线关系的维护
			// 16 对点线关系的维护
			// 17对线点线关系（车信，交限，分歧，语音引导，顺行）信息的维护
			// 18.逆方向
			if (upDownFlag == 0) {
				link.setDirect(3);
			} else {
				link.setDirect(2);
			}
			result.insertObject(link, ObjStatus.INSERT, link.getPid());
		}
	}

	// 速度限制值、行政区划值、人行便道、阶梯、总车道数，左车道数、右车道数、车道等级初始化，上下线分离属性
	private void relationNatureForlink(RdLink link) {
		link.setMultiDigitized(1);
		link.setLaneNum(2);
		link.setLaneLeft(0);
		link.setLaneRight(0);
		link.setLaneClass(2);
		link.setWalkstairFlag(0);
		link.setSidewalkFlag(0);
	}

	// link的限制类型为单行限制、穿行限制、车辆限制时，上下线分离后新link自动删除对应限制类型下的道路限制信息子表
	private void relationLimitForLink(RdLink link) {
		for (IRow row : link.getLimits()) {
			RdLinkLimit limit = (RdLinkLimit) row;
			if (limit.getType() == 1 || limit.getType() == 2
					|| limit.getType() == 3) {
				link.getLimits().remove(limit);
			}
		}
	}

	// 如果双方向道路变上下线分离，则将上行方向的RTIC信息作为“上行”赋到分离后通行方向与上行方向相同的link上；
	// 将下行方向的RTIC信息作为“上行”的RTIC信息赋到分离后通行方向与原RTIC上行方向相反的link上；
	// RTIC方向值由程序根据制作RTIC时的方向与划线方向的关系自动计算，其余信息继承原link；
	// 单方向道路变上下线分离，将单方向道路上的RTIC信息赋值给与该单方向道路通行方向相同的一侧道路上。
	private void relationRticForLink(RdLink link, int upDownFlag) {
		// 道路:LINK 与 RTIC 关系表（车导客户用）
		for (IRow row : link.getRtics()) {
			RdLinkRtic linkRtic = (RdLinkRtic) row;
			if (link.getDirect() == 1) {
				if (upDownFlag == 0) {
					if (linkRtic.getUpdownFlag() == 0) {
						link.getRtics().remove(linkRtic);
					}
					linkRtic.setRticDir(2);
				} else {
					if (linkRtic.getUpdownFlag() == 1) {
						link.getRtics().remove(linkRtic);
					}
					linkRtic.setRticDir(3);

				}
			}
			if (upDownFlag == 0) {
				if (link.getDirect() == 2 && link.getDirect() == 3) {
					link.getRtics().clear();
				}
			}
		}

		// 道路:LINK 与 RTIC 关系表（互联网客户用）
		for (IRow row : link.getIntRtics()) {
			RdLinkIntRtic linkRtic = (RdLinkIntRtic) row;
			if (link.getDirect() == 1) {
				if (upDownFlag == 0) {
					if (linkRtic.getUpdownFlag() == 0) {
						link.getRtics().remove(linkRtic);
					}
					linkRtic.setRticDir(2);
				} else {
					if (linkRtic.getUpdownFlag() == 1) {
						link.getRtics().remove(linkRtic);
					}
					linkRtic.setRticDir(3);

				}
			}
			if (upDownFlag == 0) {
				if (link.getDirect() == 2 && link.getDirect() == 3) {
					link.getRtics().clear();
				}
			}
		}

	}
	/*
	 * @param departLink 分离后生成的link
	 * @param result    
	 * @param sourceLink 分离前对应原始link
	 * @param sourceNextLink 分离前对应原始link下一条link
	 * @param map
	 * @return  返回生成上(左边)对应links
	 * @throws Exception
	 */
	private List<RdLink> createUpRdLink(RdLink departLink, Result result,
			RdLink sourceLink, RdLink sourceNextLink, Map<Geometry, RdNode> map)
			throws Exception {
		RdLinkSelector nodeSelector = new RdLinkSelector(conn);
		// 查找分离前link起始点上挂接的link
		List<RdLink> slinks = nodeSelector.loadByDepartNodePid(
				sourceLink.getsNodePid(), sourceLink.getPid(),
				sourceNextLink.getPid(), true);
		List<RdLink> elinks = nodeSelector.loadByDepartNodePid(
				sourceLink.geteNodePid(), sourceLink.getPid(),
				sourceNextLink.getPid(), true);
		RdNode sNode = null;
		RdNode eNode = null;
		// 如果对应起点没有挂接的link
		//对于上(左)线需要生成新的node
		if (slinks.size() <= 0) {
			sNode = this.getNodeByDepartGeo(departLink, 1,map ,result);
		}
		//如果对应起点有挂接的link
		//且没有下线挂接的link 需要修改原有node的属性
		//如果至少有一条下挂的link对于上(左)线需要生成新的node
		else {
			sNode= this.getDepartRdlinkNode(slinks, departLink, sourceLink, sourceNextLink, 1,1, map, result);
		}
		// 如果对应终点没有挂接的link
		//对于上(左)线需要生成新的node
		if (elinks.size() <= 0) {
			eNode = this.getNodeByDepartGeo(departLink, 0, map, result);
		} 
		//如果对应终点有挂接的link
		//且没有下线挂接的link 需要修改原有node的属性
		//如果至少有一条下挂的link对于上(左)线需要生成新的node
		else {
			eNode = this.getDepartRdlinkNode(elinks, departLink, sourceLink, sourceNextLink,0,1, map, result);
		}
		return RdLinkOperateUtils.addRdLink(sNode, eNode, departLink,
				sourceLink, result);
	}
	/*
	 * @param departLink 分离后生成的link
	 * @param result    
	 * @param sourceLink 分离前对应原始link
	 * @param sourceNextLink 分离前对应原始link下一条link
	 * @param map
	 * @return  返回生成下(右边)对应links
	 * @throws Exception
	 */
	private List<RdLink> createDownRdLink(RdLink departLink, Result result,
			RdLink sourceLink, RdLink sourceNextLink, Map<Geometry, RdNode> map)
			throws Exception {
		RdLinkSelector linkSelector = new RdLinkSelector(conn);

		// 查找分离前link起始点上挂接的link
		List<RdLink> slinks = linkSelector.loadByNodePid(
				sourceLink.geteNodePid(), true);
		List<RdLink> elinks = linkSelector.loadByNodePid(
				sourceLink.getsNodePid(), true);
		RdNode sNode = null;
		RdNode eNode = null;
		// 如果对应起点上
		if (slinks.size() <= 0) {
			sNode = this.updateAdNodeForTrack(departLink,
					sourceLink.getsNodePid(), conn, result, 1);

		} else {
			sNode = this.getDepartRdlinkNode(slinks, departLink, sourceLink, sourceNextLink, 1, 0, map, result);
		}
		if (elinks.size() <= 0) {
			eNode = this.updateAdNodeForTrack(departLink,
					sourceLink.geteNodePid(), conn, result, 0);
		} else {
			eNode=  this.getDepartRdlinkNode(elinks, departLink, sourceLink, sourceNextLink, 0, 0, map, result);
		}
		return RdLinkOperateUtils.addRdLink(sNode, eNode, departLink,
				sourceLink, result);
	}
	/*
	 * @param links 当前sourceLink 起点或终点挂接的link
	 * @param departLink 分离后的link
	 * @param sourceLink 分立前对应的link
	 * @param sourceNextLink 分离前对应link的下一条link
	 * @param flag 生成node按照线几何起始和终点node 1 起点  0 终点
	 * @param flagUpDown 生成上(左)下(右)线标志 1上 0下
	 * @param map   存放已经生成的adnode 
	 * @param result 
	 * @return 返回新生成的AdNode
	 * @throws Exception
	 */
	private RdNode getDepartRdlinkNode(List<RdLink> links,RdLink departLink,RdLink sourceLink,RdLink sourceNextLink,int flag,int flagUpDown,Map<Geometry, RdNode> map,Result result) throws Exception{
		List<Boolean> flagBooleans = new ArrayList<Boolean>();
		RdNode node = null;
		for (RdLink link : links) {
			flagBooleans.add(this.isRightSide(sourceLink, sourceNextLink, link));
		}
		if (flagBooleans.contains(true)){
			if(flagUpDown == 1){
				node =this.getNodeByDepartGeo(departLink, flag,map, result); 
			}else{
				node = this.updateAdNodeForTrack(departLink,
					sourceLink.getsNodePid(), conn, result, 1);
			}

		}
		if (!flagBooleans.contains(true)){
			if(flagUpDown == 1){
				node = this.updateAdNodeForTrack(departLink,
						sourceLink.getsNodePid(), conn, result, 1);
			}else{
				node =this.getNodeByDepartGeo(departLink, flag,map, result);
			}
		}

		return node;
	}
	/*
	 * 根据分离后的几何属性生成新的node
	 * @param departLink 分离后的link
	 * @param map 已经生成的node
	 * @param result
	 * @return
	 * @throws Exception
	 */
	private RdNode getNodeByDepartGeo(RdLink departLink,int flag,Map<Geometry, RdNode> map,Result result) throws Exception{
		RdNode node = null;
		Geometry geometry =null;
		Coordinate coordinate = null;
		if(flag == 1){
			coordinate = departLink.getGeometry().getCoordinates()[0];
		}else{
			coordinate = departLink.getGeometry().getCoordinates()[ departLink.getGeometry().getCoordinates().length-1];
		}
		geometry = new GeometryFactory().createPoint(coordinate);
		if (map.containsKey(geometry)) {
			node = map.get(geometry);
		} else {
			node = NodeOperateUtils.createNode(coordinate.x,coordinate.y);
			map.put(node.getGeometry(), node);
			result.insertObject(node, ObjStatus.INSERT, node.getPid());
		}
		return node;
	}

	// 更新分离后要移动node的几何属性
	//flag ==1 根据分离后线几何属性的最后点属性更新node的几何属性
	//flag ==0根据分离后线几何属性的开始点属性更新node的几何属性
	private RdNode updateAdNodeForTrack(RdLink link, int nodePid,
			Connection conn, Result result, int flag) throws Exception {
		JSONObject updateContent = new JSONObject();
		RdNodeSelector nodeSelector = new RdNodeSelector(conn);
		RdNode node = (RdNode) nodeSelector.loadById(nodePid, true);
		if (flag == 1) {
			updateContent.put("geometry", GeoTranslator.transform(
					new GeometryFactory().createPoint(link.getGeometry()
							.getCoordinates()[0]), 100000, 0));
		} else {
			updateContent.put("geometry", GeoTranslator.transform(
					new GeometryFactory().createPoint(link.getGeometry()
							.getCoordinates()[link.getGeometry()
							.getCoordinates().length - 1]), 100000, 0));
		}
		node.fillChangeFields(updateContent);
		result.insertObject(node, ObjStatus.UPDATE, node.getPid());
		return node;
	}

	// 获取联通线的起点和终点
	// 0 起点  1 终点
	// 根据联通线的第一条link和第二条link算出起点Node
	// 根据联通线最后一条link和倒数第二条link算出终点Node
	private RdNode getStartAndEndNode(List<RdLink> links, int flag)
			throws Exception {
		RdLink fristLink = null;
		RdLink secondLink = null;
		RdNode node = null;
		if (flag == 0) {
			fristLink = links.get(0);
			secondLink = links.get(1);
		}
		if (flag == 1) {
			fristLink = links.get(links.size() - 1);
			secondLink = links.get(links.size() - 2);
		}
		List<Integer> nodes = new ArrayList<Integer>();
		nodes.add(secondLink.getsNodePid());
		nodes.add(secondLink.geteNodePid());
		if (nodes.contains(fristLink.getsNodePid())) {
			IRow row = new RdNodeSelector(conn).loadById(
					fristLink.geteNodePid(), true);
			node = (RdNode) row;
		}
		if (nodes.contains(fristLink.geteNodePid())) {
			IRow row = new RdNodeSelector(conn).loadById(
					fristLink.getsNodePid(), true);
			node = (RdNode) row;

		}
		return node;
	}

	private boolean isRightSide(RdLink startLine, RdLink endLine,
			RdLink adjacentLine) {
		return true;
	}

}
