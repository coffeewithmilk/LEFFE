package security;

import java.util.Base64;

public class BusinessAccount {
	public static final String keyword = "account";


	private String name = "";
	private String password = ""; //ENCRYPTED 

	public BusinessAccount() {

	}
	
	public static void main(String[] args) {
		String pass = "AdminUsersTrustedOnly!";
		BusinessAccount ba = new BusinessAccount();
		ba.setPassword(pass);
	}
	
	public void setPassword(String password) {
		this.password = encrypt(password, ENCRYPTION_DEPTH);
	}
	
	public void setName(String name) {
		this.name = encrypt(name, ENCRYPTION_DEPTH);
	}
	
	public void setEncryptedPassword(String encrPassword) {
		this.password = encrPassword;
	}
	
	public void setEncryptedName(String encrName) {
		this.name = encrName;
	}
	
	public String decryptPassword() {
		return decrypt(this.password, ENCRYPTION_DEPTH);
	}
	
	public String decryptUsername() {
		return decrypt(this.name, ENCRYPTION_DEPTH);
	}
	
	public String getPassword() {
		return this.password;
	}
	
	public String getName() {
		return name;
	}

	

	public static final int ENCRYPTION_DEPTH = 4;
	
	public static String encrypt(String plain, int depth) {
		String b64encoded = Base64.getEncoder().encodeToString(plain.getBytes());

		// Reverse the string
		String reverse = new StringBuffer(b64encoded).reverse().toString();

		StringBuilder tmp = new StringBuilder();
		final int OFFSET = 4;
		for (int i = 0; i < reverse.length(); i++) {
			tmp.append((char)(reverse.charAt(i) + OFFSET));
		}
		
		depth--;
		if (depth >= 0) {
			return encrypt(tmp.toString(), depth);
		} else {
			return tmp.toString();
		}
	}

	public static String decrypt(String secret, int depth) {
		StringBuilder tmp = new StringBuilder();
		final int OFFSET = 4;
		for (int i = 0; i < secret.length(); i++) {
			tmp.append((char)(secret.charAt(i) - OFFSET));
		}

		String reversed = new StringBuffer(tmp.toString()).reverse().toString();
		depth--;
		if (depth >= 0) {
			return decrypt(new String(Base64.getDecoder().decode(reversed)), depth);
		} else {
			return new String(Base64.getDecoder().decode(reversed));
		}
	}

	

	

}
