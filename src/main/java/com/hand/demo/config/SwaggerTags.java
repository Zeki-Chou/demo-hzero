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
    public static final String TASK = "TASK";
    public static final String USER = "USER";
    public static final String FILE = "FILE";
    public static final String INV = "INV";
    public static final String EXTERNAL = "EXTERNAL";

    @Autowired
    public SwaggerTags(Docket docket) {
        docket.tags(
                new Tag(EXAMPLE, "EXAMPLE 案例"),
                new Tag(TASK, "TASK"),
                new Tag(USER, "USER"),
                new Tag(FILE, "FILE"),
                new Tag(INV, "INV"),
                new Tag(EXTERNAL, "EXTERNAL")


        );
    }
}
