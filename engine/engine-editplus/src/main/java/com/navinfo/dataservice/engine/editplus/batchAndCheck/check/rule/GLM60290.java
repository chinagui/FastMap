package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;

import com.navinfo.dataservice.dao.plus.obj.BasicObj;

/**
 * @ClassName GLM60290
 * @author Han Shaoming
 * @date 2017年2月21日 下午3:56:02
 * @Description TODO
 * 检查条件：  非删除POI对象
 * 检查原则：
 * 对于同一关系中多分类同属性(多义性)(IX_SAMEPOI.Relation_type=1)，且是同一组数据，
 * 如果分类(只有有一个分类)在（SC_POINT_KIND_NEW表的TYPE=5）记录中的POIKIND或R_KIND列表中不存在，这组数据中不包含180400分类的记录，
 * 报出Log：XXXX与XXXX分类之间不可制作同一关系
 */
public class GLM60290 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
