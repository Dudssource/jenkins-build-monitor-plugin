package com.smartcodeltd.jenkinsci.plugin.assetbundler.filters;

import com.github.sommeri.less4j.Less4jException;
import com.github.sommeri.less4j.LessCompiler;
import com.github.sommeri.less4j.core.DefaultLessCompiler;
import javax.servlet.Filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class LessCSS implements Filter {
    private final File lessFile;
    private final String pathToCSS;

    private String compiledCSS;

    public LessCSS(String pathToCSS, File pathToLess) throws URISyntaxException {
        this.pathToCSS  = pathToCSS;
        this.lessFile   = pathToLess;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        try {
            compiledCSS = cssFrom(lessFile);
        } catch (Less4jException e) {
            throw new ServletException("Couldn't compile the CSS from LESS sources", e);
        }
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String path = ((HttpServletRequest) request).getPathInfo();
        if (path == null) {
            path = ((HttpServletRequest) request).getServletPath();
        }

        if (path != null && path.matches(pathToCSS)) {
            sendCSS(response);
        } else {
            chain.doFilter(request, response);
        }
    }

    private String cssFrom(File less) throws Less4jException {
        LessCompiler compiler             = new DefaultLessCompiler();
        LessCompiler.Configuration config = new LessCompiler.Configuration();

        config.setCompressing(false);
        config.getSourceMapConfiguration().setLinkSourceMap(false);

        return compiler.compile(less, config).getCss();
    }

    private void sendCSS(ServletResponse response) throws IOException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        httpResponse.setStatus(HttpServletResponse.SC_OK);
        httpResponse.setContentType("text/css;charset=UTF-8");
        httpResponse.setContentLength(compiledCSS.length());
        httpResponse.getWriter().write(compiledCSS);
    }

    @Override
    public void destroy() {}
}
