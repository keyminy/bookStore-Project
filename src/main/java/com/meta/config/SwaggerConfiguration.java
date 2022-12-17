package com.meta.config;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.ApiSelectorBuilder;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfiguration implements WebMvcConfigurer{
	
	 private static final Set<String> DEFAULT_PRODUCES_AND_CONSUMES = new HashSet<>(
	            Arrays.asList("application/json","application/xml"));
	
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
	@Bean
	public Docket docket() {
		ApiInfoBuilder apiInfo = new ApiInfoBuilder();
		apiInfo.title("API 서버 문서");
		apiInfo.description("API 서버 문서 입니다.");
		
		Docket docket = new Docket(DocumentationType.SWAGGER_2);
		docket.produces(DEFAULT_PRODUCES_AND_CONSUMES);
		docket.consumes(DEFAULT_PRODUCES_AND_CONSUMES);
		docket.apiInfo(apiInfo.build());
		
		ApiSelectorBuilder apis = docket.select().apis(RequestHandlerSelectors.any());
		apis.paths(PathSelectors.ant("/**"));//모든 URL허용
		return apis.build();
	}

}
