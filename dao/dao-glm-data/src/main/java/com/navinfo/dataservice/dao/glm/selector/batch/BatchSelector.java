package com.navinfo.dataservice.dao.glm.selector.batch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.navicommons.database.sql.DBUtils;

public class BatchSelector {

	private Connection conn;

	public BatchSelector(Connection conn) {

		this.conn = conn;
	}

	/**
	 * 加载在adface面内或者在面上的poi且poi与face的regionId不一致
	 * 
	 * @param facePid
	 * @param isLock
	 * @return 满足条件的poi
	 * @throws Exception
	 */
	public List<IxPoi> loadPoiByAdFace(int facePid, boolean isLock)
			throws Exception {

		List<IxPoi> pois = new ArrayList<IxPoi>();

		StringBuilder sb = new StringBuilder(
				"SELECT A.* FROM IX_POI A, AD_FACE B WHERE B.FACE_PID = :1 AND A.U_RECORD != 2 ");

		sb.append(" AND B.REGION_ID!= A.REGION_ID AND B.U_RECORD != 2 AND SDO_RELATE(A.GEOMETRY, B.GEOMETRY, 'MASK=ANYINTERACT') = 'TRUE'");

		if (isLock) {

			sb.append(" for update nowait");
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {

			pstmt = conn.prepareStatement(sb.toString());

			pstmt.setInt(1, facePid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				IxPoi row = new IxPoi();

				ReflectionAttrUtils.executeResultSet(row, resultSet);

				pois.add(row);

			}
		} catch (Exception e) {

			throw e;

		} finally {

			DBUtils.closeResultSet(resultSet);

			DBUtils.closeStatement(pstmt);
		}

		return pois;
	}

	/**
	 * 加载与AdFace面相交或者在面内的link，
	 * 且link的LEFT_REGION_ID或RIGHT_REGION_ID与face的REGION_ID不同
	 * 
	 * @param facePid
	 * @param isLock
	 * @return 满足条件的link集合，link只加载主表信息
	 * @throws Exception
	 */
	public List<RdLink> loadLinkByAdFace(int facePid, boolean isLock)
			throws Exception {

		List<RdLink> rdLinks = new ArrayList<RdLink>();

		StringBuilder sb = new StringBuilder(
				"SELECT A.* FROM RD_LINK A, AD_FACE B WHERE ");

		sb.append(" (A.LEFT_REGION_ID != B.REGION_ID OR A.RIGHT_REGION_ID != B.REGION_ID) AND ");

		sb.append(" B.FACE_PID = :1 AND A.U_RECORD != 2 AND B.U_RECORD != 2 AND SDO_RELATE(A.GEOMETRY, B.GEOMETRY, 'mask=anyinteract') = 'TRUE'");

		if (isLock) {

			sb.append(" for update nowait");
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {

			pstmt = conn.prepareStatement(sb.toString());

			pstmt.setInt(1, facePid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				RdLink link = new RdLink();

				ReflectionAttrUtils.executeResultSet(link, resultSet);

				rdLinks.add(link);
			}
		} catch (Exception e) {

			throw e;

		} finally {

			DBUtils.closeResultSet(resultSet);

			DBUtils.closeStatement(pstmt);
		}

		return rdLinks;

	}

	/**
	 * 加载与LuFace面相交或者在面内的link； batchType为1：加载URBAN=0的link，batchType为0：加载URBAN=1
	 * 的link
	 * 
	 * @param facePid
	 * @param batchType
	 *            1：赋URBAN，0：删URBAN
	 * @param isLock
	 * @return 面内link集合，link只加载主表信息
	 * @throws Exception
	 */
	public List<RdLink> loadLinkByLuFace(int facePid, int batchType,
			boolean isLock) throws Exception {

		List<RdLink> rdLinks = new ArrayList<RdLink>();

		StringBuilder sb = new StringBuilder(
				"SELECT A.* FROM RD_LINK A, LU_FACE B WHERE ");

		if (batchType == 0) {

			sb.append(" A.URBAN = 1 AND ");

		} else if (batchType == 1) {

			sb.append(" A.URBAN = 0 AND ");
		}

		sb.append(" B.FACE_PID = :1 AND A.U_RECORD != 2 AND B.U_RECORD != 2 AND SDO_RELATE(A.GEOMETRY, B.GEOMETRY, 'mask=anyinteract') = 'TRUE'");

		if (isLock) {

			sb.append(" for update nowait");
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {

			pstmt = conn.prepareStatement(sb.toString());

			pstmt.setInt(1, facePid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				RdLink link = new RdLink();

				ReflectionAttrUtils.executeResultSet(link, resultSet);

				rdLinks.add(link);
			}
		} catch (Exception e) {

			throw e;

		} finally {

			DBUtils.closeResultSet(resultSet);

			DBUtils.closeStatement(pstmt);
		}

		return rdLinks;
	}

}
