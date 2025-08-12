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


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import com.mark59.metrics.data.beans.Command;
import com.mark59.metrics.data.beans.CommandParserLink;
import com.mark59.metrics.data.beans.CommandResponseParser;
import com.mark59.metrics.data.beans.CommandWithParserLinks;
import com.mark59.metrics.data.commandResponseParsers.dao.CommandResponseParsersDAO;
import com.mark59.metrics.data.commandparserlinks.dao.CommandParserLinksDAO;
import com.mark59.metrics.data.commands.dao.CommandsDAO;
import com.mark59.metrics.forms.CommandEditingForm;
import com.mark59.metrics.forms.ParserSelector;
import com.mark59.metrics.utils.MetricsConstants;
import com.mark59.metrics.utils.MetricsUtils;


/**
 * @author Philip Webb
 * Written: Australian Summer 2020  
 */

@Controller
public class CommandController {

	@Autowired
	CommandsDAO commandsDAO; 	
	
	@Autowired
	CommandParserLinksDAO commandParserLinksDAO; 	

	
	@Autowired
	CommandResponseParsersDAO commandResponseParsersDAO; 		


	@GetMapping("/registerCommand")
	public ModelAndView registerCommand(@RequestParam(required=false) String reqExecutor, @RequestParam(required=false) String reqErr, 
			@ModelAttribute CommandEditingForm commandEditingForm) {

		Command command = new Command();
		command.setExecutor(reqExecutor); 
		commandEditingForm.setCommand(command);

		commandEditingForm.setParamNamesTextboxFormat(""); 
		commandEditingForm.setParserSelectors(createListOfAllSParserSelectorsWithNothingSelected());
		
		Map<String, Object> map = createMapOfDropdowns();
		map.put("reqExecutor", reqExecutor);		
		map.put("commandEditingForm",commandEditingForm);		

		return new ModelAndView("registerCommand", "map", map);
	}
	

