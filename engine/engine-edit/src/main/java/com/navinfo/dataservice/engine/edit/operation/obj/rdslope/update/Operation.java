package com.navinfo.dataservice.engine.edit.operation.obj.rdslope.update;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.slope.RdSlopeVia;

/**
 * @author zhaokk 修改坡度信息
 */
public class Operation implements IOperation {

	private Command command;

	public Operation(Command command) {
		this.command = command;

	}

	@Override
	public String run(Result result) throws Exception {
		this.updateRdSlope(result);
		this.handRdSlopeVia(result);
		return null;
	}

	/***
	 * 修改坡度表信息
	 * 
	 * @param result
	 * @throws Exception
	 */
	private void updateRdSlope(Result result) throws Exception {

		JSONObject content = command.getContent();

		if (content.containsKey("objStatus")) {
			boolean isChanged = this.command.getSlope().fillChangeFields(
					content);

			if (isChanged) {
				result.insertObject(this.command.getSlope(), ObjStatus.UPDATE,
						this.command.getSlope().getPid());
			}

		}
	}

	/***
	 * 维护坡度接续link信息
	 * 1.如果只是修改坡度信息字段 持续link 参数就不需要传值  只需要维护主表坡度的信息
	 * 2.如果 修改退出线或者修改坡度接续link
	 * 	 a. 如果退出线修改且和以前值不一致 原有的坡度的接续link需要删除掉 同时新增新的接续link
	 *   b. 如果退出线没有修改:
	 *   		1）如果传入的接续link比原有坡度信息接续link少 则删除坡度这部分少的接续link。
	 *          2）如果传入的接续link比原有坡度信息接续link多 则新增坡度这部分多的接续link。
	 * @param result
	 */
	private void handRdSlopeVia(Result result) {
		if (this.command.getSeriesLinkPids() != null) {
			if (this.command.getOutLinkPid() != 0
					&& this.command.getOutLinkPid() != this.command.getSlope()
							.getLinkPid()) {
				for (IRow row : this.command.getSlope().getSlopeVias()) {
					result.insertObject(row, ObjStatus.DELETE,
							this.command.getPid());
				}
				for (int i = 0; i < this.command.getSeriesLinkPids().size(); i++) {
					this.addRdSlope(result, i);
				}

			} else {
				int sourceSize = this.command.getSlope().getSlopeVias().size();
				int currentSize = this.command.getSeriesLinkPids().size();

				if (sourceSize > currentSize) {
					for (int i = currentSize; i <= sourceSize; i++) {
						result.insertObject(this.command.getSlope()
								.getSlopeVias().get(i), ObjStatus.DELETE,
								this.command.getPid());

					}
				}
				if (sourceSize < currentSize) {
					for (int i = sourceSize; i <= currentSize; i++) {
						this.addRdSlope(result, i);
					}
				}

			}
		}
	}
	/***
	 * 新增坡度接续link信息
	 * @param result
	 * @param seqNum
	 */
	private void addRdSlope(Result result, int seqNum) {
		RdSlopeVia rdSlopeVia = new RdSlopeVia();
		rdSlopeVia.setSlopePid(this.command.getPid());
		rdSlopeVia.setLinkPid(this.command.getSeriesLinkPids().get(seqNum));
		rdSlopeVia.setSeqNum(seqNum);
		result.insertObject(rdSlopeVia, ObjStatus.INSERT, this.command.getPid());
	}

}
