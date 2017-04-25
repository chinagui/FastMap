package com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.create;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildlink;
import com.navinfo.dataservice.dao.glm.selector.cmg.CmgBuildlinkSelector;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.lc.LcLink;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGscLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLink;
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

		// 立交关系组成 LINK 表
		for (int i = 0; i < command.getLinkArray().size(); i++) {

			JSONObject linkObj = command.getLinkArray().getJSONObject(i);

			RdGscLink gscLink = new RdGscLink();

			gscLink.setPid(rdGsc.getPid());

			gscLink.setLinkPid(linkObj.getInt("pid"));

			gscLink.setZlevel(linkObj.getInt("zlevel"));

			String linkType = linkObj.getString("type");

			handleGscLink(gscLink, gscGeo, linkType);

			if (linkObj.containsKey("shpSeqNum")) {

				gscLink.setShpSeqNum(linkObj.getInt("shpSeqNum"));
			}

			rdGsc.getLinks().add(gscLink);
		}

		result.setPrimaryPid(rdGsc.getPid());

		result.insertObject(rdGsc, ObjStatus.INSERT, rdGsc.pid());
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

		// 数据表名
		gscLink.setTableName(linkRow.tableName().toUpperCase());

		Coordinate[] linkCoor = linkNewGeo.getCoordinates();

		// 计算立交点序号和起终点标识
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

		// 扩大100000倍保持精度
		double lon = pointGeo.getCoordinate().x;

		double lat = pointGeo.getCoordinate().y;

		for (int i = 0; i < linkGeo.getCoordinates().length - 1; i++) {

			Coordinate cs = linkGeo.getCoordinates()[i];

			Coordinate ce = linkGeo.getCoordinates()[i + 1];

			coordinates.add(cs);

			// 是否在线段上
			if (GeoTranslator.isIntersectionInLine(new double[] { cs.x, cs.y },
					new double[] { ce.x, ce.y }, new double[] { lon, lat })) {

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
}
