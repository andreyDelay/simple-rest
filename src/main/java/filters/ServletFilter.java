package filters;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@WebFilter(urlPatterns = {
        "/users/*",
        "/accounts/*",
        "/files/*",
        "/events/*"})
public class ServletFilter extends AbstractFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {
        String httpMethod = getHttpMethod(servletRequest);
        switch (httpMethod) {
            case "GET" -> {
                forwardUrlIfMatchPattern(servletRequest, servletResponse);
                if (isErrorOccurred()){
                    return;
                }
                filterChain.doFilter(servletRequest, servletResponse);
            }
            case "POST", "DELETE", "PUT" -> {
                String url = ((HttpServletRequest) servletRequest).getRequestURI();
                if (isUrlMatchesAnyPattern(url)) {
                    filterChain.doFilter(servletRequest, servletResponse);
                }
            }
            default -> {
                HttpServletRequest request = (HttpServletRequest) servletRequest;
                request.getRequestDispatcher("/").forward(servletRequest, servletResponse);
            }
        }
    }

    @Override
    public void destroy() {

    }

}
