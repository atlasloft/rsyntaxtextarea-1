package org.fife.rsta.ac.js.ecma.api;


public abstract class JSObject {

    /**
     * Object Object()
     * 
     * @constructor
     * @since Standard ECMA-262 3rd. Edition
     * @since Level 2 Document Object Model Core Definition.
     */
    public JSObject() {
    }

    /**
     * function toString()
     * 
     * @memberOf Object
     * @returns {String}
     * @see JSObject
     * @since Standard ECMA-262 3rd. Edition
     * @since Level 2 Document Object Model Core Definition.
     */
    public abstract String toString();

    /**
     * function toLocaleString()
     * 
     * @memberOf Object
     * @returns {String}
     * @see Object
     * @since Standard ECMA-262 3rd. Edition
     * @since Level 2 Document Object Model Core Definition.
     */
    public abstract JSString toLocaleString();

    /**
     * function valueOf()
     * 
     * @memberOf Object
     * @returns {Object}
     * @see Object
     * @since Standard ECMA-262 3rd. Edition
     * @since Level 2 Document Object Model Core Definition.
     */
    public abstract JSObject valueOf();

    /**
     * function hasOwnProperty(name)
     * 
     * @memberOf Object
     * @param {String} name
     * @returns {Boolean}
     * @see Object
     * @since Standard ECMA-262 3rd. Edition
     * @since Level 2 Document Object Model Core Definition.
     */
    public abstract JSBoolean hasOwnProperty();

    /**
     * function isPrototypeOf(o)
     * 
     * @memberOf Object
     * @param {Object} o
     * @returns {Boolean}
     * @see Object
     * @since Standard ECMA-262 3rd. Edition
     * @since Level 2 Document Object Model Core Definition.
     */
    public abstract JSBoolean isPrototypeOf(JSObject o);

    /**
     * function propertyIsEnumerable(name)
     * 
     * @memberOf Object
     * @param {Object} name
     * @returns {Boolean}
     * @see Object
     * @since Standard ECMA-262 3rd. Edition
     * @since Level 2 Document Object Model Core Definition.
     */
    public abstract JSBoolean propertyIsEnumerable(JSObject name);

}