	@PostMapping("/insertCommand")
	public String insertCommand( @RequestParam(required=false) String reqExecutor, @RequestParam(required=false) String reqErr, 
			@RequestParam(required=false) String fromAction,
			@ModelAttribute CommandEditingForm commandEditingForm, Model model) {

		Map<String, Object> map = createMapOfDropdowns();			
		map.put("reqExecutor", reqExecutor);
		
		Command command = commandEditingForm.getCommand();

		if (StringUtils.isEmpty(command.getCommandName()) || StringUtils.isEmpty(command.getExecutor())   ) {
			map.put("commandEditingForm", commandEditingForm);		
			model.addAttribute("map", map);	
			if ("copy".equals(fromAction)) {
				map.put("reqErr", "You must enter a Command Name");	
				return "copyCommand";
			}
			map.put("reqErr", "You must enter a Command Name and select a Command Executor");			
			return "registerCommand";
		}

		Command existingCommand  = commandsDAO.findCommand(command.getCommandName());
		
		if (existingCommand == null ){  //not trying to add something already there, so go ahead..
			command.setParamNames(MetricsUtils.textboxFormatToList(commandEditingForm.getParamNamesTextboxFormat()));
			commandsDAO.insertCommand(command);
			List<String> parserNames = createListOfSelectedParsers(commandEditingForm.getParserSelectors());   
			commandParserLinksDAO.updateCommandParserLinksForCommandName(commandEditingForm.getCommand().getCommandName(), parserNames);
			map.put("commandEditingForm", commandEditingForm);	
			
			if ("continue".equalsIgnoreCase(commandEditingForm.getSaveCmdAction())){
				return "redirect:/editCommand?&reqCommandName=" + command.getCommandName() + "&reqExecutor=" + reqExecutor
						+ "&reqLastSavedTimestamp=" + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()).replace(":","%3A")   ;
			} else { // exit
				return "redirect:/commandList?reqExecutor=" + reqExecutor;	
			}

		} else {
			map.put("commandEditingForm", commandEditingForm);		
			map.put("reqErr","Oh, a listing for a command named " + existingCommand.getCommandName() + " AlreadyExists");	
			model.addAttribute("map", map);	
			if ("copy".equals(fromAction)) {
				return "copyCommand";
			}			
			return "registerCommand";
		}
	}
	
	

	@GetMapping("/commandList")
	public ModelAndView commandList(@RequestParam(required=false) String reqExecutor) {

		List<Command> commandList;

		if (!StringUtils.isEmpty(reqExecutor)){
			commandList = commandsDAO.findCommands("EXECUTOR", reqExecutor);	
		} else {
			commandList = commandsDAO.findCommands();	
		}

		List<CommandWithParserLinks> commandWithParserLinksList = new ArrayList<>();
		
		for (Command command : commandList) {
			command.setCommand(StringUtils.abbreviate(command.getCommand(), 500));
			CommandWithParserLinks commandWithParserLinks = commandAddParserLinks(command);
			commandWithParserLinksList.add(commandWithParserLinks);
		}

		List<String> commandExecutors = new ArrayList<>(MetricsConstants.CommandExecutorDatatypes.listOfCommandExecutorDatatypes());
		commandExecutors.add(0, "");			
		
		Map<String, Object> parmsMap = new HashMap<>();
		parmsMap.put("commandWithParsersList", commandWithParserLinksList);
		parmsMap.put("commandExecutors", commandExecutors);
		parmsMap.put("reqExecutor", reqExecutor);
		return new ModelAndView("commandList", "parmsMap", parmsMap);
	}


	@GetMapping("/copyCommand")
	public String copyCommand(@RequestParam String reqCommandName, @RequestParam(required=false) String reqExecutor,  
			@ModelAttribute CommandEditingForm commandEditingForm, Model model) {
		
		Command command = commandsDAO.findCommand(reqCommandName);
		commandEditingForm.setCommand(command);
		commandEditingForm.setParamNamesTextboxFormat(MetricsUtils.listToTextboxFormat(command.getParamNames()));
		
		CommandWithParserLinks commandWithParserLinks = commandAddParserLinks(command);
		commandEditingForm.setParserSelectors(createListOfAllParserSelectors(commandWithParserLinks));
		model.addAttribute("commandEditingForm", commandEditingForm);
		
		Map<String, Object> map = createMapOfDropdowns();
		map.put("reqExecutor", reqExecutor);
		map.put("commandEditingForm", commandEditingForm);		
		model.addAttribute("map", map);	
		return "copyCommand";
	}
	
	
	@GetMapping("/editCommand")
	public String editCommand(@RequestParam String reqCommandName, @RequestParam(required=false) String reqExecutor, 
			@RequestParam(required=false) String reqLastSavedTimestamp,
			@ModelAttribute CommandEditingForm commandEditingForm, Model model) {

		Command command = commandsDAO.findCommand(reqCommandName);
		commandEditingForm.setCommand(command);
		commandEditingForm.setParamNamesTextboxFormat(MetricsUtils.listToTextboxFormat(command.getParamNames()));
		
		commandEditingForm.setLastSavedTimestamp("");
		String spanTag = "<span style='font-size: 10px'>&nbsp;&nbsp;&nbsp;";
		if (StringUtils.isNotBlank(reqLastSavedTimestamp)){
			commandEditingForm.setLastSavedTimestamp(" Last saved " + reqLastSavedTimestamp);
			commandEditingForm.setLastSavedTimestamp(spanTag + "Last saved "+reqLastSavedTimestamp+"</span>");
		} else {
			commandEditingForm.setLastSavedTimestamp(spanTag + "Inactivity timeout: 30 min</span>");
		}

		CommandWithParserLinks commandWithParserLinks = commandAddParserLinks(command);
		commandEditingForm.setParserSelectors(createListOfAllParserSelectors(commandWithParserLinks));
		model.addAttribute("commandEditingForm", commandEditingForm);
		
		Map<String, Object> map = createMapOfDropdowns();
		map.put("reqExecutor", reqExecutor);
		map.put("commandEditingForm", commandEditingForm);		
		model.addAttribute("map", map);	
		return "editCommand";
	}


	@PostMapping("/updateCommand")
	public String updateCommand(@RequestParam(required=false) String reqExecutor, @ModelAttribute CommandEditingForm commandEditingForm) {
		
		Command command = commandEditingForm.getCommand();
		command.setParamNames(MetricsUtils.textboxFormatToList(commandEditingForm.getParamNamesTextboxFormat()));
		
		commandsDAO.updateCommand(command);
		List<String> parserNames = createListOfSelectedParsers(commandEditingForm.getParserSelectors());   
		commandParserLinksDAO.updateCommandParserLinksForCommandName(command.getCommandName(), parserNames);
		
		Map<String, Object> map = createMapOfDropdowns();			
		map.put("reqExecutor", reqExecutor);
		map.put("commandEditingForm", commandEditingForm);
		
		if ("continue".equalsIgnoreCase(commandEditingForm.getSaveCmdAction())){
			return "redirect:/editCommand?&reqCommandName=" + command.getCommandName() + "&reqExecutor=" + reqExecutor
					+ "&reqLastSavedTimestamp=" + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()).replace(":","%3A")   ;
		} else { // exit
			return "redirect:/commandList?reqExecutor=" + reqExecutor;	
		}
	}


	@GetMapping("/deleteCommand")
	public String deleteCommand(@RequestParam String reqCommandName, @RequestParam String reqExecutor) {
		commandParserLinksDAO.deleteCommandParserLinksForCommandName(reqCommandName);
		commandsDAO.deleteCommand(reqCommandName); 
		return "redirect:/commandList?reqExecutor=" + reqExecutor;
	}

	
	private List<ParserSelector> createListOfAllSParserSelectorsWithNothingSelected() {
		return createListOfAllParserSelectors(null);
	}
	
	/**
	 * @param commandWithParserLinks scripts names associated with a given command
	 */
	private List<ParserSelector> createListOfAllParserSelectors(CommandWithParserLinks commandWithParserLinks){
		
		List<ParserSelector> listOfAllParserSelectors = new ArrayList<>();
		List<CommandResponseParser> listOfAllCommandResponseParsers = commandResponseParsersDAO.findCommandResponseParsers();
		
		for (CommandResponseParser commandResponseParser : listOfAllCommandResponseParsers) {
			ParserSelector parserSelector = new ParserSelector();
			parserSelector.setParserName(commandResponseParser.getParserName());
			parserSelector.setParserChecked(false);
			if (commandWithParserLinks != null &&
				commandWithParserLinks.getParserNames().contains(commandResponseParser.getParserName())) {
				parserSelector.setParserChecked(true);
			}
			listOfAllParserSelectors.add(parserSelector);
		}
		return listOfAllParserSelectors;
	}

	
	/**
	 * @param command  Command
	 * @return list of linked parser names
	 */
	private CommandWithParserLinks commandAddParserLinks(Command command) {
		List<CommandParserLink> commandParserLinkList = commandParserLinksDAO.findCommandParserLinks("COMMAND_NAME", command.getCommandName());
		List<String> parserNames = new ArrayList<>();
		for (CommandParserLink commandParserLink : commandParserLinkList) {
			parserNames.add(commandParserLink.getParserName());
		}
		CommandWithParserLinks commandWithParserLinks = new CommandWithParserLinks();
		commandWithParserLinks.setCommand(command);
		commandWithParserLinks.setParserNames(parserNames);
		return commandWithParserLinks;
	}


	private List<String> createListOfSelectedParsers(List<ParserSelector> parserSelectors) {
		List<String> parserNames = new ArrayList<>();
		for (ParserSelector parserSelector : parserSelectors) {
			if (parserSelector.isParserChecked()) {
				parserNames.add(parserSelector.getParserName());
			}
		}
		return parserNames;
	}

	
	private Map<String, Object> createMapOfDropdowns() {
		Map<String, Object> map = new HashMap<String, Object>(); 
		List<String> listOfCommandExecutors = new ArrayList<>();
		listOfCommandExecutors.add("");
		listOfCommandExecutors.addAll(MetricsConstants.CommandExecutorDatatypes.listOfCommandExecutorDatatypes());		
		map.put("commandExecutors",listOfCommandExecutors);		
		map.put("ingoreStderrYesNo",populateYesNoDropdown());		
		return map;
	}
	
	private List<String> populateYesNoDropdown( ) {
		List<String> yesNo = new ArrayList<>();
		yesNo.add("N");
		yesNo.add("Y");
		return yesNo;
	}		
}
