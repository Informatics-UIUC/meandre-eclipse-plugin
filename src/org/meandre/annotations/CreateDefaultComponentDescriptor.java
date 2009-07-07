/*
 * @(#) CreateDefaultComponentDescriptor.java @VERSION@
 * 
 * Copyright (c) 2009+ Amit Kumar
 * 
 * The software is released under ASL 2.0, Please
 * read License.txt
 *
 */
package org.meandre.annotations;

import java.io.ByteArrayOutputStream;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.logging.Logger;

import org.meandre.core.repository.CorruptedDescriptionException;
import org.meandre.core.repository.DataPortDescription;
import org.meandre.core.repository.ExecutableComponentDescription;
import org.meandre.core.repository.PropertiesDescriptionDefinition;
import org.meandre.core.repository.TagsDescription;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

/**This class generates the default component descriptor
 * that Meandre supports
 * 
 * @author Amit Kumar
 * Created on Jan 31, 2009 5:54:22 PM
 * @modified on July 7th, 2009 11:52:50 PM
 * -process throws exception when one of the
 * -required annotation attributes is null
 *
 */
public class CreateDefaultComponentDescriptor {
	// default logger
	private Logger logger = Logger.getAnonymousLogger();
	private DetectDefaultComponentAnnotations detectDefaultComponentAnnotations;

	public CreateDefaultComponentDescriptor(){
		detectDefaultComponentAnnotations = new DetectDefaultComponentAnnotations();
	}

