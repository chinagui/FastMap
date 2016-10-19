package com.navinfo.dataservice.diff.job;


import java.util.List;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.jobframework.exception.JobCreateException;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.exception.JobRuntimeException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 12-3-7 下午5:55
 */
public class DiffJobRequest extends AbstractJobRequest
{
	//左边为变化库，右边为基准库
    private int leftDbId;//左边数据库
    private int rightDbId;//右边数据库
	//两者只能一个有值，当两者都有值时只取specificTables
	private List<String> specificTables;
	private List<String> excludedTables;
    //线程数
    private int threadCount = 10;
    private String level = LEVEL_COLUMN; //差分粒度：表级，
    public static final String LEVEL_TABLE = "table";
    public static final String LEVEL_COLUMN = "column";

	@Override
	public void defineSubJobRequests() throws JobCreateException {
		
	}

	@Override
	protected int myStepCount() throws JobException {
		return 3;
	}

	@Override
	public String getJobType() {
		return "diff";
	}
	@Override
	public String getJobTypeName(){
		return "GDB差分";
	}

	@Override
	public void validate() throws JobException {
		
	}

	public int getLeftDbId() {
		return leftDbId;
	}

	public void setLeftDbId(int leftDbId) {
		this.leftDbId = leftDbId;
	}

	public int getRightDbId() {
		return rightDbId;
	}

	public void setRightDbId(int rightDbId) {
		this.rightDbId = rightDbId;
	}

	public List<String> getSpecificTables() {
		return specificTables;
	}

	public void setSpecificTables(List<String> specificTables) {
		this.specificTables = specificTables;
	}

	public List<String> getExcludedTables() {
		return excludedTables;
	}

	public void setExcludedTables(List<String> excludedTables) {
		this.excludedTables = excludedTables;
	}

	public int getThreadCount() {
		return threadCount;
	}

	public void setThreadCount(int threadCount) {
		this.threadCount = threadCount;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}
}
