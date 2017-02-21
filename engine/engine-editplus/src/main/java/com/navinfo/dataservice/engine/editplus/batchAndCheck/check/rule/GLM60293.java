package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;

import com.navinfo.dataservice.dao.plus.obj.BasicObj;

/**
 * @ClassName GLM60293
 * @author Han Shaoming
 * @date 2017年2月21日 下午8:58:28
 * @Description TODO
 * 检查条件： 非删除POI对象；
 * 检查原则：
 * 检查数据满足以下两种情况之一：
 * 1）官方原始中文名称和地址(fullname)完成相同；
 * 2）分类分别为加油站（230215）和加气站（230216），名称不同，地址相同；
 * 满足1或2且显示坐标距离在5米之内且两方分类、CHAIN值在SC_POINT_KIND_NEW中TYPE=5的
 * POIKIND、POIKIND_CHAIN和R_KIND、R_KIND_CHAIN列表中，为同一组别，
 * 但未制作同一关系的数据，报出Log：普通POI未制作多分类同属性同一关系！
 */
public class GLM60293 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
