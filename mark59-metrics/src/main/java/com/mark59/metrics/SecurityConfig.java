package com.mark59.metrics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Basic Spring Security configuration.  Forcing login into the Web Application, 
 * allowing open access to the api service.
 * 
 * <p>References<br>
 * https://www.baeldung.com/spring-boot-security-autoconfiguration<br>
 * https://www.baeldung.com/spring-security-login<br>
 * - Above reference uses classes deprecated in spring boot 2.7, refactor details at<br>
 * https://spring.io/blog/2022/02/21/spring-security-without-the-websecurityconfigureradapter<br>
 * https://stackoverflow.com/questions/41961270/h2-console-and-spring-security-permitall-not-working<br>
 * https://stackoverflow.com/questions/65894268/how-does-headers-frameoptions-disable-work
 * 
 * @author Philip Webb
 * Written: Australian Autumn 2020  
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Autowired
	PropertiesConfiguration springBootConfiguration;	
	
	
    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
    	
		PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

		String id       = springBootConfiguration.getMark59metricsid();
		String passwrd  = springBootConfiguration.getMark59metricspasswrd();
		String hide     = springBootConfiguration.getMark59metricshide();
    	
		if (hide != null && (hide.toLowerCase().startsWith("y") || hide.toLowerCase().startsWith("t"))) {
			System.out.println("hide activated");
		} else {
			System.out.println("id=" + id + ",passwrd=" + passwrd
					+ "       Please set 'mark59metricshide' as 'true' to hide credentials"
					+ " (either as a command line argument or OS environment variable)");
		}
		
        UserDetails user = User
            .withUsername(id)
            .password(encoder.encode(passwrd))
            .roles("USER")
            .build();
        return new InMemoryUserDetailsManager(user);
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests()
            .antMatchers("/login").permitAll()
            .antMatchers("/logout").permitAll()
            .antMatchers("/api/**").permitAll()
            .antMatchers("/**").hasRole("USER")            
            .and().formLogin().loginPage("/login").defaultSuccessUrl("/serverProfileList", true)
            .and().logout().logoutSuccessUrl("/login").permitAll()
             // for h2-console (see references):
            .and().csrf().ignoringAntMatchers("/h2-console/**")
	        .and().headers().frameOptions().sameOrigin(); 
        return http.build();
    }
    
}
