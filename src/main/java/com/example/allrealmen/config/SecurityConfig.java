package com.example.allrealmen.config;

import com.example.allrealmen.common.dto.ApiResponse;
import com.example.allrealmen.domain.user.dto.LoginRequest;
import com.example.allrealmen.domain.user.dto.TokenResponse;
import com.example.allrealmen.domain.user.entity.Member;
import com.example.allrealmen.domain.user.repository.MemberRepository;
import com.example.allrealmen.domain.user.security.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.Arrays;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    @Bean
    public AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository() {
        return new HttpSessionOAuth2AuthorizationRequestRepository();
    }
    
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            log.error("인증 실패: {} - {}", authException.getClass().getName(), authException.getMessage(), authException);
            
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            
            objectMapper.writeValue(response.getWriter(), 
                ApiResponse.error(authException.toString()));
        };
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
            "http://jeenie2027.cafe24app.com", 
            "https://allrealmen.netlify.app/",
            "http://localhost:3000",
            "http://localhost:5173",
                "https://www.xn--q20b38omkn.com/",
                "https://www.xn--q20b38omkn.com",
                "http://allrealwebserver:81"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        JsonAuthenticationFilter jsonAuthenticationFilter = new JsonAuthenticationFilter(objectMapper);
        jsonAuthenticationFilter.setAuthenticationManager(authenticationManager());
        jsonAuthenticationFilter.setAuthenticationSuccessHandler(
            new JsonAuthenticationSuccessHandler(objectMapper, jwtTokenProvider, memberRepository));
        jsonAuthenticationFilter.setAuthenticationFailureHandler(new JsonAuthenticationFailureHandler(objectMapper));
        jsonAuthenticationFilter.setSecurityContextRepository(new HttpSessionSecurityContextRepository());

        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(authenticationEntryPoint())
            )
            .authorizeHttpRequests(auth -> auth
                // 회원 관련
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/session/check").authenticated()

                // 카테고리 관련
                .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                    .requestMatchers(HttpMethod.PUT, "/api/categories/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/categories/initialize").hasRole("ADMIN")

                // 게시판 관련
                    .requestMatchers( "/api/posts/**").authenticated()
                    .requestMatchers("/files/**").permitAll()

                    // 사전신청 관련
                .requestMatchers(HttpMethod.POST, "/api/application").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/application/my").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/application/**").hasRole("ADMIN")

                // 채팅 관련
                .requestMatchers("/api/chat/rooms").hasAnyRole("ADMIN", "USER")
                .requestMatchers("/api/chat/rooms/**").hasAnyRole("ADMIN", "USER")
                .requestMatchers("/api/chat/consultation").hasAnyRole("ADMIN", "USER")
                .requestMatchers("/ws/**").permitAll()
                .requestMatchers("/topic/**").permitAll()
                .requestMatchers("/app/**").permitAll()

                // 관리자 전용 API
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // 나머지 요청
                .anyRequest().authenticated()
            )
            // JWT 필터 추가 (Form 로그인 필터 이전에 추가)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAt(jsonAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .logoutSuccessHandler((request, response, authentication) -> {
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.success(null)));
                })
            )
            // OAuth2 설정 추가
            .oauth2Login(oauth2 -> oauth2
                .authorizationEndpoint(authorization -> authorization
                    .baseUri("/api/auth/oauth2/authorize")
                    .authorizationRequestRepository(authorizationRequestRepository())
                )
                .redirectionEndpoint(redirection -> redirection
                    .baseUri("/api/auth/oauth2/callback/*")
                )
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService)
                )
                .successHandler(new OAuthAuthenticationSuccessHandler(objectMapper, jwtTokenProvider, memberRepository))
                .failureHandler(new OAuthAuthenticationFailureHandler(objectMapper))
            );
        
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }
}

@Slf4j
class JsonAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final ObjectMapper objectMapper;

    public JsonAuthenticationFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        setFilterProcessesUrl("/api/auth/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        try {
            LoginRequest loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequest.class);
            log.info("로그인 시도: ID={}", loginRequest.getId());
            
            UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
                    loginRequest.getId(), loginRequest.getPassword());
            setDetails(request, authRequest);
            return getAuthenticationManager().authenticate(authRequest);
        } catch (IOException e) {
            log.error("로그인 요청 형식 오류: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Invalid login request format", e);
        }
    }
}

@Slf4j
class JsonAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final ObjectMapper objectMapper;
    private final JwtTokenProvider tokenProvider;
    private final MemberRepository memberRepository;

    JsonAuthenticationSuccessHandler(ObjectMapper objectMapper, JwtTokenProvider tokenProvider, MemberRepository memberRepository) {
        this.objectMapper = objectMapper;
        this.tokenProvider = tokenProvider;
        this.memberRepository = memberRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                      Authentication authentication) throws IOException, ServletException {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Member member = memberRepository.findById(userDetails.getId())
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        
        Authentication newAuth = new UsernamePasswordAuthenticationToken(
            new CustomUserDetails(member), 
            null, 
            authentication.getAuthorities()
        );
        
        String token = tokenProvider.createToken(newAuth);
        
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        log.info("로그인 성공: principal={}", authentication.getPrincipal());

        objectMapper.writeValue(response.getWriter(), 
            ApiResponse.success(new TokenResponse(token)));
    }
}

@Slf4j
class JsonAuthenticationFailureHandler implements AuthenticationFailureHandler {
    private final ObjectMapper objectMapper;

    JsonAuthenticationFailureHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                      AuthenticationException exception) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        log.error("인증 실패 Handler: {} - {}", exception.getClass().getName(), exception.getMessage(), exception);

        objectMapper.writeValue(response.getWriter(), ApiResponse.error(exception.getMessage()));
    }
}

// OAuth 인증 성공 핸들러
@Slf4j
class OAuthAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final ObjectMapper objectMapper;
    private final JwtTokenProvider tokenProvider;
    private final MemberRepository memberRepository;

    OAuthAuthenticationSuccessHandler(ObjectMapper objectMapper, JwtTokenProvider tokenProvider, MemberRepository memberRepository) {
        this.objectMapper = objectMapper;
        this.tokenProvider = tokenProvider;
        this.memberRepository = memberRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                      Authentication authentication) throws IOException, ServletException {
        CustomOAuth2User userDetails = (CustomOAuth2User) authentication.getPrincipal();
        Member member = memberRepository.findById(userDetails.getName())
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        
        Authentication newAuth = new UsernamePasswordAuthenticationToken(
            new CustomUserDetails(member), 
            null, 
            authentication.getAuthorities()
        );
        
        String token = tokenProvider.createToken(newAuth);
        
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.sendRedirect("/");

        log.info("OAuth 로그인 성공: principal={}", authentication.getPrincipal());

        objectMapper.writeValue(response.getWriter(), 
            ApiResponse.success(new TokenResponse(token)));
    }
}

// OAuth 인증 실패 핸들러
@Slf4j
class OAuthAuthenticationFailureHandler implements AuthenticationFailureHandler {
    private final ObjectMapper objectMapper;

    OAuthAuthenticationFailureHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                      AuthenticationException exception) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        
        log.error("OAuth 인증 실패: {} - {}", exception.getClass().getName(), exception.getMessage(), exception);

        objectMapper.writeValue(response.getWriter(), ApiResponse.error("OAuth 로그인에 실패했습니다: " + exception.getMessage()));
    }
} 