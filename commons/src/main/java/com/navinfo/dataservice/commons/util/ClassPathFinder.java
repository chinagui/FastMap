/*
 * $Id: ClassPathFinder.java 894087 2009-12-27 18:00:13Z martinc $
 *
 * Copyright 2003-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navinfo.dataservice.commons.util;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * This class is an utility class that will search through the classpath
 * for files whose names match the given pattern. The filename is tested
 */
public class ClassPathFinder extends PathMatchingResourcePatternResolver {
    private static final Log logger = LogFactory.getLog(ClassPathFinder.class);

    @Override
    protected Resource[] findAllClassPathResources(String location) throws IOException {
        String path = location;
        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        URL[] parentUrls = getURLClassLoader().getURLs();
        Set<Resource> result = new LinkedHashSet<Resource>(16);
        for (int i = 0; i < parentUrls.length; i++) {
            URL url = parentUrls[i];
            if (isJarUrl(url)) {
                url = convertFile2JarURL(url);
            }
            System.out.println(url);
            if (url != null)
                result.add(convertClassLoaderURL(url));
        }
        return result.toArray(new Resource[result.size()]);
    }

    private URL convertFile2JarURL(URL url) {
        String urlStr = "jar:" + url + "!/";
        try {
            return new URL(urlStr);
        } catch (MalformedURLException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

   /* protected Set<Resource> doFindPathMatchingJarResources(Resource rootDirResource, String subPattern)
            throws IOException {

        URLConnection con = rootDirResource.getURL().openConnection();
        JarFile jarFile;
        String jarFileUrl;
        String rootEntryPath;
        boolean newJarFile = false;

        if (con instanceof JarURLConnection) {
            // Should usually be the case for traditional JAR files.
            JarURLConnection jarCon = (JarURLConnection) con;
            jarCon.setUseCaches(false);
            jarFile = jarCon.getJarFile();
            jarFileUrl = jarCon.getJarFileURL().toExternalForm();
            JarEntry jarEntry = jarCon.getJarEntry();
            rootEntryPath = (jarEntry != null ? jarEntry.getName() : "");
        } else {
            // No JarURLConnection -> need to resort to URL file parsing.
            // We'll assume URLs of the format "jar:path!/entry", with the protocol
            // being arbitrary as long as following the entry format.
            // We'll also handle paths with and without leading "file:" prefix.
            String urlFile = rootDirResource.getURL().getFile();
            int separatorIndex = urlFile.indexOf(ResourceUtils.JAR_URL_SEPARATOR);
            if (separatorIndex != -1) {
                jarFileUrl = urlFile.substring(0, separatorIndex);
                rootEntryPath = urlFile.substring(separatorIndex + ResourceUtils.JAR_URL_SEPARATOR.length());
                jarFile = getJarFile(jarFileUrl);
            } else {
                URI entryURI = null;
                try {
                    entryURI = rootDirResource.getURL().toURI();
                } catch (URISyntaxException e) {

                }
                File tempFile = new File(entryURI);
                urlFile = tempFile.getAbsolutePath();
                jarFile = new JarFile(urlFile);
                jarFileUrl = urlFile;
                rootEntryPath = "";
            }
            newJarFile = true;
        }

        try {

            logger.debug("Looking for matching resources in jar file [" + jarFileUrl + "]");
            if (!"".equals(rootEntryPath) && !rootEntryPath.endsWith("/")) {
                // Root entry path must end with slash to allow for proper matching.
                // The Sun JRE does not return a slash here, but BEA JRockit does.
                rootEntryPath = rootEntryPath + "/";
            }
            Set<Resource> result = new LinkedHashSet<Resource>(8);
            for (Enumeration entries = jarFile.entries(); entries.hasMoreElements();) {
                JarEntry entry = (JarEntry) entries.nextElement();
                String entryPath = entry.getName();
                if (entryPath.startsWith(rootEntryPath)) {
                    String relativePath = entryPath.substring(rootEntryPath.length());
                    if (getPathMatcher().match(subPattern, relativePath)) {
                        result.add(rootDirResource.createRelative(relativePath));
                    }
                }
            }
            return result;
        }
        finally {
            // Close jar file, but only if freshly obtained -
            // not from JarURLConnection, which might cache the file reference.
            if (newJarFile) {
                jarFile.close();
            }
        }
    }*/


    protected boolean isJarUrl(URL url) throws IOException {
        return url.toString().toLowerCase().endsWith(".jar");
    }


    protected URLClassLoader getURLClassLoader() {
        URLClassLoader ucl = null;
        ClassLoader loader = Thread.currentThread().getContextClassLoader();

        if (!(loader instanceof URLClassLoader)) {
            loader = ClassPathFinder.class.getClassLoader();
            if (loader instanceof URLClassLoader) {
                ucl = (URLClassLoader) loader;
            }
        } else {
            ucl = (URLClassLoader) loader;
        }

        return ucl;
    }
}
