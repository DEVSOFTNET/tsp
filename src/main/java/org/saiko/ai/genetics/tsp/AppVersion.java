/*
 * Copyright (c) 2013 dusan.saiko@gmail.com
 */
package org.saiko.ai.genetics.tsp;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

/**
 * Get app version information from manifest
 */
public class AppVersion {

    /**
     * @param c
     * @return
     * @throws MalformedURLException
     * @throws IOException
     */
    private static JarInputStream findJar(Class<? extends Object> c) throws MalformedURLException, IOException {
	String className = "/" + c.getName().replaceAll("\\.", "/") + ".class";

	String url = c.getResource(className).toString();

	if (url.startsWith("jar:")) {
	    String jarName = url.substring(4, url.indexOf('!'));
	    return new JarInputStream(new URL(jarName).openStream());
	} else {
	    return null;
	}
    }

    /**
     * Get current app version from manifest file
     */
    public static String getAppVersion(Class<? extends Object> c) {
	try (JarInputStream jar = findJar(c)) {
	    if (jar == null)
		return null;

	    Manifest manifest = jar.getManifest();
	    Attributes attributes = manifest.getMainAttributes();

	    String implementationVersion = attributes.getValue("Implementation-Version");
	    String buildDate = attributes.getValue("Implementation-Date");
	    
	    if (implementationVersion != null && buildDate != null) {
		return String.format("%s (%s)", implementationVersion, buildDate);
	    }

	    return c.getPackage().getImplementationVersion();
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return null;
    }
}