package org.fife.rsta.ac.js.ast.jsType;

import java.util.ArrayList;
import java.util.List;

import org.fife.rsta.ac.java.classreader.ClassFile;
import org.fife.rsta.ac.js.JavaScriptHelper;
import org.fife.rsta.ac.js.SourceCompletionProvider;
import org.fife.rsta.ac.js.ast.type.TypeDeclaration;
import org.fife.rsta.ac.js.ast.type.TypeDeclarationFactory;
import org.mozilla.javascript.Kit;


public class JavaScriptFunctionType {

	public static int CONVERSION_NONE = 999;

	public final static Class BooleanClass = Kit.classOrNull("java.lang.Boolean"), 
							  ByteClass = Kit.classOrNull("java.lang.Byte"), 
							  CharacterClass = Kit.classOrNull("java.lang.Character"), 
							  ClassClass = Kit.classOrNull("java.lang.Class"), 
							  DoubleClass = Kit.classOrNull("java.lang.Double"), 
							  FloatClass = Kit.classOrNull("java.lang.Float"), 
							  IntegerClass = Kit.classOrNull("java.lang.Integer"), 
							  LongClass = Kit.classOrNull("java.lang.Long"), 
							  NumberClass = Kit.classOrNull("java.lang.Number"), 
							  ObjectClass = Kit.classOrNull("java.lang.Object"), 
							  ShortClass = Kit.classOrNull("java.lang.Short"), 
							  StringClass = Kit.classOrNull("java.lang.String"), 
							  DateClass = Kit.classOrNull("java.util.Date"), 
							  JSBooleanClass = Kit.classOrNull("org.fife.rsta.ac.js.ecma.api.JSBoolean"),
							  JSStringClass = Kit.classOrNull("org.fife.rsta.ac.js.ecma.api.JSString"),
							  JSNumberClass = Kit.classOrNull("org.fife.rsta.ac.js.ecma.api.JSNumber"),
							  JSObjectClass = Kit.classOrNull("org.fife.rsta.ac.js.ecma.api.JSObject"),
							  JSDateClass = Kit.classOrNull("org.fife.rsta.ac.js.ecma.api.JSDate"),
							  JSArray = Kit.classOrNull("org.fife.rsta.ac.js.ecma.api.JSArray");

	private String name;
	private List arguments;

	private static final int JSTYPE_UNDEFINED = 0; // undefined type
	private static final int JSTYPE_BOOLEAN = 1; // boolean
	private static final int JSTYPE_NUMBER = 2; // number
	private static final int JSTYPE_STRING = 3; // string
	private static final int JSTYPE_ARRAY = 4; // array
	private static final int JSTYPE_OBJECT = 5; // object


	private JavaScriptFunctionType(String name) {
		this.name = name;
		this.arguments = new ArrayList();
	}


	private JavaScriptFunctionType(String name, List arguments) {
		this.name = name;
		this.arguments = arguments;
	}


	public String getName() {
		return name;
	}


	public List getArguments() {
		return arguments;
	}


	public void addArgument(TypeDeclaration type) {
		if (arguments == null) {
			arguments = new ArrayList();
		}
		arguments.add(type);
	}


	public int getArgumentCount() {
		return arguments != null ? arguments.size() : 0;
	}


	public TypeDeclaration getArgument(int index) {
		return arguments != null ? (TypeDeclaration) arguments.get(index)
				: null;
	}


	/**
	 * Compare this JavaScriptFunctionType with another and return a weight integer based on the parameters matching or 
	 * whether the parameters are compatible.
	 * @param compareType method to compare with this
	 * @param provider SourceCompletionProvider
	 * @return weight based on the compatibleness of method to compare
	 */
	public int compare(JavaScriptFunctionType compareType, SourceCompletionProvider provider) {
		if (compareType.getArgumentCount() != getArgumentCount()) {
			return CONVERSION_NONE;
		}

		if (!compareType.getName().equals(getName())) {
			return CONVERSION_NONE;
		}

		// check parameters
		int weight = 0;
		for (int i = 0; i < getArgumentCount(); i++) {
			TypeDeclaration param = getArgument(i);
			TypeDeclaration compareParam = compareType.getArgument(i);
			weight = weight + compareParameters(param, compareParam, provider);
			if (weight >= CONVERSION_NONE)
				break;
		}

		return weight;
	}


