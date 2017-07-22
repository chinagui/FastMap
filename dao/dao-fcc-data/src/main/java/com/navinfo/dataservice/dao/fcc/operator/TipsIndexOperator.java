package com.navinfo.dataservice.dao.fcc.operator;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.dao.fcc.model.TipsDao;
import com.navinfo.navicommons.exception.DaoOperatorException;

/**
 * @ClassName: TipsIndexOperator
 * @author xiaoxiaowen4127
 * @date 2017年7月20日
 * @Description: TipsOperator.java
 */
public interface TipsIndexOperator {
	public List<TipsDao> searchDataByTileWithGap(String parameter)
			throws DaoOperatorException;

	public void save(TipsDao ti) throws DaoOperatorException;

	public void save(Collection<TipsDao> tis) throws DaoOperatorException;

	public void delete(Collection<TipsDao> tis) throws Exception;

	public void update(Collection<TipsDao> tis) throws Exception;

    public void delete(String rowkey) throws Exception;
}
