package com.hfhk.check.config;


import com.hfhk.cairo.security.oauth2.server.resource.web.CairoBearerTokenAccessDeniedHandler;
import com.hfhk.cairo.security.oauth2.server.resource.web.CairoBearerTokenAuthenticationEntryPoint;
import com.hfhk.cairo.starter.service.security.oauth2.server.resource.authentication.CairoJwtAuthenticationConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.SessionFixationProtectionStrategy;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http,
											CairoBearerTokenAuthenticationEntryPoint entryPoint,
											CairoBearerTokenAccessDeniedHandler accessDeniedHandler,
											CairoJwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {
		http
			.csrf().disable()
			.authorizeRequests(authorizeRequests -> authorizeRequests
				// root
				.mvcMatchers("/").permitAll()
				// health
				.mvcMatchers("/actuator/**").permitAll()
				// test
				.mvcMatchers("/test/**").permitAll()
				// default
				.mvcMatchers("/**").authenticated()
			)
			.oauth2ResourceServer()
			.authenticationEntryPoint(entryPoint)
			.accessDeniedHandler(accessDeniedHandler)
			.jwt()
			.jwtAuthenticationConverter(jwtAuthenticationConverter)
			.and()
			.and()
			.sessionManagement()
			.sessionAuthenticationStrategy(new SessionFixationProtectionStrategy())
			.enableSessionUrlRewriting(true)
			.and()
			.exceptionHandling()
			.and()
		;
		return http.build();
	}

	@Bean
	WebSecurityCustomizer webSecurityCustomizer() {
		return (web) -> web.ignoring().antMatchers("/webjars/**");
	}
}