	/**
	 * Convert parameter into TypeDeclaration
	 * @param type
	 * @param provider
	 * @return
	 */
	private TypeDeclaration convertParamType(TypeDeclaration type,
			SourceCompletionProvider provider) {
		ClassFile cf = provider.getJavaScriptTypesFactory().getClassFile(
				provider.getJarManager(), type);
		if (cf != null)
			return provider.getJavaScriptTypesFactory()
					.createNewTypeDeclaration(cf, type.isStaticsOnly(), false);
		else
			return type;
	}


	/**
	 * Converts TypeDeclaration into Java Class and  compares whether another parameter is compatible based
	 * on JSR-223
	 * @param param parameter to compare
	 * @param compareParam compare parameter
	 * @param provider SourceCompletionProvider
	 * @return
	 */
	private int compareParameters(TypeDeclaration param,
			TypeDeclaration compareParam, SourceCompletionProvider provider) {

		if (compareParam.equals(param))
			return 0;

		param = convertParamType(param, provider);
		compareParam = convertParamType(compareParam, provider);

		try {
			int fromCode = getJSTypeCode(param.getQualifiedName());
			Class to = convertClassToJavaClass(compareParam.getQualifiedName());
			Class from = convertClassToJavaClass(param.getQualifiedName());
			switch (fromCode) {
				case JSTYPE_UNDEFINED: {
					if (to == StringClass || to == ObjectClass) {
						return 1;
					}

					break;
				}
				case JSTYPE_BOOLEAN: {
					// "boolean" is #1
					if (to == Boolean.TYPE) {
						return 1;
					}
					else if (to == BooleanClass) {
						return 2;
					}
					else if (to == ObjectClass) {
						return 3;
					}
					else if (to == StringClass) {
						return 4;
					}
					break;
				}
				case JSTYPE_NUMBER: {
					if (to.isPrimitive()) {
						if (to == Double.TYPE) {
							return 1;
						}
						else if (to != Boolean.TYPE) {
							return 1 + getSizeRank(to);
						}
					}
					else {
						if (to == StringClass) {
							// native numbers are #1-8
							return 9;
						}
						else if (to == ObjectClass) {
							return 10;
						}
						else if (NumberClass.isAssignableFrom(to)) {
							// "double" is #1
							return 2;
						}
					}
					break;
				}
				case JSTYPE_STRING: {
					if (to == StringClass) {
						return 1;
					}
					else if (to.isPrimitive()) {
						if (to == Character.TYPE) {
							return 3;
						}
						else if (to != Boolean.TYPE) {
							return 4;
						}
					}
					break;
				}

				case JSTYPE_ARRAY:
					if (to == JSArray) {
						return 1;
					}
					if (to == StringClass) {
						return 2;
					}
					else if (to.isPrimitive() && to != Boolean.TYPE) {
						return (fromCode == JSTYPE_ARRAY) ? CONVERSION_NONE
								: 2 + getSizeRank(to);
					}
					break;

				case JSTYPE_OBJECT: {
					// Other objects takes #1-#3 spots
					if (to != ObjectClass && from.isAssignableFrom(to)) {
						// No conversion required, but don't apply for
						// java.lang.Object
						return 1;
					}
					if (to.isArray()) {
						if (from == JSArray || from.isArray()) {
							// This is a native array conversion to a java array
							// Array conversions are all equal, and preferable
							// to object
							// and string conversion, per LC3.
							return 1;
						}
					}
					else if (to == ObjectClass) {
						return 2;
					}
					else if (to == StringClass) {
						return 3;
					}
					else if (to == DateClass) {
						if (from == DateClass) {
							// This is a native date to java date conversion
							return 1;
						}
					}

					else if (from.isPrimitive() && to != Boolean.TYPE) {
						return 3 + getSizeRank(from);
					}
					break;
				}
			}
		} catch (ClassNotFoundException cnfe) {
		}

		// check js types
		String paramJSType = TypeDeclarationFactory.Instance()
				.convertJavaScriptType(param.getQualifiedName(), true);
		String compareParamJSType = TypeDeclarationFactory.Instance()
				.convertJavaScriptType(compareParam.getQualifiedName(), true);

		try {
			Class paramClzz = Class.forName(paramJSType);
			Class compareParamClzz = Class.forName(compareParamJSType);
			if (compareParamClzz.isAssignableFrom(paramClzz))
				return 3;
		} catch (ClassNotFoundException cnfe) {

		}

		if (compareParam.equals(TypeDeclarationFactory
				.getDefaultTypeDeclaration())) {
			return 4;
		}

		return CONVERSION_NONE;
	}


