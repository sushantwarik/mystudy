package au.gov.nsw.rms.utility.ldap;

import java.util.Hashtable;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchResult;

import au.gov.nsw.rms.account.model.User;

public class LDAPHelper {
	
	private String baseDN = "";
	private DirContext ldapContext = null;
	
	public LDAPHelper(Hashtable<String, String> contextEnv, String baseDN) throws NamingException {
		ldapContext = (DirContext) new InitialDirContext(contextEnv);
		this.baseDN = baseDN;
	}
	
	public static DirContext getDirContext(Hashtable<String, String> contextEnv) {
		DirContext ldapContext = null;
		try {
			ldapContext = (DirContext) new InitialDirContext(contextEnv);
		} catch (NamingException e) {
			System.out.println(e.getMessage());
		}
		return ldapContext;
	}
	
	public boolean updateEntry(User user){
		Attributes newAttrs = new BasicAttributes();
		newAttrs.put(new BasicAttribute("sn", user.getlName()));
		newAttrs.put(new BasicAttribute("cn", user.getfName()));
		try {
			ldapContext.modifyAttributes("uid="+user.getUserId()+"," + baseDN,DirContext.REPLACE_ATTRIBUTE, newAttrs);
		} catch (NamingException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public boolean createEntry(User user){
		Attributes newAttrs = new BasicAttributes();
		newAttrs.put(new BasicAttribute("objectclass", "inetOrgPerson"));
		newAttrs.put(new BasicAttribute("userPassword", "simpletext"));
		newAttrs.put(new BasicAttribute("sn", user.getlName()));
		newAttrs.put(new BasicAttribute("cn", user.getfName()));
		try {
			ldapContext.createSubcontext("uid="+user.getUserId()+"," + baseDN, newAttrs);
		} catch (NamingException e) {
			return false;
		}
		return true;
	}
	
	public boolean deleteEntry(String entry){
		try {
			ldapContext.destroySubcontext("uid="+entry+"," + baseDN);
		} catch (NamingException e) {
			return false;
		}
		return true;
	}
	
	public User prepareUser(Attributes attributes){
		attributes.get("uid");
		User user = new User(attributes.get("uid").toString(),attributes.get("uid").toString(),attributes.get("uid").toString());
		return user;
	}
	
	public NamingEnumeration<SearchResult> searchEntry(String entry) throws NamingException{
		Attributes attributes = new BasicAttributes(true);
		attributes.put((Attribute) new BasicAttribute("uid", entry));
		NamingEnumeration<SearchResult> results = ldapContext.search(baseDN, attributes );
		return results;
	}

	public void close() throws NamingException {
		this.ldapContext.close();
		
	}
}
