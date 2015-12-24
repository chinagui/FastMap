package com.navinfo.dataservice.FosEngine.edit.operation.obj.rdrestriction.create;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.FosEngine.edit.model.IRow;
import com.navinfo.dataservice.FosEngine.edit.model.ObjStatus;
import com.navinfo.dataservice.FosEngine.edit.model.Result;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.restrict.RdRestriction;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.restrict.RdRestrictionDetail;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.restrict.RdRestrictionVia;
import com.navinfo.dataservice.FosEngine.edit.operation.IOperation;
import com.navinfo.dataservice.FosEngine.edit.operation.obj.rdrestriction.Helper;
import com.navinfo.dataservice.commons.geom.AngleCalculator;
import com.navinfo.dataservice.commons.service.PidService;
import com.vividsolutions.jts.geom.LineSegment;

public class Operation implements IOperation {

	private Command command;

	private Connection conn;

	private LineSegment inLinkSegment;

	/**
	 * key为退出线pid，value为退出线线段
	 */
	private Map<Integer, LineSegment> outLinkSegmentMap;

	/**
	 * key为退出线pid， value为经过线pid列表
	 */
	private Map<Integer, List<Integer>> viaLinkPidMap;

	/**
	 * key为退出线pid，value为交限类型
	 */
	private Map<Integer, Integer> relationTypeMap;

	public Operation(Command command, Connection conn) {
		this.command = command;

		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {

		RdRestriction restrict = new RdRestriction();

		restrict.setPid(PidService.getInstance().applyRestrictionPid());

		result.setPrimaryPid(restrict.getPid());

		restrict.setInLinkPid(command.getInLinkPid());

		restrict.setNodePid(command.getNodePid());

		List<Integer> outLinkPids = null;

		if (command.getOutLinkPids() != null) {
			outLinkPids = command.getOutLinkPids();
		}

		else {
			outLinkPids = Helper.calOutLinks(conn, command.getInLinkPid(),
					command.getNodePid());
		}
		
		Helper.calViaLinks(conn, command.getInLinkPid(), command.getNodePid(),
				outLinkPids, inLinkSegment, outLinkSegmentMap, viaLinkPidMap, relationTypeMap);

		List<IRow> details = new ArrayList<IRow>();

		List<Integer> restricInfos = new ArrayList<Integer>();
		
		List<Integer> newRestricInfos = command.getRestricInfos();
		
		for (int outLinkPid : outLinkPids) {

			RdRestrictionDetail detail = new RdRestrictionDetail();

			detail.setRestricPid(restrict.getPid());

			detail.setOutLinkPid(outLinkPid);

			LineSegment outLinkSegment = outLinkSegmentMap.get(detail
					.getOutLinkPid());

			double angle = AngleCalculator.getAngle(inLinkSegment,
					outLinkSegment);

			int restricInfo = Helper.calRestricInfo(angle);
			
			if (newRestricInfos != null){
				if (newRestricInfos.contains(restricInfo)){
					newRestricInfos.remove(Integer.valueOf(restricInfo));
				}
				else{
					continue;
				}
			}
			
			detail.setPid(PidService.getInstance().applyRestrictionDetailPid());

			detail.setRestricInfo(restricInfo);

			detail.setRelationshipType(relationTypeMap.get(detail
					.getOutLinkPid()));

			List<Integer> viaLinkPids = viaLinkPidMap.get(detail
					.getOutLinkPid());

			int seqNum = 1;

			List<IRow> vias = new ArrayList<IRow>();

			for (Integer viaLinkPid : viaLinkPids) {

				RdRestrictionVia via = new RdRestrictionVia();

				via.setDetailId(detail.getPid());

				via.setSeqNum(seqNum);

				via.setLinkPid(viaLinkPid);

				vias.add(via);

				seqNum++;
			}

			detail.setVias(vias);

			details.add(detail);

		}
		
		if (newRestricInfos != null){
			//找不到退出link的也要创建detail
			for(Integer newRestricInfo : newRestricInfos){
				RdRestrictionDetail detail = new RdRestrictionDetail();
	
				detail.setRestricPid(restrict.getPid());
	
				detail.setOutLinkPid(0);
	
				detail.setPid(PidService.getInstance().applyRestrictionDetailPid());
	
				detail.setRestricInfo(newRestricInfo);
	
				details.add(detail);
			}
		}
		
		restrict.setDetails(details);

		if (restricInfos.size() > 0) {
			StringBuilder sb = new StringBuilder();

			for (Integer restricInfo : restricInfos) {
				sb.append("[");

				sb.append(restricInfo);

				sb.append("]");

				sb.append(",");
			}

			sb.deleteCharAt(sb.length() - 1);

			restrict.setRestricInfo(sb.toString());
		}

		result.insertObject(restrict, ObjStatus.INSERT);

		return null;
	}


}
