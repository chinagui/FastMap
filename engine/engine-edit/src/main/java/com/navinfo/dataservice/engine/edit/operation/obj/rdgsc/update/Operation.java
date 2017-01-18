package com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.update;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.lc.LcLink;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGscLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLink;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.dataservice.dao.glm.selector.rd.gsc.RdGscSelector;
import com.navinfo.dataservice.engine.edit.utils.RdGscOperateUtils;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 
 * @Title: Operation.java
 * @Description: 立交的修改操作 :立交修改只允许修改属性中的“处理标识”字段和立交的高度层次
 * @author 张小龙
 * @date 2016年4月18日 下午3:04:21
 * @version V1.0
 */
public class Operation implements IOperation {

	private Command command;

	private RdGsc rdGsc;

	private String tableName;

	private Connection conn;

	public Operation(Command command, RdGsc rdGsc) {
		this.command = command;

		this.rdGsc = rdGsc;
	}

	public Operation() {
	}

	public Operation(String tableName) {
		this.tableName = tableName;
	}

	public Operation(String tableName, Connection conn) {
		this.tableName = tableName;

		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {

		JSONObject content = command.getContent();

		// 1.修改主表RD_GSC数据
		if (content.containsKey("objStatus")) {

			if (ObjStatus.DELETE.toString().equals(content.getString("objStatus"))) {
				result.insertObject(rdGsc, ObjStatus.DELETE, rdGsc.pid());

				return null;
			} else {

				boolean isChanged = rdGsc.fillChangeFields(content);

				if (isChanged) {
					result.insertObject(rdGsc, ObjStatus.UPDATE, rdGsc.pid());
				}
			}
		}

		// 2.修改关系表RD_GSC_LINK的LINK信息（高度信息）
		if (content.containsKey("links")) {
			JSONArray links = content.getJSONArray("links");

			for (int i = 0; i < links.size(); i++) {

				JSONObject json = links.getJSONObject(i);

				if (json.containsKey("objStatus")) {

					if (!ObjStatus.INSERT.toString().equals(json.getString("objStatus"))) {

						RdGscLink link = rdGsc.rdGscLinkMap.get(json.getString("rowId"));

						if (link == null) {
							throw new Exception("rowId=" + json.getString("rowId") + "的rd_gsc_link不存在");
						}

						if (ObjStatus.DELETE.toString().equals(json.getString("objStatus"))) {
							result.insertObject(link, ObjStatus.DELETE, rdGsc.pid());

							continue;
						} else if (ObjStatus.UPDATE.toString().equals(json.getString("objStatus"))) {

							boolean isChanged = link.fillChangeFields(json);

							if (isChanged) {
								result.insertObject(link, ObjStatus.UPDATE, rdGsc.pid());
							}
						}
					}
				}

			}
		}

		return null;
	}

	public void repairLink(List<RdGsc> gscList, Map<Integer, Geometry> linkMap, IRow oldLink, Result result)
			throws Exception {

		String linkTableName = "";

		int linkPid = 0;

		if (oldLink instanceof RdLink) {

			RdLink link = (RdLink) oldLink;

			linkTableName = link.tableName().toUpperCase();

			linkPid = link.pid();

		} else if (oldLink instanceof RwLink) {

			RwLink link = (RwLink) oldLink;

			linkTableName = link.tableName().toUpperCase();

			linkPid = link.pid();

		} else if (oldLink instanceof LcLink) {

			LcLink link = (LcLink) oldLink;

			linkTableName = link.tableName().toUpperCase();

			linkPid = link.pid();

		} else {

			return;
		}

		// 更新立交link
		Map<RdGscLink, Geometry> updateLink = new HashMap<RdGscLink, Geometry>();

		// 自相交立交
		List<RdGsc> selfGsc = new ArrayList<RdGsc>();

		getGscInfoForRepair(linkPid, linkTableName, gscList, updateLink, selfGsc);

		// 处理非自相交立交组成link
		repairGscLink(updateLink, linkMap, result);

		// 处理自相交立交
		rapairSelfGsc(selfGsc, linkMap, result);

	}

	private void getGscInfoForRepair(int linkPid, String linkTableName, List<RdGsc> gscList,
			Map<RdGscLink, Geometry> updateLink, List<RdGsc> selfGsc) {

		for (RdGsc gsc : gscList) {

			List<RdGscLink> gscTmp = new ArrayList<RdGscLink>();

			for (IRow row : gsc.getLinks()) {

				RdGscLink gscLink = (RdGscLink) row;

				if (gscLink.getLinkPid() != linkPid || !gscLink.getTableName().toUpperCase().equals(linkTableName)) {
					continue;
				}

				gscTmp.add(gscLink);
			}

			if (gscTmp.size() == 1) {

				updateLink.put(gscTmp.get(0), gsc.getGeometry());

			} else if (gscTmp.size() > 1) {
				selfGsc.add(gsc);
			}
		}
	}

	private void repairGscLink(Map<RdGscLink, Geometry> updateLink, Map<Integer, Geometry> linkMap, Result result)
			throws Exception {

		for (Map.Entry<RdGscLink, Geometry> entryGscLink : updateLink.entrySet()) {

			RdGscLink gscLink = entryGscLink.getKey();

			Geometry gscGeo = entryGscLink.getValue();

			int targetPid = getTargetPidforRapair(gscGeo, linkMap);

			if (targetPid == 0) {
				continue;
			}

			Geometry linkGeo = linkMap.get(targetPid);

			// 计算形状点号：SHP_SEQ_NUM
			List<Integer> shpSeqNumList = RdGscOperateUtils.calcShpSeqNum(gscGeo, linkGeo.getCoordinates());

			gscLink.changedFields().put("shpSeqNum", shpSeqNumList.get(0));

			result.insertObject(gscLink, ObjStatus.UPDATE, gscLink.getPid());
		}
	}

	private void rapairSelfGsc(List<RdGsc> selfGsc, Map<Integer, Geometry> linkMap, Result result) {
		for (RdGsc gsc : selfGsc) {
			Geometry gscGeo = gsc.getGeometry();

			int targetPid = getTargetPidforRapair(gscGeo, linkMap);

			if (targetPid == 0) {
				continue;
			}

			Geometry linkGeo = linkMap.get(targetPid);

			// 计算形状点号：SHP_SEQ_NUM
			List<Integer> shpSeqNumList = RdGscOperateUtils.calcShpSeqNum(gscGeo, linkGeo.getCoordinates());

			for (IRow row : gsc.getLinks()) {
				RdGscLink gscLink = (RdGscLink) row;

				int zLevel = gscLink.getZlevel();

				if (zLevel < 0) {

					zLevel = 0;

				} else if (zLevel > shpSeqNumList.size() - 1) {

					zLevel = shpSeqNumList.size() - 1;
				}

				gscLink.changedFields().put("shpSeqNum", shpSeqNumList.get(zLevel));

				result.insertObject(gscLink, ObjStatus.UPDATE, gscLink.getPid());
			}
		}
	}

	private int getTargetPidforRapair(Geometry gscGeo, Map<Integer, Geometry> linkMap) {
		int targetPid = 0;

		for (Map.Entry<Integer, Geometry> entryLink : linkMap.entrySet()) {

			if (gscGeo.distance(entryLink.getValue()) < 1) {

				targetPid = entryLink.getKey();

				break;
			}
		}

		return targetPid;
	}

	public void breakRdLink(Result result, IObj oldLink, List<? extends IObj> newLinks) throws Exception {

		String linkType = ReflectionAttrUtils.getTableNameByObjType(oldLink.objType());

		int pid = 0;

		if (oldLink instanceof RdLink) {
			pid = ((RdLink) oldLink).getPid();
		} else if (oldLink instanceof RwLink) {
			pid = ((RwLink) oldLink).getPid();
		} else if (oldLink instanceof LcLink) {
			pid = ((LcLink) oldLink).getPid();
		}

		// 获取由该link组成的立交（RDGSC）
		RdGscSelector selector = new RdGscSelector(conn);

		List<RdGsc> rdGscList = selector.loadRdGscLinkByLinkPid(pid, linkType, true);

		breakLineForGsc(result, oldLink, newLinks, rdGscList);
	}

	public void breakLineForGsc(Result result, IObj oldLink, List<? extends IObj> newLinks, List<RdGsc> rdGscList)
			throws Exception {
		for (RdGsc rr : rdGscList) {

			List<IRow> rdGscLinkList = rr.getLinks();

			// 判断是否是自相交立交
			boolean isSelfGsc = RdGscOperateUtils.checkIsSelfInter(rdGscLinkList);

			if (isSelfGsc) {
				handleSelfGscOnBreak(rr, result, oldLink, newLinks);
			} else {
				handleNotSelfGscOnBreak(rr, result, oldLink, newLinks);
			}

		}
	}

	/**
	 * 打断LINK后维护立交关系
	 * 
	 * @param rr
	 *            立交对象
	 * @throws Exception
	 */
	private void handleSelfGscOnBreak(RdGsc rr, Result result, IObj oldLink, List<? extends IObj> newLinks)
			throws Exception {

		Geometry gscGeo = rr.getGeometry();

		Map<Integer, Geometry> linkGeoMap = new HashMap<>();

		for (IObj link : newLinks) {
			if (tableName.equalsIgnoreCase("RD_LINK")) {
				RdLink linkOjb = (RdLink) link;
				if (linkOjb.getGeometry().distance(gscGeo) < 1) {
					linkGeoMap.put(linkOjb.pid(), linkOjb.getGeometry());
				}
			} else if (tableName.equalsIgnoreCase("LC_LINK")) {
				LcLink linkOjb = (LcLink) link;
				if (linkOjb.getGeometry().distance(gscGeo) < 1) {
					linkGeoMap.put(linkOjb.pid(), linkOjb.getGeometry());
				}
			} else if (tableName.equalsIgnoreCase("RW_LINK")) {
				RwLink linkOjb = (RwLink) link;
				if (linkOjb.getGeometry().distance(gscGeo) < 1) {
					linkGeoMap.put(linkOjb.pid(), linkOjb.getGeometry());
				}
			}
		}

		// 打断link后还是自相交立交
		if (linkGeoMap.size() == 1) {
			for (int i = 0; i < rr.getLinks().size(); i++) {
				RdGscLink rdGscLink = (RdGscLink) rr.getLinks().get(i);

				rdGscLink.changedFields().put("linkPid", linkGeoMap.keySet().iterator().next());

				result.insertObject(rdGscLink, ObjStatus.UPDATE, rdGscLink.getPid());
			}
		} else {
			// 打断后生成多线（双线）立交
			// 先删除立交组成线，再新增新生成的线，主要是连续打断后不确定是否和原始组成link条数一致，所以这里线删除后新增
			for (int j = 0; j < rr.getLinks().size(); j++) {
				RdGscLink rdGscLink = (RdGscLink) rr.getLinks().get(j);

				result.insertObject(rdGscLink, ObjStatus.DELETE, rdGscLink.getPid());
			}

			int i = 0;

			for (Map.Entry<Integer, Geometry> entry : linkGeoMap.entrySet()) {
				Geometry geometry = entry.getValue();

				int linkPid = entry.getKey();

				RdGscLink gscLink = new RdGscLink();

				// 截取精度，防止位置序号计算错误
				Geometry tmpLinkGeo = GeoTranslator.transform(geometry, 1, 0);

				List<Integer> shpSeqNumList = RdGscOperateUtils.calcShpSeqNum(gscGeo, tmpLinkGeo.getCoordinates());

				gscLink.setPid(rr.getPid());

				gscLink.setLinkPid(linkPid);

				gscLink.setTableName(tableName.toUpperCase());

				gscLink.setZlevel(i);

				gscLink.setShpSeqNum(shpSeqNumList.get(0));

				result.insertObject(gscLink, ObjStatus.INSERT, gscLink.getPid());

				i++;
			}
		}
	}

	/**
	 * 处理非自相交的立交的组成线被打断
	 * 
	 * @param rdGsc
	 *            立交对象
	 * @throws Exception
	 */
	private void handleNotSelfGscOnBreak(RdGsc rdGsc, Result result, IObj oldLink, List<? extends IObj> newLinks)
			throws Exception {
		// 每个立交至少由两条线组成，循环遍历每条link
		for (IRow row : rdGsc.getLinks()) {
			RdGscLink rdGscLink = (RdGscLink) row;

			// 找到打断的那条link
			if (rdGscLink.getLinkPid() == oldLink.pid()) {

				List<? extends IObj> linkList = newLinks;

				Map<Integer, Geometry> newGscLinkMap = new HashMap<>();

				for (IObj newLink : linkList) {
					Geometry tmpGeo = null;

					double distance = -1;

					if (newLink instanceof RdLink) {
						RdLink tmpLink = (RdLink) newLink;

						distance = rdGsc.getGeometry().distance(tmpLink.getGeometry());

						tmpGeo = tmpLink.getGeometry();
					} else if (newLink instanceof LcLink) {
						LcLink tmpLink = (LcLink) newLink;

						distance = rdGsc.getGeometry().distance(tmpLink.getGeometry());

						tmpGeo = tmpLink.getGeometry();
					} else if (newLink instanceof RwLink) {
						RwLink tmpLink = (RwLink) newLink;

						distance = rdGsc.getGeometry().distance(tmpLink.getGeometry());

						tmpGeo = tmpLink.getGeometry();
					}
					// 计算代表点和生成的线最近的link
					if (distance < 1) {
						newGscLinkMap.put(newLink.pid(), tmpGeo);
					}
				}

				if (newGscLinkMap.size() > 0) {
					for (Map.Entry<Integer, Geometry> entry : newGscLinkMap.entrySet()) {
						RdGscLink newRdGsclLink = new RdGscLink();

						int newLinkPid = entry.getKey();

						Geometry geometry = entry.getValue();

						newRdGsclLink.setLinkPid(newLinkPid);

						newRdGsclLink.setPid(rdGsc.getPid());

						int startEndFlag = GeometryUtils.getStartOrEndType(geometry.getCoordinates(),
								rdGsc.getGeometry());

						newRdGsclLink.setStartEnd(startEndFlag);

						int seqNum = 0;
						// 计算形状点号：SHP_SEQ_NUM
						if (startEndFlag == 1) {
							seqNum = 0;
						} else if (startEndFlag == 2) {
							seqNum = geometry.getCoordinates().length - 1;
						}
						else{
							// 打断后，立交的link和link序号需要重新计算
							List<Integer> shpSeqNum = RdGscOperateUtils.calcShpSeqNum(rdGsc.getGeometry(),
									geometry.getCoordinates());
							
							seqNum = shpSeqNum.get(0);
						}

						newRdGsclLink.setShpSeqNum(seqNum);
						
						newRdGsclLink.setTableName(rdGscLink.getTableName());

						result.insertObject(newRdGsclLink, ObjStatus.INSERT, rdGsc.pid());
					}
					result.insertObject(rdGscLink, ObjStatus.DELETE, rdGsc.getPid());
				}
			}
		}
	}
}
