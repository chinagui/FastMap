package com.navinfo.navicommons.pid;

import java.util.List;

import com.navinfo.navicommons.net.http.Response;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * 
 * @author xiaoxiaowen4127
 *
 */
@XStreamAlias("result")
public class RticidResponse extends Response
{
    @XStreamAlias("rticid")
    private String rticids;

    public RticidResponse(int code, String desc)
    {
        super(code, desc);
    }

    public static RticidResponse getDefaultResponse()
    {
        return new RticidResponse(SUCCESS.getCode(), SUCCESS.getDesc());
    }

    public String getRticids() {
        return rticids;
    }

    public void setRticids(String rticids) {
        this.rticids = rticids;
    }
}
