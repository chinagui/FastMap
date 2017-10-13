package com.navinfo.dataservice.engine.limit.Utils;

import java.sql.Connection;

import com.navinfo.dataservice.engine.limit.glm.iface.ISearch;
import com.navinfo.dataservice.engine.limit.glm.iface.LimitObjType;
import com.navinfo.dataservice.engine.limit.search.limit.ScPlateresFaceSearch;
import com.navinfo.dataservice.engine.limit.search.limit.ScPlateresLinkSearch;
import com.navinfo.dataservice.engine.limit.search.meta.ScPlateresGeometrySearch;
import com.navinfo.dataservice.engine.limit.search.meta.ScPlateresGroupSearch;
import com.navinfo.dataservice.engine.limit.search.meta.ScPlateresManoeuvreSearch;

public class PidApply {
	
	private Connection conn;

	private volatile static PidApply instance;

	public static PidApply getInstance(Connection conn) {
		if (instance == null) {
			synchronized (PidApply.class) {
				if (instance == null) {
					instance = new PidApply(conn);
				}
			}
		}
		return new PidApply(conn);
	}

	private PidApply(Connection conn) {
		this.conn = conn;
	}

	public String pidForInsertGroup(String infoIntelId,int adAdmin,String condition) throws Exception{
		
		String newGroupId = "";
		
		ScPlateresGroupSearch search = new ScPlateresGroupSearch(this.conn);
		
		String maxGroupId = search.loadMaxGroupId(condition+ String.valueOf(adAdmin).substring(0, 4));

		if (maxGroupId == null || maxGroupId.isEmpty()) {

			newGroupId = condition + String.valueOf(adAdmin).substring(0, 4) + "000001";

		} else {

			String num = maxGroupId.substring(6);

			int number = Integer.valueOf(num);

			number++;

			newGroupId = maxGroupId.substring(0, 5) + String.format("%06d", number);
		}
		
		return newGroupId;
	} 
	
	/**
	 * manoeuvre的pid申请
	 * @param groupId
	 * @return
	 * @throws Exception
	 */
	public int pidForInsertManoeuvre(String groupId) throws Exception{
		
		ScPlateresManoeuvreSearch search = new ScPlateresManoeuvreSearch(this.conn);
		
		int newManouvreId = 0;
		
		int maxManouvreId = search.loadMaxManoeuvreId(groupId);
	
		newManouvreId = maxManouvreId + 1;
		
		return newManouvreId;
	}
	
	/**
	 * 
	 * @param groupId
	 * @return
	 * @throws Exception
	 */
	public String pidForInsertGeometry(String groupId, LimitObjType type, int... seq) throws Exception {

		String newGeometryId = "";

		ISearch search = null;

		switch (type) {
		case SCPLATERESGEOMETRY:
			search = new ScPlateresGeometrySearch(this.conn);
			break;
		case SCPLATERESLINK:
			search = new ScPlateresLinkSearch(this.conn);
			break;
		case SCPLATERESFACE:
			search = new ScPlateresFaceSearch(this.conn);
			break;
		}

		String geometryId = search.loadMaxKeyId(groupId);

		if (geometryId == null || geometryId.isEmpty()) {

			if (seq.length == 0) {
				newGeometryId = groupId + "000001";
			} else {
				newGeometryId = groupId + String.format("%06d", seq[0] + 1);
			}

		} else {

			int length = geometryId.length();

			String num = geometryId.substring(length - 5);

			int number = Integer.valueOf(num);

			number++;

			if (seq.length != 0) {
				number += seq[0];
			}

			newGeometryId = geometryId.substring(0, length - 6) + String.format("%06d", number);
		}

		return newGeometryId;
	}
}
