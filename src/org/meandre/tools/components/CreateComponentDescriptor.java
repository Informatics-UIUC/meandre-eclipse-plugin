/**
 * @(#) CreateComponentRDFDescriptor.java @VERSION@
 *
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 */
package org.meandre.tools.components;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;

import org.meandre.annotations.Component.Runnable;
import org.meandre.core.repository.CorruptedDescriptionException;
import org.meandre.core.repository.DataPortDescription;
import org.meandre.core.repository.ExecutableComponentDescription;
import org.meandre.core.repository.PropertiesDescriptionDefinition;
import org.meandre.core.repository.TagsDescription;



import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.RDFNode;

/**This class will creates a component descriptor for the component
 *  based on the annotations.
 *
 * @author Amit Kumar
 * Created on Nov 23, 2007 8:46:03 PM
 *
 */
public class CreateComponentDescriptor {

    final static String encoding = "UTF-8";

    private String componentDescriptorFolder;
    private Component componentAnnotation = null;
    private ArrayList<ComponentOutput> outputPortAnnotations = new ArrayList<
            ComponentOutput>(5);
    private ArrayList<ComponentInput> inputPortAnnotations = new ArrayList<
            ComponentInput>(5);
    private ArrayList<ComponentProperty> propertyAnnotations = new ArrayList<
            ComponentProperty>(5);
    private Class<? > klazz = null;


    public CreateComponentDescriptor(String componentDescriptorFolder) {
        this.componentDescriptorFolder = componentDescriptorFolder;
    }


    public void init(String className) throws ClassNotFoundException {
        System.out.println("Mining annotations for: " + className);
        klazz = Class.forName(className);
        outputPortAnnotations.clear();
        inputPortAnnotations.clear();
        propertyAnnotations.clear();
    }

    public void init(Class clazz) throws ClassNotFoundException {
        System.out.println("Mining annotations for: " + clazz.getSimpleName());
        klazz = clazz;
        outputPortAnnotations.clear();
        inputPortAnnotations.clear();
        propertyAnnotations.clear();
    }
    

