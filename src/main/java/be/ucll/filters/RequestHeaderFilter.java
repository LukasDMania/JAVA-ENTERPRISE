package be.ucll.filters;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;

public class RequestHeaderFilter implements Filter {

    private final String appVersion;

    public RequestHeaderFilter(String appVersion) {
        this.appVersion = appVersion;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletResponse resp = (HttpServletResponse) response;
        resp.addHeader("App-Version", appVersion);
        chain.doFilter(request, response);
    }
}
