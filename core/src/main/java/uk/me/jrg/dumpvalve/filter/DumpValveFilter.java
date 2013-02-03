package uk.me.jrg.dumpvalve.filter;

import uk.me.jrg.dumpvalve.mbean.Configuration;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class DumpValveFilter implements Filter {

    private String contextPath;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        contextPath = filterConfig.getServletContext().getContextPath();
        Configuration.getInstance().addContext(contextPath);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        // if not enabled, invoke the next valve and exit
        if ((! Configuration.getInstance().isEnabled()) || (! Configuration.getInstance().isContextEnabled(contextPath))) {
            filterChain.doFilter(request, response);
            return;
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        try {
            MultiReadRequest multiReadRequest = new MultiReadRequest(httpRequest);

            long startTime = System.currentTimeMillis();
            filterChain.doFilter(multiReadRequest, response);

            long endTime = System.currentTimeMillis();
            long timeTaken = endTime - startTime;

            System.out.println("=============================================");
            System.out.println("URL: " + httpRequest.getRequestURI());
            System.out.println("Time taken: " + timeTaken + " ms");

            String postBody = multiReadRequest.getBodyAsString();

            System.out.println(postBody);
            System.out.println("=============================================");
        } catch (Throwable t) {
            t.printStackTrace();
            throw new ServletException(t);
        }
    }

    @Override
    public void destroy() {
    }
}
