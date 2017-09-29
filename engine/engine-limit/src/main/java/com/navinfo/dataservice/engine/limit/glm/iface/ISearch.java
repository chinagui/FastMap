package com.navinfo.dataservice.engine.limit.glm.iface;

import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import net.sf.json.JSONObject;

import java.util.List;

/**
 * 查询基类
 */
public interface ISearch {


    /**
     * 通过范围获取数据
     */
    public List<SearchSnapshot> searchDataBySpatial(String wkt)
            throws Exception;

    /**
     * 通过条件获取数据
     */
    public int searchDataByCondition(JSONObject condition, List<IRow> objList)
            throws Exception;
    /**
     * 通过瓦片号+缝隙获取数据
     */
    public List<SearchSnapshot> searchDataByTileWithGap(int x, int y, int z, int gap)
            throws Exception;
    
	
	/**
	 * 通过输入的条件id获取该表最大的主键值
	 * @param groupId
	 * @return
	 * @throws Exception
	 */
	public String loadMaxKeyId(String groupId) throws Exception;

}