	/**
	 * 
	 * @param componentClass
	 * @return RDF String
	 * @throws CorruptedDescriptionException
	 */
	public String process(Class<?> componentClass) throws CorruptedDescriptionException{
		HashMap<String,Object> componentValues=	this.detectDefaultComponentAnnotations.getComponentClassAnnotationMap(componentClass, org.meandre.annotations.Component.class);
		if(componentValues.size()==0){
			logger.warning("Did not find any Component Annotation");
			return null;
		}
		String sName = (String)componentValues.get("name");
		String sBaseURL = (String)componentValues.get("baseURL");
		String sDescription = (String)componentValues.get("description");
		String sRights = (componentValues.containsKey("rights"))?(String)componentValues.get("rights").toString():"";
		String sRightsOther = (componentValues.containsKey("rightsOther"))?(String)componentValues.get("rightsOther"):"";
		String sCreator = (componentValues.containsKey("creator"))?(String)componentValues.get("creator"):"";
		java.util.Date dateCreation = new java.util.Date();
		String sTags = (componentValues.containsKey("tags"))?(String)componentValues.get("tags"):"";
		String sFormat = (componentValues.containsKey("format"))?(String)componentValues.get("format"):"";
		String type= (componentValues.containsKey("mode"))?(String)componentValues.get("mode").toString():"";
		String sRunnable = (componentValues.containsKey("runnable"))?(String)componentValues.get("runnable").toString():"";
		String sFiringPolicy = (componentValues.containsKey("firingPolicy"))?(String)componentValues.get("firingPolicy").toString():"";
		
		if(sName.length()==0 || 
				sBaseURL.length()==0 || 
				sCreator.length()==0 || 
				sFormat.length()==0 || 
				sRunnable.length()==0 || 
				sFiringPolicy.length()==0)
		{
			logger.severe("Error : one of the required Component annotations attributes is of zero length");	
			logger.severe("-- sBaseURL: " + sBaseURL);
			logger.severe("-- sName: " + sName);
			logger.severe("-- sCreator: " + sCreator);
			logger.severe("-- sRunnable: " + sRunnable);
			logger.severe("-- sFiringPolicy: " + sFiringPolicy);
			throw new CorruptedDescriptionException("Error : one of the required Component annotations attributes is of zero length");
		}


		if (sBaseURL.charAt(sBaseURL.length() - 1) != '/') {
			sBaseURL += '/';
		}
		sRights = (sRights.equals("Other")) ? sRightsOther : sRights;
		String sComponentName = sName.toLowerCase().replaceAll("[ ]+", " ").replaceAll(" ", "-");


		String sClassName = "";
		String[] saTmp = sName.replaceAll("[ ]+", " ").split(" ");
		for (String s : saTmp) {
			char chars[] = s.trim().toCharArray();
			chars[0] = Character.toUpperCase(chars[0]);
			sClassName += new String(chars);
		}
		
		 // don't use the sLocation because this may refer to 
		// extended class and the extended class maynot have
		// the the @Component annotation.
		String sLocation = componentClass.getCanonicalName();
		Model model = ModelFactory.createDefaultModel();
		Resource resExecutableComponent = model.createResource(sBaseURL
				+ sComponentName);
		Resource resLocation = model.createResource(sBaseURL + sComponentName
				+ "/implementation/" + sLocation);

		sTags = (sTags == null) ? "" : sTags;
		if (sTags.indexOf(',') < 0) {
			saTmp = sTags.toLowerCase().replaceAll("[ ]+", " ").split(" ");
		} else {
			saTmp = sTags.toLowerCase().replaceAll("[ ]+", " ").split(",");
		}
		java.util.Set<String> setTmp = new java.util.HashSet<String>();
		for (String s : saTmp) {
			setTmp.add(s.trim());
		}
		TagsDescription tagDesc = new TagsDescription(setTmp);

		saTmp = new String[] { sBaseURL + sComponentName + "/implementation/" };
		java.util.Set<RDFNode> setContext = new java.util.HashSet<RDFNode>();
		for (String s : saTmp) {
			setContext.add(model.createResource(s.trim()));
		}





		HashMap<String,Annotation> inputList=this.detectDefaultComponentAnnotations.getComponentFieldAnnotations(componentClass, 
				org.meandre.annotations.ComponentInput.class);

		HashMap<String,Annotation> outputList=this.detectDefaultComponentAnnotations.getComponentFieldAnnotations(componentClass, 
				org.meandre.annotations.ComponentOutput.class);

		HashMap<String,Annotation> propertyList=this.detectDefaultComponentAnnotations.getComponentFieldAnnotations(componentClass, 
				org.meandre.annotations.ComponentProperty.class);




		Set<DataPortDescription> setInputs = new HashSet<DataPortDescription>();
		Set<DataPortDescription> setOutputs = new HashSet<DataPortDescription>();

		for(String key: inputList.keySet()){
			org.meandre.annotations.ComponentInput thisInput=((org.meandre.annotations.ComponentInput)inputList.get(key));
			String description=thisInput.description();
			String name=thisInput.name();
			String sID = name.toLowerCase().replaceAll("[ ]+", " ").
			replaceAll(" ", "-");
			setInputs.add(new DataPortDescription(model.createResource(
					sBaseURL + sComponentName + "/input/" + sID),
					sBaseURL + sComponentName + "/input/" + sID, name,
					description));
		}


		for(String key: outputList.keySet()){
			org.meandre.annotations.ComponentOutput thisOutput=
				((org.meandre.annotations.ComponentOutput)outputList.get(key));
			String description=thisOutput.description();
			String name=thisOutput.name();
			String sID = name.toLowerCase().replaceAll("[ ]+", " ").
			replaceAll(" ", "-");
			setOutputs.add(new DataPortDescription(model.createResource(
					sBaseURL + sComponentName + "/output/" + sID),
					sBaseURL + sComponentName + "/output/" + sID, name,
					description));
		}




		Hashtable<String, String> htValues = new Hashtable<String, String>();
		Hashtable<String, String> htDescriptions = new Hashtable<String, String>();

		for (String key: propertyList.keySet()) {
			org.meandre.annotations.ComponentProperty thisProperty =
				((org.meandre.annotations.ComponentProperty)propertyList.get(key));
			String name = thisProperty.name();
			String description = thisProperty.description();
			String defaultValue = thisProperty.defaultValue();
			htValues.put(name, defaultValue);
			htDescriptions.put(name, description);
		}

		PropertiesDescriptionDefinition pddProperties = new
		PropertiesDescriptionDefinition(htValues, htDescriptions);

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
				tagDesc,
				resMode
		);
		// Generate the descriptors
		ByteArrayOutputStream bosRDF = new ByteArrayOutputStream();
		ecd.getModel().write(bosRDF);
		String sRDFDescription = bosRDF.toString();
		logger.info(sRDFDescription);
		return sRDFDescription;
	}

	/**
	 * @return the logger
	 */
	public Logger getLogger() {
		return logger;
	}

	/**
	 * @param logger the logger to set
	 */
	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	public DetectDefaultComponentAnnotations getAnnotationReader() {
		return this.detectDefaultComponentAnnotations;
		
	}


}
