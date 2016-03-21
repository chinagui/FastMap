package com.navinfo.navicommons.pid;

import java.util.List;

import com.navinfo.navicommons.net.http.Response;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2010-7-28
 */
@XStreamAlias("result")
public class PidResponse extends Response
{
    @XStreamAlias("pid")
    private List<PidSeg> pids;

    public PidResponse(int code, String desc)
    {
        super(code, desc);
    }

    public static PidResponse getDefaultResponse()
    {
        return new PidResponse(SUCCESS.getCode(), SUCCESS.getDesc());
    }

    public List<PidSeg> getPids() {
        return pids;
    }

    public void setPids(List<PidSeg> pids) {
        this.pids = pids;
    }

    @XStreamAlias("pidSeg")
    public static class PidSeg
    {
        private String pidType;
        private long startNum;
        private long endNum;

        public PidSeg(String pidType, long startNum, long endNum) {
            this.pidType = pidType;
            this.startNum = startNum;
            this.endNum = endNum;
        }

        public String getPidType() {
            return pidType;
        }

        public void setPidType(String pidType) {
            this.pidType = pidType;
        }

        public long getStartNum() {
            return startNum;
        }

        public void setStartNum(long startNum) {
            this.startNum = startNum;
        }

        public long getEndNum() {
            return endNum;
        }

        public void setEndNum(long endNum) {
            this.endNum = endNum;
        }
    }
}
