package com.navinfo.dataservice.dao.glm.selector.poi.deep;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiGasstation;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
/**
 * 索引:POI 深度信息(加油站类)查询接口
 * @author zhaokk
 *
 */
public class IxPoiGasstationSelector extends AbstractSelector {
	private Connection conn;

	public IxPoiGasstationSelector(Connection conn) {
		super(conn);
		this.conn = conn;
		this.setCls(IxPoiGasstation.class);
	}

	public List<IRow> loadByIdForAndroid(int id) throws Exception {
		List<IRow> rows = new ArrayList<IRow>();
		

		StringBuilder sb = new StringBuilder(
				"select fuel_type,oil_type,eg_type,mg_type,payment,service,service_prov,open_hour,row_id from ix_poi_gasstation WHERE poi_pid  = :1 and  u_record !=2");

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sb.toString());

			pstmt.setInt(1, id);

			resultSet = pstmt.executeQuery();

			while(resultSet.next()) {
				IxPoiGasstation ixPoiGasstation = new IxPoiGasstation();
				ixPoiGasstation.setFuelType(resultSet.getString("fuel_type"));
				ixPoiGasstation.setOilType(resultSet.getString("oil_type"));
				ixPoiGasstation.setEgType(resultSet.getString("eg_type"));
				ixPoiGasstation.setMgType(resultSet.getString("mg_type"));
				ixPoiGasstation.setPayment(resultSet.getString("payment"));
				ixPoiGasstation.setService(resultSet.getString("service"));
				ixPoiGasstation.setServiceProv(resultSet.getString("service_prov"));
				ixPoiGasstation.setOpenHour(resultSet.getString("open_hour"));
				ixPoiGasstation.setRowId(resultSet.getString("row_id"));
				rows.add(ixPoiGasstation);
			} return rows;
		} catch (Exception e) {

			throw e;

		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt); 
		}
	}
	
}
