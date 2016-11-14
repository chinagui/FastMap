package com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGscLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.edit.utils.RdGscOperateUtils;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONObject;

public class OpRefRdGsc implements IOperation {

	private Command command;

	private Result result;

	private Connection connection;

	public OpRefRdGsc(Command command, Connection connection) {
		this.command = command;

		this.connection = connection;
	}

	@Override
	public String run(Result result) throws Exception {

		this.result = result;

		List<RdGsc> rdGscList = command.getRdGscs();

		if (CollectionUtils.isNotEmpty(rdGscList)) {
			this.handleRdGsc(rdGscList);
		}

		return null;
	}

	// 处理立交
	private void handleRdGsc(List<RdGsc> list) throws Exception {

		for (RdGsc rr : list) {

			List<IRow> rdGscLinkList = rr.getLinks();

			// 判断是否是自相交立交
			boolean isSelfGsc = RdGscOperateUtils.checkIsSelfInter(rdGscLinkList);

			if (isSelfGsc) {
				handleSelfGscOnBreak(rr);
			} else {
				handleNotSelfGscOnBreak(rr);
			}

		}
	}
	
	/**
	 * 打断LINK后维护自相交的立交关系
	 * @param rr 立交对象
	 * @throws Exception
	 */
	private void handleSelfGscOnBreak(RdGsc rr) throws Exception {
		
		Geometry gscGeo = rr.getGeometry();
		
		List<RdLink> newGscLink = new ArrayList<>();
		
		for(RdLink link : command.getNewLinks())
		{
			if(link.getGeometry().distance(gscGeo) < 1)
			{
				newGscLink.add(link);
			}
		}
		
		//打断link后还是自相交立交
		if(newGscLink.size() == 1)
		{
			for (int i = 0; i < rr.getLinks().size(); i++) {
				RdGscLink rdGscLink = (RdGscLink) rr.getLinks().get(i);
				
				rdGscLink.changedFields().put("linkPid", newGscLink.get(0).getPid());
				
				result.insertObject(rdGscLink, ObjStatus.UPDATE, rdGscLink.getPid());
			}
		}
		else
		{
			//打断后生成多线（双线）立交
			for(int i = 0;i<newGscLink.size();i++)
			{
				RdLink  newLink = newGscLink.get(i);
				
				RdGscLink gscLink = new RdGscLink();
				
				//截取精度，防止位置序号计算错误
				Geometry tmpLinkGeo = GeoTranslator.transform(newLink.getGeometry(), 1, 0);
				
				List<Integer> shpSeqNumList = RdGscOperateUtils.calcShpSeqNum(gscGeo, tmpLinkGeo.getCoordinates());
				
				gscLink.setPid(rr.getPid());
				
				gscLink.setLinkPid(newLink.getPid());
				
				gscLink.setTableName("RD_LINK");
				
				gscLink.setZlevel(i);
				
				gscLink.setShpSeqNum(shpSeqNumList.get(0));
				
				result.insertObject(gscLink, ObjStatus.UPDATE, gscLink.getPid());
			}
		}
	}

	/**
	 * 处理非自相交的立交的组成线被打断
	 * 
	 * @param rdGsc 立交对象
	 * @throws Exception
	 */
	private void handleNotSelfGscOnBreak(RdGsc rdGsc) throws Exception {
		// 每个立交至少由两条线组成，循环遍历每条link
		for (IRow row : rdGsc.getLinks()) {
			RdGscLink rdGscLink = (RdGscLink) row;

			// 找到打断的那条link
			if (rdGscLink.getLinkPid() == command.getLinkPid()) {
				
				RdLink link = null;
				
				List<RdLink> linkList = command.getNewLinks();
				
				for(RdLink newLink : linkList)
				{
					double distance = rdGsc.getGeometry().distance(linkList.get(0).getGeometry());
					
					// 计算代表点和生成的线最近的link
					if(distance == 0)
					{
						link = newLink;
						break;
					}
				}

				if (link != null) {
					// 打断后，立交的link和link序号需要重新计算
					List<Integer> shpSeqNum = RdGscOperateUtils.calcShpSeqNum(rdGsc.getGeometry(), link.getGeometry().getCoordinates());

					JSONObject updateContent = new JSONObject();

					updateContent.put("shpSeqNum", shpSeqNum.get(0));

					updateContent.put("linkPid", link.getPid());

					boolean changed = rdGscLink.fillChangeFields(updateContent);

					if (changed) {
						result.insertObject(rdGscLink, ObjStatus.UPDATE, rdGsc.pid());
					}
				}

			}
		}
	}
}
