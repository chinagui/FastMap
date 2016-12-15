/**
 * 
 */
package com.navinfo.dataservice.dao.glm.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ISearch;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdTmclocation;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.tmc.RdTmcLocationSelector;

import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

/**
 * @ClassName: TmcPointSearch
 * @author Zhang Xiaolong
 * @date 2016年11月11日 下午6:06:29
 * @Description: TODO
 */
public class RdTmcLocationSearch implements ISearch {
	
	private Connection conn;
	
	public RdTmcLocationSearch(Connection conn) {
        this.conn = conn;
    }
	
	@Override
	public IObj searchDataByPid(int pid) throws Exception {
		RdTmcLocationSelector selector = new RdTmcLocationSelector(RdTmclocation.class,conn);
		
		IObj obj = selector.getById(pid, false, true);
		return obj;
	}

	@Override
	public List<? extends IObj> searchDataByPids(List<Integer> pidList) throws Exception {
		
		List<IRow> objList = new AbstractSelector(RdTmclocation.class,conn).loadByIds(pidList, false,false);
		
		List<IObj> tmcObjList = new ArrayList<>();
		
		RdTmcLocationSelector selector = new RdTmcLocationSelector(RdTmclocation.class,conn);
		
		for(IRow row : objList)
		{
			RdTmclocation location = (RdTmclocation) row;
			List<IRow> tmcLinks = selector.loadTmclocationLinkByParentId(location.getPid(), false);
			location.setLinks(tmcLinks);
			tmcObjList.add(location);
		}
		return tmcObjList;
	}

	@Override
	public List<SearchSnapshot> searchDataBySpatial(String wkt) throws Exception {
		return null;
	}

	@Override
	public List<SearchSnapshot> searchDataByCondition(String condition) throws Exception {
		return null;
	}

	@Override
	public List<SearchSnapshot> searchDataByTileWithGap(int x, int y, int z, int gap) throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		String sql = "WITH TMP1 AS (SELECT LINK_PID,GEOMETRY FROM RD_LINK WHERE SDO_RELATE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'mask=anyinteract') = 'TRUE' AND U_RECORD != 2) select /*leading(tmp1)*/ t1.GROUP_ID,tmp1.link_pid,tmp1.geometry,t1.tmc_id,t2.loc_direct,t2.DIRECT from RD_TMCLOCATION t1,RD_TMCLOCATION_LINK t2,tmp1 where tmp1.link_pid = t2.link_pid and t1.group_id = t2.group_id and t1.u_record !=2 and t2.u_record !=2";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;
		
		try {
			pstmt = conn.prepareStatement(sql);
			
			//gap固定给100，防止文字被其他瓦片覆盖
			String wkt = MercatorProjection.getWktWithGap(x, y, z, 100);
			
			double px = MercatorProjection.tileXToPixelX(x);

			double py = MercatorProjection.tileYToPixelY(y);

			pstmt.setString(1, wkt);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				SearchSnapshot snapshot = new SearchSnapshot();
				
				JSONObject jsonM = new JSONObject();

				snapshot.setI(resultSet.getInt("GROUP_ID"));
				
				snapshot.setT(49);

				jsonM.put("a",resultSet.getString("link_pid"));
				
				String tmc = "";
				
				String locCode = "";
				
				String tmcId = resultSet.getString("tmc_id");
				
				if(tmcId.length() == 8)
				{
					tmc = tmcId.substring(0, 2);
					
					locCode = String.valueOf(Integer.parseInt(tmcId.substring(2)));
				}
				else if(tmcId.length() == 9)
				{
					tmc = tmcId.substring(0, 3);
					
					locCode = String.valueOf(Integer.parseInt(tmcId.substring(3)));
				}
				
				jsonM.put("b",tmc);
				
				jsonM.put("c",resultSet.getString("loc_direct"));
				
				jsonM.put("d",locCode);
				
				jsonM.put("e",resultSet.getString("direct"));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");
				
				JSONObject geojson = Geojson.spatial2Geojson(struct);
				
				JSONObject jo = Geojson.link2Pixel(geojson, px, py, z);

				snapshot.setG(jo.getJSONArray("coordinates"));

				snapshot.setM(jsonM);

				list.add(snapshot);
			}
		} catch (Exception e) {
			
			throw new SQLException(e);
		} finally {
			if (resultSet != null) {
				try {
					resultSet.close();
				} catch (Exception e) {
					
				}
			}

			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (Exception e) {
					
				}
			}
			
		}

		return list;
	}

}
