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
package com.sap.netweaver.porta.ide.eclipse.server.control;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.PublishOperation;
import org.eclipse.wst.server.core.model.PublishTaskDelegate;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;

import com.sap.netweaver.porta.ide.eclipse.util.SapNWServerUtil;

public class SapNWPublishTask extends PublishTaskDelegate {

	public PublishOperation[] getTasks(IServer server, int kind, List modules, List kindList) {
		if (modules == null)
			return null;
		
		SapNWServerBehavior serverControl = (SapNWServerBehavior) server.loadAdapter(SapNWServerBehavior.class, null);

		// find all root modules that needs to be deployed
		Map<IModule, Integer> rootModules = new HashMap<IModule, Integer>();
		int size = modules.size();
		for (int i = 0; i < size; i++) {
			IModule[] module = (IModule[]) modules.get(i);
			Integer deltaKind = (Integer) kindList.get(i);
			
			// check if the module is modified and its root module is deployable
			if (deltaKind != ServerBehaviourDelegate.NO_CHANGE 
					&& SapNWServerUtil.isDeployableModule(module[0])) {
				if (module.length == 1) {
					// if root module then add it to the task list
					rootModules.put(module[0], deltaKind);
				} else {
					// if submodule, then check if its root module is already added to the task list
					// if not, then add it to the task list. 
					if (!rootModules.containsKey(module[0])) {
						rootModules.put(module[0], ServerBehaviourDelegate.CHANGED);
					}
				}
			}
		}
		
		// construct publish operations
		PublishOperation[] operations = new PublishOperation[rootModules.size()];
		int i = 0;
		for (IModule module : rootModules.keySet()) {
			int deltaKind = rootModules.get(module);
			operations[i++] = new SapNWPublishOperation(serverControl, kind, new IModule[] { module }, deltaKind);
		}
		
		return operations;
	}
	
}
