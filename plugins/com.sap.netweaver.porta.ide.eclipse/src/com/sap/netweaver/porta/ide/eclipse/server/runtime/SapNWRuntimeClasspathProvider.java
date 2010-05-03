/*******************************************************************************
 * Copyright (c) 2009, 2010 SAP AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kaloyan Raev (SAP AG) - initial API and implementation
 *******************************************************************************/
package com.sap.netweaver.porta.ide.eclipse.server.runtime;

import static org.eclipse.wst.common.componentcore.internal.util.IModuleConstants.*;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jst.server.core.RuntimeClasspathProviderDelegate;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.eclipse.wst.server.core.IRuntime;

import com.sap.netweaver.porta.ide.eclipse.SapNWPlugin;

public class SapNWRuntimeClasspathProvider extends RuntimeClasspathProviderDelegate {
	
	public IClasspathEntry[] resolveClasspathContainer(IProject project, IRuntime runtime) {
		try {
			IFacetedProject facetedProject = ProjectFacetsManager.create(project);
			return resolveClasspathContainer(facetedProject, runtime);
		} catch (CoreException e) {
			SapNWPlugin.logError("Error while reading faceted metadata from project " + project.getName(), e);
			return null;
		}
	}
	
	IClasspathEntry[] resolveClasspathContainer(IFacetedProject project, IRuntime runtime) {
		IPath javaInstanceDir = runtime.getLocation();
		if (javaInstanceDir == null)
			return new IClasspathEntry[0];

		SapNWRuntime sapRuntime = (SapNWRuntime) runtime.loadAdapter(SapNWRuntime.class, null);
		String[] jarPaths = getJarPaths(project, sapRuntime);
		if (jarPaths == null) 
			return new IClasspathEntry[0];

		boolean cached = sapRuntime.doesCacheJars();
		IPath cacheLocation = sapRuntime.getCacheLocation();
		
		List<IClasspathEntry> list = new ArrayList<IClasspathEntry>();
		for (String jarPath : jarPaths) {
			IPath fullJarPath;
			if (cached) {
				fullJarPath = cacheLocation.append(new Path(jarPath).lastSegment());
			} else {
				fullJarPath = javaInstanceDir.append(jarPath);
			}
			list.add(JavaCore.newLibraryEntry(fullJarPath, null, null));
		}
		
		return list.toArray(new IClasspathEntry[list.size()]);
	}

	private String[] getJarPaths(IFacetedProject project, SapNWRuntime sapRuntime) {
		if (project.hasProjectFacet(getProjectFacet(JST_WEB_MODULE, "2.5"))
				|| project.hasProjectFacet(getProjectFacet(JST_EJB_MODULE, "3.0"))
				|| project.hasProjectFacet(getProjectFacet(JST_EAR_MODULE, "5.0"))
				|| project.hasProjectFacet(getProjectFacet(JST_APPCLIENT_MODULE, "5.0"))
				|| project.hasProjectFacet(getProjectFacet(JST_UTILITY_MODULE, "1.0"))) {
			return sapRuntime.getJavaEE5Classpath();
		} else if (project.hasProjectFacet(getProjectFacet(JST_WEB_MODULE, "2.4"))
				|| project.hasProjectFacet(getProjectFacet(JST_EJB_MODULE, "2.1"))
				|| project.hasProjectFacet(getProjectFacet(JST_EAR_MODULE, "1.4"))
				|| project.hasProjectFacet(getProjectFacet(JST_APPCLIENT_MODULE, "1.4"))
				|| project.hasProjectFacet(getProjectFacet(JST_CONNECTOR_MODULE, "1.5"))) {
			return sapRuntime.getJ2EE14Classpath();
		}
		
		return null;
	}

	/*
	 * Use the below helper method to get the project facets instead of using
	 * the IJ2EEFacetConstants.
	 * 
	 * This is for compatibility with Europa.
	 */
	private IProjectFacetVersion getProjectFacet(String facetName, String version) {
		return ProjectFacetsManager.getProjectFacet(facetName).getVersion(version);
	}
	
}
