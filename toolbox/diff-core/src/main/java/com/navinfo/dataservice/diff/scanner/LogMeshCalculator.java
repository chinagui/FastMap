package com.navinfo.dataservice.diff.scanner;

import com.navinfo.dataservice.datahub.glm.GlmTable;
import com.navinfo.dataservice.diff.exception.DiffException;

/** 
* @ClassName: LogMeshCalculator 
* @author Xiao Xiaowen 
* @date 2016年4月13日 下午2:31:28 
* @Description: TODO
*/
public interface LogMeshCalculator {
	void calc(GlmTable table, String leftTableFullName, String rightTableFullName) throws DiffException;
}
