package com.savit.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = {
        "com.savit.user.controller",
        "com.savit.budget.controller",
        "com.savit.card.controller",
        "com.savit.challenge.controller",
        "com.savit.notification.controller",
        "com.savit.scheduler.controller",
        "com.savit.common.exception"
})public class ServletConfig {
}
