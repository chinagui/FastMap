package com.navinfo.dataservice.diff.scanner;

import java.util.List;

import com.navinfo.dataservice.diff.config.Table;
import com.navinfo.dataservice.diff.exception.DiffException;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 12-3-13 上午10:13
 */
public interface DiffScanner
{
	public void scan(Table table,String leftTableFullName,String rightTableFullName)throws DiffException;

}
