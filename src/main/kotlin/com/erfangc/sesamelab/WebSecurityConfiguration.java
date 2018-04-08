package com.erfangc.sesamelab;

import com.auth0.spring.security.api.JwtWebSecurityConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

@Configuration
@EnableWebSecurity(debug = true)
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    private String apiAudience = System.getenv("AUTH0_API_AUDIENCE");
    private String issuer = System.getenv("AUTH0_ISSUER");

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedHeaders(singletonList("Authorization"));
        configuration.setAllowedOrigins(singletonList("http://localhost:3000"));
        configuration.setAllowedMethods(asList("GET", "POST", "OPTIONS"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        JwtWebSecurityConfigurer
                .forRS256(apiAudience, issuer)
                .configure(http)
                .authorizeRequests()
                .antMatchers("/")
                .permitAll()
                .antMatchers("/api/**")
                .authenticated();
    }

}

