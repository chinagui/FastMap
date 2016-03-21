package com.navinfo.dataservice.engine.edit.edit.search;

import java.util.List;

import com.navinfo.dataservice.engine.edit.edit.model.IObj;

/**
 * 查询基类
 */
public interface ISearch {

	/**
	 * 通过pid获取数据
	 * @param pid
	 * @return
	 * @throws Exception
	 */
	public IObj searchDataByPid(int pid) throws Exception;

	/**
	 * 通过范围获取数据
	 * @param wkt
	 * @return
	 * @throws Exception
	 */
	public List<SearchSnapshot> searchDataBySpatial(String wkt)
			throws Exception;

	/**
	 * 通过条件获取数据
	 * @param condition
	 * @return
	 * @throws Exception
	 */
	public List<SearchSnapshot> searchDataByCondition(String condition)
			throws Exception;
	
	
	/**
	 * 通过瓦片号+缝隙获取数据
	 * @param x
	 * @param y
	 * @param z
	 * @param gap
	 * @return
	 * @throws Exception
	 */
	public List<SearchSnapshot> searchDataByTileWithGap(int x,int y,int z,int gap)
			throws Exception;

}
