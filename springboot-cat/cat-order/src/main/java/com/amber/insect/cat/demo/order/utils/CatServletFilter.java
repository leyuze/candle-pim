package com.amber.insect.cat.demo.order.utils;

import com.alidaodao.app.Cat;
import com.alidaodao.app.CatConstants;
import com.alidaodao.app.message.Message;
import com.alidaodao.app.message.Transaction;

import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;


/**
 * <p>Description: </p>
 * @date  
 * @author   
 * @version 1.0
 * @name   
 * <p>Copyright:Copyright(c)2020</p>
 */
public class CatServletFilter implements Filter {

    private String[] urlPatterns = new String[0];

    /**
     * 初始化配置
     * @param filterConfig
     * @throws ServletException
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String patterns = filterConfig.getInitParameter("CatHttpModuleUrlPatterns");
        if (patterns != null) {
            patterns = patterns.trim();
            urlPatterns = patterns.split(",");
            for (int i = 0; i < urlPatterns.length; i++) {
                urlPatterns[i] = urlPatterns[i].trim();
            }
        }
    }

    /**
     * 请求过滤处理
     * @param servletRequest
     * @param servletResponse
     * @param filterChain
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;

        String url = request.getRequestURL().toString();
        for (String urlPattern : urlPatterns) {
            if (url.startsWith(urlPattern)) {
                url = urlPattern;
            }
        }

        // cat 上下文信息设置
        CatContext catContext = new CatContext();
        catContext.addProperty(Cat.Context.ROOT, request.getHeader(CatHttpConstants.CAT_HTTP_HEADER_ROOT_MESSAGE_ID));
        catContext.addProperty(Cat.Context.PARENT, request.getHeader(CatHttpConstants.CAT_HTTP_HEADER_PARENT_MESSAGE_ID));
        catContext.addProperty(Cat.Context.CHILD, request.getHeader(CatHttpConstants.CAT_HTTP_HEADER_CHILD_MESSAGE_ID));
        Cat.logRemoteCallServer(catContext);

        Transaction t = Cat.newTransaction(CatConstants.TYPE_URL, url);

        try {
            // cat日志记录
            Cat.logEvent("Service.method", request.getMethod(), Message.SUCCESS, request.getRequestURL().toString());
            Cat.logEvent("Service.client", request.getRemoteHost());

            filterChain.doFilter(servletRequest, servletResponse);

            t.setStatus(Transaction.SUCCESS);
        } catch (Exception ex) {
            t.setStatus(ex);
            Cat.logError(ex);
            throw ex;
        } finally {
            t.complete();
        }
    }
}