    public static void main(String args[]) {
        if (args.length != 2) {
            System.out.println(
                    "Usage: java CreateComponentDescriptor org.foo.Component descriptor_directory");
            System.exit(0);
        }
        String className = args[0];
        String componentDescriptorFolder = args[1];
        if (!(new File(componentDescriptorFolder)).exists()) {
            System.out.println("Cannot continue... " +
                               (new File(componentDescriptorFolder)).
                               getAbsolutePath() + " does not exist.");
            System.exit(0);
        }
        CreateComponentDescriptor ccd = new CreateComponentDescriptor(
                componentDescriptorFolder);
        try {
            ccd.init(className);
            ccd.process();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (CorruptedDescriptionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    /*
      public boolean process(String className, String componentDescriptorFolder) throws CorruptedDescriptionException{
     //"org.monkproject.meandre.components.io.StringWriterToFile";
     // = "descriptors/components";

     if(args.length != 2) {
      System.out.println("Usage: java CreateComponentDescriptor org.foo.Component descriptor_directory");
      System.exit(0);
     }

     String className= args[0];
     String componentDescriptorFolder = args[1];
     if(!(new File(componentDescriptorFolder)).exists()){
      System.out.println("Cannot continue... " + (new File(componentDescriptorFolder)).getAbsolutePath() + " does not exist.");
      System.exit(0);
     }
     CreateComponentDescriptor ccd = new CreateComponentDescriptor(componentDescriptorFolder);
     try {
      ccd.init(className);
     } catch (ClassNotFoundException e) {
      System.out.println("Could not process: " + className + " class could not be found.");
     }
     ccd.process();
      }
     */




    public String process() throws CorruptedDescriptionException {
        componentAnnotation = klazz.getAnnotation(Component.class);
        if (componentAnnotation != null) {
            System.out.println("Processing: " + klazz.getName());
            //System.out.println("componentAnnotation... " + componentAnnotation.baseURL());
        } else {
           // System.out.println(
           //         "ComponentAnnotation is null: Cannot continue...");
            return null;
        }

        System.out.println("Name: " + ((Component) componentAnnotation).name());
        System.out.println("Location: " + klazz.getName());
        System.out.println("Creator: " +
                           ((Component) componentAnnotation).creator());

        Field fields[] = klazz.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Annotation[] annField = fields[i].getAnnotations();
            int annotationLocation = -1;
            if ((annotationLocation = contains(annField, "ComponentOutput")) !=
                                      -1) {
                outputPortAnnotations.add((ComponentOutput) annField[
                                          annotationLocation]);
                System.out.println("Found output: " +
                                   ((ComponentOutput) annField[
                                    annotationLocation]).name());
            } else if ((annotationLocation = contains(annField,
                    "ComponentInput")) != -1) {
                inputPortAnnotations.add((ComponentInput) annField[
                                         annotationLocation]);
                System.out.println("Found input: " +
                                   ((ComponentInput) annField[annotationLocation]).
                                   name());
            } else if ((annotationLocation = contains(annField,
                    "ComponentProperty")) != -1) {
                propertyAnnotations.add((ComponentProperty) annField[
                                        annotationLocation]);
                System.out.println("Found property: " +
                                   ((ComponentProperty) annField[
                                    annotationLocation]).name());
            }
        }

        if (componentAnnotation == null) {
            System.out.println("Error Component Annotation is missing.");
            System.exit(1);
        }

        String sName = componentAnnotation.name();
        String sBaseURL = componentAnnotation.baseURL();
        String sDescription = componentAnnotation.description();
        String sRights = getRights(componentAnnotation.rights());
        String sRightsOther = componentAnnotation.rightsOther();
        String sCreator = componentAnnotation.creator();
        Date dateCreation = new Date();
        String sTags = componentAnnotation.tags();
        String sFormat = componentAnnotation.format();
        String type= getType(componentAnnotation.mode());
       
        Runnable runnable = componentAnnotation.runnable();
        String sRunnable = getRunnable(runnable);
        String sFiringPolicy = getFiringPolicy(componentAnnotation.firingPolicy());

        String sLocation = klazz.getCanonicalName();
        if (sBaseURL.charAt(sBaseURL.length() - 1) != '/') {
            sBaseURL += '/';
        }
        sRights = (sRights.equals("Other")) ? sRightsOther : sRights;
        
        

        String sComponentName = sName.toLowerCase().replaceAll("[ ]+", " ").
                                replaceAll(" ", "-");
        String sClassName = "";
        //String [] saTmp = sName.toLowerCase().replaceAll("[ ]+", " ").split(" ");
        String[] saTmp = sName.replaceAll("[ ]+", " ").split(" ");
        for (String s : saTmp) {
            char chars[] = s.trim().toCharArray();
            chars[0] = Character.toUpperCase(chars[0]);
            sClassName += new String(chars);
        }

        Model model = ModelFactory.createDefaultModel();
        Resource resExecutableComponent = model.createResource(sBaseURL +
                sComponentName);
        Resource resLocation = model.createResource(sBaseURL + sComponentName +
                "/implementation/" + sLocation);

        sTags = (sTags == null) ? "" : sTags;
        if (sTags.indexOf(',') < 0) {
            saTmp = sTags.toLowerCase().replaceAll("[ ]+", " ").split(" ");
        } else {
            saTmp = sTags.toLowerCase().replaceAll("[ ]+", " ").split(",");
        }
        Set<String> setTmp = new HashSet<String>();
        for (String s : saTmp) {
            setTmp.add(s.trim());
        }
        TagsDescription tagDesc = new TagsDescription(setTmp);

        saTmp = new String[] {sBaseURL + sComponentName + "/implementation/"};
        Set<RDFNode> setContext = new HashSet<RDFNode>();
        for (String s : saTmp) {
            setContext.add(model.createResource(s.trim()));
        }

        Set<DataPortDescription> setInputs = new HashSet<DataPortDescription>();
        if (inputPortAnnotations != null) {
            for (int i = 0, iMax = inputPortAnnotations.size(); i < iMax; i++) {
                String name = inputPortAnnotations.get(i).name();
                String description = inputPortAnnotations.get(i).description();

                String sID = name.toLowerCase().replaceAll("[ ]+", " ").
                             replaceAll(" ", "-");
                setInputs.add(new DataPortDescription(model.createResource(
                        sBaseURL + sComponentName + "/input/" + sID),
                        sBaseURL + sComponentName + "/input/" + sID, name,
                        description));
            }
        }

        Set<DataPortDescription> setOutputs = new HashSet<DataPortDescription>();
        if (outputPortAnnotations != null) {
            for (int i = 0, iMax = outputPortAnnotations.size(); i < iMax; i++) {
                String name = outputPortAnnotations.get(i).name();
                String description = outputPortAnnotations.get(i).description();
                String sID = name.toLowerCase().replaceAll("[ ]+", " ").
                             replaceAll(" ", "-");
                setOutputs.add(new DataPortDescription(model.createResource(
                        sBaseURL + sComponentName + "/output/" + sID),
                        sBaseURL + sComponentName + "/output/" + sID, name,
                        description));
            }
        }

        Hashtable<String, String> htValues = new Hashtable<String, String>();
        Hashtable<String, String> htDescriptions = new Hashtable<String, String>();
        if (propertyAnnotations != null) {
            for (int i = 0, iMax = propertyAnnotations.size(); i < iMax; i++) {
                String name = propertyAnnotations.get(i).name();
                String description = propertyAnnotations.get(i).description();
                String defaultValue = propertyAnnotations.get(i).defaultValue();
                String sKey = name;
                htValues.put(name, defaultValue);
                htDescriptions.put(sKey, description);
            }
        }

        PropertiesDescriptionDefinition pddProperties = new
                PropertiesDescriptionDefinition(htValues, htDescriptions);

        // Generating the description
        Resource resMode = ExecutableComponentDescription.COMPUTE_COMPONENT;
        if(type.equals("webui")){
        	resMode = ExecutableComponentDescription.WEBUI_COMPONENT;
        }else{
        	resMode = ExecutableComponentDescription.COMPUTE_COMPONENT;
        }
        
        ExecutableComponentDescription ecd = new ExecutableComponentDescription(
                resExecutableComponent,
                sName,
                sDescription,
                sRights,
                sCreator,
                dateCreation,
                sRunnable,
                sFiringPolicy,
                sFormat,
                setContext,
                resLocation,
                setInputs,
                setOutputs,
                pddProperties,
                tagDesc,resMode
                                );
        // Generate the descriptors
        ByteArrayOutputStream bosRDF = new ByteArrayOutputStream();
        ecd.getModel().write(bosRDF);
        String sRDFDescription = bosRDF.toString();
        /*
         ByteArrayOutputStream bosTTL = new ByteArrayOutputStream();
         ecd.getModel().write(bosTTL,"TTL");
         String sTTLDescription = bosTTL.toString();

         ByteArrayOutputStream bosN3 = new ByteArrayOutputStream();
         ecd.getModel().write(bosN3,"N-TRIPLE");
         String sN3Description = bosN3.toString();

         */
        String dirPath = null;
        if (klazz.getName().lastIndexOf(".") == -1) {
            dirPath = componentDescriptorFolder;
        } else {
            dirPath = componentDescriptorFolder + File.separator +
                      klazz.
                      getName().substring(0, klazz.getName().lastIndexOf("."));

        }

        dirPath = dirPath.replace('.', File.separatorChar);

        if (!(new File(dirPath)).exists()) {
            new File(dirPath).mkdirs();
        }

        String componentDescriptor = klazz.getSimpleName();
        // write the descriptor to a file
        writeToFile(sRDFDescription,
                    dirPath + File.separator + componentDescriptor + ".rdf");

        System.out.println("Descriptor written to: " + dirPath + File.separator +
                           componentDescriptor + ".rdf");
        
        return  dirPath + File.separator + componentDescriptor + ".rdf";
       }




	// write the component descriptor to the file
    private void writeToFile(String description, String absoluteFilePath) {
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(new
                    FileOutputStream(absoluteFilePath), encoding));
            out.write(description.trim());
            out.flush();
            out.close();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }


    // return the firing policy
    private String getFiringPolicy(FiringPolicy firingPolicy) {
        return firingPolicy.toString();
    }


    // return the full rights information
    private String getRights(Licenses rights) {
        String rightsString = "Others";
        switch (rights) {
        case UofINCSA:
            rightsString = "University of Illinois/NCSA Open Source License";
            break;
        case ASL_2:
            rightsString = "Apache License 2.0";
            break;
        default:
            rightsString = "Others";
            break;
        }
        return rightsString;
    }

    private String getRunnable(Runnable runnable){
    	String runnableString = "java";
    	switch(runnable){
    	case java:
    		runnableString="java";
    		break;
    	case python:
    		runnableString="python";
    		break;
    	case lisp:
    		runnableString="lisp";
    		break;
    	default:
    		break;
    	}
    	
    	return runnableString;
    }
    

    private String getType(Mode type) {
		String typeString ="webui";
		switch(type){
		case webui:
			typeString ="webui";
			break;
		case compute:
			typeString ="compute";
			break;
		default:
			break;
		}
		return typeString;
	}

    
    

    // return the index where a particular annotation was found
    private int contains(Annotation[] annField, String annotationType) {
        int countAnnotations = annField.length;
        if (countAnnotations == 0) {
            return -1;
        }
        for (int thisAnnotation = 0; thisAnnotation < countAnnotations;
                                  thisAnnotation++) {
            if (annField[thisAnnotation].annotationType().getCanonicalName().
                endsWith(annotationType)) {
                return thisAnnotation;
            }
        }

        return -1;
    }

}
