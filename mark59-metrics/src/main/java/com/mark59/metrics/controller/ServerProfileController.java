/*
 *  Copyright 2019 Mark59.com
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

package com.mark59.metrics.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.mark59.metrics.PropertiesConfiguration;
import com.mark59.metrics.data.base.dao.BaseDAO;
import com.mark59.metrics.data.beans.Command;
import com.mark59.metrics.data.beans.CommandParserLink;
import com.mark59.metrics.data.beans.CommandResponseParser;
import com.mark59.metrics.data.beans.ServerCommandLink;
import com.mark59.metrics.data.beans.ServerProfile;
import com.mark59.metrics.data.beans.ServerProfileWithCommandLinks;
import com.mark59.metrics.data.commandResponseParsers.dao.CommandResponseParsersDAO;
import com.mark59.metrics.data.commandparserlinks.dao.CommandParserLinksDAO;
import com.mark59.metrics.data.commands.dao.CommandsDAO;
import com.mark59.metrics.data.servercommandlinks.dao.ServerCommandLinksDAO;
import com.mark59.metrics.data.serverprofiles.dao.ServerProfilesDAO;
import com.mark59.metrics.forms.CommandParameter;
import com.mark59.metrics.forms.CommandSelector;
import com.mark59.metrics.forms.ServerProfileEditingForm;
import com.mark59.metrics.utils.MetricsConstants;
import com.mark59.metrics.utils.MetricsConstants.CommandExecutorDatatypes;

/**
 * @author Philip Webb
 * Written: Australian Summer 2020  
 */

@Controller
public class ServerProfileController {
	
	@Autowired
	ServerProfilesDAO serverProfilesDAO; 
	
	@Autowired
	ServerCommandLinksDAO  serverCommandLinksDAO; 
	
	@Autowired
	CommandsDAO commandsDAO; 

	@Autowired
	CommandParserLinksDAO commandParserLinksDAO; 
	
	@Autowired
	CommandResponseParsersDAO commandResponseParsersDAO; 
	
	@Autowired
	BaseDAO baseDAO;
	
