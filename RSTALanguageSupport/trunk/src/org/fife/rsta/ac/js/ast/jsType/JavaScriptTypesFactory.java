/*
 * 02/25/2012
 *
 * Copyright (C) 2012 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://fifesoft.com/rsyntaxtextarea
 *
 * This code is licensed under the LGPL.  See the "license.txt" file included
 * with this project.
 */
package org.fife.rsta.ac.js.ast.jsType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.fife.rsta.ac.java.JarManager;
import org.fife.rsta.ac.java.classreader.ClassFile;
import org.fife.rsta.ac.java.classreader.FieldInfo;
import org.fife.rsta.ac.java.classreader.MemberInfo;
import org.fife.rsta.ac.java.classreader.MethodInfo;
import org.fife.rsta.ac.js.ast.type.TypeDeclaration;
import org.fife.rsta.ac.js.ast.type.TypeDeclarationFactory;
import org.fife.rsta.ac.js.completion.JSBeanCompletion;
import org.fife.rsta.ac.js.completion.JSFieldCompletion;
import org.fife.rsta.ac.js.completion.JSFunctionCompletion;
import org.fife.ui.autocomplete.DefaultCompletionProvider;


public abstract class JavaScriptTypesFactory {

	protected HashMap cachedTypes = new HashMap();
	private boolean useBeanproperties;
	
	private static ArrayList UNSUPPORTED_COMPLETIONS = new ArrayList();
	private static String SPECIAL_METHOD = "<clinit>";
	
	//list of unsupported completions e.g java.lang.Object as JavaScript has it's own
	static
	{
		UNSUPPORTED_COMPLETIONS.add("java.lang.Object");
	}


	private static class DefaultJavaScriptTypeFactory extends
			JavaScriptTypesFactory {

		public DefaultJavaScriptTypeFactory() {
		}
	}


	public static JavaScriptTypesFactory getDefaultJavaScriptTypesFactory() {
		return new DefaultJavaScriptTypeFactory();
	}


	public void setUseBeanProperties(boolean useBeanproperties) {
		this.useBeanproperties = useBeanproperties;
	}


	public boolean isUseBeanProperties() {
		return useBeanproperties;
	}


	/**
	 * Find CachedType for TypeDeclaration. If it is not found, then lookup the
	 * type and add all completions, then cache. Extracts all function and type
	 * completions from API based on the <code>TypeDeclaration</code>.
	 * 
	 * @param type TypeDeclaration to read from the API e.g JSString
	 * @param manager JarManager containing source and classes
	 * @param text - full text entered by user
	 * @param provider CompletionsProvider to bind the <code>Completion</code>
	 */
	public JavaScriptType getCachedType(TypeDeclaration type,
			JarManager manager, DefaultCompletionProvider provider,
			String text) {
		if (manager == null || type == null) // nothing to add
			return null;

		// already processed type, so no need to do again
		if (cachedTypes.containsKey(type))
			return (JavaScriptType) cachedTypes.get(type);

		// now try to read the functions from the API
		ClassFile cf = getClassFile(manager, type);

		JavaScriptType cachedType = makeJavaScriptType(type);
		// cache if required
		cachedTypes.put(type, cachedType);
		readClassFile(cachedType, cf, provider, manager, type);
		return cachedType;
	}
	
	
	public ClassFile getClassFile(JarManager manager, TypeDeclaration type)
	{
		return manager != null ? manager.getClassEntry(type.getQualifiedName()) : null;
	}
	
	/**
	 * Read the class file and extract all completions, add them all to the
	 * CachedType
	 * 
	 * @param cachedType CachedType to populate all completions
	 * @param cf ClassFile to read
	 * @param provider CompletionsProvider to bind to <code>Completion</code>
	 * @param manager JarManager containing source and classes
	 * @param type TypeDeclaration to read from the API e.g JString
	 */
	private void readClassFile(JavaScriptType cachedType, ClassFile cf,
			DefaultCompletionProvider provider, JarManager manager,
			TypeDeclaration type) {

		if (cf != null) {
			readMethodsAndFieldsFromTypeDeclaration(cachedType,
					provider,  manager, cf);
		}
	}


	/**
	 * @param method
	 * @return true if the method starts with get or is.
	 */
	private boolean isBeanProperty(MethodInfo method) {
		return method.getParameterCount() == 0
				&& (method.getName().startsWith("get") || method.getName()
						.startsWith("is"));
	}


