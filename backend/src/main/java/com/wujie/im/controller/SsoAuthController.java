package com.wujie.im.controller;

import com.wujie.im.service.AuthService;
import com.wujie.im.service.SsoOidcService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.util.Map;

import static com.wujie.im.service.SsoOidcService.rolesClaim;
import static com.wujie.im.service.SsoOidcService.stringClaim;
import static com.wujie.im.service.SsoOidcService.urlEncode;

/**
 * pnkx 统一登录（SSO）入口：
 * /api/auth/sso/login → 302 pnkx 授权页；
 * /api/auth/sso/callback → 换用户信息、映射本地账号、签发本系统双 token，
 * 经 URL fragment 回跳前端。路径在 /api/auth/** 鉴权白名单内。
 */
@Slf4j
@RestController
@RequestMapping("/api/auth/sso")
public class SsoAuthController {

    private final AuthService authService;
    private final SsoOidcService ssoOidcService;

    @Value("${sso.frontend-uri:http://localhost:3000}")
    private String frontendUri;

    public SsoAuthController(AuthService authService, SsoOidcService ssoOidcService) {
        this.authService = authService;
        this.ssoOidcService = ssoOidcService;
    }

    @GetMapping("/login")
    public void login(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!ssoOidcService.enabled()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "SSO 未启用");
            return;
        }
        response.sendRedirect(ssoOidcService.buildAuthorizeUrl(callbackUri(request)));
    }

    @GetMapping("/callback")
    public void callback(@RequestParam(value = "code", required = false) String code,
                         @RequestParam(value = "state", required = false) String state,
                         @RequestParam(value = "error", required = false) String error,
                         HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!ssoOidcService.enabled()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "SSO 未启用");
            return;
        }
        if (error != null) {
            redirectFailure(response, "pnkx 授权失败：" + error);
            return;
        }
        if (code == null || !ssoOidcService.consumeState(state)) {
            redirectFailure(response, "SSO 状态校验失败，请重新登录");
            return;
        }
        try {
            Map<String, Object> claims = ssoOidcService.exchangeCodeForUserInfo(code, callbackUri(request));
            Map<String, String> result = authService.loginWithSso(
                    stringClaim(claims, "sub"),
                    stringClaim(claims, "preferred_username"),
                    stringClaim(claims, "name"),
                    rolesClaim(claims).contains("admin"));
            String userJson = "{\"userId\":\"" + result.get("userId") + "\",\"username\":\""
                    + escape(result.get("username")) + "\",\"role\":\"" + result.get("role") + "\"}";
            response.sendRedirect(frontendUri + "/sso/callback#access_token=" + result.get("accessToken")
                    + "&refresh_token=" + result.get("refreshToken")
                    + "&user=" + urlEncode(userJson));
        } catch (Exception e) {
            log.error("SSO 登录失败", e);
            redirectFailure(response, "SSO 登录失败：" + e.getMessage());
        }
    }

    private String callbackUri(HttpServletRequest request) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/auth/sso/callback").build().toUriString();
    }

    private void redirectFailure(HttpServletResponse response, String message) throws IOException {
        response.sendRedirect(frontendUri + "/login?sso_error=" + urlEncode(message));
    }

    private static String escape(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
