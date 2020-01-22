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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mark59.metrics.data.application.dao.ApplicationDAO;
import com.mark59.metrics.data.application.dao.ApplicationDAOjdbcTemplateImpl;
import com.mark59.metrics.data.eventMapping.dao.EventMappingDAO;
import com.mark59.metrics.data.eventMapping.dao.EventMappingDAOjdbcTemplateImpl;
import com.mark59.metrics.data.graphMapping.dao.GraphMappingDAO;
import com.mark59.metrics.data.graphMapping.dao.GraphMappingDAOjdbcTemplateImpl;
import com.mark59.metrics.data.metricSla.dao.MetricSlaDAO;
import com.mark59.metrics.data.metricSla.dao.MetricSlaDAOjdbcImpl;
import com.mark59.metrics.data.run.dao.RunDAO;
import com.mark59.metrics.data.run.dao.RunDAOjdbcTemplateImpl;
import com.mark59.metrics.data.sla.dao.SlaDAO;
import com.mark59.metrics.data.sla.dao.SlaDAOjdbcImpl;
import com.mark59.metrics.data.testTransactions.dao.TestTransactionsDAO;
import com.mark59.metrics.data.testTransactions.dao.TestTransactionsDAOjdbcTemplateImpl;
import com.mark59.metrics.data.transaction.dao.TransactionDAO;
import com.mark59.metrics.data.transaction.dao.TransactionDAOjdbcTemplateImpl;
import com.mark59.metrics.services.SlaService;
import com.mark59.metrics.services.SlaServiceImpl;

/**
 * Create  Spring bean(s) via program rather than XML configuration<br>
 * <p>For example the applicationDAO method is equivalent to the following appConfig.xml:
 * <pre><code>
 * &lt;bean id="applicationDAO" 	
 *	class="com.mark59.metrics.data.application.dao.ApplicationDAOjdbcTemplateImpl"&gt;
 * &lt;/bean&gt;
 * </code></pre>
 */
@Configuration
public class ApplicationConfig {

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
    public SlaService slaService() {
        return new SlaServiceImpl();
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
    
}