	@Autowired
	PropertiesConfiguration springBootConfiguration;	
	
	
	@GetMapping("/downloadServerProfiles")
	public ResponseEntity<ByteArrayResource> downloadServerProfiles() {
		try {
			HttpHeaders httpHeaders = new HttpHeaders();
			httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			httpHeaders.set(HttpHeaders.CONTENT_DISPOSITION,
							"attachment; filename=" + MetricsConstants.MARK59_SERVER_PROFILES_EXCEL_FILE);
			ByteArrayOutputStream stream = serverProfilesToExcelFile();
			return new ResponseEntity<>(new ByteArrayResource(stream.toByteArray()), httpHeaders, HttpStatus.CREATED);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}	
	
	
	public ByteArrayOutputStream serverProfilesToExcelFile() throws IOException {
		
		Workbook workbook = new XSSFWorkbook();
		CellStyle headerCellStyle = workbook.createCellStyle();
		headerCellStyle.setFillForegroundColor(IndexedColors.DARK_RED.getIndex());
		Font headerFont = workbook.createFont();
		headerFont.setColor(IndexedColors.WHITE.getIndex());
		headerFont.setBold(true); // sometimes causing display of dup cols in header??
		headerCellStyle.setFont(headerFont);
		headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		
		Sheet sheet = workbook.createSheet("SERVERPROFILES");

		DataFormat fmt = workbook.createDataFormat();
		CellStyle textStyle = workbook.createCellStyle();
		textStyle.setDataFormat(fmt.getFormat("@"));
		sheet.setDefaultColumnStyle(0, textStyle);
		
		int columnCount = createHeaderRow(sheet, headerCellStyle, "SERVERPROFILES");
		int rowIndex = 1;
		List<ServerProfile> serverProfiles = serverProfilesDAO.findServerProfiles();
		
		for (ServerProfile serverProfile : serverProfiles) {
			Row dataRow = sheet.createRow(rowIndex);
			dataRow.createCell(0).setCellValue(serverProfile.getServerProfileName());			
			dataRow.createCell(1).setCellValue(serverProfile.getExecutor());
			dataRow.createCell(2).setCellValue(serverProfile.getServer());
			dataRow.createCell(3).setCellValue(serverProfile.getAlternativeServerId());
			dataRow.createCell(4).setCellValue(serverProfile.getUsername());
			dataRow.createCell(5).setCellValue(serverProfile.getPassword());
			dataRow.createCell(6).setCellValue(serverProfile.getPasswordCipher());
			dataRow.createCell(7).setCellValue(serverProfile.getConnectionPort());
			dataRow.createCell(8).setCellValue(serverProfile.getConnectionTimeout());
			dataRow.createCell(9).setCellValue(serverProfile.getComment());
			dataRow.createCell(10).setCellValue(serverProfilesDAO.serializeMapToJson(serverProfile.getParameters()));
			rowIndex++;
		}
		autoSizeColumns(sheet, columnCount);
		
		sheet = workbook.createSheet("SERVERCOMMANDLINKS");
		columnCount = createHeaderRow(sheet, headerCellStyle, "SERVERCOMMANDLINKS");
		rowIndex = 1;
		List<ServerCommandLink> serverCommandLinks = serverCommandLinksDAO.findServerCommandLinks();
		for (ServerCommandLink serverCommandLink : serverCommandLinks) {
			Row dataRow = sheet.createRow(rowIndex);
			dataRow.createCell(0).setCellValue(serverCommandLink.getServerProfileName());
			dataRow.createCell(1).setCellValue(serverCommandLink.getCommandName());
			rowIndex++;
		}
		autoSizeColumns(sheet, columnCount);
		
		sheet = workbook.createSheet("COMMANDS");
		columnCount = createHeaderRow(sheet, headerCellStyle, "COMMANDS");
		rowIndex = 1;
		List<Command> commands = commandsDAO.findCommands() ;
		for (Command command : commands) {
			Row dataRow = sheet.createRow(rowIndex);
			dataRow.createCell(0).setCellValue(command.getCommandName());
			dataRow.createCell(1).setCellValue(command.getExecutor());
			dataRow.createCell(2).setCellValue(command.getCommand());
			dataRow.createCell(3).setCellValue(command.getIngoreStderr());
			dataRow.createCell(4).setCellValue(command.getComment());
			dataRow.createCell(5).setCellValue(commandsDAO.serializeListToJson(command.getParamNames()));
			rowIndex++;
		}
		autoSizeColumns(sheet, columnCount);
	
		sheet = workbook.createSheet("COMMANDPARSERLINKS");
		columnCount = createHeaderRow(sheet, headerCellStyle, "COMMANDPARSERLINKS");
		rowIndex = 1;
		List<CommandParserLink> commandparserlinks = commandParserLinksDAO.findCommandParserLinks();
		for (CommandParserLink commandParserLink : commandparserlinks) {
			Row dataRow = sheet.createRow(rowIndex);
			dataRow.createCell(0).setCellValue(commandParserLink.getCommandName());
			dataRow.createCell(1).setCellValue(commandParserLink.getParserName());
			rowIndex++;
		}
		autoSizeColumns(sheet, columnCount);
	
		sheet = workbook.createSheet("COMMANDRESPONSEPARSERS");
		columnCount = createHeaderRow(sheet, headerCellStyle, "COMMANDRESPONSEPARSERS");
		rowIndex = 1;
		List<CommandResponseParser> commandResponseParsers = commandResponseParsersDAO.findCommandResponseParsers();
		for (CommandResponseParser commandResponseParser : commandResponseParsers) {
			Row dataRow = sheet.createRow(rowIndex);
			dataRow.createCell(0).setCellValue(commandResponseParser.getParserName());
			dataRow.createCell(1).setCellValue(commandResponseParser.getMetricTxnType());
			dataRow.createCell(2).setCellValue(commandResponseParser.getMetricNameSuffix());
			dataRow.createCell(3).setCellValue(commandResponseParser.getScript());
			dataRow.createCell(4).setCellValue(commandResponseParser.getComment());
			dataRow.createCell(5).setCellValue(commandResponseParser.getSampleCommandResponse());
			rowIndex++;
		}
		autoSizeColumns(sheet, columnCount);
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		workbook.write(outputStream);
		workbook.close();
		return outputStream;        
	}	
		
	private int createHeaderRow(Sheet sheet, CellStyle headerCellStyle, String tableName) {
		Row headerRow = sheet.createRow(0);
		List<String> columnNames = baseDAO.findColumnNamesForTable(tableName.toUpperCase()) ;

		for (int i = 0; i < columnNames.size(); i++) {
			Cell cell = headerRow.createCell(i);
			cell.setCellValue(columnNames.get(i));
			cell.setCellStyle(headerCellStyle);
		}
		return columnNames.size();
	}
		
	private void autoSizeColumns(Sheet sheet, int columnCount) {
		for (int i = 0; i < columnCount; i++) {
			sheet.autoSizeColumn(i);
		}
	}
	

	@RequestMapping("/registerServerProfile")
	public ModelAndView registerServerProfile(@RequestParam(required=false) String reqExecutor, @RequestParam(required=false) String reqErr, 
			@ModelAttribute ServerProfileEditingForm serverProfileEditingForm) { 
		ServerProfile serverProfile = new ServerProfile();
		serverProfile.setExecutor(reqExecutor);
		serverProfileEditingForm.setServerProfile(serverProfile);		
		serverProfileEditingForm.setCommandSelectors(new ArrayList<>());

		ServerProfileWithCommandLinks serverProfileWithCommandLinks = serverProfileSetSelectedCommandLinksFromDb(serverProfile);
		
		if (CommandExecutorDatatypes.GROOVY_SCRIPT.getExecutorText().equals(serverProfile.getExecutor())){
			serverProfileEditingForm.setCommandNames(createListOfAllGroovyScriptCommands());
			String selectedScriptCommandName = selectedScriptCommandName(serverProfileWithCommandLinks);		//?	
			serverProfileEditingForm.setSelectedScriptCommandName(selectedScriptCommandName);					//? 
			serverProfileEditingForm.setCommandParameters(new ArrayList<CommandParameter>());
		} else if (CommandExecutorDatatypes.SSH_LINUX_UNIX.getExecutorText().equals(serverProfile.getExecutor()) ||
					CommandExecutorDatatypes.WMIC_WINDOWS.getExecutorText().equals(serverProfile.getExecutor()) ||
					CommandExecutorDatatypes.POWERSHELL_WINDOWS.getExecutorText().equals(serverProfile.getExecutor())){  
			serverProfileEditingForm.setCommandSelectors(createListOfAllCommandSelectors(serverProfileWithCommandLinks));
		}
		Map<String, Object> map = createMapOfDropdowns();
		map.put("serverProfileEditingForm",serverProfileEditingForm);		
		map.put("reqExecutor", reqExecutor);
		return new ModelAndView("registerServerProfile", "map", map);
	}
	

	@RequestMapping("/insertServerProfile")
	public ModelAndView insertServerProfile( @RequestParam(required=false) String reqExecutor, @RequestParam(required=false) String reqErr,  
			@ModelAttribute ServerProfileEditingForm serverProfileEditingForm, Model model) {
		Map<String, Object> map = createMapOfDropdowns();			
		map.put("reqExecutor", reqExecutor);
		
		ServerProfile serverProfile = serverProfileEditingForm.getServerProfile();
		
		if (StringUtils.isBlank(serverProfile.getExecutor())){
			map.put("serverProfileEditingForm", serverProfileEditingForm);		
			map.put("reqErr", "You must select a Command Executor from the dropdown");			
			model.addAttribute("map", map);	
			return new ModelAndView("registerServerProfile", "map", map);
		}
		
		if ("true".equalsIgnoreCase(serverProfileEditingForm.getSelectedExecutorChanged())){
			serverProfileEditingForm.setSelectedExecutorChanged("false");
			serverProfileEditingForm.setSelectedScriptCommandNameChanged("false");
			serverProfileEditingForm.setSelectedScriptCommandName("");
			serverProfileEditingForm.setCommandSelectors(new ArrayList<>());
			serverProfileEditingForm.setCommandNames(new ArrayList<>());
			
			serverProfileEditingForm.setCommandParameters(new ArrayList<CommandParameter>());
			serverProfile.setParameters(new HashMap<>());

			if (CommandExecutorDatatypes.GROOVY_SCRIPT.getExecutorText().equals(serverProfile.getExecutor())){
				serverProfileEditingForm.setCommandNames(createListOfAllGroovyScriptCommands());
				//clear server related data:
				serverProfile.setServer("");
				serverProfile.setAlternativeServerId("");
				serverProfile.setUsername("");
				serverProfile.setPassword("");
				serverProfile.setPasswordCipher("");
				serverProfile.setConnectionPort("");
				serverProfile.setConnectionTimeout("");
			} else if (CommandExecutorDatatypes.SSH_LINUX_UNIX.getExecutorText().equals(serverProfile.getExecutor()) ||
						CommandExecutorDatatypes.WMIC_WINDOWS.getExecutorText().equals(serverProfile.getExecutor()) ||
						CommandExecutorDatatypes.POWERSHELL_WINDOWS.getExecutorText().equals(serverProfile.getExecutor())){ 
				ServerProfileWithCommandLinks serverProfileWithEmptyCommandLinks = new ServerProfileWithCommandLinks();
				serverProfileWithEmptyCommandLinks.setServerProfile(serverProfile);
				serverProfileWithEmptyCommandLinks.setCommandNames(new ArrayList<>());
				serverProfileEditingForm.setCommandSelectors(createListOfAllCommandSelectors(serverProfileWithEmptyCommandLinks));
				if (CommandExecutorDatatypes.SSH_LINUX_UNIX.getExecutorText().equals(serverProfile.getExecutor())){
					serverProfile.setConnectionPort("22");
					serverProfile.setConnectionTimeout("60000");
				} else {
					serverProfile.setConnectionPort("");
					serverProfile.setConnectionTimeout("");
				}
						
			} // (else do nothing if the blank executor has been selected)
			map.put("reqExecutor", reqExecutor);
			map.put("serverProfileEditingForm", serverProfileEditingForm);	
			model.addAttribute("map", map);	
			return new ModelAndView("registerServerProfile", "map", map);
		}

		List<String> selectedCommandNames = new ArrayList<>();
		if (CommandExecutorDatatypes.GROOVY_SCRIPT.getExecutorText().equals(serverProfile.getExecutor())){
			serverProfileEditingForm.setCommandNames(createListOfAllGroovyScriptCommands());
			selectedCommandNames.add(serverProfileEditingForm.getSelectedScriptCommandName());
		} else if (CommandExecutorDatatypes.SSH_LINUX_UNIX.getExecutorText().equals(serverProfile.getExecutor()) ||
			CommandExecutorDatatypes.WMIC_WINDOWS.getExecutorText().equals(serverProfile.getExecutor()) ||
			CommandExecutorDatatypes.POWERSHELL_WINDOWS.getExecutorText().equals(serverProfile.getExecutor())){ 
			selectedCommandNames = createListOfSelectedCommands(serverProfileEditingForm.getCommandSelectors());
		}

		if ("true".equalsIgnoreCase(serverProfileEditingForm.getSelectedScriptCommandNameChanged())) {
			serverProfile.setParameters(createParmsMap(serverProfileEditingForm.getCommandParameters()));
			serverProfileEditingForm.setCommandParameters(alignProfileToCurrentCommandParameters(serverProfile, selectedCommandNames));
			serverProfileEditingForm.setSelectedScriptCommandNameChanged("false");
			
			map.put("reqExecutor", reqExecutor);
			map.put("serverProfileEditingForm", serverProfileEditingForm);	
			model.addAttribute("map", map);	
			return new ModelAndView("registerServerProfile", "map", map);
		}

		serverProfile.setParameters(createParmsMap(serverProfileEditingForm.getCommandParameters()));
		
		if (StringUtils.isBlank(serverProfile.getServerProfileName())){
			map.put("serverProfileEditingForm", serverProfileEditingForm);		
			map.put("reqErr", "ServerProfile id is required");			
			model.addAttribute("map", map);	
			return new ModelAndView("registerServerProfile", "map", map);
		}
		
		ServerProfile existingServerProfile = serverProfilesDAO.findServerProfile(serverProfile.getServerProfileName());
		
		if (existingServerProfile == null ){  //not trying to add something already there, so go ahead..

			serverProfile.setParameters(createParmsMap(serverProfileEditingForm.getCommandParameters()));
			
			serverProfilesDAO.insertServerProfile(serverProfile);
			serverCommandLinksDAO.updateServerCommandLinksForServerProfileName(serverProfile.getServerProfileName(), selectedCommandNames);

			serverProfileEditingForm.setServerProfile(serverProfile);
			map.put("serverProfileEditingForm", serverProfileEditingForm);	
			model.addAttribute("map", map);	
			return new ModelAndView("viewServerProfile", "map", map);		
			
		} else {
			serverProfileEditingForm.setServerProfile(serverProfile);
			map.put("serverProfileEditingForm", serverProfileEditingForm);	
			map.put("reqErr","Oh, a listing for serverProfileName " + existingServerProfile.getServerProfileName() + " AlreadyExists");			
			model.addAttribute("map", map);	
			return new ModelAndView("registerServerProfile", "map", map);
		}
	}


	@RequestMapping("/serverProfileList")
	public ModelAndView serverProfileList(@RequestParam(required=false) String reqExecutor) {
		List<ServerProfile> serverProfileList;

		if (!StringUtils.isEmpty(reqExecutor)){
			serverProfileList = serverProfilesDAO.findServerProfiles("EXECUTOR", reqExecutor);	
		} else {
			serverProfileList = serverProfilesDAO.findServerProfiles();	
		}
		
		List<ServerProfileWithCommandLinks> serverProfileWithCommandLinksList = new ArrayList<>();
		
		for (ServerProfile serverProfile : serverProfileList) {
			ServerProfileWithCommandLinks serverProfileWithCommandLinks = serverProfileSetSelectedCommandLinksFromDb(serverProfile);
			serverProfileWithCommandLinksList.add(serverProfileWithCommandLinks);
		}		
		
		List<String> commandExecutors = new ArrayList<>();
		commandExecutors.add("");
		commandExecutors.addAll(MetricsConstants.CommandExecutorDatatypes.listOfCommandExecutorDatatypes());		
		
		Map<String, Object> parmsMap = new HashMap<>();
		parmsMap.put("serverProfileWithCommandLinksList", serverProfileWithCommandLinksList);
		parmsMap.put("commandExecutors",commandExecutors);	
		parmsMap.put("reqExecutor", reqExecutor);
		return new ModelAndView("serverProfileList", "parmsMap", parmsMap);
	}

	
	@RequestMapping("/viewServerProfile")
	public String viewServerProfile(@RequestParam String reqServerProfileName, @RequestParam(required=false) String reqExecutor,  
			@ModelAttribute ServerProfileEditingForm serverProfileEditingForm, Model model) {
		ServerProfile serverProfile = serverProfilesDAO.findServerProfile(reqServerProfileName); 
		serverProfileEditingForm.setServerProfile(serverProfile);
		ServerProfileWithCommandLinks serverProfileWithCommandLinks = serverProfileSetSelectedCommandLinksFromDb(serverProfile);
		
		if (CommandExecutorDatatypes.GROOVY_SCRIPT.getExecutorText().equals(serverProfile.getExecutor())){
			serverProfileEditingForm.setCommandNames(createListOfAllGroovyScriptCommands());
			String selectedScriptCommandName = selectedScriptCommandName(serverProfileWithCommandLinks);
			serverProfileEditingForm.setSelectedScriptCommandName(selectedScriptCommandName); 
			serverProfileEditingForm.setCommandParameters(alignProfileToCurrentCommandParameters(serverProfile, Arrays.asList(selectedScriptCommandName)));
		} else {  
			serverProfileEditingForm.setCommandSelectors(createListOfAllCommandSelectors(serverProfileWithCommandLinks));
			serverProfileEditingForm.setCommandParameters(alignProfileToCurrentCommandParameters(serverProfile,serverProfileWithCommandLinks.getCommandNames()));			
		}
		
		serverProfileEditingForm.setApiAuthToken("");
		if (String.valueOf(true).equalsIgnoreCase(springBootConfiguration.getMark59metricsapiauth())) {
			String basicAuthToken = Base64.getEncoder().encodeToString((
					springBootConfiguration.getMark59metricsapiuser() + ":" +
					springBootConfiguration.getMark59metricsapipass()).getBytes(StandardCharsets.UTF_8));
			serverProfileEditingForm.setApiAuthToken(basicAuthToken);
		}		
		
		model.addAttribute("serverProfileEditingForm", serverProfileEditingForm);
		
		Map<String, Object> map = createMapOfDropdowns();
		map.put("reqExecutor", reqExecutor);
		map.put("serverProfileEditingForm", serverProfileEditingForm);
		model.addAttribute("map", map);		
		return "viewServerProfile";
	}
	
	
	@RequestMapping("/copyServerProfile")
	public String copyServerProfile(@RequestParam String reqServerProfileName, @RequestParam(required=false) String reqExecutor,  
			@ModelAttribute ServerProfileEditingForm serverProfileEditingForm, Model model) {
		ServerProfile serverProfile = serverProfilesDAO.findServerProfile(reqServerProfileName); 
		serverProfileEditingForm.setServerProfile(serverProfile);
		ServerProfileWithCommandLinks serverProfileWithCommandLinks = serverProfileSetSelectedCommandLinksFromDb(serverProfile);
		
		if (CommandExecutorDatatypes.GROOVY_SCRIPT.getExecutorText().equals(serverProfile.getExecutor())){
			serverProfileEditingForm.setCommandNames(createListOfAllGroovyScriptCommands());
			String selectedScriptCommandName = selectedScriptCommandName(serverProfileWithCommandLinks);
			serverProfileEditingForm.setSelectedScriptCommandName(selectedScriptCommandName); 
			serverProfileEditingForm.setCommandParameters(alignProfileToCurrentCommandParameters(serverProfile, Arrays.asList(selectedScriptCommandName)));
		} else {  
			serverProfileEditingForm.setCommandSelectors(createListOfAllCommandSelectors(serverProfileWithCommandLinks));
			serverProfileEditingForm.setCommandParameters(alignProfileToCurrentCommandParameters(serverProfile,serverProfileWithCommandLinks.getCommandNames()));				
		}
		model.addAttribute("serverProfileEditingForm", serverProfileEditingForm);
		
		Map<String, Object> map = createMapOfDropdowns();
		map.put("reqExecutor", reqExecutor);
		map.put("serverProfileEditingForm", serverProfileEditingForm);
		model.addAttribute("map", map);		
		return "copyServerProfile";
	}
	
	
	@RequestMapping("/editServerProfile")
	public String editServerProfile(@RequestParam String reqServerProfileName, @RequestParam(required=false) String reqExecutor,  
			@ModelAttribute ServerProfileEditingForm serverProfileEditingForm, Model model) {
		ServerProfile serverProfile = serverProfilesDAO.findServerProfile(reqServerProfileName); 
		serverProfileEditingForm.setServerProfile(serverProfile);
		ServerProfileWithCommandLinks serverProfileWithCommandLinks = serverProfileSetSelectedCommandLinksFromDb(serverProfile);
		
		if (CommandExecutorDatatypes.GROOVY_SCRIPT.getExecutorText().equals(serverProfile.getExecutor())){
			serverProfileEditingForm.setCommandNames(createListOfAllGroovyScriptCommands());
			String selectedScriptCommandName = selectedScriptCommandName(serverProfileWithCommandLinks);
			serverProfileEditingForm.setSelectedScriptCommandName(selectedScriptCommandName); 
			serverProfileEditingForm.setCommandParameters(alignProfileToCurrentCommandParameters(serverProfile, Arrays.asList(selectedScriptCommandName)));
		} else {  
			serverProfileEditingForm.setCommandSelectors(createListOfAllCommandSelectors(serverProfileWithCommandLinks));
			serverProfileEditingForm.setCommandParameters(alignProfileToCurrentCommandParameters(serverProfile,serverProfileWithCommandLinks.getCommandNames()));			
		}
		model.addAttribute("serverProfileEditingForm", serverProfileEditingForm);
		
		Map<String, Object> map = createMapOfDropdowns();
		map.put("reqExecutor", reqExecutor);
		map.put("serverProfileEditingForm", serverProfileEditingForm);
		model.addAttribute("map", map);	
		return "editServerProfile";
	}

	
	@RequestMapping("/updateServerProfile")
	public ModelAndView updateServerProfile(@RequestParam(required = false) String reqExecutor,
			@ModelAttribute ServerProfileEditingForm serverProfileEditingForm) {
		
		ServerProfile serverProfile = serverProfileEditingForm.getServerProfile();  
		
		List<String> selectedCommandNames = new ArrayList<>();
		if (CommandExecutorDatatypes.GROOVY_SCRIPT.getExecutorText().equals(serverProfile.getExecutor())){
			serverProfileEditingForm.setCommandNames(createListOfAllGroovyScriptCommands());
			selectedCommandNames.add(serverProfileEditingForm.getSelectedScriptCommandName());
		} else if (CommandExecutorDatatypes.SSH_LINUX_UNIX.getExecutorText().equals(serverProfile.getExecutor()) ||
			CommandExecutorDatatypes.WMIC_WINDOWS.getExecutorText().equals(serverProfile.getExecutor()) ||
			CommandExecutorDatatypes.POWERSHELL_WINDOWS.getExecutorText().equals(serverProfile.getExecutor())){ 
			selectedCommandNames = createListOfSelectedCommands(serverProfileEditingForm.getCommandSelectors());
		}

		if ("true".equalsIgnoreCase(serverProfileEditingForm.getSelectedScriptCommandNameChanged())) {
			serverProfile.setParameters(createParmsMap(serverProfileEditingForm.getCommandParameters()));
			serverProfileEditingForm.setCommandParameters(alignProfileToCurrentCommandParameters(serverProfile, selectedCommandNames));
			serverProfileEditingForm.setSelectedScriptCommandNameChanged("false");

			Map<String, Object> map = new HashMap<>();
			map.put("reqExecutor", reqExecutor);
			map.put("serverProfileEditingForm", serverProfileEditingForm);
			return new ModelAndView("editServerProfile", "map", map);
		}

		serverProfile.setParameters(createParmsMap(serverProfileEditingForm.getCommandParameters()));
		
		serverProfilesDAO.updateServerProfile(serverProfile);
		serverCommandLinksDAO.updateServerCommandLinksForServerProfileName(serverProfile.getServerProfileName(), selectedCommandNames);

		Map<String, Object> map = createMapOfDropdowns();
		map.put("reqExecutor", reqExecutor);
		serverProfileEditingForm.setCommandParameters(alignProfileToCurrentCommandParameters(serverProfileEditingForm.getServerProfile(), selectedCommandNames));
		map.put("serverProfileEditingForm", serverProfileEditingForm);
		return new ModelAndView("viewServerProfile", "map", map);
	}

	
	@RequestMapping("/deleteServerProfile")
	public String deleteServerProfile(@RequestParam String reqServerProfileName, @RequestParam String reqExecutor) {
		serverCommandLinksDAO.deleteServerCommandLinksForServerProfile(reqServerProfileName);
		serverProfilesDAO.deleteServerProfile(reqServerProfileName);
		return "redirect:/serverProfileList?reqExecutor=" + reqExecutor;
	}
	
	
	// for Groovy 
	private String selectedScriptCommandName(ServerProfileWithCommandLinks serverProfileWithCommandLinks) {
		String selectedScriptCommandName = "";
		if (! serverProfileWithCommandLinks.getCommandNames().isEmpty()){
			//assume the first (and should be only) command for this profile is the GROOVY_SCRIPT command you want
			selectedScriptCommandName = serverProfileWithCommandLinks.getCommandNames().get(0);
		} 
		// System.out.println("selectedScriptCommandName=" + selectedScriptCommandName);
		return selectedScriptCommandName; 	
	}
	
	
	// for non-Groovy 
	private List<String> createListOfSelectedCommands(List<CommandSelector> commandSelectors) {
		List<String> commandNames = new ArrayList<>();
		if (commandSelectors != null) {
			for (CommandSelector commandSelector : commandSelectors) {
				if (commandSelector.isCommandChecked()) {
					commandNames.add(commandSelector.getCommandName());
				}
			}
		}
		return commandNames;
	}

	
	/**
	 * @param serverProfile
	 * @param selectedScriptCommandNames
	 * @return list of parameters current for the profile command(s) 
	 * 
	 * Refreshes the expected parameter list for the command(s) of a server profile, as the parameters for a command or the commands for a 
	 * profile may of changed since last edited.
	 * <p>When a parameter is repeated (exists on multiple commands), any already existing or newly inserted value for the parameter
	 * (only the copy of the parameter on the serverProfile can have a value) is used to populate the parameter edit list.
	 * <p>Also, parameter is 'asterisked' when it exists on multiple commands. 
	 */
	private List<CommandParameter> alignProfileToCurrentCommandParameters(ServerProfile serverProfile, List<String> selectedScriptCommandNames){
		// using a TreeMap to force natural ordering of parameters by name
		TreeMap<String, CommandParameter> parametersMap = new TreeMap<String,CommandParameter>();
		Map<String,String> currentProfileParams = serverProfile.getParameters() == null ? new HashMap<>() : serverProfile.getParameters();
		
		for (String selectedScriptCommandName : selectedScriptCommandNames) {
			Command scriptCommand = commandsDAO.findCommand(selectedScriptCommandName);
			if (scriptCommand != null ) {
				List<String> commandParmNames = scriptCommand.getParamNames() == null ? new ArrayList<>() : scriptCommand.getParamNames();
				
				for (String commandParmName : commandParmNames) {
					CommandParameter parameter = new CommandParameter(commandParmName, currentProfileParams.getOrDefault(commandParmName, ""), "");
					
					if (parametersMap.containsKey(commandParmName)){ 
						// a duplicate - uses the value (if exists and set) from the serverProfile (blank otherwise) 
						parameter.setParamDuplicated("&#42;");
						parameter.setParamDuplicated("<span style=\"color: maroon\">*</span>");
						parameter.setParamValue( parameter.getParamValue());
						parametersMap.put(commandParmName, parameter);
					} else {
						parametersMap.put(commandParmName, parameter);
					};	
				}
			}
		}
		return createParmsList(parametersMap);
	}

	
	private ServerProfileWithCommandLinks serverProfileSetSelectedCommandLinksFromDb(ServerProfile serverProfile) {
		ServerProfileWithCommandLinks serverProfileWithCommandLinks = new ServerProfileWithCommandLinks();
		serverProfileWithCommandLinks.setServerProfile(serverProfile); 
		serverProfileWithCommandLinks.setCommandNames(new ArrayList<>());
		
		if (serverProfile != null && serverProfile.getServerProfileName() != null ) {
			List<ServerCommandLink> serverCommandLinkList = serverCommandLinksDAO.findServerCommandLinksForServerProfile(serverProfile.getServerProfileName());
			List<String> commandNames = new ArrayList<>();
			for (ServerCommandLink commandParserLink : serverCommandLinkList) {
				commandNames.add(commandParserLink.getCommandName());
			}
			serverProfileWithCommandLinks.setCommandNames(commandNames);
		}
		return serverProfileWithCommandLinks;
	}
	

	private List<String> createListOfAllGroovyScriptCommands(){
		List<String> listOfAllGroovyScriptCommandNames = new ArrayList<>();
		listOfAllGroovyScriptCommandNames.add("");
		for (Command command : commandsDAO.findCommands("EXECUTOR", CommandExecutorDatatypes.GROOVY_SCRIPT.getExecutorText())) {
			listOfAllGroovyScriptCommandNames.add(command.getCommandName());
		}
		return listOfAllGroovyScriptCommandNames;
	}
	
	
	private List<CommandSelector> createListOfAllCommandSelectors(ServerProfileWithCommandLinks serverProfileWithCommandLinks){
		List<CommandSelector> listOfAllCommandSelectors = new ArrayList<>();
			
		for (Command command : commandsDAO.findCommands("EXECUTOR", serverProfileWithCommandLinks.getServerProfile().getExecutor())) {
			CommandSelector commandSelector = new CommandSelector();
			commandSelector.setCommandName(command.getCommandName());
			commandSelector.setCommandChecked(false);
			if (serverProfileWithCommandLinks != null &&
				serverProfileWithCommandLinks.getCommandNames().contains(command.getCommandName())) {
				commandSelector.setCommandChecked(true);
			}
			commandSelector.setExecutor(command.getExecutor());
			listOfAllCommandSelectors.add(commandSelector);
		}
		return listOfAllCommandSelectors;
	}
	
	
	private Map<String,String> createParmsMap(List<CommandParameter> commandParameters) {
		if (commandParameters == null) { 
			return new HashMap<String,String>();
		}
		Map<String,String> parametersMap = new HashMap<String,String>(); 
		for (CommandParameter commandParameter : commandParameters) {
			parametersMap.put(commandParameter.getParamName() , commandParameter.getParamValue());
		}
		return parametersMap;
	}

	
	private static List<CommandParameter> createParmsList(TreeMap<String,CommandParameter> parametersMap) {
		List<CommandParameter> commandParameters = new ArrayList<CommandParameter>(); 
		if (parametersMap == null) { 
			return commandParameters;
		}
		for ( Entry<String, CommandParameter> parametersMapEntry : parametersMap.entrySet()) {
			commandParameters.add(new CommandParameter(
					parametersMapEntry.getKey(),
					parametersMapEntry.getValue().getParamValue(),
					parametersMapEntry.getValue().getParamDuplicated()));
		}		
		return commandParameters;
	}

	
	private Map<String, Object> createMapOfDropdowns() {
		Map<String, Object> map = new HashMap<>();
		List<String> listOfCommandExecutors = new ArrayList<>();
		listOfCommandExecutors.add("");
		listOfCommandExecutors.addAll(MetricsConstants.CommandExecutorDatatypes.listOfCommandExecutorDatatypes());		
		map.put("commandExecutors",listOfCommandExecutors);	
		return map;
	}
	
}
