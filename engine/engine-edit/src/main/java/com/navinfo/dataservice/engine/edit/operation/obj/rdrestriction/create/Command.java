package com.navinfo.dataservice.engine.edit.operation.obj.rdrestriction.create;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Command extends AbstractCommand {

    private String requester;

    private int inLinkPid;

    private int nodePid;
    
    //退出线的集合
    private List<Integer> outLinkPidList = new ArrayList<>();

    //不需要计算退出线的交限		
    private JSONArray outLinkObjs;
    
    //需要计算退出线的交限
    private JSONArray calOutLinkObjs;

    private String restricInfos;

    /**
     * 0:普通交限;
     * 1:卡车交限;
     */
    private int restricType;

    public int getInLinkPid() {
        return inLinkPid;
    }

    public List<Integer> getOutLinkPidList() {
		return outLinkPidList;
	}

	public void setOutLinkPidList(List<Integer> outLinkPidList) {
		this.outLinkPidList = outLinkPidList;
	}

	public void setInLinkPid(int inLinkPid) {
        this.inLinkPid = inLinkPid;
    }

    public int getNodePid() {
        return nodePid;
    }

    public void setNodePid(int nodePid) {
        this.nodePid = nodePid;
    }

    public JSONArray getOutLinkObjs() {
		return outLinkObjs;
	}

	public void setOutLinkObjs(JSONArray outLinkObjs) {
		this.outLinkObjs = outLinkObjs;
	}
	
	public JSONArray getCalOutLinkObjs() {
		return calOutLinkObjs;
	}

	public void setCalOutLinkObjs(JSONArray calOutLinkObjs) {
		this.calOutLinkObjs = calOutLinkObjs;
	}

	public String getRestricInfos() {
        return restricInfos;
    }

    public int getRestricType() {
        return restricType;
    }

    @Override
    public OperType getOperType() {
        return OperType.CREATE;
    }

    @Override
    public ObjType getObjType() {
        return ObjType.RDRESTRICTION;
    }

    @Override
    public String getRequester() {
        return requester;
    }

    public Command(JSONObject json, String requester) {
        this.requester = requester;

        this.setDbId(json.getInt("dbId"));

        JSONObject data = json.getJSONObject("data");

        this.nodePid = data.getInt("nodePid");

        this.inLinkPid = data.getInt("inLinkPid");
        
        outLinkObjs = new JSONArray();
        
        calOutLinkObjs = new JSONArray();
        
        StringBuffer buf = new StringBuffer();

        if (data.containsKey("infos")) {
        	JSONArray infosArray = data.getJSONArray("infos");
        	
        	for(int i = 0;i<infosArray.size();i++)
        	{
        		JSONObject infoObj = infosArray.getJSONObject(i);
        		
        		String info = infoObj.getString("arrow");
        		
        		buf.append(info);
        		
        		buf.append(",");
        		
        		if(infoObj.containsKey("outLinkPid"))
        		{
        			int outLinkPid = infoObj.getInt("outLinkPid");
        			
        			outLinkPidList.add(outLinkPid);
        			
        			outLinkObjs.add(infoObj);
        		}
        		else
        		{
        			calOutLinkObjs.add(infoObj);
        		}
        	}
        	restricInfos = buf.deleteCharAt(buf.lastIndexOf(",")).toString();
        }
        if (data.containsKey("restricType")) {
            restricType = data.getInt("restricType");
        }
    }

}
