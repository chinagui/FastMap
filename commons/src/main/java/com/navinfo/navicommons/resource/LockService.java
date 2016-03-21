package com.navinfo.navicommons.resource;


import java.util.List;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2011-1-10
 */
public class LockService {
    public static ResourceLock takeResourceLock(ResourceCategory category, String version) throws ResourceLockException {
        return ResourceManager.getInstance().takeResourceLock(category, version);
    }

    public static ResourceLock takeResourceLock(String parentId, ResourceCategory category, String version) throws ResourceLockException {
        return ResourceManager.getInstance().takeResourceLock(parentId, category, version);
    }


    public static ResourceLock takeResourceLock(ResourceCategory category) throws ResourceLockException {
        return ResourceManager.getInstance().takeResourceLock(category);
    }

    public static ResourceLock takeResourceLock(String parentId, ResourceCategory category) throws ResourceLockException {
        return ResourceManager.getInstance().takeResourceLock(parentId, category);
    }

    public static ResourceLock takeResourceLock(String parentId, ResourceCategory category, String clientId, String version) throws ResourceLockException {
        return ResourceManager.getInstance().takeResourceLock(parentId, category, clientId, version);
    }

    public static ResourceLock takeResourceLock(ResourceCategory category, String clientId, String version) throws ResourceLockException {
        return ResourceManager.getInstance().takeResourceLock(category, clientId, version);
    }

    public static void releaseResourceLock(ResourceLock resourceLock) throws ResourceLockException {
        ResourceManager.getInstance().releaseResourceLock(resourceLock);
    }

    public static void releaseResourceLock(String resourceId) throws ResourceLockException {
        ResourceManager.getInstance().releaseResourceLock(resourceId);
    }

    public static void updateResourceLock(String resourceId, ResourceStatus status) throws ResourceLockException {
        ResourceManager.getInstance().updateResourceLock(resourceId, status);
    }

    public static void updateResourceLock(ResourceLock resourceLock, ResourceStatus status) throws ResourceLockException {
        ResourceManager.getInstance().updateResourceLock(resourceLock, status);
    }

    public static ResourceLock peekResourceAsResourceLock(String resourceId) {
        return ResourceManager.getInstance().getResourceAsResourceLock(resourceId);
    }

    public static List<ResourcePool> getFreeResourcePool() {
        return ResourceManager.getInstance().getFreeResourcePool();
    }

    public static void updateResourceVersion(String resourceId, String version) {
        ResourceManager.getInstance().updateResourceVersion(resourceId, version);
    }

    public static ResourceLock peekResourceLock(ResourceCategory category, String version) throws ResourceLockException {
        return ResourceManagerExt.getInstance().takeResourceLock(category, version);
    }

	public static ResourceLock takeResourceLock(String resourceId) {
		return ResourceManagerExt.getInstance().takeResourceLock(resourceId);
	}
}
