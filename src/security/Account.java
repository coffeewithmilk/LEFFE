package security;

public class Account {
	public static boolean loggedIn = false;
	
	public static String password1 = "AdminUsersTrustedOnly!";
	public static String password2 = "x";
	
	public static String AUTH_EMAIL = "";
	public static String AUTH_UNAME = "";
	public static String AUTH_PW = "";
	
	public static String PASSCODE = "";
	
	public static String[] password = new String[] {
		password1,
		password2
	};
	


	public static String user = "";
	
	public static String getUser() {
		return user;
	}
	
	public static boolean tryPassword(String pass) {
		for (String p : password) {
			if (pass.equals(p)) {
				return true;
			}
		}
		return false;
	}
	

}