	/**
	 * Converts TypeDeclation qualified name to Java Class
	 * @param name
	 * @return
	 * @throws ClassNotFoundException
	 */
	private Class convertClassToJavaClass(String name)
			throws ClassNotFoundException {

		if (name.equals("any"))
			return ObjectClass;

		// check type is converted properly
		TypeDeclaration type = TypeDeclarationFactory.Instance()
				.getTypeDeclaration(name);

		String clsName = type != null ? type.getQualifiedName() : name;

		Class cls = Class.forName(clsName);

		if (cls == JSStringClass) {
			cls = StringClass;
		}
		else if (cls == JSBooleanClass) {
			cls = BooleanClass;
		}
		else if (cls == JSNumberClass) {
			cls = NumberClass;
		}
		else if (cls == JSDateClass) {
			cls = DateClass;
		}
		else if (cls == JSObjectClass) {
			cls = ObjectClass;
		}

		return cls;
	}


	/**
	 * Convenience method to parse function string and converts to JavaScriptFunctionType
	 * @param function String to parse e.g convertValue(java.util.String val);
	 * @return
	 */
	public static JavaScriptFunctionType parseFunction(String function) {
		int paramStartIndex = function.indexOf('(');
		int paramEndIndex = function.indexOf(')');
		JavaScriptFunctionType functionType = new JavaScriptFunctionType(
				function.substring(0, paramStartIndex));

		if (paramStartIndex > -1 && paramEndIndex > -1) {
			// strip parameters and resolve types
			String paramsStr = function.substring(paramStartIndex + 1,
					paramEndIndex);
			// iterate through params
			String[] params = paramsStr.split(",");
			for (int i = 0; i < params.length; i++) {
				functionType.addArgument(JavaScriptHelper
						.createNewTypeDeclaration(params[i]));
			}
		}

		return functionType;
	}

	/**
	 * Convenience method to parse function string and converts to JavaScriptFunctionType
	 * @param function String to parse e.g convertValue(java.util.String val);
	 * @param provider used for type conversions
	 * @return
	 */
	public static JavaScriptFunctionType parseFunction(String function,
			SourceCompletionProvider provider) {
		int paramStartIndex = function.indexOf('(');
		int paramEndIndex = function.indexOf(')');
		JavaScriptFunctionType functionType = new JavaScriptFunctionType(
				function.substring(0, paramStartIndex));

		if (paramStartIndex > -1 && paramEndIndex > -1) {
			// strip parameters and resolve types
			String paramsStr = function.substring(paramStartIndex + 1,
					paramEndIndex);
			// iterate through params
			String[] params = paramsStr.split(",");
			for (int i = 0; i < params.length; i++) {
				String param = TypeDeclarationFactory.convertJavaScriptType(
						params[i], true);
				TypeDeclaration type = TypeDeclarationFactory.Instance()
						.getTypeDeclaration(param);
				if (type != null) {
					functionType.addArgument(type);
				}
				else {

					functionType.addArgument(JavaScriptHelper
							.createNewTypeDeclaration(param));
				}
			}
		}

		return functionType;
	}


	/**
	 * Converts JavaScript class name to integer code 
	 * @param clsName
	 * @return
	 * @throws ClassNotFoundException
	 */
	private static int getJSTypeCode(String clsName)
			throws ClassNotFoundException {

		if (clsName.equals("any")) {
			return JSTYPE_UNDEFINED;
		}

		TypeDeclaration dec = TypeDeclarationFactory.Instance()
				.getTypeDeclaration(clsName);
		clsName = dec != null ? dec.getQualifiedName() : clsName;

		Class cls = Class.forName(clsName);

		if (cls == BooleanClass || cls == JSBooleanClass) {
			return JSTYPE_BOOLEAN;
		}

		if (NumberClass.isAssignableFrom(cls) || cls == JSNumberClass) {
			return JSTYPE_NUMBER;
		}

		if (StringClass.isAssignableFrom(cls) || cls == JSStringClass) {
			return JSTYPE_STRING;
		}

		if (cls.isArray() || cls == JSArray) {
			return JSTYPE_ARRAY;
		}

		return JSTYPE_OBJECT;

	}


	static int getSizeRank(Class aType) {
		if (aType == Double.TYPE) {
			return 1;
		}
		else if (aType == Float.TYPE) {
			return 2;
		}
		else if (aType == Long.TYPE) {
			return 3;
		}
		else if (aType == Integer.TYPE) {
			return 4;
		}
		else if (aType == Short.TYPE) {
			return 5;
		}
		else if (aType == Character.TYPE) {
			return 6;
		}
		else if (aType == Byte.TYPE) {
			return 7;
		}
		else if (aType == Boolean.TYPE) {
			return CONVERSION_NONE;
		}
		else {
			return 8;
		}
	}

}
