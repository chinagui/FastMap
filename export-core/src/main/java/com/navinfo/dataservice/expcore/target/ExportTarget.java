package com.navinfo.dataservice.expcore.target;

import com.navinfo.dataservice.expcore.exception.ExportException;

/**
 * User: Xiao Xiaowen
 * 数据导出的目标(库)
 */
public interface ExportTarget {

	/**
	 * 导出失败时，销毁目标实现此方法
	 */
	public void init(String gdbVersion)throws ExportException;
    public void release(boolean destroyTarget);
    public boolean isNewTarget();
    
}
