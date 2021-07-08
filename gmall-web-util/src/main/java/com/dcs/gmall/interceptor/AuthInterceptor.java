package com.dcs.gmall.interceptor;

import com.alibaba.fastjson.JSON;
import com.dcs.gmall.annotation.LoginRequire;
import com.dcs.gmall.util.CookieUtil;
import com.dcs.gmall.util.HttpClientUtils;
import io.jsonwebtoken.impl.Base64UrlCodec;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * 拦截所有请求。
 * 先检查token是否存在于cookie里
 * 1.如果token存在，则去认证中心认证（登陆时将用户信息存入redis以便验证token）
 *      1.1.如果认证不通过，说明redis已过期或者token造伪，重新登陆（response.sendRedirect()）
 *      1.2.如果认证通过，则将token的信息解码存入request
 * 2.如果token不存在，则判断拦截的是登陆后的请求（request.getParam...("newToken")）
 *      2.1.如果是登陆后的请求，则将token存入cookie，并将token解析存入request
 *      2.2.如果不是登陆后的请求，则判断拦截的请求是否是需要登录的请求
 *          2.2.1如果是需要登陆的请求，则跳转登录（response.sendRedirect()）
 */
@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String token = CookieUtil.getCookieValue(request, "token", false);

        if (token!=null && token.length()>0){
            //TODO 认证该token是否对应一个用户
            saveUserInfo(request, token);
        }else {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            LoginRequire loginRequire = handlerMethod.getMethodAnnotation(LoginRequire.class);
            if (loginRequire != null){
                return saveTokenOrLogin(request, response);
            }else {
                saveToken(request, response);
            }
        }
        return true;
    }

    private boolean saveTokenOrLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String newToken = request.getParameter("newToken");
        if(newToken!=null && newToken.length()>0){
            CookieUtil.setCookie(request, response, "token", newToken, 30*24*3600,false);
            saveUserInfo(request, newToken);
        }else {
            String requestUrl = request.getRequestURL().toString();
            String originUrl = URLEncoder.encode(requestUrl);
            response.sendRedirect("http://passport.gmall.com/index.html?originUrl=" + originUrl);
            return false;
        }
        return true;
    }

    private void saveToken(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
        String newToken = request.getParameter("newToken");
        if(newToken!=null && newToken.length()>0){
            CookieUtil.setCookie(request, response, "token", newToken, 30*24*3600,false);
            saveUserInfo(request, newToken);
        }
    }

    private void saveUserInfo(HttpServletRequest request, String token) throws UnsupportedEncodingException {
        String middle = StringUtils.substringBetween(token, ".");
        Base64UrlCodec base64UrlCodec = new Base64UrlCodec();
        byte[] userInfoBytes = base64UrlCodec.decode(middle);
        String userInfoString = new String(userInfoBytes, "UTF-8");
        Map map = JSON.parseObject(userInfoString, Map.class);
        String userId = (String) map.get("userId");
        String nickName = (String) map.get("nickName");

        request.setAttribute("userId", userId);
        request.setAttribute("nickName", nickName);
    }
}
