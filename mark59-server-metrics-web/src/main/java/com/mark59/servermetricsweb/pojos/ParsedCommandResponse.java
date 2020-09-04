package com.mark59.servermetricsweb.pojos;

public class ParsedCommandResponse {
	
	String commandName;
	String scriptName;
	String metricTxnType;
	String metricNameSuffix;
	String candidateTxnId;
	String commandResponse;
	String parsedCommandResponse;
	String txnPassed;

	
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

	public String getMetricTxnType() {
		return metricTxnType;
	}

	public void setMetricTxnType(String metricTxnType) {
		this.metricTxnType = metricTxnType;
	}

	public String getMetricNameSuffix() {
		return metricNameSuffix;
	}

	public void setMetricNameSuffix(String metricNameSuffix) {
		this.metricNameSuffix = metricNameSuffix;
	}
	
	public String getCandidateTxnId() {
		return candidateTxnId;
	}

	public void setCandidateTxnId(String candidateTxnId) {
		this.candidateTxnId = candidateTxnId;
	}

	public String getCommandResponse() {
		return commandResponse;
	}

	public void setCommandResponse(String commandResponse) {
		this.commandResponse = commandResponse;
	}

	public String getParsedCommandResponse() {
		return parsedCommandResponse;
	}

	public void setParsedCommandResponse(String parsedCommandResponse) {
		this.parsedCommandResponse = parsedCommandResponse;
	}

	public String getTxnPassed() {
		return txnPassed;
	}

	public void setTxnPassed(String txnPassed) {
		this.txnPassed = txnPassed;
	}


	@Override
    public String toString() {
        return   "[commandName" + commandName
         	   + ", scriptName="+ scriptName   
         	   + ", metricTxnType="+ metricTxnType   
         	   + ", metricNameSuffix="+ metricNameSuffix   
         	   + ", candidateTxnId="+ candidateTxnId   
         	   + ", commandResponse="+ commandResponse   
         	   + ", parsedCommandResponse="+ parsedCommandResponse   
         	   + ", txnPassed="+ txnPassed   
        	   + "]";
	}


}
