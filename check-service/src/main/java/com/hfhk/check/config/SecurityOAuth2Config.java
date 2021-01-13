package com.hfhk.check.config;

import com.hfhk.cairo.feign.interceptor.ClientOAuth2RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;

@Configuration
@Slf4j
public class SecurityOAuth2Config {

	@Bean
	public OAuth2RestTemplate oAuth2RestTemplate(ClientCredentialsResourceDetails client) {
		OAuth2RestTemplate template = new OAuth2RestTemplate(client, new DefaultOAuth2ClientContext());
		template.setRequestFactory(new OkHttp3ClientHttpRequestFactory());
		return template;
	}

	@Bean
	public ClientOAuth2RequestInterceptor clientOAuth2RequestInterceptor(OAuth2RestTemplate template) {
		return new ClientOAuth2RequestInterceptor(template);
	}

}
