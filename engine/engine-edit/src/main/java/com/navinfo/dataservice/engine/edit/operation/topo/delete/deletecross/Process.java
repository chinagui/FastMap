package com.navinfo.dataservice.engine.edit.operation.topo.delete.deletecross;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.dao.glm.iface.AlertObject;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdBranchSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.cross.RdCrossSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.laneconnexity.RdLaneConnexitySelector;
import com.navinfo.dataservice.dao.glm.selector.rd.restrict.RdRestrictionSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

import net.sf.json.JSONObject;

public class Process extends AbstractProcess<Command> {

	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	public void lockRdCross() throws Exception {
		// 获取该cross对象
		RdCrossSelector selector = new RdCrossSelector(this.getConn());

		RdCross cross = (RdCross) selector.loadById(this.getCommand().getPid(), true);

		this.getCommand().setCross(cross);
	}

	public void lockRdRestriction() throws Exception {
		RdRestrictionSelector selector = new RdRestrictionSelector(this.getConn());

		List<RdRestriction> rdRestrictions = selector.getRestrictionByCrossPid(this.getCommand().getPid(), true);

		this.getCommand().setRestricts(rdRestrictions);
	}

	public void lockRdLaneConnexity() throws Exception {
		RdLaneConnexitySelector selector = new RdLaneConnexitySelector(getConn());

		List<RdLaneConnexity> rdLaneConnexities = selector.getRdLaneConnexityByCrossPid(this.getCommand().getPid(),
				true);

		this.getCommand().setLanes(rdLaneConnexities);
	}

	public void lockRdBranch() throws Exception {

		RdBranchSelector selector = new RdBranchSelector(getConn());

		List<RdBranch> branches = selector.getRdBranchByCrossPid(this.getCommand().getPid(), true);

		this.getCommand().setBranches(branches);
	}
	
	@Override
	public boolean prepareData() throws Exception {

		lockRdCross();

		if (this.getCommand().getCross() == null) {

			throw new Exception("指定删除的路口不存在！");
		}

		lockRdRestriction();

		lockRdLaneConnexity();

		lockRdBranch();

		return true;
	}
	
	@Override
	public String run() throws Exception {

		try {
			if (!this.getCommand().isCheckInfect()) {
				this.getConn().setAutoCommit(false);
				String preCheckMsg = this.preCheck();
				if (preCheckMsg != null) {
					throw new Exception(preCheckMsg);
				}

				prepareData();

				updataRelationObj();

				recordData();

				postCheck();

				this.getConn().commit();
			} else {
				prepareData();
				
				Map<String, List<AlertObject>> infects = confirmRelationObj();
				
				this.getConn().commit();

				return JSONObject.fromObject(infects).toString();
			}

		} catch (Exception e) {

			this.getConn().rollback();

			throw e;
		} finally {
			try {
				this.getConn().close();
			} catch (Exception e) {

			}
		}

		return null;
	}
	
	/**
	 * @throws Exception 
	 * 
	 */
	private void updataRelationObj() throws Exception {
		IOperation op = new OpTopo(this.getCommand(), getConn());

		op.run(this.getResult());

		//交限
		IOperation opRefRestrict = new OpRefRdRestriction(this.getCommand());

		opRefRestrict.run(this.getResult());

		//车信
		IOperation opRefLaneConnexity = new OpRefRdLaneConnexity(this.getCommand());

		opRefLaneConnexity.run(this.getResult());

		// 删除信号灯
		OpRefTrafficsignal opRefTrafficsignal = new OpRefTrafficsignal(this.getConn());

		opRefTrafficsignal.run(this.getResult(), this.getCommand().getCross().getNodes());
		
//		//分歧
//		IOperation opRefBranch = new OpRefRdBranch(this.getCommand());
//		
//		opRefBranch.run(this.getResult());
		
		//顺行
		OpRefDirectoroute opRefDirectoroute = new OpRefDirectoroute(this.getConn());
		opRefDirectoroute.run(this.getResult(), this.getCommand().getPid());
		
		//语音引导
		OpRefVoiceGuide opRefVoiceGuide = new OpRefVoiceGuide(this.getConn());
		opRefVoiceGuide.run(this.getResult(), this.getCommand().getPid());
	}

