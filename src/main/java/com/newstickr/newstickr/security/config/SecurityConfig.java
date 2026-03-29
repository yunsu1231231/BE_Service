package com.newstickr.newstickr.security.config;

import com.newstickr.newstickr.security.jwt.JWTFilter;
import com.newstickr.newstickr.security.jwt.JWTUtil;
import com.newstickr.newstickr.security.oauth2.CustomSuccessHandler;
import com.newstickr.newstickr.user.enums.Role;
import com.newstickr.newstickr.user.service.CustomOAuth2UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomSuccessHandler customSuccessHandler;
    private final JWTUtil jwtUtil;

    public SecurityConfig(CustomOAuth2UserService customOAuth2UserService, CustomSuccessHandler customSuccessHandler, JWTUtil jwtUtil) {
        this.customOAuth2UserService = customOAuth2UserService;
        this.customSuccessHandler = customSuccessHandler;
        this.jwtUtil = jwtUtil;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .cors(corsCustomizer -> corsCustomizer.configurationSource(new CorsConfigurationSource() {
                    @Override
                    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                        CorsConfiguration configuration = new CorsConfiguration();
                        configuration.setAllowedOrigins(List.of(
                                "http://localhost",
                                "http://localhost:5173",
                                "http://127.0.0.1:5173"
                        ));
                        configuration.setAllowedMethods(List.of("*"));
                        configuration.setAllowCredentials(true);
                        configuration.setAllowedHeaders(List.of("*"));
                        configuration.setMaxAge(3600L);
                        configuration.setExposedHeaders(Arrays.asList("Set-Cookie", "Authorization"));
                        return configuration;
                    }
                }));

        // CSRF 비활성화
        http.csrf(csrf -> csrf.disable());

        // 경로별 인가 설정
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/").permitAll()
                .requestMatchers("/news/post", "/admin/users/news/","/api/comment/").authenticated()
                .requestMatchers("/swagger-ui","/news/**","/api/comment/**","/auth/logout").permitAll()
                .requestMatchers("/admin/users/**").hasAuthority(Role.ADMIN.getValue())  // 관리자 페이지 접근 제한 (ROLE_ADMIN 필요)
                .anyRequest().authenticated()
        );

        // 로그인 및 인증 방식 비활성화
        http.formLogin(form -> form.disable());
        http.httpBasic(basic -> basic.disable());

        // JWT 필터 추가
        http.addFilterBefore(new JWTFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);

        // OAuth2 로그인 설정
        http.oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                .successHandler(customSuccessHandler)
        );

        // 예외 처리 설정
        http.exceptionHandling(exceptionHandling -> exceptionHandling
                .authenticationEntryPoint((request, response, authException) -> {
                    // 로그인되지 않은 사용자는 네이버 로그인 페이지로 이동
                    response.sendRedirect("/oauth2/authorization/naver");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    // 로그인은 했지만 ADMIN 권한이 없는 경우 403 응답
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("{\"error\": \"ADMIN 권한이 없습니다.\"}");
                })
        );

        // 세션 상태 설정 (STATELESS)
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}
