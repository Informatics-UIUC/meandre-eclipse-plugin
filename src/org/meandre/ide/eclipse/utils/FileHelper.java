/** Copyright (c) 2008, Board of Trustees-University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 */

package org.meandre.ide.eclipse.utils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;

/**
 * @author Joni Freeman
 * @author Ville Peurala
 */
public class FileHelper {
    public static boolean deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDirectory(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        return dir.delete();
    }

    public static void copyDirectory(File source, File destination) throws IOException {
        if (source.isDirectory()) {
            if (!destination.exists()) {
                destination.mkdir();
            }

            String[] children = source.list();
            for (int i = 0; i < children.length; i++) {
                copyDirectory(new File(source, children[i]), new File(destination, children[i]));
            }
        } else {
            copyFile(source, destination);
        }
    }

    public static void copyFile(File source, File destination) throws IOException {
        InputStream in = new FileInputStream(source);
        OutputStream out = new FileOutputStream(destination);

        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    public static String getNameWithoutExtension(IFile file) {
        int index = file.getName().lastIndexOf(file.getFileExtension());
        return file.getName().substring(0, index - 1);
    }

    /**
     * Does the same as "basename" command in UNIX - basename("/foo/bar/somefile.x") ->
     * "somefile.x". Does not access the file system, does only string manipulation.
     * 
     * @param filename
     *            a path to a file, like "/foo/bar/somefile.x".
     * @return the base name of the file, like "somefile.x".
     */
    public static String basename(String filename) {
        if (filename == null) {
            throw new IllegalArgumentException(
                    "Parameter 'filename' to method 'basename' cannot be null!");
        }
        if (filename.lastIndexOf('/') == -1) {
            return filename;
        } 
        return filename.substring(filename.lastIndexOf('/') + 1);
    }

    // copy the src file to the destination and replace tokens represented as keys in the hashmap with the
    // the corresponding values
	public static void copyAndFilterFile(File source, File destination,
			HashMap<String, String> hm) throws IOException {
		FileOutputStream out = new FileOutputStream(destination);
		BufferedOutputStream fos = new BufferedOutputStream(out);
		BufferedReader bfr = new BufferedReader(new FileReader(source));

		String eol = System.getProperty("line.separator");
		String line= null;
		while ((line = bfr.readLine() ) != null) {
			line = filterLine(line, hm);
			line = line + eol;
			fos.write(line.getBytes());
        }
		
		bfr.close();
        fos.flush();
        fos.close();
	}

	private static String filterLine(String line, HashMap<String, String> hm) {
		Iterator<String> it = hm.keySet().iterator();
		String key=null;
		String value=null;
		while(it.hasNext()){
			key = it.next();
			value = hm.get(key);
			line=line.replaceAll(key, value);
		}
	
		return line;
	}

}
