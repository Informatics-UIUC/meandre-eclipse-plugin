/*
 * @(#) MeandreComponentProjectStatus.java @VERSION@
 * 
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 */
package org.meandre.ide.eclipse.component.wizard;



import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Status;

/**
 * 
 * @author Amit Kumar
 * Created on Jun 6, 2008 12:05:39 AM
 *
 */
public class MeandreComponentProjectStatus extends Status implements ResourceCreationListener {
    private List<IResource> createdResources = new ArrayList<IResource>();

    public MeandreComponentProjectStatus(int severity, String pluginId, int code, String message, Throwable exception) {
        super(severity, pluginId, code, message, exception);
    }
    
    public List<IResource> getCreatedResources() {
        return createdResources;
    }
    
    public void resourceCreated(IResource resource) {
        createdResources.add(resource);
    }
}
