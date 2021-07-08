package com.dcs.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.dcs.gmall.annotation.LoginRequire;
import com.dcs.gmall.bean.UserInfo;
import com.dcs.gmall.passport.util.JwtUtil;
import com.dcs.gmall.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
@CrossOrigin
public class PassportController {

    @Reference
    private UserService userService;

    @Value("${jwt.key}")
    private String jwtKey;

    @RequestMapping("index.html")
    public String index(HttpServletRequest request){
        String originUrl = request.getParameter("originUrl");

        request.setAttribute("originUrl", originUrl);

        return "index";
    }

    @RequestMapping("login")
    @ResponseBody
    public String login(UserInfo userInfo, HttpServletRequest request){

        String salt = request.getHeader("X-forwarded-for");

        UserInfo user = userService.loginByLoginNameAndPasswd(userInfo);

        if (user != null){
            Map<String, Object> map = new HashMap<>();
            map.put("userId", user.getId());
            map.put("nickName", user.getNickName());

            String token = JwtUtil.encode(jwtKey, map, salt);

            return token;
        }

        return "fail";
    }
}
