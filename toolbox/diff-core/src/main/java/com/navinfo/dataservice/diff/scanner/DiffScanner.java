package com.navinfo.dataservice.diff.scanner;

import com.navinfo.dataservice.bizcommons.glm.GlmTable;
import com.navinfo.dataservice.diff.dataaccess.DataAccess;
import com.navinfo.dataservice.diff.exception.DiffException;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 12-3-13 上午10:13
 */
public interface DiffScanner
{
	public int scan(GlmTable table,DataAccess leftAccess,DataAccess rightAccess)throws DiffException;

}
