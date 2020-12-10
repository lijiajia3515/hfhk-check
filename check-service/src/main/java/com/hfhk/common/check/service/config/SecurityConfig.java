package com.hfhk.common.check.service.config;


import com.hfhk.cairo.starter.service.security.oauth2.server.resource.authentication.CairoJwtAuthenticationConverter;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {

	@Configuration
	@EnableWebSecurity
	public static class CairoWebSecurityConfiguration extends WebSecurityConfigurerAdapter {
		private final OAuth2ResourceServerProperties oAuth2ResourceServerProperties;
		private final CairoJwtAuthenticationConverter authenticationConverter;

		public CairoWebSecurityConfiguration(OAuth2ResourceServerProperties oAuth2ResourceServerProperties,
											 CairoJwtAuthenticationConverter authenticationConverter) {
			this.oAuth2ResourceServerProperties = oAuth2ResourceServerProperties;
			this.authenticationConverter = authenticationConverter;
		}

		@Override
		protected void configure(AuthenticationManagerBuilder auth) throws Exception {
			super.configure(auth);
		}

		@Override
		public void configure(WebSecurity web) throws Exception {
			super.configure(web);
		}

		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http.csrf().disable();

			http.oauth2ResourceServer()
				.jwt()
				.jwkSetUri(oAuth2ResourceServerProperties.getJwt().getJwkSetUri())
				.jwtAuthenticationConverter(authenticationConverter)
			;


			// 异常处理
			http.exceptionHandling();

			http.authorizeRequests()
				.mvcMatchers("/actuator").permitAll()
				.mvcMatchers("/actuators").permitAll()
				.mvcMatchers(HttpMethod.GET, "/file/**").permitAll()
				.mvcMatchers("/**").permitAll();

		}
	}
}
