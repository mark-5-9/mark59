/*
 *  Copyright 2019 Insurance Australia Group Limited
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  you may not use this file except in compliance with the License. 
 *  You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mark59.converter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * Parses a Jmeter results file(s) in XML or CSVformat, and converts the data to CSV formatted file(s) suitable to produce Jmeter Reports. See program arguments descriptions for more detail on file output options.
 * 
 * <p>For XML, the general format of the file we want to parse:
 * <pre>{@code
 *   <sample t="871" it="0" lt="0" ct="0" ts="1513135421532" s="true" lb="MyApp_010_HomePage" rc="200" rm="PASS" tn="tgMyApp 1-1" dt="" by="0" sby="0" ng="1" na="2"/>
 * }</pre>    
 * - see http://jmeter.apache.org/usermanual/listeners.html  (section 12.4 and 12.7 for formats).  We want to turn it into a line of csv which kinda looks like:
 * <pre>
 *     timeStamp,elapsed,label,responseCode,responseMessage,threadName,dataType,success,failureMessage,bytes,sentBytes,grpThreads,allThreads,URL,Latency,IdleTime,Connect
 *     1513135421532,871,MyApp_010_HomePage,200,PASS,tgMyApp 1-1,,true,,0,0,1,2,null,0,0,0
 * </pre>
 * 
 * <p>More generally, the field CSV format and its relationship to XML attribute names (data line stretched out to make it easier to read) is assumed to be:  
 * <pre>
 *     timeStamp,elapsed,label,responseCode,responseMessage,threadName,dataType,success,failureMessage,bytes,sentBytes,grpThreads,allThreads,URL,Latency,IdleTime,Connect
 *     ts,       t,      lb   ,rc (added)  ,rm (added)     ,tn        ,dt      ,s      ,(unmapped)    ,by   ,sby      ,ng        ,na        ,*  ,lt     ,it      ,ct       
 * </pre> 
 * * URL is unmapped (TODO: could be obtained via the java.net.URL child node when present)
 * <p>For CSV, the fields are simply (re)ordered the same way for all files (so that only one format for the header is required).
 *   
 * <p>For both CSV and XML files, additional entries may be produced for ERROR reporting,  See the "errortransactionnaming (e)" program argument description.
 *   
 * <p>Will process all the jmeter results files (suffixes .xml, .csv or .jtl) residing in the top level of the input directory.
 * 
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public class ResultFilesConverter { 

	public static final String ERROR_TXNS_NO						= "No";
	public static final String ERROR_TXNS_RENAME					= "Rename";
	public static final String ERROR_TXNS_DUPLICATE					= "Duplicate";
	
	public static final String METRICS_FILE_NO 						= "No";
	public static final String METRICS_FILE_CREATE_METRICS_REPORT	= "CreateMetricsReport";
	public static final String METRICS_FILE_SPLIT_BY_DATATYPE 		= "SplitByDataType";

	private static final String IGNORE ="IGNORE";
	
	/* taken from openCSV */
	private static final int INITIAL_STRING_SIZE 		= 256;
	private static final char DEFAULT_SEPARATOR 		= ',';	
	private static final char DEFAULT_ESCAPE_CHARACTER 	= '"';
	private static final char DEFAULT_QUOTE_CHARACTER 	= '"';
	private static final String DEFAULT_LINE_END 		= "\n";
	
	/* taken from Commons StringUtils */
    public static final int INDEX_NOT_FOUND = -1;
	
	private static final String DEFAULT_OUTPUT_FOLDER 	= "MERGED";
	private static final String DEFAULT_NO			  	= "NO";
		
	private static String argInputdirectory;	
	private static String argOutputdirectoy;	
	private static String argOutputFilename;	
	private static String argErrortransactions;	
	private static String argeXcludeResultsWithSub;	
	private static String argMetricsfile;	

	
	private static final String[] blankLine = {"0","0","","0","","","","","","0","0","0","0","","0","","0","0"};
	
	private String[] nextLine = new String[18];	

	private int fieldPostimeStamp;
	private int fieldPoselapsed;	
	private int fieldPoslabel;	
	private int fieldPosresponseCode;  
	private int fieldPosresponseMessage;		
	private int fieldPosthreadName; 
	private int fieldPosdataType;	
	private int fieldPossuccess;
	private int fieldPosfailureMessage;		
	private int fieldPosbytes;	
	private int fieldPossentBytes;
	private int fieldPosgrpThreads;
	private int fieldPosallThreads;
	private int fieldPosURL;	
	private int fieldPosLatency;
	private int fieldPosHostname;	
	private int fieldPosIdleTime;
	private int fieldPosConnect;

	private String outputBaseCsvFileName;
	
	private BufferedWriter baseCsvFileNameWriter; 	
	private BufferedWriter metrics_CsvFileNameWriter; 	
	private BufferedWriter datapoint_CsvFileNameWriter; 	
	private BufferedWriter cpu_util_CsvFileNameWriter; 	
	private BufferedWriter memory_CsvFileNameWriter;



	public void parseArguments(String[] args) {
		
		Options options = new Options(); 
		options.addOption("i", "inputdirectory",		true, "The directory containing the performance test result file(s).  Multiple xml/csv/jtl results files allowed.  Default is current directory");			
		options.addOption("o", "outputdirectoy", 		true, "Directory in which to write the output CSV file. Must already exist.  Default is a folder named 'MERGED' under the input directory");
		options.addRequiredOption("f", "outputFilename",true, "Base output CSV file name.  File extension will be .csv (will be suffixed .csv even if not included in the argument).  If metrics are split out, an additional file ending will be added for metric datafile(s) - see 'splitMetrics' options for details." );
		options.addOption("e", "errortransactionnaming",true, "How to handle txns marked as failed. 'Rename' suffixes the failed txn names with '_ERRORED'. 'Duplicate' keeps the orignal txn plus adds a '_ERRORED' txn. Default is 'No' - just keep original txn name." );
		options.addOption("x", "eXcludeResultsWithSub",	true, "TRUE (the default) will exclude the XML file main httpsample transaction for entries which has sub-results ('FALSE' to include)") ;		
		options.addOption("m", "Metricsreportsplit", 	true, "Option to create separate file(s) for metric data.  'CreateMetricsReport' - create separate file with all non-txn data, suffixed _METRICS , 'SplitByDataType' create a file per datatype, suffixed with _{datatype}.  Default is 'No' - just put everything in the one output file" );		
	
		
		HelpFormatter formatter = new HelpFormatter();
		CommandLine commandLine = null; 
		CommandLineParser parser = new DefaultParser();
		try {
			commandLine = parser.parse( options, args );
		} catch( ParseException exp ) {
			System.err.println( "ERROR:  ERROR : Parsing failed.  Reason: " + exp.getMessage() );
			formatter.printHelp( "ResultsConverter", options );
			printSampleUsage();
			throw new RuntimeException(); 
		}
		
		String cwd = System.getProperty("user.dir");
		System.out.println("cwd = "  + cwd);
		
		argInputdirectory 		 = commandLine.getOptionValue("i", cwd);	
		argOutputdirectoy		 = commandLine.getOptionValue("o", argInputdirectory + File.separator + DEFAULT_OUTPUT_FOLDER );	
		argOutputFilename		 = commandLine.getOptionValue("f");	
		argErrortransactions	 = commandLine.getOptionValue("e", DEFAULT_NO);	
		argeXcludeResultsWithSub = commandLine.getOptionValue("x", "True");	
		argMetricsfile			 = commandLine.getOptionValue("m", DEFAULT_NO);	
	
	
		File inputdirectory = new File(argInputdirectory);
		if (! inputdirectory.exists()) {
			throwRuntimeError(options, "The input directory (option i) '" + argInputdirectory + "' must exist! "  );
		}
		if (! inputdirectory.isDirectory()) {
			throwRuntimeError(options, "The input directory (option i) '" + argInputdirectory + "' must be a directory (not a file) "  );
		}
		
		File outputdirectory = new File(argOutputdirectoy);
		if (! outputdirectory.exists()) {
			throwRuntimeError(options, "The output directory (option o) '" + argOutputdirectoy + "' must exist! (please note it should also be a directory) "  );
		}
		if (! outputdirectory.isDirectory()) {
			throwRuntimeError(options, "The output directory (option o) '" + argOutputdirectoy + "' must be a directory (not a file) "  );
		}
			
		if ( !ERROR_TXNS_NO.equalsIgnoreCase(argErrortransactions)  
				&&  !ERROR_TXNS_RENAME.equalsIgnoreCase(argErrortransactions)
				&&  !ERROR_TXNS_DUPLICATE.equalsIgnoreCase(argErrortransactions)) {
			throwRuntimeError(options, "The errortransactions (-e) arg must be " + ERROR_TXNS_NO + ", " + ERROR_TXNS_RENAME + ", or  " + ERROR_TXNS_DUPLICATE );   
		}
			
		if ( !"TRUE".equalsIgnoreCase(argeXcludeResultsWithSub) &&  !"FALSE".equalsIgnoreCase(argeXcludeResultsWithSub)) {
			throwRuntimeError(options, "The argeXcludeResultsWithSub (-x) arg must be True or False" );   
		}
		
		if ( !METRICS_FILE_NO.equalsIgnoreCase(argMetricsfile)  
				&&  !METRICS_FILE_CREATE_METRICS_REPORT.equalsIgnoreCase(argMetricsfile)
				&&  !METRICS_FILE_SPLIT_BY_DATATYPE.equalsIgnoreCase(argMetricsfile)) {
			throwRuntimeError(options, "The metricsfile (-m) arg must be " + METRICS_FILE_NO + ", " + METRICS_FILE_CREATE_METRICS_REPORT + ", or  " + METRICS_FILE_SPLIT_BY_DATATYPE );   
		}
		
		System.out.println();
		System.out.println("JmterResultsConverter  executing using the following arguments " );
		System.out.println("-------------------------------------------------------------- " );	
		System.out.println(" inputdirectory        : " + argInputdirectory );				    
		System.out.println(" outputdirectoy        : " + argOutputdirectoy );
		System.out.println(" outputFilename        : " + argOutputFilename );
		System.out.println(" errortransactions     : " + argErrortransactions );		
		System.out.println(" eXcludeResultsWithSub : " + argeXcludeResultsWithSub );		
		System.out.println(" metricsfile           : " + argMetricsfile );				
		System.out.println("-------------------------------------------------------------- " );			    
		System.out.println();
	}	
	
	

	private void throwRuntimeError(Options options, String error) {
		new HelpFormatter().printHelp( "JmterResultsConverter", options );
		printSampleUsage();
		throw new RuntimeException(error); 
	}



	private void printSampleUsage() {
		System.out.println();	
		System.out.println( "Sample usage");
		System.out.println( "------------");
		System.out.println( "1.  Concatenate a set of Jmeter result files in D:/Jmeter_Results/MyTestApp, into a single .csv result file, output file MyTestAppJmeterResult.csv to directory D:/Jmeter_Results/MyTestApp/MERGED : ");
		System.out.println( "      java -jar ResultFilesConverter.jar -iD:\\Jmeter_Results\\MyTestApp\\ -fMyTestAppJmeterResult" );
		System.out.println( "2.  As above (but with the current directory set as D:/Jmeter_Results/MyTestApp before running), but this time split the metrics dataypes out into separate csv files, and suffix errored txns named with _ERRORED  ");
		System.out.println( "      java -jar ResultFilesConverter.jar -fMyTestAppJmeterResult -eRename -mSplitByDataType" );		
		System.out.println();
	}

	
	public void clearOutputDirectory() {
		for (File file : new File(argOutputdirectoy).listFiles()) {
			if (!file.isDirectory()) {
				file.delete();
			}
		}
	}	
	
	public int convert() throws FileNotFoundException, IOException,  ParserConfigurationException, SAXException {

		outputBaseCsvFileName = argOutputdirectoy + File.separator +  removeCsvSuffixIfEntered(argOutputFilename); 
		baseCsvFileNameWriter = initializeCsvWriter(outputBaseCsvFileName + ".csv");
		
		boolean metricsOutputCsvFilesInitialized = false;
		int sampleCount = 0;
		
		for (File jmeterResultsFile : new File(argInputdirectory).listFiles()){  
		
			if ( jmeterResultsFile.isFile() && (
					jmeterResultsFile.getName().toUpperCase().endsWith(".JTL") || 
					jmeterResultsFile.getName().toUpperCase().endsWith(".XML") || 
					jmeterResultsFile.getName().toUpperCase().endsWith(".CSV"))){
				
				if (!metricsOutputCsvFilesInitialized) {
					metricsOutputCsvFilesInitialized = true;
					initializeCsvMetricWriters(outputBaseCsvFileName);
				}				
				
				try {
					sampleCount = sampleCount + convertaJmeterFile(jmeterResultsFile);
				} catch (IOException e) {
					System.out.println( "Error: problem with processing Jmeter results file transactions " + jmeterResultsFile.getName() );
					e.printStackTrace();
				}
				
			} else {
				System.out.println( "\n   " + jmeterResultsFile.getName() + " bypassed"  ); 
			}
		}

		baseCsvFileNameWriter.close();
	    if (metricsOutputCsvFilesInitialized) {
	    	closeCsvMetricWriters();
	    }
	    System.out.println("____________________________________" );
	    System.out.println(sampleCount + " Total samples written" );
	    System.out.println(" " );	    
	    return sampleCount;
	}

	
	/**
	 * A validly name named jmeter results file is expected to be passed for conversion, now need determine the data format 
	 * 
	 * @param jmeterResultsFile
	 * @throws IOException
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	private int convertaJmeterFile(File jmeterResultsFile) throws IOException, ParserConfigurationException, SAXException {
		
		BufferedReader brOneLine = new BufferedReader(new FileReader(jmeterResultsFile));
		String firstLineOfFile = brOneLine.readLine();
		brOneLine.close();
		
		if (firstLineOfFile == null) {
			System.out.println("   Warning : " + jmeterResultsFile.getName() + " bypassed - empty file" );
			return 0;
		
		} else  if (firstLineOfFile.trim().startsWith("<")) {
			return convertXMLFile(jmeterResultsFile);
			
		} else if (firstLineOfFile.trim().startsWith("timeStamp") &&  firstLineOfFile.matches("timeStamp.elapsed.*")){	
			return reformatCSVFile(jmeterResultsFile, true);
			
		} else if ( firstLineOfFile.length() > 28 && countMatches(firstLineOfFile, ",") > 14  && firstLineOfFile.indexOf(",") == 13   ){  //assuming a headerless CSV file in default layout	
			return reformatCSVFile(jmeterResultsFile, false);
			
		} else {
			System.out.println("   Warning : " + jmeterResultsFile.getName() + " bypassed - not in expected Jmeter results format. (Does not start with regex 'timeStamp.elapsed' (csv) or '<' (xml))" );
			return 0;
		}

	}
	
	
	private String removeCsvSuffixIfEntered(String argOutputFilename) {
		if ( argOutputFilename.toUpperCase().endsWith(".CSV")){
			return argOutputFilename.substring(0, argOutputFilename.length()-4);
		}
		return argOutputFilename;
	}


	private void initializeCsvMetricWriters(String outputBaseCsvFileName) {
		
		if (METRICS_FILE_CREATE_METRICS_REPORT.equalsIgnoreCase(argMetricsfile)) {
			metrics_CsvFileNameWriter  	= initializeCsvWriter(outputBaseCsvFileName + "_METRICS.csv");

		} else if (METRICS_FILE_SPLIT_BY_DATATYPE.equalsIgnoreCase(argMetricsfile)) {
			cpu_util_CsvFileNameWriter 	= initializeCsvWriter(outputBaseCsvFileName + "_" + OutputDatatypes.CPU_UTIL.name() + ".csv");		
			datapoint_CsvFileNameWriter	= initializeCsvWriter(outputBaseCsvFileName + "_" + OutputDatatypes.DATAPOINT.name() + ".csv");		
			memory_CsvFileNameWriter 	= initializeCsvWriter(outputBaseCsvFileName + "_" + OutputDatatypes.MEMORY.name() + ".csv");		
		}
	}

	
	private void closeCsvMetricWriters() throws IOException {
		
		if (METRICS_FILE_CREATE_METRICS_REPORT.equalsIgnoreCase(argMetricsfile)) {
			metrics_CsvFileNameWriter.close();

		} else if (METRICS_FILE_SPLIT_BY_DATATYPE.equalsIgnoreCase(argMetricsfile)) {
			cpu_util_CsvFileNameWriter.close();
			datapoint_CsvFileNameWriter.close();		
			memory_CsvFileNameWriter.close();		
		}
	}


	private BufferedWriter initializeCsvWriter(String csvWriterFileName) {
		System.out.println("initializeCsvWriter " + csvWriterFileName);
		
		BufferedWriter csvWriter = null;
		try {
			FileWriter fileWriter = new FileWriter(csvWriterFileName, false);
			csvWriter = new BufferedWriter(fileWriter);		
			csvWriter.write("timeStamp,elapsed,label,responseCode,responseMessage,threadName,dataType,success,failureMessage,bytes,sentBytes,grpThreads,allThreads,URL,Latency,Hostname,IdleTime,Connect");  
			csvWriter.newLine();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Was unable to open output csv file '" + csvWriterFileName + "' for output'.  \n\n    " + e.getMessage());
		}
		return csvWriter;
	}
	
	
	public int convertXMLFile(File inputXmlFileName) throws FileNotFoundException, IOException,  ParserConfigurationException, SAXException {

		long startLoadms = System.currentTimeMillis(); 
		System.out.println("\n\nProcessing Xml formatted Jmeter Results File " + inputXmlFileName.getName() + " at " + new Date(startLoadms));		
		
		int samplesCreated=0;
		BufferedReader xmlReader = new BufferedReader(new FileReader(inputXmlFileName));
		int lineCount = 0; 
		boolean isWithinASampleResult = false;
		String potentialSampleResultWithNoSubResults = null;
		
	    for (String jmeterFileLine; (jmeterFileLine = xmlReader.readLine()) != null; ){
	    	jmeterFileLine = jmeterFileLine.trim();
	    	
	    	if ( jmeterXmlLineIsAClosedSample(jmeterFileLine) ) {   
	    		samplesCreated = samplesCreated + createOutputForAnXmlDataSample(jmeterFileLine);
    			potentialSampleResultWithNoSubResults = null;
    			
	    	} else if ( jmeterXmlLineIsAnUnclosedSample(jmeterFileLine) ){   
	    		
	    		if (argeXcludeResultsWithSub.equalsIgnoreCase("FALSE") ){
	    			samplesCreated = samplesCreated + createOutputForAnXmlDataSample(jmeterFileLine);	
    			
	    		} else if (isWithinASampleResult) {
	    			samplesCreated = samplesCreated + createOutputForAnXmlDataSample(jmeterFileLine);
	    			potentialSampleResultWithNoSubResults = null;
	    			lineCount = readLinesToSubResultEndTag(xmlReader, lineCount);
	    			
	    		} else { 
	    			isWithinASampleResult = true;
	    			potentialSampleResultWithNoSubResults = jmeterFileLine;
	    		}
	    		
	    	} else if ( jmeterXmlLineIsAnEndSampleTag(jmeterFileLine) ){  
    			
	    		if (potentialSampleResultWithNoSubResults != null ){
	    			samplesCreated = samplesCreated + createOutputForAnXmlDataSample(potentialSampleResultWithNoSubResults);
	    		}	
	    		isWithinASampleResult = false;
	    		potentialSampleResultWithNoSubResults = null;
	    	}

	    	lineCountProgressDisplay(lineCount);
	    	lineCount++;		    	
	    } 
	    
		long endLoadms = System.currentTimeMillis(); 	    
		System.out.println("\n   " + inputXmlFileName.getName() + " processing completed  at " +  new Date(endLoadms) + " :" );
	    System.out.println("        " + lineCount + " file lines processed" );
	    System.out.println("        " + samplesCreated + " transaction samples loaded" );
		System.out.println("        took " +  (endLoadms -startLoadms)/1000 + " secs" );	    
		System.out.println();
	    
	    xmlReader.close();
	    return samplesCreated;
	}


	private boolean jmeterXmlLineIsAClosedSample(String trimmedJmeterFileLine) {
		boolean isUnclosedSample = jmeterXmlLineIsASample(trimmedJmeterFileLine) && trimmedJmeterFileLine.endsWith("/>") ?true:false;	
		return isUnclosedSample;
	}
	
	private boolean jmeterXmlLineIsAnUnclosedSample(String trimmedJmeterFileLine) {
		boolean isUnclosedSample = jmeterXmlLineIsASample(trimmedJmeterFileLine) && trimmedJmeterFileLine.endsWith("\">") ?true:false;	
		return isUnclosedSample;
	}

	private boolean jmeterXmlLineIsASample(String trimmedJmeterFileLine) {
		boolean isSample = trimmedJmeterFileLine.startsWith("<sample") || trimmedJmeterFileLine.startsWith("<httpSample") ?true:false;	
		return isSample;
	}
	
	private boolean jmeterXmlLineIsAnEndSampleTag(String trimmedJmeterFileLine) {
		boolean isSample = trimmedJmeterFileLine.startsWith("</sample>") || trimmedJmeterFileLine.startsWith("</httpSample>") ?true:false;	
		return isSample;
	}	

	private int readLinesToSubResultEndTag(BufferedReader xmlReader, int lineCount) throws IOException {
	    for (String jmeterFileLine; (jmeterFileLine = xmlReader.readLine()) != null; ){
	    	lineCountProgressDisplay(lineCount);
	    	lineCount++;
	    	jmeterFileLine = jmeterFileLine.trim();
	    	if (jmeterXmlLineIsAnEndSampleTag(jmeterFileLine)) {
	    		return lineCount;
	    	}			
	    }	
	    throw new RuntimeException("Read to end of file when looking for and sub-result end tag!!!"  );
	}

	private void lineCountProgressDisplay(int lineCount) {
		if ( (lineCount % 1000 )   == 0 ){	System.out.print("^");};
		if ( (lineCount % 100000 ) == 0 ){	System.out.println();};
	}
	
		
	private int createOutputForAnXmlDataSample(String jmeterFileLine) throws ParserConfigurationException, SAXException, IOException {
		int samplesCreatedForLine=0; 
		
		if ( ! jmeterFileLine.trim().endsWith("/>")) {
    		//need the line to complete with /> instead of > in order to parse as xml
    		jmeterFileLine = jmeterFileLine.trim().substring(0, jmeterFileLine.trim().length() -1 ) + "/>";
		}
//		System.out.println(" xml line = " + xmlLine  );	

		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		InputSource src = new InputSource();
		src.setCharacterStream(new StringReader(jmeterFileLine));
		
		Document document = builder.parse(src);			

		NodeList nodeList;
		if ( jmeterFileLine.trim().startsWith("<sample")){
			nodeList = document.getElementsByTagName("sample");
		} else {
			nodeList = document.getElementsByTagName("httpSample");
		}

		Node sampleNode = nodeList.item(0);
		NamedNodeMap nodeItems = sampleNode.getAttributes();
		
		String transactionNameLabel = nodeItems.getNamedItem("lb").getNodeValue();
		String failureMessage = "";
		String success = nodeItems.getNamedItem("s").getNodeValue();
		String inputDatatype = nodeItems.getNamedItem("dt").getNodeValue();
		
		if (! "true".equalsIgnoreCase(success)) {
			// the response message is used as the failure message 
			if  (nodeItems.getNamedItem("rm") != null) {
				failureMessage = nodeItems.getNamedItem("rm").getNodeValue(); 
			}
		}

		if (!transactionNameLabel.startsWith(IGNORE)){
			
			System.arraycopy(blankLine, 0, nextLine, 0, blankLine.length);

			nextLine[0]  =  nodeItems.getNamedItem("ts").getNodeValue();   
			nextLine[1]  =  nodeItems.getNamedItem("t").getNodeValue(); 
			nextLine[2]  =  transactionNameLabel;
			if (nodeItems.getNamedItem("rc")!=null) nextLine[3]  = nodeItems.getNamedItem("rc").getNodeValue(); 
			if (nodeItems.getNamedItem("rm")!=null) nextLine[4]  = nodeItems.getNamedItem("rm").getNodeValue();
			if (nodeItems.getNamedItem("tn")!=null) nextLine[5]  = nodeItems.getNamedItem("tn").getNodeValue(); 			
			nextLine[6]  =  inputDatatype; 
			nextLine[7]  =  success;
			nextLine[8]  =  failureMessage;
			if (nodeItems.getNamedItem("by")!=null) nextLine[9]  = nodeItems.getNamedItem("by").getNodeValue(); 				
			if (nodeItems.getNamedItem("sby")!=null) nextLine[10] = nodeItems.getNamedItem("sby").getNodeValue(); 				
			if (nodeItems.getNamedItem("ng")!=null) nextLine[11] = nodeItems.getNamedItem("ng").getNodeValue(); 				
			if (nodeItems.getNamedItem("na")!=null) nextLine[12] = nodeItems.getNamedItem("na").getNodeValue(); 				
			if (nodeItems.getNamedItem("by")!=null) nextLine[13] = "";  // URL 				
			if (nodeItems.getNamedItem("lt")!=null) nextLine[14] = nodeItems.getNamedItem("lt").getNodeValue(); 				
			if (nodeItems.getNamedItem("hn")!=null) nextLine[15] = nodeItems.getNamedItem("hn").getNodeValue(); 				
			if (nodeItems.getNamedItem("it")!=null) nextLine[16] = nodeItems.getNamedItem("it").getNodeValue(); 				
			if (nodeItems.getNamedItem("ct")!=null) nextLine[17] = nodeItems.getNamedItem("ct").getNodeValue(); 				
			
			if ("true".equalsIgnoreCase(success) || ERROR_TXNS_NO.equalsIgnoreCase(argErrortransactions)) {
				writeCsvOuptput(inputDatatype, writeNext(nextLine) );	
				samplesCreatedForLine++;
			
			} else {   // an error transaction that needs handling 

				nextLine[2]  =  transactionNameLabel + "_ERRORED";		    			
				writeCsvOuptput(inputDatatype, writeNext(nextLine) );
				samplesCreatedForLine++;
				
				if (ERROR_TXNS_DUPLICATE.equalsIgnoreCase(argErrortransactions)) {
					nextLine[2]  =  transactionNameLabel;		    			
					writeCsvOuptput(inputDatatype, writeNext(nextLine) );
					samplesCreatedForLine++;
				} 
			}

		} 
		return samplesCreatedForLine;
	}



	/**
	 * CSV files are treated in a similarly to XML, without the complexity of dealing with sub-transactions 
	 * (each line is considered a transaction unless it appears to be invalid, in which case it is just bypassed) 
	 * 
	 * @param inputCsvFileName
	 * @throws IOException
	 */
	private int reformatCSVFile(File inputCsvFileName, boolean hasHeader) throws IOException {
		
		int samplesCreated=0; 
		BufferedReader csvReader = new BufferedReader(new FileReader(inputCsvFileName));
		int lineCount = 0; 
		String csvDelimiter= ",";
		
		long startLoadms = System.currentTimeMillis(); 
		System.out.println("\n\nProcessing CSV formatted Jmeter Results File " + inputCsvFileName.getName() + " at " + new Date(startLoadms));					
		
		if (hasHeader) { 
			String csvHeader = csvReader.readLine();
			csvDelimiter = substringBetween(csvHeader, "timeStamp", "elapsed");
			
			List<String> csvHeaderFieldsList =  Arrays.asList(csvHeader.trim().split(Pattern.quote(csvDelimiter)));  
			fieldPostimeStamp 		= csvHeaderFieldsList.indexOf("timeStamp"); 
			fieldPoselapsed   		= csvHeaderFieldsList.indexOf("elapsed"); 
			fieldPoslabel     		= csvHeaderFieldsList.indexOf("label");
			fieldPosresponseCode  	= csvHeaderFieldsList.indexOf("responseCode"); 
			fieldPosresponseMessage	= csvHeaderFieldsList.indexOf("responseMessage"); 		
			fieldPosthreadName  	= csvHeaderFieldsList.indexOf("threadName");		
			fieldPosdataType  		= csvHeaderFieldsList.indexOf("dataType"); 
			fieldPossuccess   		= csvHeaderFieldsList.indexOf("success"); 
			fieldPosfailureMessage 	= csvHeaderFieldsList.indexOf("failureMessage"); 		
			fieldPosbytes  			= csvHeaderFieldsList.indexOf("bytes"); 		
			fieldPossentBytes  		= csvHeaderFieldsList.indexOf("sentBytes"); 		
			fieldPosgrpThreads  	= csvHeaderFieldsList.indexOf("grpThreads"); 		
			fieldPosallThreads  	= csvHeaderFieldsList.indexOf("allThreads"); 
			fieldPosURL				= csvHeaderFieldsList.indexOf("URL");
			fieldPosLatency 		= csvHeaderFieldsList.indexOf("Latency"); 		
			fieldPosHostname  		= csvHeaderFieldsList.indexOf("Hostname"); 		
			fieldPosIdleTime  		= csvHeaderFieldsList.indexOf("IdleTime"); 		
			fieldPosConnect  		= csvHeaderFieldsList.indexOf("Connect"); 		
			
			if (fieldPostimeStamp==-1 || fieldPoselapsed==-1 || fieldPoslabel==-1 || fieldPosdataType==-1 || fieldPossuccess==-1 ){
				System.out.println("\n   Severe Error.  Unexpected csv file header format, terminating run");
				System.out.println("   - the header is expected to contain at least these field names:  timeStamp, elapsed, label, dataType, success\n");
				csvReader.close();
				throw new RuntimeException("Error : Unexpected csv file header format for file " + inputCsvFileName.getName());
			}
		
		} else {
			setFieldPositionsAssumingTheDefaultCsvLayout();
		}
		
	    for (String csvDataLine; (csvDataLine = csvReader.readLine()) != null; ) {
    	
	    	long approxFieldCount =  countMatches(csvDataLine, csvDelimiter);

			if  ( !(  (csvDataLine.trim().length() < 17 )  || approxFieldCount < 4 ||  (csvDataLine.trim().indexOf(",") != 13) )){
				//would be too short,  not enough fields or first field cannot be a time stamp .. so bypass 		
		    	//format all csv files the same way (the header is always the same) ...
		    	
		    	String[] csvDataLineFields =  csvDataLine.trim().split(Pattern.quote(csvDelimiter));  
	    		String transactionNameLabel = csvDataLineFields[fieldPoslabel];
	    		String inputDatatype 		= csvDataLineFields[fieldPosdataType];
	    		String success 				= csvDataLineFields[fieldPossuccess];

	    		if ( ! (transactionNameLabel.startsWith(IGNORE) || 
	    			    inputDatatype.equals(OutputDatatypes.PARENT.getOutputDatatypeText()) && argeXcludeResultsWithSub.equalsIgnoreCase("TRUE")) ){
    			
	    			System.arraycopy(blankLine, 0, nextLine, 0, blankLine.length);
    			
		    		nextLine[0]  =  csvDataLineFields[fieldPostimeStamp];
		    		nextLine[1]  =  csvDataLineFields[fieldPoselapsed]; 
		    		nextLine[2]  =  transactionNameLabel;
		    		if (fieldPosresponseCode>0) 	nextLine[3]  =  csvDataLineFields[fieldPosresponseCode]; 
		    		if (fieldPosresponseMessage>0) 	nextLine[4]  =  csvDataLineFields[fieldPosresponseMessage]; 
		    		if (fieldPosthreadName>0) 		nextLine[5]  =  csvDataLineFields[fieldPosthreadName];  
		    		nextLine[6]  =  inputDatatype; 
		    		nextLine[7]  =  success;
		    		if (fieldPosfailureMessage>0) 	nextLine[8]  =  csvDataLineFields[fieldPosfailureMessage];
		    		if (fieldPosbytes>0) 			nextLine[9]  =  csvDataLineFields[fieldPosbytes];
		    		if (fieldPossentBytes>0) 		nextLine[10] =  csvDataLineFields[fieldPossentBytes];
		    		if (fieldPosgrpThreads>0) 		nextLine[11] =  csvDataLineFields[fieldPosgrpThreads];
		    		if (fieldPosallThreads>0) 		nextLine[12] =  csvDataLineFields[fieldPosallThreads]; 
		    		if (fieldPosURL>0) 				nextLine[13] =  csvDataLineFields[fieldPosURL]; 	    		
		    		if (fieldPosLatency>0) 			nextLine[14] =  csvDataLineFields[fieldPosLatency]; 
		    		if (fieldPosHostname>0) 		nextLine[15] =  csvDataLineFields[fieldPosHostname]; 		    		
		    		if (fieldPosIdleTime>0) 		nextLine[16] =  csvDataLineFields[fieldPosIdleTime]; 
		    		if (fieldPosConnect>0) 			nextLine[17] =  csvDataLineFields[fieldPosConnect]; 
		    		
					if ("true".equalsIgnoreCase(success) || ERROR_TXNS_NO.equalsIgnoreCase(argErrortransactions)) {
						writeCsvOuptput(inputDatatype, writeNext(nextLine) );	
						samplesCreated++;
					
					} else {   // an error transaction that needs handling 

						nextLine[2]  =  transactionNameLabel + "_ERRORED";		    			
						writeCsvOuptput(inputDatatype, writeNext(nextLine) );
						samplesCreated++;
						
						if (ERROR_TXNS_DUPLICATE.equalsIgnoreCase(argErrortransactions)) {
							nextLine[2]  =  transactionNameLabel;		    			
							writeCsvOuptput(inputDatatype, writeNext(nextLine) );
							samplesCreated++;
						} 
					}
		    	}
	    	} 
			
	    	lineCountProgressDisplay(lineCount);
	    	lineCount++;	
			
	    } // end for loop
	    
		long endLoadms = System.currentTimeMillis(); 	    
		System.out.println("\n   " + inputCsvFileName.getName() + " processing completed  at " +  new Date(endLoadms) + " :" );
	    System.out.println("        " + lineCount + " file lines processed" );
	    System.out.println("        " + samplesCreated + " transaction samples loaded" );
		System.out.println("        took " +  (endLoadms - startLoadms)/1000 + " secs" );	    
		System.out.println();
   
	    csvReader.close();
		return samplesCreated;
    }
	
	private void setFieldPositionsAssumingTheDefaultCsvLayout() {
		System.out.println("\n   This file is assumed to be a CSV file WITHOUT A HEADER.  Therefore the default Jmeter CSV field layout has to be assumed:" );
		System.out.println("\n       timeStamp,elapsed,label,responseCode,responseMessage,threadName,dataType,success,failureMessage,bytes,sentBytes,grpThreads,allThreads,URL,Latency,IdleTime,Connect");
		fieldPostimeStamp 		= 0; 
		fieldPoselapsed   		= 1; 
		fieldPoslabel     		= 2;
		fieldPosresponseCode  	= 3; 
		fieldPosresponseMessage	= 4; 		
		fieldPosthreadName  	= 5;		
		fieldPosdataType  		= 6; 
		fieldPossuccess   		= 7; 
		fieldPosfailureMessage 	= 8; 		
		fieldPosbytes  			= 9; 		
		fieldPossentBytes  		= 10; 		
		fieldPosgrpThreads  	= 11; 		
		fieldPosallThreads  	= 12;
		fieldPosURL			  	= 13;
		fieldPosLatency 		= 14; 
		fieldPosHostname 		= -1; 			
		fieldPosIdleTime  		= 15; 		
		fieldPosConnect  		= 16; 		
	}

	private void writeCsvOuptput(String inputDatatype, String csvDataLine) throws IOException {
//		System.out.println("writeCsvOuptput  inputDatatype=" + inputDatatype + ", csvDataLine=" + csvDataLine);
		
		if (inputDatatype.equals(OutputDatatypes.TRANSACTION.getOutputDatatypeText())) {
			baseCsvFileNameWriter.write( csvDataLine );
			return;
		}
		
		if (METRICS_FILE_NO.equalsIgnoreCase(argMetricsfile)) {
			baseCsvFileNameWriter.write( csvDataLine );
			return;
		}	
		
		if (METRICS_FILE_CREATE_METRICS_REPORT.equalsIgnoreCase(argMetricsfile)) {
			if ( inputDatatype.equals(OutputDatatypes.CPU_UTIL.getOutputDatatypeText()) || 
				 inputDatatype.equals(OutputDatatypes.DATAPOINT.getOutputDatatypeText()) || 
				 inputDatatype.equals(OutputDatatypes.MEMORY.getOutputDatatypeText()) ) {
				metrics_CsvFileNameWriter.write(csvDataLine);  
			} else {											//assume its a (non-metric) normal Transaction
				baseCsvFileNameWriter.write( csvDataLine );
			}	
			return;
		}
		
		if (METRICS_FILE_SPLIT_BY_DATATYPE.equalsIgnoreCase(argMetricsfile)) {
			if ( inputDatatype.equals(OutputDatatypes.CPU_UTIL.getOutputDatatypeText() )) {
				cpu_util_CsvFileNameWriter.write( csvDataLine );
			} else if ( inputDatatype.equals(OutputDatatypes.DATAPOINT.getOutputDatatypeText() )) {
				datapoint_CsvFileNameWriter.write( csvDataLine );
			} else if ( inputDatatype.equals(OutputDatatypes.MEMORY.getOutputDatatypeText() )) {
				memory_CsvFileNameWriter.write( csvDataLine );
			} else {											//assume its a (non-metric) normal Transaction
				baseCsvFileNameWriter.write( csvDataLine );
			}	
			return;
		}
		
		throw new RuntimeException("Logic error in writeCsvOuptput : " + inputDatatype + ", argMetricsfile=" + argMetricsfile + ", csvDataLine=" + csvDataLine); 
	}	

	
	
	/*
	 *   The methods below are based on code in openCSV   
	 */
	
	/**
	 * Formats a CSV line, ready to write to file. This method is a fail-fast method
	 * that will throw the IOException of the writer supplied to the CSVWriter (if
	 * the Writer does not handle the exceptions itself like the PrintWriter class).
	 *
	 * @param nextLine
	 *            a string array with each comma-separated element as a separate
	 *            entry.
	 * @throws IOException
	 */
	private String writeNext(String[] nextLine) throws IOException {

		Appendable appendable = new StringBuilder(INITIAL_STRING_SIZE);

		if (nextLine == null) {
			return null;
		}

		for (int i = 0; i < nextLine.length; i++) {

			if (i != 0) {
				appendable.append(DEFAULT_SEPARATOR);
			}

			String nextElement = nextLine[i];

			if (nextElement == null) {
				continue;
			}

			Boolean stringContainsSpecialCharacters = stringContainsSpecialCharacters(nextElement);

			appendQuoteCharacterIfNeeded(false, appendable, stringContainsSpecialCharacters);

			if (stringContainsSpecialCharacters) {
				processLine(nextElement, appendable);
			} else {
				appendable.append(nextElement);
			}

			appendQuoteCharacterIfNeeded(false, appendable, stringContainsSpecialCharacters);
		}

		appendable.append(DEFAULT_LINE_END);
		return appendable.toString();
	}
	
	private void appendQuoteCharacterIfNeeded(boolean applyQuotesToAll, Appendable appendable,	Boolean stringContainsSpecialCharacters) throws IOException {
		if ((applyQuotesToAll || stringContainsSpecialCharacters)) {
			appendable.append(DEFAULT_QUOTE_CHARACTER);
		}
	}	
	
	
	/**
	 * Checks to see if the line contains special characters.
	 * 
	 * @param line           Element of data to check for special characters.
	 * @return True if the line contains the quote, escape, separator, newline, or   return.
	 */
	protected boolean stringContainsSpecialCharacters(String line) {
		return line.indexOf(DEFAULT_QUOTE_CHARACTER) != -1 || line.indexOf(DEFAULT_ESCAPE_CHARACTER) != -1
				|| line.indexOf(DEFAULT_SEPARATOR) != -1 || line.contains(DEFAULT_LINE_END) || line.contains("\r");
	}

	/**
	 * Processes all the characters in a line.
	 * 
	 * @param nextElement  - Element to process.
	 * @param appendable   - Appendable holding the processed data.
	 * @throws IOException - IOException thrown by the writer supplied to the CSVWriter
	 */
	protected void processLine(String nextElement, Appendable appendable) throws IOException {
		for (int j = 0; j < nextElement.length(); j++) {
			char nextChar = nextElement.charAt(j);
			processCharacter(appendable, nextChar);
		}
	}

	/**
	 * Appends the character to the StringBuilder adding the escape character if
	 * needed.
	 * 
	 * @param appendable    - Appendable holding the processed data.
	 * @param nextChar        Character to process
	 * @throws IOException  - IOException thrown by the writer supplied to the CSVWriter.
	 */
	protected void processCharacter(Appendable appendable, char nextChar) throws IOException {
		if (checkCharactersToEscape(nextChar)) {
			appendable.append(DEFAULT_ESCAPE_CHARACTER);
		}
		appendable.append(nextChar);
	}

	/**
	 * Checks whether the next character that is to be written out is a special
	 * character that must be quoted. The quote character, escape character, and
	 * separator are special characters.
	 *
	 * @param nextChar   The next character to be written
	 * @return Whether the character needs to be quoted or not
	 */
	protected boolean checkCharactersToEscape(char nextChar) {
		return (nextChar == DEFAULT_QUOTE_CHARACTER || nextChar == DEFAULT_ESCAPE_CHARACTER);
	}	
	
	
	/*
	 *   The methods below are based on code in Commons StringUtils  
	 */

	
    /**
     * <p>Gets the String that is nested in between two Strings.
     * Only the first match is returned.</p>
     * @param str  the String containing the substring, may be null
     * @param open  the String before the substring, may be null
     * @param close  the String after the substring, may be null
     * @return the substring, {@code null} if no match
     */
    public static String substringBetween(final String str, final String open, final String close) {
        final int start = str.indexOf(open);
        if (start != INDEX_NOT_FOUND) {
            final int end = str.indexOf(close, start + open.length());
            if (end != INDEX_NOT_FOUND) {
                return str.substring(start + open.length(), end);
            }
        }
        return null;
    }
	
	
	
    /**
     * <p>Counts how many times the substring appears in the larger string.</p>
     */
    private static int countMatches(CharSequence str, CharSequence sub) {
        if (str == null || str.length() == 0) {   //assuming passed charSequence is not a null
            return 0;
        }
        int count = 0;
        int idx = 0;
        while ((idx = str.toString().indexOf(sub.toString(), idx)) != INDEX_NOT_FOUND) {
            count++;
            idx += sub.length();
        }
        return count;
    }
 
	
    public static void main( String[] args ) throws IOException, SAXException, ParserConfigurationException
    {
        System.out.println( "Result Files Converter starting .. (v2.2.0) " );

//        for a quick and dirty test ...
//        args = new String[]{"-i", "C:/Jmeter_Results/myapp", "-f", "myapp_TestResults_converted.csv", "-m", "SplitByDataType", "-e", "No", "-x", "True" };
        
		ResultFilesConverter resultFilesConverter = new ResultFilesConverter(); 
		resultFilesConverter.parseArguments(args);    
		resultFilesConverter.clearOutputDirectory();     
        resultFilesConverter.convert();
        
        System.out.println();
        System.out.println( "Result Files Converter completed." );        
    }
	
}
