package com.navinfo.dataservice.dao.glm.iface;

import java.util.List;

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
	 * 通过pids获取数据(框选功能)
	 * @param pids
	 * @return
	 * @throws Exception
	 */
	public IObj searchDataByPids(List<Integer> pidList) throws Exception;

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
