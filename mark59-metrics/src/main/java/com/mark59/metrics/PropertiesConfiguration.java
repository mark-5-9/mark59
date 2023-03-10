package com.mark59.metrics;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Reference: https://www.theserverside.com/video/How-applicationproperties-simplifies-Spring-config 
 * Note: something like @ConfigurationProperties(prefix="mark59.metrics") as shown in reference 
 * isn't used as dots don't work nicely with shell. Also in Linux you need to execute from terminal 
 * (a GUI editor may not pick up env vars)    
 * 
 * @author Philip Webb
 * Written: Australian Autumn 2020    
 */
@ConfigurationProperties()
public class PropertiesConfiguration {
	
	private String mark59metricsid;
	private String mark59metricspasswrd;
	private String mark59metricshide;

	private String mark59metricsapiauth;
	private String mark59metricsapiuser;
	private String mark59metricsapipass;
	
	public String getMark59metricsid() {
		return mark59metricsid;
	}
	public void setMark59metricsid(String mark59metricsid) {
		this.mark59metricsid = mark59metricsid;
	}
	public String getMark59metricspasswrd() {
		return mark59metricspasswrd;
	}
	public void setMark59metricspasswrd(String mark59metricspasswrd) {
		this.mark59metricspasswrd = mark59metricspasswrd;
	}
	public String getMark59metricshide() {
		return mark59metricshide;
	}
	public void setMark59metricshide(String mark59metricshide) {
		this.mark59metricshide = mark59metricshide;
	}
	public String getMark59metricsapiauth() {
		return mark59metricsapiauth;
	}
	public void setMark59metricsapiauth(String mark59metricsapiauth) {
		this.mark59metricsapiauth = mark59metricsapiauth;
	}
	public String getMark59metricsapiuser() {
		return mark59metricsapiuser;
	}
	public void setMark59metricsapiuser(String mark59metricsapiuser) {
		this.mark59metricsapiuser = mark59metricsapiuser;
	}
	public String getMark59metricsapipass() {
		return mark59metricsapipass;
	}
	public void setMark59metricsapipass(String mark59metricsapipass) {
		this.mark59metricsapipass = mark59metricsapipass;
	}

}
