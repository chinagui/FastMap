package com.navinfo.navicommons.resource;

import java.util.List;


/**
 * 
 * 
 */
public class ResourcePoolExt extends ResourcePool
{


    public ResourcePoolExt ()
    {
    	super();
    }
    
    public String getLabel() {
        return this.getDescp();
    }
    
    private List<ResourcePoolExt> children;

    private Boolean hasChildren;


	public List<ResourcePoolExt> getChildren() {
		return children;
	}

	public void setChildren(List<ResourcePoolExt> children) {
		this.children = children;
	}

	public Boolean getHasChildren() {
		return hasChildren;
	}

	public void setHasChildren(Boolean hasChildren) {
		this.hasChildren = hasChildren;
	}
}
