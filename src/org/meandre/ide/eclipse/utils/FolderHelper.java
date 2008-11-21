/** Copyright (c) 2008, Board of Trustees-University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 */
package org.meandre.ide.eclipse.utils;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;


public class FolderHelper {
    public static void mkdirs(IContainer folder) throws CoreException {
        if (folder == null || folder.exists()) {
            return;
        }
        FolderHelper.mkdirs(folder.getParent());
        if (folder.getType() == IResource.FOLDER) {
            ((IFolder) folder).create(false, true, null);
        }
    }
}
