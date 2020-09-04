package com.mark59.servermetricsweb;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * reference: 
 * https://www.theserverside.com/video/How-applicationproperties-simplifies-Spring-config 
 * Note: something like @ConfigurationProperties(prefix="mark59.user") as dots don't work nicely with shell. Also 
 * in Linux you need to execute from terminal (a GUI editor may not pick up env vars)    
 * 
 * @author Philip Webb
 * Written: Australian Autumn 2020    
 */
@ConfigurationProperties()
public class PropertiesConfiguration {
	
	private String mark59servermetricswebuserid;
	private String mark59servermetricswebpasswrd;
	private String mark59servermetricswebhide;
	
	public String getMark59servermetricswebuserid() {
		return mark59servermetricswebuserid;
	}
	public void setMark59servermetricswebuserid(String mark59servermetricswebuserid) {
		this.mark59servermetricswebuserid = mark59servermetricswebuserid;
	}
	public String getMark59servermetricswebpasswrd() {
		return mark59servermetricswebpasswrd;
	}
	public void setMark59servermetricswebpasswrd(String mark59servermetricswebpasswrd) {
		this.mark59servermetricswebpasswrd = mark59servermetricswebpasswrd;
	}
	public String getMark59servermetricswebhide() {
		return mark59servermetricswebhide;
	}
	public void setMark59servermetricswebhide(String mark59servermetricswebhide) {
		this.mark59servermetricswebhide = mark59servermetricswebhide;
	}
	
	
}
