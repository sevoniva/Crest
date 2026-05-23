package io.dataease.auth.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import java.io.IOException;

import io.dataease.utils.CommunityUtils;
import io.dataease.utils.CrestPermissionUtils;

public class CommunityTokenFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        try {
            CommunityUtils.setInfo(CrestPermissionUtils.communityScopeSql());
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            CommunityUtils.removeInfo();
        }
    }
}