	/**
	 * Extract all methods and fields from CompilationUnit and add to the
	 * completions map. Only public methods and fields will be added to
	 * completions
	 * 
	 * @param cachedType CachedType to populate all completions
	 * @param dec TypeDeclaration to read from the API e.g JString
	 * @param provider CompletionsProvider to bind to <code>Completion</code>
	 * @param cu CompilationUnit that binds source to class
	 * @param jarManager JarManager containing source and classes
	 * @return map of all completions
	 */
	private void readMethodsAndFieldsFromTypeDeclaration(
			JavaScriptType cachedType,
			DefaultCompletionProvider provider,
			JarManager jarManager, ClassFile cf) {

		boolean staticOnly = cachedType.getType().isStaticsOnly();
		// get methods
		int methodCount = cf.getMethodCount();
		for (int i = 0; i < methodCount; i++) {
			MethodInfo info = cf.getMethodInfo(i);
			if (!info.isConstructor() && !SPECIAL_METHOD.equals(info.getName())) {
				if(isAccessible(info.getAccessFlags()) && ((staticOnly && info.isStatic()) || !staticOnly)) {
					JSFunctionCompletion completion = new JSFunctionCompletion(provider, info, true);
					cachedType.addCompletion(completion);
				}
				// check java bean types (get/is methods)
				
				if (!staticOnly && useBeanproperties && isBeanProperty(info)) {
					JSBeanCompletion beanCompletion = new JSBeanCompletion(
							provider, info, jarManager);
					cachedType.addCompletion(beanCompletion);
				}
			}
		}

		// now get fields
		int fieldCount = cf.getFieldCount();
		for (int i = 0; i < fieldCount; i++) {
			FieldInfo info = cf.getFieldInfo(i);
			if (isAccessible(info, staticOnly)) {
				JSFieldCompletion completion = new JSFieldCompletion(provider, info);
				cachedType.addCompletion(completion);
			}
		}
		
		// Add completions for any non-overridden super-class methods.
		String superClassName = cf.getSuperClassName(true);
		ClassFile superClass = getClassFileFor(cf, superClassName, jarManager);
		if (superClass != null && !ignoreClass(superClassName)) {
			TypeDeclaration type = createNewTypeDeclaration(superClass, staticOnly, false);
			JavaScriptType extendedType = makeJavaScriptType(type);
			cachedType.addExtension(extendedType);
			readClassFile(extendedType, superClass, provider, jarManager, type);

		}

		// Add completions for any interface methods, in case this class is
		for (int i = 0; i < cf.getImplementedInterfaceCount(); i++) {
			String inter = cf.getImplementedInterfaceName(i, true);
			ClassFile intf = getClassFileFor(cf, inter, jarManager);
			if (intf != null && !ignoreClass(inter)) {
				TypeDeclaration type = createNewTypeDeclaration(intf, staticOnly, false);
				
				JavaScriptType extendedType = new JavaScriptType(type);
				cachedType.addExtension(extendedType);
				readClassFile(extendedType, intf, provider, jarManager, type);
			}
		}
	}
	
	public static boolean ignoreClass(String className)
	{
		return UNSUPPORTED_COMPLETIONS.contains(className);
	}


	private boolean isAccessible(MemberInfo info, boolean staticOnly) {

		boolean accessible = false;
		int access = info.getAccessFlags();
		accessible = isAccessible(access);

		return (!staticOnly && accessible) || ((staticOnly && info.isStatic() && accessible));

	}
	
	private boolean isAccessible(int access)
	{
		boolean accessible = false;
		if (org.fife.rsta.ac.java.classreader.Util.isPublic(access)) {
			accessible = true;
		}
		return accessible;
	}


	public TypeDeclaration createNewTypeDeclaration(ClassFile cf, boolean staticOnly)
	{
		return createNewTypeDeclaration(cf, staticOnly, true);
	}
	
	public TypeDeclaration createNewTypeDeclaration(ClassFile cf, boolean staticOnly, boolean addToCache) {
		String className = cf.getClassName(false);
		String packageName = cf.getPackageName();
		TypeDeclaration td = new TypeDeclaration(packageName, className, cf
				.getClassName(true), staticOnly);
		// now add to types factory
		if(addToCache)
			TypeDeclarationFactory.Instance().addType(cf.getClassName(true), td);
		return td;
	}


	/**
	 * @return returns the ClassFile for the class name, checks all packages
	 *         available to match to the class name
	 */
	private ClassFile getClassFileFor(ClassFile cf, String className,
			JarManager jarManager) {

		if (className == null) {
			return null;
		}

		ClassFile superClass = null;

		// Determine the fully qualified class to grab
		if (!org.fife.rsta.ac.java.Util.isFullyQualified(className)) {

			// Check in this source file's package first
			String pkg = cf.getPackageName();
			if (pkg != null) {
				String temp = pkg + "." + className;
				superClass = jarManager.getClassEntry(temp);
			}			
		}

		else {
			superClass = jarManager.getClassEntry(className);
		}

		return superClass;

	}


	/**
	 * Populate Completions for types... included extended classes. TODO
	 * optimise this.
	 * 
	 * @param completionsMap
	 * @param completions
	 * @param type
	 * @param manager
	 */
	public void populateCompletionsForType(JavaScriptType cachedType,
			Set completions) {

		if (cachedType != null) {
			HashMap completionsForType = cachedType.getCompletions();
			for (Iterator i = completionsForType.values().iterator(); i
					.hasNext();) {
				completions.add(i.next());
			}

			// get any extended classes and recursivley populate
			List extendedClasses = cachedType.getExtendedClasses();
			for (Iterator i = extendedClasses.iterator(); i.hasNext();) {
				JavaScriptType extendedType = (JavaScriptType) i.next();
				populateCompletionsForType(extendedType, completions);
			}
		}
	}


	public void removeCachedType(TypeDeclaration typeDef) {
		cachedTypes.remove(typeDef);
	}


	public void clearCache() {
		cachedTypes.clear();
	}
	
	public JavaScriptType makeJavaScriptType(TypeDeclaration type) {
		return new JavaScriptType(type);
	}
	
}