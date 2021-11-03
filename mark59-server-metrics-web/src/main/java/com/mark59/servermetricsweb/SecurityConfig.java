package com.mark59.servermetricsweb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Basic Spring Security configuration.  Forcing login into the Web Application, 
 * allowing open access to the api service.
 * 
 * <p>References:<br>
 * https://www.baeldung.com/spring-boot-security-autoconfiguration<br>
 * https://www.baeldung.com/spring-security-login
 * 
 * @author Philip Webb
 * Written: Australian Autumn 2020  
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	PropertiesConfiguration springBootConfiguration;	
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

		String userid   = springBootConfiguration.getMark59servermetricswebuserid();
		String passwrd  = springBootConfiguration.getMark59servermetricswebpasswrd();
		String hide     = springBootConfiguration.getMark59servermetricswebhide();
		
		if ( hide!= null && (hide.toLowerCase().startsWith("y") || hide.toLowerCase().startsWith("t"))){
			System.out.println("hide activated");
		} else {
			System.out.println("userid=" + userid + ",passwrd=" + passwrd 
					+ "       Please set 'mark59servermetricswebhide' as 'true' to hide credentials"
					+ " (either as a command line argument or OS environment variable)");
		}
		auth.inMemoryAuthentication().withUser(userid).password(encoder.encode(passwrd)).roles("USER");
	}
    

	@Override
	public void configure(HttpSecurity http) throws Exception {
		//https://www.yawintutor.com/how-to-enable-and-disable-csrf
        http.authorizeRequests()
        .antMatchers("/login").permitAll()
        .antMatchers("/logout").permitAll()
        .antMatchers("/api/**").permitAll()
        .antMatchers("/**").hasRole("USER")
        .and().formLogin().loginPage("/login").defaultSuccessUrl("/serverProfileList", true)
        .and().logout().logoutSuccessUrl("/login").permitAll();
	}
	
   @Override
    public void configure(WebSecurity web) throws Exception {
        web
         .ignoring()
         .antMatchers("/h2-console/**");
    }

}