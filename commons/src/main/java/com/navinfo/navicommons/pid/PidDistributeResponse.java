package com.navinfo.navicommons.pid;

import java.util.List;

import com.navinfo.navicommons.net.http.Response;
import com.navinfo.navicommons.pid.PidResponse.PidSeg;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2010-7-28
 */
@XStreamAlias("result")
public class PidDistributeResponse extends Response
{
    private String distributeId;
    private String remark;
    private int limit;
    private String createTime;
    private String clientId;
    @XStreamAlias("pid")
    private List<PidSeg> pids;

    public PidDistributeResponse(int code, String desc)
    {
        super(code, desc);
    }

    public static PidDistributeResponse getDefaultResponse()
    {
        return new PidDistributeResponse(SUCCESS.getCode(), SUCCESS.getDesc());
    }

    public List<PidSeg> getPids() {
        return pids;
    }

    public void setPids(List<PidSeg> pids) {
        this.pids = pids;
    }


	public String getDistributeId() {
		return distributeId;
	}

	public void setDistributeId(String distributeId) {
		this.distributeId = distributeId;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}


	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}
}
