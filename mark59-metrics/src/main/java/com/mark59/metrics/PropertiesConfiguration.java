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
	private String mark59metricswmicdelay;
	
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
	public String getMark59metricswmicdelay() {
		return mark59metricswmicdelay;
	}
	public void setMark59metricswmicdelay(String mark59metricswmicdelay) {
		this.mark59metricswmicdelay = mark59metricswmicdelay;
	}
}
