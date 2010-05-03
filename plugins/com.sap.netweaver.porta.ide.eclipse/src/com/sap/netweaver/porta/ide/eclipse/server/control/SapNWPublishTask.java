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

import java.util.ArrayList;
import java.util.List;

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
		
		List<PublishOperation> tasks = new ArrayList<PublishOperation>();
		int size = modules.size();
		for (int i = 0; i < size; i++) {
			IModule[] module = (IModule[]) modules.get(i);
			Integer deltaKind = (Integer) kindList.get(i);
			if (module.length == 1 
					&& deltaKind != ServerBehaviourDelegate.NO_CHANGE 
					&& SapNWServerUtil.isDeployableModule(module[0])) {
				tasks.add(new SapNWPublishOperation(serverControl, kind, module, deltaKind));
			}
		}
		
		return (PublishOperation[]) tasks.toArray(new PublishOperation[tasks.size()]);
	}

}
