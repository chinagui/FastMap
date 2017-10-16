package com.navinfo.dataservice.dao.plus.model.ixpointaddress;

import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;

/**
 * @ClassName: IxPointaddressChildren
 * @author code generator
 * @date 2017-10-16 02:52:34
 * @Description: TODO
 */
public class IxPointaddressChildren extends BasicRow {
	private long groupId;
	private long childPaPid;

	public IxPointaddressChildren() {
		super();
	}

	public IxPointaddressChildren(long objPid) {
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

	public long getChildPaPid() {
		return childPaPid;
	}

	public void setChildPaPid(long childPaPid) {
		if (this.checkValue("CHILD_PA_PID", this.childPaPid, childPaPid)) {
			this.childPaPid = childPaPid;
		}
	}

	@Override
	public String tableName() {
		// TODO Auto-generated method stub
		return "IX_POINTADDRESS_CHILDREN";
	}

	public static final String GROUP_ID = "GROUP_ID";
	public static final String CHILD_PA_PID = "CHILD_PA_PID";
}
