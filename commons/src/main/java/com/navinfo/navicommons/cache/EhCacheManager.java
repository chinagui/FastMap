package com.navinfo.navicommons.cache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

/**
 * 缓存管理类，报表显示的数据查询后先保存到缓存中，缓存的具体配置参考/ehcache.xml
 *
 * @author liuqing
 */
public class EhCacheManager {

    private CacheManager cacheManager;
    private static EhCacheManager provider;
    public static String DETECT_DUPLICATE_HTTPREQUEST_CACHE = "detectDuplicateHttpRequestCache";
    public static String AU_VERSION_CACHE = "auVersionCache";

    private EhCacheManager() {
        cacheManager = CacheManager.create(EhCacheManager.class.getResourceAsStream("/ehcache.xml"));
    }

    public static synchronized EhCacheManager getInstance() {
        if (provider == null) {
            provider = new EhCacheManager();
        }
        return provider;
    }

    /**
     * 放入对象到缓存
     *
     * @param cacheName 缓存名称，见配置文件ehcache.xml
     * @param key
     * @param value
     */
    public void putElement(String cacheName, String key, Object value) {
        Cache cache = cacheManager.getCache(cacheName);
        Element element = new Element(key, value);
        cache.put(element);
    }

    /**
     * 从缓存中取对象，缓存名称，见配置文件ehcache.xml
     *
     * @param cacheName
     * @param key
     * @return
     */
    public Object getElement(String cacheName, String key) {
        Cache cache = cacheManager.getCache(cacheName);
        Element element = cache.get(key);
        if (element != null) {
            return element.getObjectValue();
        }
        return null;
    }

}
