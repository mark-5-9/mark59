package com.mark59.datahunter.pojo;

public class ReindexResult {

	Boolean success;
	String message;
	Integer rowsMoved;
	Integer ixCount; 

	public Boolean getSuccess() {
		return success;
	}
	public void setSuccess(Boolean success) {
		this.success = success;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Integer getRowsMoved() {
		return rowsMoved;
	}
	public void setRowsMoved(Integer rowsMoved) {
		this.rowsMoved = rowsMoved;
	}
	public Integer getIxCount() {
		return ixCount;
	}
	public void setIxCount(Integer ixCount) {
		this.ixCount = ixCount;
	}

	@Override
    public String toString() {
        return  super.toString() + 
        		", success= "+ success +         	
        		", message= "+ message +         	
        		", rowsMoved= "+ rowsMoved +        
        		", ixCount= "+ ixCount +                		
        		"]";
	}

}
