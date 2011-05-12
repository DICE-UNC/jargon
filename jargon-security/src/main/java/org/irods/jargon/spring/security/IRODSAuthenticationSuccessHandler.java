/**
 * 
 */
package org.irods.jargon.spring.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

/**
 * Handle login success and direction to proper place in application immediately
 * after login
 * 
 * @author Mike Conway - DICE (www.irods.org)
 */
public class IRODSAuthenticationSuccessHandler extends
		SavedRequestAwareAuthenticationSuccessHandler {

	private static Logger log = LoggerFactory
			.getLogger(IRODSAuthenticationSuccessHandler.class);
	
	private String securityRedirectUrl = "spring-security-redirect";

	/**
	 * Default (no-values) constructor.
	 */
	public IRODSAuthenticationSuccessHandler() {
	}
	
	/**
	 * 
	 * @param securityRedirectUrl
	 */
	public void setSecurityRedirectUrl(String securityRedirectUrl) {
		
		if (securityRedirectUrl == null || securityRedirectUrl.isEmpty()) {
			throw new IllegalArgumentException("null or empty securityRedirectUrl");
		}
		
		this.securityRedirectUrl = securityRedirectUrl;
	}

	@Override
	public void onAuthenticationSuccess(final HttpServletRequest request,
			final HttpServletResponse response,
			final Authentication authentication) throws ServletException,
			IOException {

		// log.debug("authentication success, request:{}", request);
		// log.debug("response:{}", response);

		// log.debug("request parms:{}", request.getParameterMap());

		if (this.getTargetUrlParameter() != null) {
			log.debug("targetURL:{}", this.getTargetUrlParameter());
			if (this.getTargetUrlParameter().equals("spring-security-redirect")) {
				log.debug("targetURL was security, redirect to home");
				this.setTargetUrlParameter("/home");
			}

		}

		super.onAuthenticationSuccess(request, response, authentication);
	}

}
