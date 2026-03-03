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

package com.mark59.datahunter;

import org.h2.server.web.JakartaWebServlet;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Manual H2 Console configuration for Spring Boot 4.x compatibility
 * Required because H2 console auto-configuration may not work properly with context paths in Spring Boot 4.x
 */
@Configuration
@ConditionalOnProperty(name = "spring.h2.console.enabled", havingValue = "true")
public class H2ConsoleConfiguration {

    @Bean
    ServletRegistrationBean<JakartaWebServlet> h2ConsoleServletRegistration() {
        ServletRegistrationBean<JakartaWebServlet> registrationBean =
            new ServletRegistrationBean<>(new JakartaWebServlet());
        registrationBean.addUrlMappings("/h2-console/*");
        registrationBean.setLoadOnStartup(1);
        return registrationBean;
    }
}
