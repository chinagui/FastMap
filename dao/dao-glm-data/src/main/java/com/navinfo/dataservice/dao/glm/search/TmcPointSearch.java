/**
 * 
 */
package com.navinfo.dataservice.dao.glm.search;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.ISearch;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;

import net.sf.json.JSONArray;

/**
 * @ClassName: TmcPointSearch
 * @author Zhang Xiaolong
 * @date 2016年11月11日 下午6:06:29
 * @Description: TODO
 */
public class TmcPointSearch implements ISearch {
	
	private Connection conn;
	
	public TmcPointSearch(Connection conn) {
        this.conn = conn;
    }
	
	@Override
	public IObj searchDataByPid(int pid) throws Exception {
		return null;
	}

	@Override
	public IObj searchDataByPids(List<Integer> pidList) throws Exception {
		return null;
	}

	@Override
	public List<SearchSnapshot> searchDataBySpatial(String wkt) throws Exception {
		return null;
	}

	@Override
	public List<SearchSnapshot> searchDataByCondition(String condition) throws Exception {
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<SearchSnapshot> searchDataByTileWithGap(int x, int y, int z, int gap) throws Exception {

		List<SearchSnapshot> snapshotList = new ArrayList<>();
		
		MetadataApi metaApi = (MetadataApi) ApplicationContextUtil.getBean("metaApi");

		JSONArray array = metaApi.queryTmcPoint(x, y, z, gap);

		List<SearchSnapshot> list = JSONArray.toList(array, SearchSnapshot.class, JsonUtils.getJsonConfig());
		
		if(CollectionUtils.isNotEmpty(list))
		{
			snapshotList = list;
		}

		return snapshotList;
	}

}
