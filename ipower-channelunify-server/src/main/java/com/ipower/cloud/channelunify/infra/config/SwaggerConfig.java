package com.ipower.cloud.channelunify.infra.config;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;

import java.time.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * swagger配置文件
 */

//自定义swagger3文档信息
@Configuration
@ConditionalOnProperty(value = "springfox.documentation.enabled", havingValue = "true", matchIfMissing = true)
//如果有自定义SwaggerAutoConfiguration覆盖了配置，则不再进行自动配置
@ConditionalOnMissingBean(value = {Docket.class})
public class SwaggerConfig {

    @Autowired
    Environment env;

    private String productEnv = "pro";

    private Boolean isProductEnv() {
        String[] activeEnvs = env.getActiveProfiles();
        for (String activeEnv : activeEnvs) {
            if (productEnv.contains(activeEnv)) {
                return true;
            }
        }
        return false;
    }

//    设置一个通用参数这个是swagger2的写法，已经过期了
//    ParameterBuilder tokenPar = new ParameterBuilder();
//    List<Parameter> pars = new ArrayList<Parameter>();
//        tokenPar.name("x-access-token").description("令牌").modelRef(new ModelRef("string")).parameterType("header").required(false).build();
//        pars.add(tokenPar.build());

    //下面举例 配置代码，为swagger-ui.html页面添加全局header参数，user_id、token、firm_source_id
    //设置一个公用的安全请求参数结构类型
    private List<SecurityScheme> securitySchemes() {
        ArrayList arrayList = new ArrayList();
        arrayList.add(new ApiKey("x-access-token", "x-access-token", "header"));
        arrayList.add(new ApiKey("jwt-access-token", "jwt-access-token", "header"));
        arrayList.add(new ApiKey("access-token", "access-token", "header"));
        arrayList.add(new ApiKey("client-token", "client-token", "header"));
        arrayList.add(new ApiKey("client-id", "client-id", "header"));
        arrayList.add(new ApiKey("source", "source", "header"));
        arrayList.add(new ApiKey("comId", "comId", "header"));
        arrayList.add(new ApiKey("clubId", "clubId", "header"));
        return arrayList;
    }

    //设置securitySchemes起作用的范围
    private List<SecurityContext> securityContexts() {
        List<SecurityContext> securityContextList = new ArrayList<>();
        List<SecurityReference> securityReferenceList = new ArrayList<>();
        securityReferenceList.add(new SecurityReference("x-access-token", scopes())); //正常登录 token使用
        securityReferenceList.add(new SecurityReference("jwt-access-token", scopes())); //jwt token
        securityReferenceList.add(new SecurityReference("access-token", scopes()));//oauth2 1,2,3模式使用
        securityReferenceList.add(new SecurityReference("client-token", scopes()));//oauth2 4模式使用

        securityReferenceList.add(new SecurityReference("comId", scopes()));//oauth2 4模式使用
        securityReferenceList.add(new SecurityReference("clubId", scopes()));//oauth2 4模式使用

        securityContextList.add(SecurityContext
                .builder()
                .securityReferences(securityReferenceList)
                .forPaths(PathSelectors.any())
                .build()
        );
        return securityContextList;
    }

    private AuthorizationScope[] scopes() {
        return new AuthorizationScope[]{
                new AuthorizationScope("global", "accessAnything")
        };
    }


    @Bean
    public Docket appDocket() {

        return new Docket(
//                DocumentationType.OAS_30)
                DocumentationType.SWAGGER_2)
                .enable(!isProductEnv())
//                //设置接口安全类型,增加一个全局的JWT参数
//                .securitySchemes(Collections.singletonList(HttpAuthenticationScheme.JWT_BEARER_BUILDER.name("JWT").build()))//显示用
//                //设置安全验证的作用域
//                .securityContexts(
//                        Collections.singletonList(SecurityContext.builder().securityReferences(Collections.singletonList(SecurityReference.builder().scopes(new AuthorizationScope[0]).reference("JWT").build()))
//                        // 声明作用域对所有请求链接生效
//                        .operationSelector(o -> o.requestMappingPattern().matches("/.*"))
//                        .build()))
                .securitySchemes(securitySchemes())
                .securityContexts(securityContexts())
                //选择器，要有ApiOperation注解
                .select()
                .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
                //路径选择器，一般会把app,web,ws,api等等按请求来源不同做n种类型的分类
                .paths(PathSelectors.ant("/app/**"))
                .build()
                //展示分组
                .groupName("1.app")
                //设置通用参数,这个是swagger2的写法，已经过期了
//                .globalOperationParameters(pars)
                .apiInfo(apiInfo())
                //设置日期在文档中显示为字符串
                .directModelSubstitute(Date.class, String.class)
                .directModelSubstitute(LocalDateTime.class, String.class)
                .directModelSubstitute(LocalDate.class, String.class)
                .directModelSubstitute(LocalTime.class, String.class)
                .directModelSubstitute(ZonedDateTime.class, String.class)
                .directModelSubstitute(YearMonth.class, String.class)
                ;
    }

