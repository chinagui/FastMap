/**
 * 
 */
package com.navinfo.dataservice.dao.glm.selector.rd.tmc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdTmclocation;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdTmclocationLink;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.navicommons.database.sql.DBUtils;
import com.vividsolutions.jts.geom.Geometry;

import oracle.sql.STRUCT;

/** 
* @ClassName: RdTmcLocationSelector 
* @author Zhang Xiaolong
* @date 2016年12月5日 下午7:39:03 
* @Description: TODO
*/
public class RdTmcLocationSelector extends AbstractSelector {

	/**
	 * @param cls
	 * @param conn
	 */
	public RdTmcLocationSelector(Class<?> cls, Connection conn) {
		super(cls, conn);
	}

	public IObj getById(int id, boolean isLock, boolean... noChild) throws Exception {
		RdTmclocation tmclocation = (RdTmclocation) this.loadById(id, isLock, noChild);
		
		List<IRow> tmcLinks = this.loadTmclocationLinkByParentId(tmclocation.getPid(), isLock);

		tmclocation.setLinks(tmcLinks);
		
		for(IRow row : tmcLinks)
		{
			RdTmclocationLink tmclocationLink = (RdTmclocationLink) row;
			tmclocation.linkMap.put(tmclocationLink.rowId(), tmclocationLink);
		}
		
		return tmclocation;
	}
	
	public List<IRow> loadTmclocationLinkByParentId(int groupId, boolean isLock) throws Exception {
		List<IRow> tmcLinks = new ArrayList<>();

		StringBuilder sb = new StringBuilder(
				"select t1.*,t2.geometry,t2.s_node_pid,t2.e_node_pid from rd_tmclocation_link t1 left join rd_link t2 on t1.LINK_PID = t2.link_pid  where t1.GROUP_ID = :1 and t1.U_RECORD !=2 and t2.U_RECORD !=2");

		if (isLock) {

			sb.append(" for update nowait");
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {

			pstmt = getConn().prepareStatement(sb.toString());

			pstmt.setInt(1, groupId);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				RdTmclocationLink tmclocationLink = new RdTmclocationLink();

				ReflectionAttrUtils.executeResultSet(tmclocationLink, resultSet);
				
				Geometry value = GeoTranslator.struct2Jts((STRUCT) resultSet.getObject("geometry"), 100000, 0);
				
				tmclocationLink.setGeometry(value);
				
				int sNodePid = resultSet.getInt("s_node_pid");
				
				tmclocationLink.setsNodePid(sNodePid);
				
				int eNodePid = resultSet.getInt("e_node_pid");
				
				tmclocationLink.seteNodePid(eNodePid);
				
				tmcLinks.add(tmclocationLink);
			}
		} catch (Exception e) {

			throw e;

		} finally {

			DBUtils.closeResultSet(resultSet);

			DBUtils.closeStatement(pstmt);
		}

		return tmcLinks;
	}
	
	public List<Integer> loadTmclocationLinkPidByParentId(int groupId, boolean isLock) throws Exception {
		List<Integer> linkPidList = new ArrayList<>();

		StringBuilder sb = new StringBuilder(
				"select link_pid from rd_tmclocation_link where GROUP_ID = :1 and U_RECORD !=2");

		if (isLock) {

			sb.append(" for update nowait");
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {

			pstmt = getConn().prepareStatement(sb.toString());

			pstmt.setInt(1, groupId);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				int linkPid = resultSet.getInt("link_pid");
				
				linkPidList.add(linkPid);
			}
		} catch (Exception e) {

			throw e;

		} finally {

			DBUtils.closeResultSet(resultSet);

			DBUtils.closeStatement(pstmt);
		}

		return linkPidList;
	}
}
