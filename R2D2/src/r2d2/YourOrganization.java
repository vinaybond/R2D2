/*
 *  Use this class for your enterprise. This will be the place to define your org related methods.
 */


package r2d2;

import java.util.HashMap;
import java.util.Map;


public class YourOrganization extends Utilities {
	protected YourOrganization(){}; // do not allow instanace creation without getIntance(long)
	
	private static Map<Long, YourOrganization> threadsObject = new HashMap<Long, YourOrganization>();
	

	// return the instance for current thread
	public static YourOrganization getInstance(long threadID){
		if (threadsObject.get(threadID) == null){
			threadsObject.put(threadID, new YourOrganization());
		}
		return (YourOrganization) threadsObject.get(threadID);
	}


}
