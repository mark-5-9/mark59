package com.mark59.metrics.pojos;

import java.util.List;

public class ParsedCommandResponse {
	
	private String commandName;
	private String commandResponse;
	private List<ParsedMetric> parsedMetrics;
	private boolean commandFailure;

	public ParsedCommandResponse() {
	}
	
	public String getCommandName() {
		return commandName;
	}
	
	public void setCommandName(String commandName) {
		this.commandName = commandName;
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

	public boolean isCommandFailure() {
		return commandFailure;
	}

	public void setCommandFailure(boolean commandFailure) {
		this.commandFailure = commandFailure;
	}

	
	@Override
    public String toString() {
        return   "[commandName" + commandName
         	   + ", commandResponse="+ commandResponse
         	   + ", parsedMetrics="+ parsedMetrics  
         	   + ", commandFailure="+ commandFailure            	   
        	   + "]";
	}

}
