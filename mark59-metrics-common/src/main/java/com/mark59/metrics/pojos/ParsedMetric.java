package com.mark59.metrics.pojos;

public class ParsedMetric {

	String label;
	Number result;
	String dataType;
	Boolean success;
	String parseFailMsg;

	public ParsedMetric() {
	}

	public ParsedMetric(String label, Number result, String dataType){
		this(label, result, dataType, true, null);
	}
	
	public ParsedMetric(String label, Number result, String dataType, String parseFailMsg){
		this(label, result, dataType, true, parseFailMsg);
	}
	
	public ParsedMetric(String label, Number result, String dataType, Boolean success, String parseFailMsg){
		super();
		this.label = label;
		this.result = result;
		this.dataType = dataType;
		this.success = success;
		this.parseFailMsg = parseFailMsg;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Number getResult() {
		return result;
	}

	public void setResult(Number result) {
		this.result = result;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public Boolean getSuccess() {
		return success;
	}

	public void setSuccess(Boolean success) {
		this.success = success;
	}
	
	public String getParseFailMsg() {
		return parseFailMsg;
	}

	public void setParseFailMsg(String parseFailMsg) {
		this.parseFailMsg = parseFailMsg;
	}

	@Override
    public String toString() {
        return   "[label="+ label   
         	   + ", result="+ result   
         	   + ", dataType=" + dataType         	   
         	   + ", success="+ success
         	   + ", parseFailMsg="+ parseFailMsg
        	   + "]";
	}

}
