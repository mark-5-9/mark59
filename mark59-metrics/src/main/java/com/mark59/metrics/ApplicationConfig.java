/*
 *  Copyright 2019 Insurance Australia Group Limited
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  you may not use this file except in compliance with the License. 
 *  You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mark59.metrics;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mark59.metrics.data.base.dao.BaseDAO;
import com.mark59.metrics.data.base.dao.BaseDAOjdbcTemplateImpl;
import com.mark59.metrics.data.commandResponseParsers.dao.CommandResponseParsersDAO;
import com.mark59.metrics.data.commandResponseParsers.dao.CommandResponseParsersDAOjdbcTemplateImpl;
import com.mark59.metrics.data.commandparserlinks.dao.CommandParserLinksDAO;
import com.mark59.metrics.data.commandparserlinks.dao.CommandParserLinksDAOjdbcTemplateImpl;
import com.mark59.metrics.data.commands.dao.CommandsDAO;
import com.mark59.metrics.data.commands.dao.CommandsDAOjdbcTemplateImpl;
import com.mark59.metrics.data.servercommandlinks.dao.ServerCommandLinksDAO;
import com.mark59.metrics.data.servercommandlinks.dao.ServerCommandLinksDAOjdbcTemplateImpl;
import com.mark59.metrics.data.serverprofiles.dao.ServerProfilesDAO;
import com.mark59.metrics.data.serverprofiles.dao.ServerProfilesDAOjdbcTemplateImpl;


/**
 * Create  Spring bean(s) via program rather than XML configuration<br>
 * 
 * @author Philip Webb
 * Written: Australian Autumn 2020  
 */
@Configuration
public class ApplicationConfig {

    @Value("${spring.profiles.active}")
    private String springProfilesActive;	
	
    @Bean
    public String currentDatabaseProfile() {
        return springProfilesActive;
    }   
	
    @Bean
    public PropertiesConfiguration PropertiesConfiguration() {
        return new PropertiesConfiguration();
    }   
    
    /**
     * This method is equivalent to the following appConfig.xml:
	 * <pre><code>
 	 * &lt;bean id="ServersDAO" 	
	 *	class="com.mark59.metrics.data.serverprofiles.dao.ServerProfilesDAOjdbcTemplateImpl"&gt;
	 * &lt;/bean&gt;
     * </code></pre>
     */
    @Bean
    public ServerProfilesDAO serverProfilesDAO() {
        return new ServerProfilesDAOjdbcTemplateImpl();
    }
    
    @Bean
    public CommandsDAO commandsDAO() {
    	return new CommandsDAOjdbcTemplateImpl();
    } 

    @Bean
    public CommandResponseParsersDAO commandResponseParsersDAO() {
        return new CommandResponseParsersDAOjdbcTemplateImpl();
    } 
 
   @Bean
    public ServerCommandLinksDAO serverCommandLinksDAO() {
	    return new ServerCommandLinksDAOjdbcTemplateImpl();
	}     

    @Bean
    public CommandParserLinksDAO commandParserLinksDAO() {
	    return new CommandParserLinksDAOjdbcTemplateImpl();
	}     
        
    @Bean
    public BaseDAO baseDAO() {
	    return new BaseDAOjdbcTemplateImpl();
	}     
    
}