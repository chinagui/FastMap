package com.navinfo.dataservice.expcore.source;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: liuqing
 * Date: 11-10-27
 * Time: 下午2:49
 * 此类用于记录按图幅扩圈时，哪些图幅是
 * 0 毛边图幅
 * 1 核心图幅
 * 2 接边图幅
 */
public class MMeshType {
    private Integer meshId;
    /*0 毛边图幅
    1 核心图幅
    2 接边图幅*/
    private Integer type;
    private String memo;

    public MMeshType(Integer meshId, Integer type, String memo) {
        this.meshId = meshId;
        this.type = type;
        this.memo = memo;
    }

    public static MMeshType newCore(Integer meshId) {
        return new MMeshType(meshId, 1, "核心图幅");
    }

    public static MMeshType newBorder(Integer meshId) {
        return new MMeshType(meshId, 2, "接边图幅");
    }

    public Integer getMeshId() {
        return meshId;
    }

    public void setMeshId(Integer meshId) {
        this.meshId = meshId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    
    
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((meshId == null) ? 0 : meshId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MMeshType other = (MMeshType) obj;
		if (meshId == null) {
			if (other.meshId != null)
				return false;
		} else if (!meshId.equals(other.meshId))
			return false;
		return true;
	}

	public static void main(String[] args) {
		Set s=new HashSet();
		s.add(new MMeshType(1,1,"1"));
		s.add(new MMeshType(1,1,"1"));
		s.add(new MMeshType(1,1,"1"));
		
		System.out.println(s.size());
	}

	@Override
	public String toString() {
		return "MMeshType [meshId=" + meshId + ", type=" + type + ", memo=" + memo + "]";
	}
	
	
	
    
    
}
