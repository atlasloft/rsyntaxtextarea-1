package org.fife.rsta.ac.js.completion;


public interface JSCompletion extends JSCompletionUI {

	/**
	 * @return a logical lookup name that is unique
	 */
	String getLookupName();


	/**
	 * @return JavaScript type from Completion qualified
	 */
	String getType(boolean qualified);
		
	
}
