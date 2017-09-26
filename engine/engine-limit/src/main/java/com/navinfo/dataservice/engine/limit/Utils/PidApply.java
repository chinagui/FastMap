package com.navinfo.dataservice.engine.limit.Utils;

import java.sql.Connection;

import com.navinfo.dataservice.engine.limit.glm.model.limit.ScPlateresInfo;
import com.navinfo.dataservice.engine.limit.search.limit.ScPlateresInfoSearch;
import com.navinfo.dataservice.engine.limit.search.meta.ScPlateresGroupSearch;
import com.navinfo.dataservice.engine.limit.search.meta.ScPlateresManoeuvreSearch;

public class PidApply {
	
	private Connection conn;
	
	public static PidApply getInstance(Connection conn){
		return new PidApply(conn);
	}
	
	public PidApply(Connection conn){
		this.conn = conn;
	}
	
	public String pidForInsertGroup(String infoIntelId,int adAdmin) throws Exception{
		
		String newGroupId = "";
		
		ScPlateresGroupSearch search = new ScPlateresGroupSearch(this.conn);
		
		String maxGroupId = search.loadMaxGroupId(infoIntelId);
		
		if(maxGroupId==null || maxGroupId.isEmpty()){
			
			ScPlateresInfoSearch infoSearch = new ScPlateresInfoSearch(this.conn);
			
			ScPlateresInfo info = infoSearch.loadById(infoIntelId);
			
			newGroupId = info.getCondition() +  String.valueOf(adAdmin).substring(0, 4) + "000001";
			
		}else{
			
			String num = maxGroupId.substring(6);
			
			int number = Integer.valueOf(num);
			
			number ++;
			
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
}
