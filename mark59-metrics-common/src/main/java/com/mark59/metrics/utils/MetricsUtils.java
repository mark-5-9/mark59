package com.mark59.metrics.utils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;

import com.mark59.core.utils.SimpleAES;
import com.mark59.metrics.data.beans.ServerProfile;
import com.mark59.metrics.utils.MetricsConstants.OS;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

public class MetricsUtils {

	
	public static String obtainOperatingSystemForLocalhost() {	
	
		String operatingSystem = System.getProperty("os.name",OS.UNKNOWN.getOsName());
		
		if ( operatingSystem.toUpperCase().contains("WIN")) {
			operatingSystem = OS.WINDOWS.getOsName();
		} else if ( operatingSystem.toUpperCase().contains("LINUX")) {
			operatingSystem = OS.LINUX.getOsName();
		} else if ( operatingSystem.toUpperCase().contains("UNIX")) {
			operatingSystem = OS.UNIX.getOsName();
		} else {
			operatingSystem = OS.UNKNOWN.getOsName();
		}
		return operatingSystem;
	}

	
	public static Object runGroovyScript(String commandResponseParserScript, String commandResponse) {
		Binding binding = new Binding();
	    binding.setVariable("commandResponse", commandResponse);
		GroovyShell shell = new GroovyShell(binding);
		Object result = shell.evaluate(commandResponseParserScript);
		return result;
	}

	
	public static Object runGroovyScript(String groovyScript, Map<String,Object> scriptParms){
		Binding binding = new Binding();
		for(Map.Entry<String,Object> scriptParam : scriptParms.entrySet()) {
			binding.setVariable(scriptParam.getKey(), scriptParam.getValue());
		}
		GroovyShell shell = new GroovyShell(binding);
		Object result = shell.evaluate(groovyScript);
		return result;
	}


	public static String createMultiLineLiteral(List<String> multiLineStringList) {
		String [] multiLineStringArray = multiLineStringList.toArray(new String[0]);
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < multiLineStringArray.length; i++) {
			sb.append(multiLineStringArray[i]); 
			if ( i < multiLineStringArray.length - 1 ) {
				sb.append("\n");
			}
		}
		return sb.toString();
	}

	
	public static String listToTextboxFormat(List<String> listOfStrings) {
		if (listOfStrings == null) {
			return "";
		}
		StringBuilder textboxFormatSb = new StringBuilder();
		boolean firstTimeThruNoComma = true;
		for (String str : listOfStrings) {
			if (!firstTimeThruNoComma ){ 
				textboxFormatSb.append(System.lineSeparator());
			}
			firstTimeThruNoComma = false;
			textboxFormatSb.append(str); 
		}
		return textboxFormatSb.toString();
	}

	
	public static List<String> textboxFormatToList(String stringTextboxFormat) {
		List<String> listOfStrings = new ArrayList<String>();
		if ( stringTextboxFormat != null ){
			String spaceDelimitedStr = StringUtils.normalizeSpace(stringTextboxFormat).replace(',', ' ');
			// when an empty string is passed to the split, it creates a empty first element, not what we want 
			if (StringUtils.isAllBlank(spaceDelimitedStr)) {
				return listOfStrings; 
			}				
			listOfStrings = Arrays.asList(spaceDelimitedStr.split("\\s+")); 
		} 
		// System.out.println(">> textboxFormatToList stringTextboxFormat=" +  stringTextboxFormat); 
		// System.out.println("<< textboxFormatToList listOfStrings=" +  listOfStrings); 
		return listOfStrings;
	}
	
	
	/**
	 * If possible get the directory for the Window WMIC executable.  If it is 
	 * not found, then it's still possible to get the WMIC command to work by adding the 
	 * location of WMIC to the 'path' windows env variable.
	 * 
	 * @return String containing absolute path of WMIC execution directory, with 
	 * a file separator appended (for directly prefixing to 'wmic') 
	 */
	public static String wmicExecutableDirectory() {
		String root = System.getenv("SystemRoot");
		File wmicDir = new File(root, "System32" + File.separatorChar + "wbem");
		if (!wmicDir.exists() || !wmicDir.isDirectory()) { // not expected to occur
			System.out.println("WMIC Executable Directory : " + wmicDir + " either is a dir or does not exist!");
			return "";
		}
		return wmicDir.getAbsolutePath() + File.separatorChar;
	}	
	
	
	
	public static String cellValue(Cell cell) {
		if (cell == null) return ""; 
		return cell.getStringCellValue();
	}

	
	public static String actualPwd(ServerProfile serverProfile) {
		String actualPwd = "";
		if (StringUtils.isBlank(serverProfile.getPasswordCipher())) {
			actualPwd = serverProfile.getPassword();
		} else {
			try {
				actualPwd = SimpleAES.decrypt(serverProfile.getPasswordCipher());
			} catch (Exception e) {
				throw new RuntimeException("pwd decryption error: " + e.getMessage());
			}
		}
		return actualPwd;
	}
	
	
	/**
	 * Not actually used within the application itself, included to indicate how to encode user/password for 
	 * API Basic Authentication.  Review the test case of this method for usage example. 
	 */
	public static String createBasicAuthToken(String user, String pass) {
		String basicAuthToken = Base64.getEncoder().encodeToString((user + ":" + pass).getBytes(StandardCharsets.UTF_8));
		return basicAuthToken;
	}	
	
}
