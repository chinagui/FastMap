package com.navinfo.dataservice.dao.fcc.operator;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.dao.fcc.model.TipsIndexDao;
import com.navinfo.navicommons.exception.DaoOperatorException;

/** 
 * @ClassName: TipsIndexOperator
 * @author xiaoxiaowen4127
 * @date 2017年7月20日
 * @Description: TipsOperator.java
 */
public interface TipsIndexOperator {
	public List<TipsIndexDao> searchDataByTileWithGap(String parameter)throws DaoOperatorException;
	public void save(TipsIndexDao ti)throws DaoOperatorException;
	public void save(Collection<TipsIndexDao> tis)throws DaoOperatorException;
}
