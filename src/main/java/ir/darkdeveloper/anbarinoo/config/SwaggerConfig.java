package ir.darkdeveloper.anbarinoo.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.Contact;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
    

    @Bean
    public Docket swaggerConfiguration(){
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .securityContexts(Collections.singletonList(securityContext()))
                .securitySchemes(Arrays.asList(apiKey("refresh","refresh_token"), apiKey("access","access_token")))
                .select()
                .paths(PathSelectors.ant("/api/**"))
                .apis(RequestHandlerSelectors.basePackage("ir.darkdeveloper"))
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfo(
          "REST API OVERVIEW",
          "apis are only for overview in swagger",
          "1.0",
          "Terms of service",
          new Contact("DarkDeveloper", "", "darkdeveloper@outlook.com"),
          "License of API",
          "API license URL",
          Collections.emptyList());
    }
 
    private ApiKey apiKey(String key, String name) { 
        return new ApiKey(key, name, "header"); 
    }

    private SecurityContext securityContext() { 
        return SecurityContext.builder().securityReferences(defaultAuth()).build(); 
    } 
    
    private List<SecurityReference> defaultAuth() { 
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything"); 
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[2]; 
        authorizationScopes[0] = authorizationScope; 
        authorizationScopes[1] = authorizationScope; 
        return Arrays.asList(new SecurityReference("refresh", authorizationScopes), new SecurityReference("access", authorizationScopes)); 
    }

}
