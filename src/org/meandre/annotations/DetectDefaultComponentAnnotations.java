/*
 * @(#) DetectComponentAnnotations.java @VERSION@
 * 
 * Copyright (c) 2009+ Amit Kumar
 * 
 * The software is released under ASL 2.0, Please
 * read License.txt
 *
 */
package org.meandre.annotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Stack;


/**This class abstracts the process of detecting annotations
 * from the the component classes.
 * 
 * @author Amit Kumar
 * Created on Jan 29, 2009 12:24:52 PM
 *
 */
public class DetectDefaultComponentAnnotations {
	
	/**This function returns the ComponentNature Annotation 
	 * @param componentClass
	 * @return
	 */
	public HashMap<String, ComponentNature> getComponentNatureAnnotation(Class<?> componentClass) {
		HashMap<String,ComponentNature>  hashMap = new HashMap<String,ComponentNature>();
		
		Stack<ComponentNature> annotationStack = new Stack<ComponentNature>(); 
		if(componentClass.getAnnotation( ComponentNature.class)!=null){
			annotationStack.push(componentClass.getAnnotation(ComponentNature.class));
		}
		Class<?> superClazz = componentClass.getSuperclass();
		while(!superClazz.getName().equals("java.lang.Object")){
		if(superClazz.getAnnotation(ComponentNature.class)!=null){
			annotationStack.push(superClazz.getAnnotation(ComponentNature.class));
		}
		superClazz = superClazz.getSuperclass();
		}
		
		ComponentNature thisAnnotation;
		while(!annotationStack.empty()){
			thisAnnotation = annotationStack.pop();
			String value= thisAnnotation.type();
			hashMap.put(value, thisAnnotation);
		}
		return hashMap;
	}

	/**This function finds the component annotation class and component nature 
	 * annotation applied to the component class. This function returns
	 * a hashmap with size 0 if the component class does not have any any 
	 * annotations
	 * 
	 * @param clazz
	 * @param componentAnnotationClass
	 * @return HashMap<String,Object>
	 */
	public HashMap<String,Object> getComponentClassAnnotationMap(Class<?> clazz, 
			Class<? extends Annotation> componentAnnotationClass){
		Stack<Annotation> annotationStack = new Stack<Annotation>(); 
		if(clazz.getAnnotation(componentAnnotationClass)!=null){
			annotationStack.push(clazz.getAnnotation(componentAnnotationClass));
		}
		Class<?> superClazz = clazz.getSuperclass();
		while(!superClazz.getName().equals("java.lang.Object")){
		if(superClazz.getAnnotation(componentAnnotationClass)!=null){
			annotationStack.push(superClazz.getAnnotation(componentAnnotationClass));
		}
		superClazz = superClazz.getSuperclass();
		}
		return mergeAnnotationsMap(annotationStack, componentAnnotationClass);
	}
	/**Return field level annotations: This function returns component input/output
	 * and property annotations
	 * 
	 * @param component
	 * @param componentFieldAnnotationClass
	 * @return
	 */
	public HashMap<String, Annotation> getComponentFieldAnnotations(Class<?> component, 
			Class<? extends Annotation> componentFieldAnnotationClass){
		System.out.println("Component: " + component.getName());
		HashMap<String, Annotation> annotationList = new HashMap<String,Annotation>();
		Stack<Class<?>> classStack = new Stack<Class<?>>();
		Class<?> superClazz = component.getSuperclass();
		classStack.push(component);
		while(!superClazz.getName().equalsIgnoreCase("java.lang.Object")){
			System.out.println("pushing: " + superClazz.getName());
			classStack.push(superClazz);
			superClazz = superClazz.getSuperclass();
		}
		
		while(!classStack.isEmpty()){
		Class<?> thisClass=classStack.pop();
		Field[] fields = thisClass.getDeclaredFields();
		for(Field field: fields){
		Annotation annot=	field.getAnnotation(componentFieldAnnotationClass);
			if(annot!=null){
			// get the name method and then add it to the hashmap with return value of name as the
			// the key
			Method method;
			String value=null;
			try {
				method = annot.getClass().getDeclaredMethod("name",(Class[])null);
				if(method!=null){
				value = (String)(method.invoke(annot,(Object[])null));
				}
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(value!=null){
				annotationList.put(value, annot);
			}
			
			}
			
		}
		}
		
		
		return annotationList;	
	}
	/**
	 * 
	 * @param annotationStack
	 * @return
	 */
	private HashMap<String,Object> mergeAnnotationsMap(
			Stack<Annotation> annotationStack,Class<? extends Annotation> componentAnnotationClass) {
		HashMap<String,Object> annotationKeyValues = new HashMap<String,Object>();
		if(annotationStack.isEmpty()){
			return annotationKeyValues;
		}
		Annotation thisAnnotation=null;
		while(!annotationStack.empty()){
			thisAnnotation = annotationStack.pop();
			System.out.println(thisAnnotation.getClass().getDeclaredMethods());
			for(Method method: thisAnnotation.getClass().getDeclaredMethods()){
				String methodName = method.getName();
				// the three methods below are inherited from
				// java.lang.Object -ignore these
				if(methodName.equalsIgnoreCase("hashCode")|| 
						methodName.equalsIgnoreCase("equals")||
						methodName.equalsIgnoreCase("toString")||
						methodName.equalsIgnoreCase("annotationType")){
					continue;
				}
				Class<?> returnType = method.getReturnType();
				Object value=null;
				try {
					value = (Object)(method.invoke(thisAnnotation,(Object[])null));
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(value == null){
					value = "";
				}
				
				if(returnType.isArray() ){
					Object w[] = (Object[])value ;
					annotationKeyValues.put(methodName, w);							
				}else{
					annotationKeyValues.put(methodName, value);
				}
			}
		}
	return annotationKeyValues;
	}

}
