package com.navinfo.dataservice.dao.glm.search;

import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.ISearch;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildface;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;

import java.sql.Connection;
import java.util.List;

/**
 * @Title: CmgBuildfaceSearch
 * @Package: com.navinfo.dataservice.dao.glm.search
 * @Description: ${TODO}
 * @Author: Crayeres
 * @Date: 2017/4/13
 * @Version: V1.0
 */
public class CmgBuildfaceSearch implements ISearch{

    /**
     * 数据库链接
     */
    private Connection conn;

    public CmgBuildfaceSearch(Connection conn) {
        this.conn = conn;
    }

    /**
     * 通过pid获取数据
     *
     * @param pid
     * @return
     * @throws Exception
     */
    @Override
    public IObj searchDataByPid(int pid) throws Exception {
        return (IObj) new AbstractSelector(CmgBuildface.class, conn).loadById(pid, false);
    }

    /**
     * 通过pids获取数据(框选功能)
     *
     * @param pidList@return
     * @throws Exception
     */
    @Override
    public List<? extends IObj> searchDataByPids(List<Integer> pidList) throws Exception {
        return null;
    }

    /**
     * 通过范围获取数据
     *
     * @param wkt
     * @return
     * @throws Exception
     */
    @Override
    public List<SearchSnapshot> searchDataBySpatial(String wkt) throws Exception {
        return null;
    }

    /**
     * 通过条件获取数据
     *
     * @param condition
     * @return
     * @throws Exception
     */
    @Override
    public List<SearchSnapshot> searchDataByCondition(String condition) throws Exception {
        return null;
    }

    /**
     * 通过瓦片号+缝隙获取数据
     *
     * @param x
     * @param y
     * @param z
     * @param gap
     * @return
     * @throws Exception
     */
    @Override
    public List<SearchSnapshot> searchDataByTileWithGap(int x, int y, int z, int gap) throws Exception {
        return null;
    }
}
