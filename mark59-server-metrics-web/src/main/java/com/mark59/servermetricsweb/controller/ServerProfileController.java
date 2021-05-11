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

package com.mark59.servermetricsweb.controller;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.mark59.servermetricsweb.data.base.dao.BaseDAO;
import com.mark59.servermetricsweb.data.beans.Command;
import com.mark59.servermetricsweb.data.beans.CommandParserLink;
import com.mark59.servermetricsweb.data.beans.CommandResponseParser;
import com.mark59.servermetricsweb.data.beans.ServerCommandLink;
import com.mark59.servermetricsweb.data.beans.ServerProfile;
import com.mark59.servermetricsweb.data.beans.ServerProfileWithCommandLinks;
import com.mark59.servermetricsweb.data.commandResponseParsers.dao.CommandResponseParsersDAO;
import com.mark59.servermetricsweb.data.commandparserlinks.dao.CommandParserLinksDAO;
import com.mark59.servermetricsweb.data.commands.dao.CommandsDAO;
import com.mark59.servermetricsweb.data.servercommandlinks.dao.ServerCommandLinksDAO;
import com.mark59.servermetricsweb.data.serverprofiles.dao.ServerProfilesDAO;
import com.mark59.servermetricsweb.forms.CommandParameter;
import com.mark59.servermetricsweb.forms.CommandSelector;
import com.mark59.servermetricsweb.forms.ServerProfileEditingForm;
import com.mark59.servermetricsweb.utils.AppConstantsServerMetricsWeb;
import com.mark59.servermetricsweb.utils.AppConstantsServerMetricsWeb.CommandExecutorDatatypes;
import com.mark59.servermetricsweb.utils.ServerMetricsWebUtils;



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
	
	
	
	
	@GetMapping("/downloadServerProfiles")
	public ResponseEntity<ByteArrayResource> downloadServerProfiles() {
		try {
			HttpHeaders httpHeaders = new HttpHeaders();
			httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			httpHeaders.set(HttpHeaders.CONTENT_DISPOSITION,
							"attachment; filename=" + AppConstantsServerMetricsWeb.MARK59_SERVER_PROFILES_EXCEL_FILE);
			ByteArrayOutputStream stream = serverProfilesToExcelFile();
			return new ResponseEntity<>(new ByteArrayResource(stream.toByteArray()), httpHeaders, HttpStatus.CREATED);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}	
	
	
	public ByteArrayOutputStream serverProfilesToExcelFile() throws IOException {
		int columnCount = 0;
		int rowIndex = 0;
		
		Workbook workbook = new XSSFWorkbook();
		CellStyle headerCellStyle = workbook.createCellStyle();
		headerCellStyle.setFillForegroundColor(IndexedColors.DARK_RED.getIndex());
		Font headerFont = workbook.createFont();
		headerFont.setColor(IndexedColors.WHITE.getIndex());
		headerFont.setBold(true);
		headerCellStyle.setFont(headerFont);
		headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		
		
		Sheet sheet = workbook.createSheet("SERVERPROFILES");

		DataFormat fmt = workbook.createDataFormat();
		CellStyle textStyle = workbook.createCellStyle();
		textStyle.setDataFormat(fmt.getFormat("@"));
		sheet.setDefaultColumnStyle(0, textStyle);
		
		columnCount = createHeaderRow(sheet, headerCellStyle, "SERVERPROFILES");
		rowIndex = 1;
		List<ServerProfile> serverProfiles = serverProfilesDAO.findServerProfiles();
		for (ServerProfile serverProfile : serverProfiles) {
			Row dataRow = sheet.createRow(rowIndex);
			
			// System.out.println("serverProfile [" + serverProfile + "]" ); 
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
			dataRow.createCell(1).setCellValue(commandParserLink.getScriptName());
			rowIndex++;
		}
		autoSizeColumns(sheet, columnCount);
	
		
		sheet = workbook.createSheet("COMMANDRESPONSEPARSERS");
		columnCount = createHeaderRow(sheet, headerCellStyle, "COMMANDRESPONSEPARSERS");
		rowIndex = 1;
		List<CommandResponseParser> commandResponseParsers = commandResponseParsersDAO.findCommandResponseParsers();
		for (CommandResponseParser commandResponseParser : commandResponseParsers) {
			Row dataRow = sheet.createRow(rowIndex);
			dataRow.createCell(0).setCellValue(commandResponseParser.getScriptName());
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

		ServerProfileWithCommandLinks serverProfileWithCommandLinks = serverProfileAddSelectedCommandLinks(serverProfile);
		
		serverProfileEditingForm.setCommandSelectors(new ArrayList<CommandSelector>());
		
		if (CommandExecutorDatatypes.GROOVY_SCRIPT.getExecutorText().equals(serverProfile.getExecutor())){
			serverProfileEditingForm.setCommandNames(createListOfAllGroovyScriptCommands());
			String selectedScriptCommandName = selectedScriptCommandName(serverProfileWithCommandLinks);
			serverProfileEditingForm.setSelectedScriptCommandName(selectedScriptCommandName); 
			serverProfileEditingForm.setCommandParameters(alignProfileToCurrentCommandParameters(serverProfile, selectedScriptCommandName));
		} else if (CommandExecutorDatatypes.SSH_LINIX_UNIX.getExecutorText().equals(serverProfile.getExecutor()) ||
				   CommandExecutorDatatypes.WMIC_WINDOWS.getExecutorText().equals(serverProfile.getExecutor())){  
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
	
//		System.out.println("insertServerProfile exec change : " + serverProfileEditingForm.getSelectedExecutorChanged());
//		System.out.println("insertServerProfile exec        : " + serverProfileEditingForm.getServerProfile().getExecutor());	
//		System.out.println("insertServerProfile cmd change  : " + serverProfileEditingForm.getSelectedScriptCommandNameChanged() );
//		System.out.println("insertServerProfile cmd         : " + serverProfileEditingForm.getSelectedScriptCommandName());
//		System.out.println("insertServerProfile cmd parms   : " + serverProfileEditingForm.getCommandParameters());
		
		if ("true".equalsIgnoreCase(serverProfileEditingForm.getSelectedExecutorChanged())){
		
			serverProfileEditingForm.setSelectedExecutorChanged("false");
			serverProfileEditingForm.setSelectedScriptCommandNameChanged("false");
			serverProfileEditingForm.setSelectedScriptCommandName("");
			serverProfileEditingForm.setCommandSelectors(new ArrayList<CommandSelector>());
			serverProfileEditingForm.setCommandNames(new ArrayList<String>());
			
			serverProfileEditingForm.setCommandParameters(new ArrayList<CommandParameter>());
			serverProfile.setParameters(new HashMap<String,String>());

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
			} else if (CommandExecutorDatatypes.SSH_LINIX_UNIX.getExecutorText().equals(serverProfile.getExecutor()) ||
					   CommandExecutorDatatypes.WMIC_WINDOWS.getExecutorText().equals(serverProfile.getExecutor())){ 
				ServerProfileWithCommandLinks blankServerProfileWithCommandLinks = new ServerProfileWithCommandLinks();
				blankServerProfileWithCommandLinks.setServerProfile(new ServerProfile());
				blankServerProfileWithCommandLinks.setCommandNames(new ArrayList<String>());
				serverProfileEditingForm.setCommandSelectors(createListOfAllCommandSelectors(blankServerProfileWithCommandLinks));
				if (CommandExecutorDatatypes.SSH_LINIX_UNIX.getExecutorText().equals(serverProfile.getExecutor())){
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

		if ("true".equalsIgnoreCase(serverProfileEditingForm.getSelectedScriptCommandNameChanged())){
			//back to register profile page with selected Groovy command parms added
			serverProfileEditingForm.setCommandNames(createListOfAllGroovyScriptCommands());
			List<CommandParameter> commandParameters = new ArrayList<CommandParameter>();
			Command command = commandsDAO.findCommand(serverProfileEditingForm.getSelectedScriptCommandName());
			if (command != null) {
				for (String paramName : command.getParamNames()) {
					commandParameters.add(new CommandParameter(paramName, "") );
				} 
			}
			serverProfileEditingForm.setCommandParameters(commandParameters);
			serverProfileEditingForm.setSelectedScriptCommandNameChanged("false");
			map.put("reqExecutor", reqExecutor);
			map.put("serverProfileEditingForm", serverProfileEditingForm);	
			model.addAttribute("map", map);	
			return new ModelAndView("registerServerProfile", "map", map);
		}
		
		// verify data and prepare for server profile and command links inserts  
		
		if (StringUtils.isBlank(serverProfile.getServerProfileName())){
			map.put("serverProfileEditingForm", serverProfileEditingForm);		
			map.put("reqErr", "ServerProfile id is required");			
			model.addAttribute("map", map);	
			return new ModelAndView("registerServerProfile", "map", map);
		}
		if (StringUtils.isBlank(serverProfile.getExecutor())){
			map.put("serverProfileEditingForm", serverProfileEditingForm);		
			map.put("reqErr", "You must select a Command Executor from the dropdown");			
			model.addAttribute("map", map);	
			return new ModelAndView("registerServerProfile", "map", map);
		}
		
		
		ServerProfile existingServerProfile = serverProfilesDAO.findServerProfile(serverProfile.getServerProfileName());
		
		if (existingServerProfile == null ){  //not trying to add something already there, so go ahead..

			List<String> commandNames = new ArrayList<String>();
			if (CommandExecutorDatatypes.GROOVY_SCRIPT.getExecutorText().equals(serverProfile.getExecutor())){
				commandNames.add(serverProfileEditingForm.getSelectedScriptCommandName()); 
				serverProfile.setParameters(ServerMetricsWebUtils.createParmsMap(serverProfileEditingForm.getCommandParameters()));
			} else { // win or nix
				commandNames = createListOfSelectedCommands(serverProfileEditingForm.getCommandSelectors());
				serverProfile.setParameters(new HashMap<String,String>());
			}
			
			serverProfilesDAO.insertServerProfile(serverProfile);
			serverCommandLinksDAO.updateServerCommandLinksForServerProfileName(serverProfile.getServerProfileName(), commandNames);

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

		List<ServerProfile> serverProfileList = new ArrayList<ServerProfile>(); 

		if (!StringUtils.isEmpty(reqExecutor)){
			serverProfileList = serverProfilesDAO.findServerProfiles("EXECUTOR", reqExecutor);	
		} else {
			serverProfileList = serverProfilesDAO.findServerProfiles();	
		}
		
		List<ServerProfileWithCommandLinks> serverProfileWithCommandLinksList = new ArrayList<ServerProfileWithCommandLinks>(); 
		
		for (ServerProfile serverProfile : serverProfileList) {
			ServerProfileWithCommandLinks serverProfileWithCommandLinks = serverProfileAddSelectedCommandLinks(serverProfile);
			serverProfileWithCommandLinksList.add(serverProfileWithCommandLinks);
		}		
		
		List<String> commandExecutors = new ArrayList<String>();
		commandExecutors.add("");
		commandExecutors.addAll(AppConstantsServerMetricsWeb.CommandExecutorDatatypes.listOfCommandExecutorDatatypes());		
		
		
		Map<String, Object> parmsMap = new HashMap<String, Object>();
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
		ServerProfileWithCommandLinks serverProfileWithCommandLinks = serverProfileAddSelectedCommandLinks(serverProfile);
		
		if (CommandExecutorDatatypes.GROOVY_SCRIPT.getExecutorText().equals(serverProfile.getExecutor())){
			serverProfileEditingForm.setCommandNames(createListOfAllGroovyScriptCommands());
			String selectedScriptCommandName = selectedScriptCommandName(serverProfileWithCommandLinks);
			serverProfileEditingForm.setSelectedScriptCommandName(selectedScriptCommandName); 
			serverProfileEditingForm.setCommandParameters(alignProfileToCurrentCommandParameters(serverProfile, selectedScriptCommandName));
		} else {  
			serverProfileEditingForm.setCommandSelectors(createListOfAllCommandSelectors(serverProfileWithCommandLinks));
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
		ServerProfileWithCommandLinks serverProfileWithCommandLinks = serverProfileAddSelectedCommandLinks(serverProfile);
		
		if (CommandExecutorDatatypes.GROOVY_SCRIPT.getExecutorText().equals(serverProfile.getExecutor())){
			serverProfileEditingForm.setCommandNames(createListOfAllGroovyScriptCommands());
			String selectedScriptCommandName = selectedScriptCommandName(serverProfileWithCommandLinks);
			serverProfileEditingForm.setSelectedScriptCommandName(selectedScriptCommandName); 
			serverProfileEditingForm.setCommandParameters(alignProfileToCurrentCommandParameters(serverProfile, selectedScriptCommandName));
		} else {  
			serverProfileEditingForm.setCommandSelectors(createListOfAllCommandSelectors(serverProfileWithCommandLinks));
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
		ServerProfileWithCommandLinks serverProfileWithCommandLinks = serverProfileAddSelectedCommandLinks(serverProfile);
		
		if (CommandExecutorDatatypes.GROOVY_SCRIPT.getExecutorText().equals(serverProfile.getExecutor())){
			serverProfileEditingForm.setCommandNames(createListOfAllGroovyScriptCommands());
			String selectedScriptCommandName = selectedScriptCommandName(serverProfileWithCommandLinks);
			serverProfileEditingForm.setSelectedScriptCommandName(selectedScriptCommandName); 
			serverProfileEditingForm.setCommandParameters(alignProfileToCurrentCommandParameters(serverProfile, selectedScriptCommandName));
		} else {  
			serverProfileEditingForm.setCommandSelectors(createListOfAllCommandSelectors(serverProfileWithCommandLinks));
		}
		model.addAttribute("serverProfileEditingForm", serverProfileEditingForm);
		
		Map<String, Object> map = createMapOfDropdowns();
		map.put("reqExecutor", reqExecutor);
		map.put("serverProfileEditingForm", serverProfileEditingForm);
		model.addAttribute("map", map);	
		return "editServerProfile";
	}



	@RequestMapping("/updateServerProfile")
  public ModelAndView updateServerProfile(@RequestParam(required=false) String reqExecutor, @ModelAttribute ServerProfileEditingForm serverProfileEditingForm) {
	
//		System.out.println("updateServerProfile cmd change : " + serverProfileEditingForm.getSelectedScriptCommandNameChanged() );
//		System.out.println("updateServerProfile cmd        : " + serverProfileEditingForm.getSelectedScriptCommandName());
		
		if ("true".equalsIgnoreCase(serverProfileEditingForm.getSelectedScriptCommandNameChanged())){
			//rebuild form with the newly selected cmd params
			serverProfileEditingForm.setCommandNames(createListOfAllGroovyScriptCommands());
			
			Command command = commandsDAO.findCommand(serverProfileEditingForm.getSelectedScriptCommandName());
			List<CommandParameter> commandParameters = new ArrayList<CommandParameter>();
			if (command != null ) {
				for (String paramName : command.getParamNames()) {
					commandParameters.add(new CommandParameter(paramName, "") );
				} 
			}
			serverProfileEditingForm.setCommandParameters(commandParameters);
			serverProfileEditingForm.setSelectedScriptCommandNameChanged("false"); ;
			Map<String, Object> map = new HashMap<String, Object>(); 		
			map.put("reqExecutor", reqExecutor);
			map.put("serverProfileEditingForm", serverProfileEditingForm);		
			return new ModelAndView("editServerProfile", "map", map);
		}
		
		ServerProfile serverProfile = serverProfileEditingForm.getServerProfile();
		List<String> commandNames = new ArrayList<String>();
		
		if (CommandExecutorDatatypes.GROOVY_SCRIPT.getExecutorText().equals(serverProfile.getExecutor())){
			commandNames.add(serverProfileEditingForm.getSelectedScriptCommandName()); 
			serverProfile.setParameters(ServerMetricsWebUtils.createParmsMap(serverProfileEditingForm.getCommandParameters()));
		} else {
			commandNames = createListOfSelectedCommands(serverProfileEditingForm.getCommandSelectors());
		}
		serverProfilesDAO.updateServerProfile(serverProfile);
		serverCommandLinksDAO.updateServerCommandLinksForServerProfileName(serverProfile.getServerProfileName(), commandNames);
		
		Map<String, Object> map = createMapOfDropdowns();			
		map.put("reqExecutor", reqExecutor);
		map.put("serverProfileEditingForm", serverProfileEditingForm);		
		return new ModelAndView("viewServerProfile", "map", map);
	}


	@RequestMapping("/deleteServerProfile")
	public String deleteServerProfile(@RequestParam String reqServerProfileName, @RequestParam String reqExecutor) {
		serverCommandLinksDAO.deleteServerCommandLinksForServerProfile(reqServerProfileName);
		serverProfilesDAO.deleteServerProfile(reqServerProfileName);
		return "redirect:/serverProfileList?reqExecutor=" + reqExecutor;
	}
	
	

	private String selectedScriptCommandName(ServerProfileWithCommandLinks serverProfileWithCommandLinks) {
		String selectedScriptCommandName = "";
		if (! serverProfileWithCommandLinks.getCommandNames().isEmpty()){
			//assume the first (and should be only) command for this profile is the GROOVY_SCRIPT command you want
			selectedScriptCommandName = serverProfileWithCommandLinks.getCommandNames().get(0);
		} 
//		System.out.println("selectedScriptCommandName=" + selectedScriptCommandName);
		return selectedScriptCommandName; 	
	}
	
	
	private ServerProfileWithCommandLinks serverProfileAddSelectedCommandLinks(ServerProfile serverProfile) {
		ServerProfileWithCommandLinks serverProfileWithCommandLinks = new ServerProfileWithCommandLinks();
		serverProfileWithCommandLinks.setServerProfile(serverProfile); 
		serverProfileWithCommandLinks.setCommandNames(new ArrayList<String>());
		
		if (serverProfile != null && serverProfile.getServerProfileName() != null ) {
			List<ServerCommandLink> serverCommandLinkList = serverCommandLinksDAO.findServerCommandLinksForServerProfile(serverProfile.getServerProfileName());
			List<String> commandNames = new ArrayList<String>();
			for (ServerCommandLink commandParserLink : serverCommandLinkList) {
				commandNames.add(commandParserLink.getCommandName());
			}
			serverProfileWithCommandLinks.setCommandNames(commandNames);
		}
		return serverProfileWithCommandLinks;
	}
	

	private List<String> createListOfAllGroovyScriptCommands(){
		List<String> listOfAllGroovyScriptCommandNames = new ArrayList<String>();
		listOfAllGroovyScriptCommandNames.add("");
		for (Command command : commandsDAO.findCommands("EXECUTOR", CommandExecutorDatatypes.GROOVY_SCRIPT.getExecutorText())) {
			listOfAllGroovyScriptCommandNames.add(command.getCommandName());
		}
		return listOfAllGroovyScriptCommandNames;
	}
	
	
	private List<CommandSelector> createListOfAllCommandSelectors(ServerProfileWithCommandLinks serverProfileWithCommandLinks){
		List<CommandSelector> listOfAllCommandSelectors = new ArrayList<CommandSelector>();	
			
		for (Command command : commandsDAO.findCommands()) {
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

	
	private List<CommandParameter> alignProfileToCurrentCommandParameters(ServerProfile serverProfile, String selectedScriptCommandName){
		List<CommandParameter> editableParameters = new ArrayList<CommandParameter>();
		
		if (StringUtils.isNotBlank(selectedScriptCommandName)  ){
			
			Command scriptCommand = commandsDAO.findCommand(selectedScriptCommandName);
			if (scriptCommand != null ) {
				
				Map<String,String> currentProfileParams = serverProfile.getParameters() == null ? new HashMap<String,String>() : serverProfile.getParameters();
				List<String> commandParmNames = scriptCommand.getParamNames() == null ? new ArrayList<String>() : scriptCommand.getParamNames();
				
				for (String commandParmName : commandParmNames) {
					if (currentProfileParams.containsKey(commandParmName)) {
						editableParameters.add(new CommandParameter(commandParmName, currentProfileParams.get(commandParmName)));
					} else {
						editableParameters.add(new CommandParameter(commandParmName, ""));
					}
				}
			}
		}
		return editableParameters;
	}
	
	
	private List<String> createListOfSelectedCommands(List<CommandSelector> commandSelectors) {
		List<String> commandNames = new ArrayList<String>();
		if (commandSelectors != null) {
			for (CommandSelector commandSelector : commandSelectors) {
				if (commandSelector.isCommandChecked()) {
					commandNames.add(commandSelector.getCommandName());
				}
			}
		}
		return commandNames;
	}
	

	private Map<String, Object> createMapOfDropdowns() {
		Map<String, Object> map = new HashMap<String, Object>(); 
		List<String> listOfCommandExecutors = new ArrayList<String>();
		listOfCommandExecutors.add("");
		listOfCommandExecutors.addAll(AppConstantsServerMetricsWeb.CommandExecutorDatatypes.listOfCommandExecutorDatatypes());		
		map.put("commandExecutors",listOfCommandExecutors);	
		return map;
	}
	
}
