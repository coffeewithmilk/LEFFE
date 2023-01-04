package model.scripting;

import security.BusinessAccount;

public class AccountVar extends Variable {

	public AccountVar(String key) {
		super(key);
	}
	
	BusinessAccount account;
	
	public void setValue(Object value) {
		account = new BusinessAccount();
		String val = value.toString();
		String[] acc = val.split(" ");
		account.setEncryptedName(acc[0]);
		account.setEncryptedPassword(acc[1]);
	}
	
	public Object getValue() {
		return account;
	}

	public String revealPassword() {
		return account.decryptPassword();
	}
	
	public String revealUsername() {
		return account.decryptUsername();
	}
	
	public String toString() {
		return "Username: " + account.getName() + ", Password: " + account.getPassword();
	}
	
}
