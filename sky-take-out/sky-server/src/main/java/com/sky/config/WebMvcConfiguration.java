package com.sky.config;

import com.sky.interceptor.JwtTokenAdminInterceptor;
import com.sky.interceptor.JwtTokenUserInterceptor;
import com.sky.json.JacksonObjectMapper;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * 配置类，注册 web 层相关组件。
 *
 * <p>升级要点一：把 Springfox/Knife4j 的两个 Docket 换成 SpringDoc 的
 * OpenAPI + GroupedOpenApi 分组；接口行为零改动，文档地址变为 /swagger-ui/index.html。</p>
 *
 * <p>升级要点二：由 extends WebMvcConfigurationSupport 改为 implements WebMvcConfigurer。
 * 前者会禁用 Spring Boot 的 WebMvc 自动配置，导致 SpringDoc 的 swagger-ui 静态资源
 * 映射失效（/swagger-ui/index.html 404）；改为实现接口后，SpringDoc 自动映射生效，
 * 而拦截器 / 消息转换器 / CORS 逻辑全部保留。</p>
 */
@Configuration
@Slf4j
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Autowired
    private JwtTokenAdminInterceptor jwtTokenAdminInterceptor;
    @Autowired
    private JwtTokenUserInterceptor jwtTokenUserInterceptor;

    /**
     * 注册自定义拦截器。
     *
     * @param registry 拦截器注册表
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("开始注册自定义拦截器...");
        registry.addInterceptor(jwtTokenAdminInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/employee/login");

        registry.addInterceptor(jwtTokenUserInterceptor)
                .addPathPatterns("/user/**")
                // 排除不需要拦截的路径
                .excludePathPatterns("/user/user/login")
                // 网站登录用 mockLogin，无需 token，必须放行
                .excludePathPatterns("/user/user/mockLogin")
                // 短信验证码登录相关：发送验证码与验证码登录均无需 token，必须放行
                .excludePathPatterns("/user/user/sendCode")
                .excludePathPatterns("/user/user/smsLogin")
                .excludePathPatterns("/user/shop/status");
    }

    /**
     * 全局 API 文档元信息（标题、版本）。
     *
     * @return OpenAPI 对象
     */
    @Bean
    public OpenAPI weilaiOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("味来 Weilai 接口文档")
                .version("2.0")
                .description("味来 Weilai 接口文档"));
    }

    /**
     * 管理端接口分组，扫描 com.sky.controller.admin 包。
     *
     * @return 分组对象
     */
    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("管理端接口")
                .packagesToScan("com.sky.controller.admin")
                .build();
    }

    /**
     * 用户端接口分组，扫描 com.sky.controller.user 包。
     *
     * @return 分组对象
     */
    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("用户端接口")
                .packagesToScan("com.sky.controller.user")
                .build();
    }

    /**
     * 跨域配置：允许前端开发服务器（Vite 默认 5173、备用 5174）访问，
     * 放行 token / authentication 自定义请求头及常用 HTTP 方法。
     *
     * @param registry 跨域注册表
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(
                        "http://localhost:5173",
                        "http://localhost:5174",
                        "http://127.0.0.1:5173",
                        "http://127.0.0.1:5174")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD")
                .allowedHeaders("*")
                .exposedHeaders("token", "authentication")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 扩展消息转换器：注入自定义 Jackson ObjectMapper（日期格式等）。
     *
     * @param converters 消息转换器列表
     */
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("扩展消息转换器...");
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(new JacksonObjectMapper());
        converters.add(0, converter);
    }
}
