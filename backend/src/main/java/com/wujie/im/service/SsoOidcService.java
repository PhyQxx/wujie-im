package com.wujie.im.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * pnkx 统一登录（OIDC）客户端：授权 URL 生成、state 校验、授权码换令牌、userinfo 拉取。
 */
@Slf4j
@Service
public class SsoOidcService {

    private static final String STATE_KEY = "wujie:sso:state:";
    private static final Duration STATE_TTL = Duration.ofMinutes(5);

    private final StringRedisTemplate redisTemplate;
    private final RestClient restClient = RestClient.create();

    @Value("${sso.issuer:}")
    private String issuer;

    @Value("${sso.client-id:}")
    private String clientId;

    @Value("${sso.client-secret:}")
    private String clientSecret;

    public SsoOidcService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean enabled() {
        return issuer != null && !issuer.isBlank()
                && clientId != null && !clientId.isBlank()
                && clientSecret != null && !clientSecret.isBlank();
    }

    public String buildAuthorizeUrl(String redirectUri) {
        String state = java.util.UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(STATE_KEY + state, "1", STATE_TTL);
        return UriComponentsBuilder.fromHttpUrl(issuer + "/oauth2/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("scope", "openid profile")
                .queryParam("state", state)
                .encode()
                .build().toUriString();
    }

    public boolean consumeState(String state) {
        if (state == null || state.isBlank()) {
            return false;
        }
        String key = STATE_KEY + state;
        if (redisTemplate.opsForValue().get(key) == null) {
            return false;
        }
        redisTemplate.delete(key);
        return true;
    }

    /**
     * 授权码换 userinfo（服务端 Basic 认证 → access token → userinfo）
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> exchangeCodeForUserInfo(String code, String redirectUri) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("code", code);
        form.add("redirect_uri", redirectUri);
        Map<String, Object> tokenResponse;
        try {
            tokenResponse = restClient.post()
                    .uri(issuer + "/oauth2/token")
                    .headers(headers -> headers.setBasicAuth(clientId, clientSecret))
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(Map.class);
        } catch (Exception e) {
            log.error("SSO token exchange failed", e);
            throw new RuntimeException("SSO 令牌交换失败");
        }
        if (tokenResponse == null || tokenResponse.get("access_token") == null) {
            throw new RuntimeException("SSO 令牌交换失败");
        }
        try {
            Map<String, Object> userInfo = restClient.get()
                    .uri(issuer + "/userinfo")
                    .headers(headers -> headers.setBearerAuth(String.valueOf(tokenResponse.get("access_token"))))
                    .retrieve()
                    .body(Map.class);
            if (userInfo == null || userInfo.get("sub") == null) {
                throw new RuntimeException("SSO 用户信息获取失败");
            }
            return userInfo;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("SSO userinfo failed", e);
            throw new RuntimeException("SSO 用户信息获取失败");
        }
    }

    public static String stringClaim(Map<String, Object> claims, String key) {
        Object value = claims.get(key);
        return value == null ? null : String.valueOf(value);
    }

    public static List<String> rolesClaim(Map<String, Object> claims) {
        Object roles = claims.get("roles");
        if (roles instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return List.of();
    }

    public static String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
