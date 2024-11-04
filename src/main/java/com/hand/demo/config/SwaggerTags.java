package com.hand.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.service.Tag;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * Swagger Api 描述配置
 */
@Configuration
public class SwaggerTags {

    public static final String EXAMPLE = "Example";
    public static final String TASK = "Task";
    public static final String USER = "User";
    public static final String MESSAGE = "Message";

    @Autowired
    public SwaggerTags(Docket docket) {
        docket.tags(
                new Tag(EXAMPLE, "EXAMPLE case"),
                new Tag(TASK, "TASK case"),
                new Tag(USER, "USER case"),
                new Tag(MESSAGE, "MESSAGE case")
        );
    }
}
