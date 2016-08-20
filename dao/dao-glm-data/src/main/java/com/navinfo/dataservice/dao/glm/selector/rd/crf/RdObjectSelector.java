/**
 * 
 */
package com.navinfo.dataservice.dao.glm.selector.rd.crf;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.model.rd.crf.RdObject;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.navicommons.database.sql.DBUtils;

/** 
* @ClassName: RdObjectSelector 
* @author Zhang Xiaolong
* @date 2016年8月20日 下午5:36:33 
* @Description: TODO
*/
public class RdObjectSelector extends AbstractSelector {
	
	public RdObjectSelector(Connection conn)
	{
		super(RdObject.class, conn);
	}
	
	/**
	 * 通过crf道路pid查询可以选择的道路名
	 * 如果选择的CRFO中，存在名称类型为立交桥名（连接路）并且属性为“匝道”或者名称类型为立交桥名（主路）的link，
	 * 则此字段将CRFO中名称类型为立交桥名（连接路）并且属性为“匝道”或者名称类型为“立交桥名（主路）”的link上的道路名称显示出来，
	 * 供用户下拉选择，并且可以手工编辑
	 * @param pid
	 * @param isLock
	 * @return
	 * @throws Exception
	 */
	public List<String> getRdObjectName(int pid,boolean isLock) throws Exception
	{
		List<String> nameList = new ArrayList<>();
		
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			StringBuilder sb = new StringBuilder(
					"select b.name from( select c.NAME_GROUPID from rd_object_link a,rd_link_form b,rd_link_name c where a.pid = :1 and a.LINK_PID = b.LINK_PID and b.LINK_PID=c.LINK_PID and ((b.FORM_OF_WAY = 15 and c.NAME_TYPE = 1) or c.NAME_TYPE = 2) and a.U_RECORD !=2 group by c.NAME_GROUPID union all select /*+ leading(A,B) use_hash(A,B)*/ d.NAME_GROUPID from rd_object_inter a,rd_inter_link b,rd_link_form c,rd_link_name d where a.pid = 7514 and a.INTER_PID = b.PID and b.LINK_PID=c.LINK_PID and c.LINK_PID = d.LINK_PID and ((c.FORM_OF_WAY = 15 and d.NAME_TYPE = 1) or d.NAME_TYPE = 2) and a.U_RECORD !=2 group by d.NAME_GROUPID union all select /*+ leading(A,B) use_hash(A,B)*/ d.NAME_GROUPID from rd_object_road a,rd_road_link b,rd_link_form c,rd_link_name d where a.pid = 7514 and a.road_PID = b.PID and b.LINK_PID=c.LINK_PID and c.LINK_PID = d.LINK_PID and ((c.FORM_OF_WAY = 15 and d.NAME_TYPE = 1) or d.NAME_TYPE = 2) and a.U_RECORD !=2 group by d.NAME_GROUPID)tmp,rd_name b where tmp.name_groupid=b.name_groupid(+) and b.lang_code(+)='CHI' group by tmp.NAME_GROUPID,b.name");
			if (isLock) {
				sb.append(" for update nowait");
			}
			pstmt = getConn().prepareStatement(sb.toString());
			
			pstmt.setInt(1, pid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				nameList.add(resultSet.getString("name"));
			}
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
		return nameList;
	}
}	
