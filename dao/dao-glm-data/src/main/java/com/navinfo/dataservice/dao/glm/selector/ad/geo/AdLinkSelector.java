package com.navinfo.dataservice.dao.glm.selector.ad.geo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLinkMesh;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.vividsolutions.jts.geom.Geometry;

import oracle.sql.STRUCT;

public class AdLinkSelector extends AbstractSelector {

	private static Logger logger = Logger.getLogger(AdFaceSelector.class);

	private Connection conn;

	public AdLinkSelector(Connection conn) {
		super(conn);
		this.conn = conn;
		this.setCls(AdLink.class);
	}

	public List<AdLink> loadByNodePid(int nodePid, boolean isLock)
			throws Exception {

		List<AdLink> links = new ArrayList<AdLink>();

		StringBuilder sb = new StringBuilder(
				"select * from ad_link where (s_node_pid = :1 or e_node_pid = :2) and u_record!=2");

		if (isLock) {
			sb.append(" for update nowait");
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sb.toString());

			pstmt.setInt(1, nodePid);

			pstmt.setInt(2, nodePid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				AdLink adLink = new AdLink();

				adLink.setPid(resultSet.getInt("link_pid"));
				adLink.setsNodePid(resultSet.getInt("s_node_pid"));
				adLink.seteNodePid(resultSet.getInt("e_node_pid"));
	            adLink.setKind(resultSet.getInt("kind"));
				adLink.setForm(resultSet.getInt("form"));
				STRUCT struct = (STRUCT) resultSet.getObject("geometry");
				Geometry geometry = GeoTranslator.struct2Jts(struct, 100000, 0);
				adLink.setGeometry(geometry);
				adLink.setLength(resultSet.getInt("length"));
				adLink.setScale(resultSet.getInt("scale"));
				adLink.setEditFlag(resultSet.getInt("edit_flag"));
				adLink.setRowId(resultSet.getString("row_id"));
				List<IRow> forms = new AbstractSelector(AdLinkMesh.class,conn).loadRowsByParentId(adLink.getPid(), isLock);
				
				//loadRowsByParentId已经查询了mesh,是否可以不做设置
				for (IRow row : forms) {
					row.setMesh(adLink.mesh());
				}

				adLink.setMeshes(forms);

				for (IRow row : adLink.getMeshes()) {
					AdLinkMesh mesh = (AdLinkMesh) row;

					adLink.meshMap.put(mesh.rowId(), mesh);
				}
				links.add(adLink);
				}
			}catch (Exception e) {

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
				
		return links;

	}


	@Override
	public IRow loadByRowId(String rowId, boolean isLock) throws Exception {
		return null;
	}

	@Override
	public List<IRow> loadRowsByParentId(int id, boolean isLock) throws Exception {
		return null;
	}

}
