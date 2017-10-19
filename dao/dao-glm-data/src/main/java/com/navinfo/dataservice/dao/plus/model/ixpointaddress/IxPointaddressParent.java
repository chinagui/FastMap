package com.navinfo.dataservice.dao.plus.model.ixpointaddress;

import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;

/**
 * @ClassName: IxPointaddressParent
 * @author code generator
 * @date 2017-10-16 02:51:44
 * @Description: TODO
 */
public class IxPointaddressParent extends BasicRow {
	private long groupId;
	private long parentPaPid;

	public IxPointaddressParent() {
		super();
		setParentPaPid(objPid);
	}

	public IxPointaddressParent(long objPid) {
		super(objPid);
	}

	public long getGroupId() {
		return groupId;
	}

	public void setGroupId(long groupId) {
		if (this.checkValue("GROUP_ID", this.groupId, groupId)) {
			this.groupId = groupId;
		}
	}

	public long getParentPaPid() {
		return parentPaPid;
	}

	public void setParentPaPid(long parentPaPid) {
		if (this.checkValue("PARENT_PA_PID", this.parentPaPid, parentPaPid)) {
			this.parentPaPid = parentPaPid;
		}
	}

	@Override
	public String tableName() {
		// TODO Auto-generated method stub
		return "IX_POINTADDRESS_PARENT";
	}

	public static final String GROUP_ID = "GROUP_ID";
	public static final String PARENT_PA_PID = "PARENT_PA_PID";

}
