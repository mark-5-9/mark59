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

package com.mark59.trends;

import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;
import org.h2.tools.Server;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mark59.trends.data.application.dao.ApplicationDAO;
import com.mark59.trends.data.application.dao.ApplicationDAOjdbcTemplateImpl;
import com.mark59.trends.data.eventMapping.dao.EventMappingDAO;
import com.mark59.trends.data.eventMapping.dao.EventMappingDAOjdbcTemplateImpl;
import com.mark59.trends.data.graphMapping.dao.GraphMappingDAO;
import com.mark59.trends.data.graphMapping.dao.GraphMappingDAOjdbcTemplateImpl;
import com.mark59.trends.data.metricSla.dao.MetricSlaDAO;
import com.mark59.trends.data.metricSla.dao.MetricSlaDAOjdbcImpl;
import com.mark59.trends.data.run.dao.RunDAO;
import com.mark59.trends.data.run.dao.RunDAOjdbcTemplateImpl;
import com.mark59.trends.data.sla.dao.SlaDAO;
import com.mark59.trends.data.sla.dao.SlaDAOjdbcImpl;
import com.mark59.trends.data.testTransactions.dao.TestTransactionsDAO;
import com.mark59.trends.data.testTransactions.dao.TestTransactionsDAOjdbcTemplateImpl;
import com.mark59.trends.data.transaction.dao.TransactionDAO;
import com.mark59.trends.data.transaction.dao.TransactionDAOjdbcTemplateImpl;
import com.mark59.trends.graphic.data.VisGraphicDataProduction;
import com.mark59.trends.graphic.data.VisGraphicDataProductionInterface;

/**
 * Create  Spring bean(s) via program rather than XML configuration<br>
 * <p>For example the applicationDAO method is equivalent to the following appConfig.xml:
 * <pre><code>
 * &lt;bean id="applicationDAO" 	
 *	class="com.mark59.metrics.data.application.dao.ApplicationDAOjdbcTemplateImpl"&gt;
 * &lt;/bean&gt;
 * </code></pre>
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

    @Value("${h2.port:UNUSED}")
    private String h2port;
    
    @Bean
    public String h2Port() {
        return h2port;
    }   
   
    
    @Bean
    public ApplicationDAO applicationDAO() {
        return new ApplicationDAOjdbcTemplateImpl();
    }
    
    @Bean
    public RunDAO runDAO() {
        return new RunDAOjdbcTemplateImpl();
    }
    
    @Bean
    public TransactionDAO transactionDAO() {
        return new TransactionDAOjdbcTemplateImpl();
    }
    
    @Bean
    public SlaDAO slaDAO() {
        return new SlaDAOjdbcImpl();
    }
    
    @Bean
    public MetricSlaDAO metricSlaDAO() {
        return new MetricSlaDAOjdbcImpl();
    }

    @Bean
    public GraphMappingDAO graphMappingDAO() {
        return new GraphMappingDAOjdbcTemplateImpl();
    }

    @Bean
    public EventMappingDAO eventMappingDAO() {
        return new EventMappingDAOjdbcTemplateImpl();
    }

    @Bean
    public TestTransactionsDAO testTransactionsDAO() {
        return new TestTransactionsDAOjdbcTemplateImpl();
    }
    
    @Bean
    public VisGraphicDataProductionInterface visGraphicDataProduction() {
        return new VisGraphicDataProduction();
    }    
  
    @Bean(initMethod = "start", destroyMethod = "stop")
    public Server h2DatabaseServer() throws SQLException {

    	if ( "h2tcpserver".equalsIgnoreCase(currentDatabaseProfile())){
    		if (StringUtils.isNumeric(h2Port())){
    			System.out.println("Starting H2 database using tcp port " + h2Port()  );
    	        return Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", h2Port());
    		} else {
    			System.out.println("Starting H2 database using tcp default port 9092");
    	        return Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", "9092"); 
    		}    
    	}
    	return null;	
    }    
    
}