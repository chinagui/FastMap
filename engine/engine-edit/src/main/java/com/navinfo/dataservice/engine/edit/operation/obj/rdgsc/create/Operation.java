package com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.create;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildlink;
import com.navinfo.dataservice.dao.glm.model.lc.LcLink;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGscLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLink;
import com.navinfo.dataservice.dao.glm.selector.cmg.CmgBuildlinkSelector;
import com.navinfo.dataservice.dao.glm.selector.lc.LcLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.gsc.RdGscSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.rw.RwLinkSelector;
import com.navinfo.dataservice.engine.edit.utils.RdGscOperateUtils;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

/**
 * 
 * 
 * @author luyao
 * 
 */
public class Operation implements IOperation {

	protected Logger log = Logger.getLogger(this.getClass());

	private Command command;

	private Connection conn;

	private Result result;
	
	// 增加形状点后的几何。String：表名+linkPid;Coordinate[]：link几何
	Map<String, Coordinate[]> linkCoorMap = new HashMap<String, Coordinate[]>();

	public Operation(Command command, Check check, Connection conn) {

		this.command = command;

		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {

		this.result = result;

		log.info("开始创建立交");

		if (command.getLinkArray().size() < 1) {

			throw new Exception("没有立交组成线");
		}

		createGsc();

		return null;
	}

	/**
	 * 创建立交
	 * 
	 * @throws Exception
	 */
	private void createGsc() throws Exception {

		Geometry gscGeo = command.getGscPoint();

		// 立交表
		RdGsc rdGsc = new RdGsc();

		rdGsc.setPid(PidUtil.getInstance().applyRdGscPid());

		rdGsc.setGeometry(gscGeo);

		rdGsc.setProcessFlag(1);

		log.info("新建立交组成线");
		
		// 同一个link的gscLink组
		Map<String, List<RdGscLink>> josnLinkMap = new HashMap<String, List<RdGscLink>>();

		// Map<link标识, TreeMap<Integer： 交点所在该gscLink对应的link的线段序号,
		// RdGscLink：新增的立交link>>
		Map<String, TreeMap<Integer, RdGscLink>> laneNumInfo = new HashMap<String, TreeMap<Integer, RdGscLink>>();

		// 立交关系组成 LINK 表
		for (int i = 0; i < command.getLinkArray().size(); i++) {

			JSONObject linkObj = command.getLinkArray().getJSONObject(i);

			RdGscLink gscLink = new RdGscLink();

			gscLink.setPid(rdGsc.getPid());

			gscLink.setLinkPid(linkObj.getInt("pid"));

			gscLink.setZlevel(linkObj.getInt("zlevel"));

			String linkType = linkObj.getString("type");

			handleGscLink(gscLink, gscGeo, linkType);

			rdGsc.getLinks().add(gscLink);

			String linkFlag = getLinkFlag( gscLink);

			if (!josnLinkMap.containsKey(linkFlag)) {

				josnLinkMap.put(linkFlag, new ArrayList<RdGscLink>());
			}

			josnLinkMap.get(linkFlag).add(gscLink);
			
			if (!laneNumInfo.containsKey(linkFlag)) {

				laneNumInfo.put(linkFlag, new TreeMap<Integer, RdGscLink>());
			}
			
			laneNumInfo.get(linkFlag).put(linkObj.getInt("lineNum"), gscLink);
		}
		
		handleIntersectSelf(gscGeo, josnLinkMap, laneNumInfo);

		result.setPrimaryPid(rdGsc.getPid());

		result.insertObject(rdGsc, ObjStatus.INSERT, rdGsc.pid());
	}
	
	private void handleIntersectSelf(Geometry gscGeo,
			Map<String, List<RdGscLink>> josnLinkMap,
			Map<String, TreeMap<Integer, RdGscLink>> laneNumInfo) {

		for (String linkFlag : josnLinkMap.keySet()) {

			//非自相交不处理
			if (josnLinkMap.get(linkFlag).size() < 2) {

				continue;
			}

			Coordinate[] linkCoor = linkCoorMap.get(linkFlag);

			List<Integer> shpSeqNumList = RdGscOperateUtils.calcShpSeqNum(
					gscGeo, linkCoor);

			TreeMap<Integer, RdGscLink> group = laneNumInfo.get(linkFlag);

			int index = 0;

			for (Map.Entry<Integer, RdGscLink> entry : group.entrySet()) {

				RdGscLink gscLink=entry.getValue();
				
				gscLink.setShpSeqNum(shpSeqNumList.get(index++));
			
				// 0 形状点1 起点2 终点
				gscLink.setStartEnd(0);

				if (gscLink.getShpSeqNum() == 0) {

					gscLink.setStartEnd(1);

				} else if (gscLink.getShpSeqNum() == linkCoor.length - 1) {

					gscLink.setStartEnd(2);
				}
			}
		}
	}

	/**
	 * 处理立交组成link
	 * 
	 * @param gscLink
	 * @param type
	 * @param gscGeo
	 * @throws Exception
	 */
	private void handleGscLink(RdGscLink gscLink, Geometry gscGeo, String type)
			throws Exception {	

		IRow linkRow = getLink(gscLink, type);

		Geometry linkGeometry = null;

		if (linkRow instanceof RdLink) {

			RdLink linkObj = (RdLink) linkRow;

			linkGeometry = linkObj.getGeometry();

		} else if (linkRow instanceof RwLink) {

			RwLink linkObj = (RwLink) linkRow;

			linkGeometry = linkObj.getGeometry();

		} else if (linkRow instanceof LcLink) {

			LcLink linkObj = (LcLink) linkRow;

			linkGeometry = linkObj.getGeometry();
		} else if (linkRow instanceof CmgBuildlink) {

            CmgBuildlink linkObj = (CmgBuildlink) linkRow;
            linkGeometry = linkObj.getGeometry();
		} else {
		    return;
        }

		// 数据表名
		gscLink.setTableName(linkRow.tableName().toUpperCase());

		String linkFlag = getLinkFlag(gscLink);

		// 过滤已经处理过的自相交link
		if (linkCoorMap.containsKey(linkFlag)) {
			
			return;
		}

		if (!RdGscOperateUtils.pointOnline(linkGeometry, gscGeo)) {

			throw new Exception("立交点" + gscGeo.toString() + "不在" + type + gscLink + "上");
		}
		
		LineString linkNewGeo = lineStringInsertPoint(linkGeometry, gscGeo);

		Geometry newGeo = GeoTranslator.transform(linkNewGeo,
				GeoTranslator.dPrecisionMap, 5);

		JSONObject jsonNewGeo = GeoTranslator.jts2Geojson(newGeo);

		JSONObject updateContent = new JSONObject();

		updateContent.put("geometry", jsonNewGeo);

		boolean changed = linkRow.fillChangeFields(updateContent);

		if (changed) {
			 
			result.insertObject(linkRow, ObjStatus.UPDATE, gscLink.getLinkPid());
			
			if (linkRow instanceof RdLink) {

				RdLink linkObj = (RdLink) linkRow;

				OpRefRdGsc opRefRdGsc = new OpRefRdGsc(this.conn);

				opRefRdGsc.handleSameLink(linkObj, jsonNewGeo,
						this.command.getDbId(), result);
			}
		}

		Coordinate[] linkCoor = linkNewGeo.getCoordinates();

		linkCoorMap.put(linkFlag, linkCoor);

		// 计算立交点序号和起终点标识，自相交link后续会在再次计算
		calShpSeqNum(gscLink, gscGeo, linkCoor);

		// 更新线上其他立交的形状点号
		handleOtherGscLink(gscLink, result, linkCoor);
	}

	/**
	 * 计算立交点在组成线上的形状点号
	 * 
	 * @param rdGscLink
	 * @param gscGeo
	 * @param linkCoor
	 * @throws Exception
	 */
	public static void calShpSeqNum(RdGscLink rdGscLink, Geometry gscGeo,
			Coordinate[] linkCoor) throws Exception {
		List<Integer> shpSeqNumList = null;

		// 获取link起终点标识
		int startEndFlag = GeometryUtils.getStartOrEndType(linkCoor, gscGeo);

		rdGscLink.setStartEnd(startEndFlag);

		int seqNum = 0;
		// 计算形状点号：SHP_SEQ_NUM
		if (startEndFlag == 1) {
			seqNum = 0;
		} else if (startEndFlag == 2) {
			seqNum = linkCoor.length - 1;
		} else {
			shpSeqNumList = RdGscOperateUtils.calcShpSeqNum(gscGeo, linkCoor);

			if (shpSeqNumList != null && shpSeqNumList.size() > 0) {
				seqNum = shpSeqNumList.get(0);
			}
		}
		rdGscLink.setShpSeqNum(seqNum);
	}

	/**
	 * 处理link上其他立交的位置序号
	 * 
	 * @param gscLink
	 * @param result
	 * @param linkCoor
	 *            线上新的几何点
	 * @throws Exception
	 */
	private void handleOtherGscLink(RdGscLink gscLink, Result result,
			Coordinate[] linkCoor) throws Exception {

		RdGscSelector selector = new RdGscSelector(conn);

		List<RdGsc> rdGscList = selector.onlyLoadTargetRdGscLink(
				gscLink.getLinkPid(), gscLink.getTableName(), true);

		for (RdGsc gsc : rdGscList) {

			List<IRow> gscLinkList = gsc.getLinks();

			boolean flag = RdGscOperateUtils.checkIsSelfInter(gscLinkList);

			// 处理对于立交的影响
			RdGscOperateUtils.handleInterEffect(flag, gsc, linkCoor, result);
		}
	}

	/**
	 * 插入形状点
	 * 
	 * @param linkGeo
	 *            扩大100000倍保持精度
	 * @param pointGeo
	 *            扩大100000倍保持精度
	 * @return
	 * @throws Exception
	 */
	public LineString lineStringInsertPoint(Geometry linkGeo, Geometry pointGeo)
			throws Exception {

		List<Coordinate> coordinates = new ArrayList<Coordinate>();

		Coordinate newCoordinate = pointGeo.getCoordinate();

		for (int i = 0; i < linkGeo.getCoordinates().length - 1; i++) {

			Coordinate cs = linkGeo.getCoordinates()[i];

			Coordinate ce = linkGeo.getCoordinates()[i + 1];

			coordinates.add(cs);

			// 是否在线段上
			if (GeoTranslator.isIntersectionInLine(cs, ce, newCoordinate)) {

				coordinates.add(newCoordinate);
			}

			if (i == linkGeo.getCoordinates().length - 2) {

				coordinates.add(ce);
			}
		}

		return GeoTranslator.createLineString(coordinates);
	}

	/**
	 * 根据立交组成link信息获取组成link对象
	 * 
	 * @param gscLink
	 * @param type
	 * @return
	 * @throws Exception
	 */
	public IRow getLink(RdGscLink gscLink, String type) throws Exception {

		int linkPid = gscLink.getLinkPid();

		switch (type) {

		case "RDLINK":

			RdLinkSelector linkSelector = new RdLinkSelector(this.conn);

			return linkSelector.loadById(linkPid, true);

		case "RWLINK":

			RwLinkSelector rwLinkSelector = new RwLinkSelector(this.conn);

			return rwLinkSelector.loadById(linkPid, true);

		case "LCLINK":

			LcLinkSelector lcLinkSelector = new LcLinkSelector(this.conn);

			return lcLinkSelector.loadById(linkPid, true);

        case "CMGBUILDLINK":

            CmgBuildlinkSelector cmgBuildlinkSelector = new CmgBuildlinkSelector(conn);

            return cmgBuildlinkSelector.loadById(linkPid, true);
		default:
			throw new Exception(type + "不支持创建立交");
		}
	}
	
	/**
	 * 获取gscLink标识
	 * @param gscLink
	 * @return
	 */
	private String getLinkFlag(RdGscLink gscLink) {
		return gscLink.getTableName() + gscLink.getLinkPid();
	}

}
