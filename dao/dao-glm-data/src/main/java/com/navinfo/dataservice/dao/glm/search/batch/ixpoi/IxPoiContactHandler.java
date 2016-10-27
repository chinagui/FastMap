package com.navinfo.dataservice.dao.glm.search.batch.ixpoi;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.ResultSetHandler;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiContact;

public class IxPoiContactHandler implements ResultSetHandler<Map<Long,List<IRow>>>{
	
	@Override
	public Map<Long, List<IRow>> handle(ResultSet rs) throws SQLException {
		Map<Long, List<IRow>> contactMap = new HashMap<Long, List<IRow>>();
		try {
			while (rs.next()){
				List<IRow> contactList = new ArrayList<IRow>();
				IxPoiContact ixPoiContact = new IxPoiContact();
				ixPoiContact.setContactType(rs.getInt("contact_type"));
				ixPoiContact.setContact(rs.getString("contact"));
				ixPoiContact.setContactDepart(rs.getInt("contact_depart"));
				ixPoiContact.setPriority(rs.getInt("priority"));
				ixPoiContact.setRowId(rs.getString("row_id"));
				
				if (contactMap.containsKey(rs.getLong("poi_pid"))) {
					contactList = contactMap.get(rs.getLong("poi_pid"));
					contactList.add(ixPoiContact);
					contactMap.put(rs.getLong("poi_pid"), contactList);
				} else {
					contactList.add(ixPoiContact);
					contactMap.put(rs.getLong("poi_pid"), contactList);
				}
			}
			return contactMap;
		} catch (Exception e) {
			throw e;
		}
	}
}
