package au.gov.nsw.rms.account.ldap;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchResult;

import au.gov.nsw.rms.account.model.User;
import au.gov.nsw.rms.constants.Constants;
import au.gov.nsw.rms.utility.ldap.LDAPHelper;

import com.ibm.broker.javacompute.MbJavaComputeNode;
import com.ibm.broker.plugin.MbElement;
import com.ibm.broker.plugin.MbException;
import com.ibm.broker.plugin.MbJSON;
import com.ibm.broker.plugin.MbMessage;
import com.ibm.broker.plugin.MbMessageAssembly;
import com.ibm.broker.plugin.MbOutputTerminal;

public class LDAPAccountProcessor_UpdRetLDAPAccount extends MbJavaComputeNode {

	public void evaluate(MbMessageAssembly assembly) throws MbException {
		MbOutputTerminal out = getOutputTerminal("out");
		MbMessageAssembly outMessageAssembly;
		MbMessage outMessage = new MbMessage();
		outMessageAssembly = new MbMessageAssembly(assembly, outMessage);
		try {
			MbMessage inMessage = assembly.getMessage();
			
			outMessageAssembly = new MbMessageAssembly(assembly, outMessage);
			// ----------------------------------------------------------
			String action = (String) getUserDefinedAttribute("ACTION");
			String baseDN = (String) getUserDefinedAttribute("BASEDN");
			Hashtable<String,String> env = getEnv();
			LDAPHelper helper = new LDAPHelper(env,baseDN);
			
			if(Constants.READ.equals(action)){
				String userId = assembly.getLocalEnvironment().getRootElement().getFirstElementByPath("REST/Input/Parameters/userId").getValueAsString();
				NamingEnumeration<SearchResult> searchList = helper.searchEntry(userId);
				if(searchList.hasMoreElements()) {
					SearchResult entry = searchList.next();
					Attributes attrs = entry.getAttributes();
					MbElement outRoot =  outMessage.getRootElement();
					MbElement outJsonRoot = outRoot.createElementAsLastChild(MbJSON.PARSER_NAME);
					MbElement outJsonData = outJsonRoot.createElementAsLastChild(MbElement.TYPE_NAME, MbJSON.DATA_ELEMENT_NAME, null);
					outJsonData.createElementAsLastChild(MbElement.TYPE_NAME_VALUE, "UserId", attrs.get("uid").get());
					MbElement mbName = outJsonData.createElementAsLastChild(MbElement.TYPE_NAME, "Name", null);
					mbName.createElementAsLastChild(MbElement.TYPE_NAME, "FirstName", attrs.get("cn").get());
					mbName.createElementAsLastChild(MbElement.TYPE_NAME, "LastName", attrs.get("sn").get());
				} else {
					setHttpStatusCode(outMessageAssembly.getLocalEnvironment().getRootElement(),Constants.HTTP.RESOURCE_NOT_FOUND);
				}
			} else {
				if(Constants.DELETE.equals(action)){
					String userId = assembly.getLocalEnvironment().getRootElement().getFirstElementByPath("REST/Input/Parameters/userId").getValueAsString();
					NamingEnumeration<SearchResult> searchList = helper.searchEntry(userId);
					if(searchList.hasMoreElements()) {
						if(helper.deleteEntry(userId))
							setHttpStatusCode(outMessageAssembly.getLocalEnvironment().getRootElement(),Constants.HTTP.RESOURCE_DELETED);
						else 
							setHttpStatusCode(outMessageAssembly.getLocalEnvironment().getRootElement(),Constants.HTTP.INTERNAL_SERVER_ERROR);
					} else {
						setHttpStatusCode(outMessageAssembly.getLocalEnvironment().getRootElement(),Constants.HTTP.RESOURCE_NOT_FOUND);
					}
				} else {
					MbElement bodyJSONData = inMessage.getRootElement().getLastChild().getLastChild();
					MbElement mbUserId = bodyJSONData.getFirstElementByPath("UserId");
					MbElement mbFName = bodyJSONData.getFirstElementByPath("Name/FirstName");
					MbElement mbLName = bodyJSONData.getFirstElementByPath("Name/LastName");
					
					User user = new User(mbFName.getValueAsString(),mbLName.getValueAsString(),mbUserId.getValueAsString());
					
					if(Constants.CREATE.equals(action)){
						String userId = assembly.getLocalEnvironment().getRootElement().getFirstElementByPath("REST/Input/Parameters/userId").getValueAsString();
						NamingEnumeration<SearchResult> searchList = helper.searchEntry(userId);
						if(!searchList.hasMoreElements()) {
							helper.createEntry(user);
							if(helper.createEntry(user))
								setHttpStatusCode(outMessageAssembly.getLocalEnvironment().getRootElement(),Constants.HTTP.RESOURCE_CREATED);
							else 
								setHttpStatusCode(outMessageAssembly.getLocalEnvironment().getRootElement(),Constants.HTTP.INTERNAL_SERVER_ERROR);
						} else {
							setHttpStatusCode(outMessageAssembly.getLocalEnvironment().getRootElement(),Constants.HTTP.RESOURCE_CONFLICT);
						}
						
					}
					if(Constants.UPDATE.equals(action)){
						String userId = assembly.getLocalEnvironment().getRootElement().getFirstElementByPath("REST/Input/Parameters/userId").getValueAsString();
						NamingEnumeration<SearchResult> searchList = helper.searchEntry(userId);
						if(searchList.hasMoreElements()) {
							if(helper.updateEntry(user))
								setHttpStatusCode(outMessageAssembly.getLocalEnvironment().getRootElement(),Constants.HTTP.RESOURCE_UPDATED);
							else 
								setHttpStatusCode(outMessageAssembly.getLocalEnvironment().getRootElement(),Constants.HTTP.INTERNAL_SERVER_ERROR);
						} else {
							setHttpStatusCode(outMessageAssembly.getLocalEnvironment().getRootElement(),Constants.HTTP.RESOURCE_NOT_FOUND);
						}
					}
				}
			}
			helper.close();
			// End of user code
			// ----------------------------------------------------------
		} catch (MbException e) {
			setHttpStatusCode(outMessageAssembly.getLocalEnvironment().getRootElement(),Constants.HTTP.INTERNAL_SERVER_ERROR);
		} catch (RuntimeException e) {
			setHttpStatusCode(outMessageAssembly.getLocalEnvironment().getRootElement(),Constants.HTTP.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			setHttpStatusCode(outMessageAssembly.getLocalEnvironment().getRootElement(),Constants.HTTP.INTERNAL_SERVER_ERROR);
		}

		out.propagate(outMessageAssembly);
	}
	
	private void setHttpStatusCode(MbElement rootElement,int httpStatusCode) throws MbException {
		//OutputLocalEnvironment.Destination.HTTP.ReplyStatusCode
		rootElement.createElementAsFirstChild(MbElement.TYPE_NAME,"Destination",null)
				.createElementAsFirstChild(MbElement.TYPE_NAME,"HTTP",null)
					.createElementAsFirstChild(MbElement.TYPE_NAME_VALUE,"ReplyStatusCode",httpStatusCode);
	}

	public Hashtable<String, String> getEnv(){
		String url="ldap://localhost:10389";
		Hashtable<String,String> env = new Hashtable<String,String>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, url);
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		//env.put(Context.SECURITY_PRINCIPAL, "uid=admin,ou=system");
		//env.put(Context.SECURITY_CREDENTIALS, "secret");
		return env;
	}
	
	public MbElement convertUserToJson(){
		
		return null;
	}
}
