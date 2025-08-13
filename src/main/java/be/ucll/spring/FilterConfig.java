package be.ucll.spring;

import be.ucll.filters.LoggingFilter;
import be.ucll.filters.RequestHeaderFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Value("${info.app.version}")
    private String appVersion;

    @Bean
    public FilterRegistrationBean<LoggingFilter> loggingFilter() {
        FilterRegistrationBean<LoggingFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new LoggingFilter());
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(1);
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<RequestHeaderFilter> requestHeaderFilter() {
        FilterRegistrationBean<RequestHeaderFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new RequestHeaderFilter(appVersion));
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(2);
        return registrationBean;
    }
}
