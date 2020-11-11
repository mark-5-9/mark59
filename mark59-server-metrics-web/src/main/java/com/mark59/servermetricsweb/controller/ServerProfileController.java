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
import com.mark59.servermetricsweb.forms.CommandSelector;
import com.mark59.servermetricsweb.forms.ServerProfileEditingForm;
import com.mark59.servermetricsweb.utils.AppConstantsServerMetricsWeb;



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
			dataRow.createCell(1).setCellValue(serverProfile.getServer());
			dataRow.createCell(2).setCellValue(serverProfile.getAlternativeServerId());
			dataRow.createCell(3).setCellValue(serverProfile.getUsername());
			dataRow.createCell(4).setCellValue(serverProfile.getPassword());
			dataRow.createCell(5).setCellValue(serverProfile.getPasswordCipher());
			dataRow.createCell(6).setCellValue(serverProfile.getOperatingSystem());
			dataRow.createCell(7).setCellValue(serverProfile.getConnectionPort());
			dataRow.createCell(8).setCellValue(serverProfile.getConnectionTimeout());
			dataRow.createCell(9).setCellValue(serverProfile.getComment());
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
	public ModelAndView registerServerProfile(@RequestParam(required=false) String reqOperatingSystem, @RequestParam(required=false) String reqErr, 
			@ModelAttribute ServerProfileEditingForm serverProfileEditingForm) { 
		
		ServerProfile serverProfile = new ServerProfile();
		serverProfile.setOperatingSystem(reqOperatingSystem);
		serverProfileEditingForm.setServerProfile(serverProfile);
				
		ServerProfileWithCommandLinks serverProfileWithCommandLinks = serverProfileAddSelectedCommandLinks(serverProfile);
		serverProfileEditingForm.setCommandSelectors(createListOfAllCommandSelectors(serverProfileWithCommandLinks));

		Map<String, Object> map = createMapOfDropdowns();
		map.put("serverProfileEditingForm",serverProfileEditingForm);		
		map.put("reqOperatingSystem",reqOperatingSystem);
		
		return new ModelAndView("registerServerProfile", "map", map);
	}
	

	@RequestMapping("/insertServerProfile")
	public ModelAndView insertServerProfile( @RequestParam(required=false) String reqOperatingSystem, @RequestParam(required=false) String reqErr,  
			@ModelAttribute ServerProfileEditingForm serverProfileEditingForm, Model model) {

		Map<String, Object> map = createMapOfDropdowns();			
		map.put("reqOperatingSystem",reqOperatingSystem);
		
		ServerProfile serverProfile = serverProfileEditingForm.getServerProfile();
		
		if (StringUtils.isEmpty(serverProfile.getServerProfileName()) ) {
			map.put("serverProfileEditingForm", serverProfileEditingForm);		
			map.put("reqErr", "ServerProfile id is required");			
			model.addAttribute("map", map);	
			return new ModelAndView("registerServerProfile", "map", map);
		}

		ServerProfile existingServerProfile = serverProfilesDAO.findServerProfile(serverProfile.getServerProfileName());
		
		if (existingServerProfile == null ){  //not trying to add something already there, so go ahead..
		
			serverProfilesDAO.insertServerProfile(serverProfile);
			List<String> commandNames = createListOfSelectedCommands(serverProfileEditingForm.getCommandSelectors());
			serverCommandLinksDAO.updateServerCommandLinksForServerProfileName(serverProfileEditingForm.getServerProfile().getServerProfileName(), commandNames);
			map.put("serverProfileEditingForm", serverProfileEditingForm);	
			return new ModelAndView("viewServerProfile", "map", map);		
			
		} else {
			map.put("serverProfileEditingForm", serverProfileEditingForm);	
			map.put("reqErr","Oh, a listing for serverProfileName " + existingServerProfile.getServerProfileName() + " AlreadyExists");			
			model.addAttribute("map", map);	
			return new ModelAndView("registerServerProfile", "map", map);
		}
	}
	

	@RequestMapping("/serverProfileList")
	public ModelAndView serverProfileList(@RequestParam(required=false) String reqOperatingSystem) {

		List<ServerProfile> serverProfileList = new ArrayList<ServerProfile>(); 

		if (!StringUtils.isEmpty(reqOperatingSystem)){
			serverProfileList = serverProfilesDAO.findServerProfiles("OPERATING_SYSTEM", reqOperatingSystem);	
		} else {
			serverProfileList = serverProfilesDAO.findServerProfiles();	
		}
		
		List<ServerProfileWithCommandLinks> serverProfileWithCommandLinksList = new ArrayList<ServerProfileWithCommandLinks>(); 
		
		for (ServerProfile serverProfile : serverProfileList) {
			ServerProfileWithCommandLinks serverProfileWithCommandLinks = serverProfileAddSelectedCommandLinks(serverProfile);
			serverProfileWithCommandLinksList.add(serverProfileWithCommandLinks);
		}		
		
		List<String>operatingSystems  =  new ArrayList<String>(); 
		operatingSystems.addAll(AppConstantsServerMetricsWeb.OPERATING_SYSTEM_LIST);
		operatingSystems.add(0, "");			
		
		Map<String, Object> parmsMap = new HashMap<String, Object>();
		parmsMap.put("serverProfileWithCommandLinksList", serverProfileWithCommandLinksList);
		parmsMap.put("operatingSystems", operatingSystems);
		parmsMap.put("reqOperatingSystem", reqOperatingSystem);
		return new ModelAndView("serverProfileList", "parmsMap", parmsMap);
	}

	
	@RequestMapping("/viewServerProfile")
	public String viewServerProfile(@RequestParam String reqServerProfileName, @RequestParam(required=false) String reqOperatingSystem,  
			@ModelAttribute ServerProfileEditingForm serverProfileEditingForm, Model model) {
		
		ServerProfile serverProfile = serverProfilesDAO.findServerProfile(reqServerProfileName); 
		serverProfileEditingForm.setServerProfile(serverProfile);
		
		ServerProfileWithCommandLinks serverProfileWithCommandLinks = serverProfileAddSelectedCommandLinks(serverProfile);
		serverProfileEditingForm.setCommandSelectors(createListOfAllCommandSelectors(serverProfileWithCommandLinks));
		model.addAttribute("serverProfileEditingForm", serverProfileEditingForm);
		
		Map<String, Object> map = createMapOfDropdowns();
		map.put("reqOperatingSystem", reqOperatingSystem);
		map.put("serverProfileEditingForm", serverProfileEditingForm);
		model.addAttribute("map", map);		
		return "viewServerProfile";
	}
	
	
	
	@RequestMapping("/copyServerProfile")
	public String copyServerProfile(@RequestParam String reqServerProfileName, @RequestParam(required=false) String reqOperatingSystem,  
			@ModelAttribute ServerProfileEditingForm serverProfileEditingForm, Model model) {
		
		ServerProfile serverProfile = serverProfilesDAO.findServerProfile(reqServerProfileName); 
		serverProfileEditingForm.setServerProfile(serverProfile);
		
		ServerProfileWithCommandLinks serverProfileWithCommandLinks = serverProfileAddSelectedCommandLinks(serverProfile);
		serverProfileEditingForm.setCommandSelectors(createListOfAllCommandSelectors(serverProfileWithCommandLinks));
		model.addAttribute("serverProfileEditingForm", serverProfileEditingForm);
		
		Map<String, Object> map = createMapOfDropdowns();
		map.put("reqOperatingSystem", reqOperatingSystem);
		map.put("serverProfileEditingForm", serverProfileEditingForm);
		model.addAttribute("map", map);		
		return "copyServerProfile";
	}
	
	
	@RequestMapping("/editServerProfile")
	public String editServerProfile(@RequestParam String reqServerProfileName, @RequestParam(required=false) String reqOperatingSystem,  
			@ModelAttribute ServerProfileEditingForm serverProfileEditingForm, Model model) {
		
		ServerProfile serverProfile = serverProfilesDAO.findServerProfile(reqServerProfileName); 
		serverProfileEditingForm.setServerProfile(serverProfile);
		
		ServerProfileWithCommandLinks serverProfileWithCommandLinks = serverProfileAddSelectedCommandLinks(serverProfile);
		serverProfileEditingForm.setCommandSelectors(createListOfAllCommandSelectors(serverProfileWithCommandLinks));
		model.addAttribute("serverProfileEditingForm", serverProfileEditingForm);
		
		Map<String, Object> map = createMapOfDropdowns();
		map.put("reqOperatingSystem", reqOperatingSystem);
		map.put("serverProfileEditingForm", serverProfileEditingForm);
		model.addAttribute("map", map);	
		return "editServerProfile";
	}

	
	@RequestMapping("/updateServerProfile")
	public ModelAndView updateServerProfile(@RequestParam(required=false) String reqOperatingSystem, @ModelAttribute ServerProfileEditingForm serverProfileEditingForm) {
		
		// System.out.println("updateServerProfile serverProfileEditingForm.getCommandSelectors() = " + serverProfileEditingForm.getCommandSelectors() );
		
		serverProfilesDAO.updateServerProfile(serverProfileEditingForm.getServerProfile());
		List<String> commandNames = createListOfSelectedCommands(serverProfileEditingForm.getCommandSelectors());
		serverCommandLinksDAO.updateServerCommandLinksForServerProfileName(serverProfileEditingForm.getServerProfile().getServerProfileName(), commandNames);
		
		Map<String, Object> map = createMapOfDropdowns();			
		map.put("reqOperatingSystem",reqOperatingSystem);
		map.put("serverProfileEditingForm", serverProfileEditingForm);		
		return new ModelAndView("viewServerProfile", "map", map);
	}


	@RequestMapping("/deleteServerProfile")
	public String deleteServerProfile(@RequestParam String reqServerProfileName, @RequestParam String reqOperatingSystem) {
		serverCommandLinksDAO.deleteServerCommandLinksForServerProfile(reqServerProfileName);
		serverProfilesDAO.deleteServerProfile(reqServerProfileName);
		return "redirect:/serverProfileList?reqOperatingSystem=" + reqOperatingSystem;
	}
	
	
	
	/**
	 * @param ServerProfileWithCommandLinks
	 * @param operatingSystem
	 */
	private List<CommandSelector> createListOfAllCommandSelectors(ServerProfileWithCommandLinks ServerProfileWithCommandLinks){
		
		List<CommandSelector> listOfAllCommandSelectors = new ArrayList<CommandSelector>();	
			
		for (Command command : commandsDAO.findCommands()) {
			CommandSelector commandSelector = new CommandSelector();
			commandSelector.setCommandName(command.getCommandName());
			commandSelector.setCommandChecked(false);
			if (ServerProfileWithCommandLinks != null &&
				ServerProfileWithCommandLinks.getCommandNames().contains(command.getCommandName())) {
				commandSelector.setCommandChecked(true);
			}
			commandSelector.setExecutor(command.getExecutor());
			listOfAllCommandSelectors.add(commandSelector);
		}
		return listOfAllCommandSelectors;
	}



	private ServerProfileWithCommandLinks serverProfileAddSelectedCommandLinks(ServerProfile serverProfile) {
		
		ServerProfileWithCommandLinks serverProfileWithCommandLinks = new ServerProfileWithCommandLinks();
		serverProfileWithCommandLinks.setServerProfile(serverProfile); 
		
		if (serverProfile == null || serverProfile.getServerProfileName() == null ) {
			serverProfileWithCommandLinks.setCommandNames(new ArrayList<String>());
		} else { 
			List<ServerCommandLink> serverCommandLinkList = serverCommandLinksDAO.findServerCommandLinksForServerProfile(serverProfile.getServerProfileName());
			List<String> commandNames = new ArrayList<String>();
			for (ServerCommandLink commandParserLink : serverCommandLinkList) {
				commandNames.add(commandParserLink.getCommandName());
			}
			serverProfileWithCommandLinks.setCommandNames(commandNames);
		}
		return serverProfileWithCommandLinks;
	}
		
	
	private List<String> createListOfSelectedCommands(List<CommandSelector> commandSelectors) {
		List<String> commandNames = new ArrayList<String>();
		for (CommandSelector commandSelector : commandSelectors) {
			if (commandSelector.isCommandChecked()) {
				commandNames.add(commandSelector.getCommandName());
			}
		}
		return commandNames;
	}

	

	private Map<String, Object> createMapOfDropdowns() {
		Map<String, Object> map = new HashMap<String, Object>(); 
		map.put("operatingSystems",AppConstantsServerMetricsWeb.OPERATING_SYSTEM_LIST);		
		return map;
	}
	
}
