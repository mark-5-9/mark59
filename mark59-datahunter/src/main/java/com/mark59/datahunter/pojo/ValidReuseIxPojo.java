package com.mark59.datahunter.pojo;

import com.mark59.datahunter.data.beans.Policies;

public class ValidReuseIxPojo {

	Boolean policyReusableIndexed;
	Boolean validatedOk;
	String errorMsg;
	Policies ixPolicy;
	int currentIxCount;
	int validIdsinRangeCount;
	
	public Boolean getPolicyReusableIndexed() {
		return policyReusableIndexed;
	}
	public void setPolicyReusableIndexed(Boolean policyReusableIndexed) {
		this.policyReusableIndexed = policyReusableIndexed;
	}
	public Boolean getValidatedOk() {
		return validatedOk;
	}
	public void setValidatedOk(Boolean validatedOk) {
		this.validatedOk = validatedOk;
	}
	public String getErrorMsg() {
		return errorMsg;
	}
	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}
	public Policies getIxPolicy() {
		return ixPolicy;
	}
	public void setIxPolicy(Policies ixPolicy) {
		this.ixPolicy = ixPolicy;
	}
	public int getCurrentIxCount() {
		return currentIxCount;
	}
	public void setCurrentIxCount(int currentIxCount) {
		this.currentIxCount = currentIxCount;
	}
	public int getValidIdsinRangeCount() {
		return validIdsinRangeCount;
	}
	public void setValidIdsinRangeCount(int validIdsinRangeCount) {
		this.validIdsinRangeCount = validIdsinRangeCount;
	}
	
	@Override
    public String toString() {
        return  super.toString() + 
        		", policyReusableIndexed= "+ policyReusableIndexed +         	
        		", validatedOk= "+ validatedOk +         	
        		", errorMsg= "+ errorMsg +        
        		", currentIxCount= "+ currentIxCount +                		
        		", validIdsinRangeCount= "+ validIdsinRangeCount +                		
        		"]";
	}

}
