package com.navinfo.dataservice.dao.glm.selector.poi.deep;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiCarrental;
/**
 * 索引:POI 深度信息(汽车租赁) 
 * @author zhaokk
 *
 */
public class IxPoiCarrentalSelector implements ISelector {
	private Connection conn;

	public IxPoiCarrentalSelector(Connection conn) {
		super();
		this.conn = conn;
	}

	@Override
	public IRow loadById(int id, boolean isLock) throws Exception {
		return null;
	}

	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock)
			throws Exception {
		List<IRow> rows = new ArrayList<IRow>();
		

		StringBuilder sb = new StringBuilder(
				"select * from ix_poi_carrental  WHERE poi_pid  = :1 and  u_record !=2");

		if (isLock) {
			sb.append(" for update nowait");
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sb.toString());

			pstmt.setInt(1, id);

			resultSet = pstmt.executeQuery();

			while(resultSet.next()) {
				IxPoiCarrental ixPoiCarrental= new IxPoiCarrental();
				this.setAttr(ixPoiCarrental, resultSet);
				rows.add(ixPoiCarrental);
			} return rows;
		} catch (Exception e) {

			throw e;

		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
			} catch (Exception e) {

			}

			try {
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (Exception e) {

			}

		}
	}

	private void setAttr(IxPoiCarrental ixPoiCarrental,ResultSet resultSet) throws SQLException{
		
		ixPoiCarrental.setPoiPid(resultSet.getInt("poi_pid"));
		ixPoiCarrental.setOpenHour(resultSet.getString("open_hour"));
		ixPoiCarrental.setAdress(resultSet.getString("address"));
		ixPoiCarrental.setHowToGo(resultSet.getString("how_to_go"));
		ixPoiCarrental.setPhone400(resultSet.getString("phone_400"));
		ixPoiCarrental.setWebsite(resultSet.getString("web_site"));
		ixPoiCarrental.setRowId(resultSet.getString("rowId"));

	}
	
}
