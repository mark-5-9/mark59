package com.mark59.servermetricsweb.utils;

import java.io.File;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

public class ServerMetricsWebUtils {

	
	public static String obtainOperatingSystemForLocalhost() {	
	
		String operatingSystem = System.getProperty("os.name", AppConstantsServerMetricsWeb.UNKNOWN).toUpperCase();

		if ( operatingSystem.contains("WIN")) {
			operatingSystem = AppConstantsServerMetricsWeb.WINDOWS;
		} else if ( operatingSystem.contains("LINUX")) {
			operatingSystem = AppConstantsServerMetricsWeb.LINUX;
		} else if ( operatingSystem.contains("UNIX")) {
			operatingSystem = AppConstantsServerMetricsWeb.UNIX;
		} else {
			operatingSystem = AppConstantsServerMetricsWeb.UNKNOWN;
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
	
	
}
