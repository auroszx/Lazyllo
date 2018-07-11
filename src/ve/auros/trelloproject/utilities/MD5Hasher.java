package ve.auros.trelloproject.utilities;

import java.security.MessageDigest;
import java.util.Base64;

public class MD5Hasher {
	
	MessageDigest md = null;
	
	private MD5Hasher() {}
	
	//Returns Base64 MD5 hashed string.
	public String hashString(String original) {
		
		byte[] result = null;
		try {
			md = MessageDigest.getInstance("MD5");
			result = md.digest(original.getBytes("UTF-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return Base64.getEncoder().encodeToString(result);
		
	}
	
	private static MD5Hasher mh = new MD5Hasher();
	
	public static MD5Hasher getInstance() {
		return mh;
	}
}
