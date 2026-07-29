/*
 *  Copyright 2019 Mark59.com
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

import javax.sql.DataSource;

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
import com.mark59.trends.slaIcons.SlaIconColourCodes;
import com.mark59.trends.slaIcons.SlaIconColourCodesInterface;

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
    String currentDatabaseProfile() {
        return springProfilesActive;
    }

    @Value("${h2.port:UNUSED}")
    private String h2port;

    @Bean
    String h2Port() {
        return h2port;
    }


    @Bean
    ApplicationDAO applicationDAO(DataSource dataSource) {
        return new ApplicationDAOjdbcTemplateImpl(dataSource);
    }

    @Bean
    GraphMappingDAO graphMappingDAO(DataSource dataSource) {
        return new GraphMappingDAOjdbcTemplateImpl(dataSource);
    }

    @Bean
    TransactionDAO transactionDAO(DataSource dataSource) {
        return new TransactionDAOjdbcTemplateImpl(dataSource, graphMappingDAO(dataSource));
    }

    @Bean
    RunDAO runDAO(DataSource dataSource) {
        return new RunDAOjdbcTemplateImpl(dataSource, applicationDAO(dataSource), transactionDAO(dataSource));
    }

    @Bean
    SlaDAO slaDAO(DataSource dataSource) {
        return new SlaDAOjdbcImpl(dataSource);
    }

    @Bean
    MetricSlaDAO metricSlaDAO(DataSource dataSource) {
        return new MetricSlaDAOjdbcImpl(dataSource);
    }

    @Bean
    EventMappingDAO eventMappingDAO(DataSource dataSource) {
        return new EventMappingDAOjdbcTemplateImpl(dataSource, currentDatabaseProfile());
    }

    @Bean
    TestTransactionsDAO testTransactionsDAO(DataSource dataSource) {
        return new TestTransactionsDAOjdbcTemplateImpl(dataSource, currentDatabaseProfile());
    }

    @Bean
    VisGraphicDataProductionInterface visGraphicDataProduction(DataSource dataSource) {
        return new VisGraphicDataProduction(transactionDAO(dataSource));
    }

    @Bean
    SlaIconColourCodesInterface slaIconColourCodes(DataSource dataSource) {
	    return new SlaIconColourCodes(runDAO(dataSource), slaDAO(dataSource), metricSlaDAO(dataSource), transactionDAO(dataSource));
	}


    @Bean(initMethod = "start", destroyMethod = "stop")
    Server h2DatabaseServer() throws SQLException {

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