	/**
	 * 删除node影响到的关联要素
	 * 
	 * @return
	 * @throws Exception
	 */
	private Map<String, List<AlertObject>> confirmRelationObj() throws Exception {
		Map<String, List<AlertObject>> infects = new HashMap<String, List<AlertObject>>();
		
		Connection conn = getConn();
		
		int crossPid = this.getCommand().getPid();
		
		//路口自身删除提示
		OpTopo crossTopo = new OpTopo();
		List<AlertObject> alertObject = crossTopo.getDeleteCrossInfectData(crossPid);
		if(CollectionUtils.isNotEmpty(this.getCommand().getCross().getNames()))
		{
			infects.put("删除路口(此路口记录有路口名称信息，请注意维护)", alertObject);
		}
		else
		{
			infects.put("删除路口", alertObject);
		}
		
		//信号灯
		com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.delete.Operation trafficOperation = new com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.delete.Operation(
				conn);		
		List<AlertObject> trafficAlertData = trafficOperation.getDeleteCrossTrafficInfectData(this.getCommand().getCross().getNodes(),conn);
		if(CollectionUtils.isNotEmpty(trafficAlertData))
		{
			infects.put("删除路口删除路口信号灯", trafficAlertData);
		}
		//车信
		com.navinfo.dataservice.engine.edit.operation.obj.rdlaneconnexity.delete.Operation rdLaneConOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdlaneconnexity.delete.Operation();
		List<AlertObject> rdLaneConAlertData = rdLaneConOperation.getDeleteCrossRdlaneConInfectData(this.getCommand().getLanes());
		if(CollectionUtils.isNotEmpty(rdLaneConAlertData))
		{
			infects.put("删除路口删除路口车信", rdLaneConAlertData);
		}
		//交限
		com.navinfo.dataservice.engine.edit.operation.obj.rdrestriction.delete.Operation rdResOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdrestriction.delete.Operation();
		List<AlertObject> rdResAlertData = rdResOperation.getDeleteCrossRestrictInfectData(this.getCommand().getRestricts());
		if(CollectionUtils.isNotEmpty(rdResAlertData))
		{
			infects.put("删除路口删除路口交限", rdResAlertData);
		}
//		//分歧
//		com.navinfo.dataservice.engine.edit.operation.obj.rdbranch.delete.Operation rdBranchOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdbranch.delete.Operation();
//		List<AlertObject> rdBranchAlertData = rdBranchOperation.getDeleteCrossBranchInfectData(this.getCommand().getBranches());
//		if(CollectionUtils.isNotEmpty(rdBranchAlertData))
//		{
//			infects.put("删除路口删除路口分歧", rdBranchAlertData);
//		}
		//语音引导
		com.navinfo.dataservice.engine.edit.operation.obj.rdvoiceguide.delete.Operation voiceGuideOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rdvoiceguide.delete.Operation(conn);
		List<AlertObject> voiceGuideAlertData = voiceGuideOperation.getDeleteCrossVoiceGuideInfectData(crossPid);
		if(CollectionUtils.isNotEmpty(voiceGuideAlertData))
		{
			infects.put("删除路口删除语音引导", voiceGuideAlertData);
		}
		//顺行
		com.navinfo.dataservice.engine.edit.operation.obj.rddirectroute.delete.Operation directoureOperation = new com.navinfo.dataservice.engine.edit.operation.obj.rddirectroute.delete.Operation(conn);
		List<AlertObject> directoureAlertData = directoureOperation.getDeleteCrossDirectoureInfectData(crossPid);
		if(CollectionUtils.isNotEmpty(directoureAlertData))
		{
			infects.put("删除路口删除顺行", directoureAlertData);
		}
		return infects;
	}

	@Override
	public String exeOperation() throws Exception {
		return null;
	}
}
