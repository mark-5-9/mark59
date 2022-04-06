package com.mark59.servermetricsweb.pojos;

import java.util.List;

public class ParsedCommandResponse {
	
	private String commandName;
	private String scriptName;
	private String commandResponse;
	private List<ParsedMetric> parsedMetrics;

	public ParsedCommandResponse() {
	}
	
	public String getCommandName() {
		return commandName;
	}
	
	public void setCommandName(String commandName) {
		this.commandName = commandName;
	}
	
	public String getScriptName() {
		return scriptName;
	}
	
	public void setScriptName(String scriptName) {
		this.scriptName = scriptName;
	}
	
	public String getCommandResponse() {
		return commandResponse;
	}

	public void setCommandResponse(String commandResponse) {
		this.commandResponse = commandResponse;
	}

	public List<ParsedMetric> getParsedMetrics() {
		return parsedMetrics;
	}

	public void setParsedMetrics(List<ParsedMetric> parsedMetrics) {
		this.parsedMetrics = parsedMetrics;
	}

	@Override
    public String toString() {
        return   "[commandName" + commandName
         	   + ", scriptName="+ scriptName   
         	   + ", commandResponse="+ commandResponse
         	   + ", parsedMetrics="+ parsedMetrics  
        	   + "]";
	}

}