    @Bean
    public Docket webDocket() {

        return new Docket(
//                DocumentationType.OAS_30)
                DocumentationType.SWAGGER_2)
                .enable(!isProductEnv())
                .securitySchemes(securitySchemes())
                .securityContexts(securityContexts())
                //选择器，要有ApiOperation注解
                .select()
                .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
                //路径选择器，一般会把app,web,ws,api等等按请求来源不同做n种类型的分类
                .paths(PathSelectors.ant("/web/**"))
                .build()
                //展示分组
                .groupName("2.web")
                .apiInfo(apiInfo())
                //设置日期在文档中显示为字符串
                .directModelSubstitute(Date.class, String.class)
                .directModelSubstitute(LocalDateTime.class, String.class)
                .directModelSubstitute(LocalDate.class, String.class)
                .directModelSubstitute(LocalTime.class, String.class)
                .directModelSubstitute(ZonedDateTime.class, String.class)
                .directModelSubstitute(YearMonth.class, String.class)
                ;
    }

    @Bean
    public Docket wsDocket() {

        return new Docket(
//                DocumentationType.OAS_30)
                DocumentationType.SWAGGER_2)
                .enable(!isProductEnv())
                .securitySchemes(securitySchemes())
                .securityContexts(securityContexts())
                //选择器，要有ApiOperation注解
                .select()
                .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
                //路径选择器，一般会把app,web,ws,api等等按请求来源不同做n种类型的分类
                .paths(PathSelectors.ant("/ws/**"))
                .build()
                //展示分组
                .groupName("3.ws")
                .apiInfo(apiInfo())
                //设置日期在文档中显示为字符串
                .directModelSubstitute(Date.class, String.class)
                .directModelSubstitute(LocalDateTime.class, String.class)
                .directModelSubstitute(LocalDate.class, String.class)
                .directModelSubstitute(LocalTime.class, String.class)
                .directModelSubstitute(ZonedDateTime.class, String.class)
                .directModelSubstitute(YearMonth.class, String.class)
                ;
    }

    @Bean
    public Docket apiDocket() {

        return new Docket(
//                DocumentationType.OAS_30)
                DocumentationType.SWAGGER_2)
                .enable(!isProductEnv())
                .securitySchemes(securitySchemes())
                .securityContexts(securityContexts())
                //选择器，要有ApiOperation注解
                .select()
                .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
                //路径选择器，一般会把app,web,ws,api等等按请求来源不同做n种类型的分类
                .paths(PathSelectors.ant("/api/**"))
                .build()
                //展示分组
                .groupName("4.api")
                .apiInfo(apiInfo())
                //设置日期在文档中显示为字符串
                .directModelSubstitute(Date.class, String.class)
                .directModelSubstitute(LocalDateTime.class, String.class)
                .directModelSubstitute(LocalDate.class, String.class)
                .directModelSubstitute(LocalTime.class, String.class)
                .directModelSubstitute(ZonedDateTime.class, String.class)
                .directModelSubstitute(YearMonth.class, String.class)
                ;
    }

    /**
     * 基础描述信息
     */
    private ApiInfo apiInfo() {

        return new ApiInfoBuilder()
                .title("mars")
                .termsOfServiceUrl("http://www.mars.net")
                .description("mars swagger3")
                .contact(new Contact("mars", "http://www.mars.net", "mars@mars.com"))
                .build();
    }

}
