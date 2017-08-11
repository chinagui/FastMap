package com.navinfo.dataservice.dao.glm.selector.ad.zone;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.exception.DataNotFoundException;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISelector;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneFace;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneFaceTopo;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.navicommons.database.sql.DBUtils;

/**
 * ZONE:Face Topo 查询接口
 * 
 * @author zhaokk
 * 
 */
public class ZoneFaceTopoSelector extends AbstractSelector {

	private static Logger logger = Logger.getLogger(ZoneFaceTopoSelector.class);

	private Connection conn;

	public ZoneFaceTopoSelector(Connection conn) {
		super(conn);
		this.conn = conn;
		this.setCls(ZoneFaceTopo.class);
	}
	/**
	 * 
	 * 加载Zone_link和topo的信息
	 * @param linkPid
	 * @param isLock
	 * @return
	 * @throws Exception
	 */
	public List<ZoneFaceTopo> loadByLinkPid(Integer linkPid, boolean isLock)
			throws Exception {

		List<ZoneFaceTopo> zoneFaceTopos= new ArrayList<ZoneFaceTopo>();
		String sql = "SELECT a.* FROM zone_face_topo a WHERE a.link_pid =:1 and  a.u_record !=2 ";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(sql);

			pstmt.setInt(1, linkPid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				ZoneFaceTopo zoneFaceTopo = new ZoneFaceTopo();
				ReflectionAttrUtils.executeResultSet(zoneFaceTopo, resultSet);
				zoneFaceTopos.add(zoneFaceTopo);
			}
		} catch (Exception e) {

			throw e;

		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);

		}

		return zoneFaceTopos;
	}

}
