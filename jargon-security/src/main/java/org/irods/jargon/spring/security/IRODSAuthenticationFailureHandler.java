/**
 * 
 */
package org.irods.jargon.spring.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.irods.jargon.core.exception.JargonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

/**
 * Handles login failures in IRODS
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class IRODSAuthenticationFailureHandler implements
		AuthenticationFailureHandler {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private String defaultFailureUrl = null;

	public IRODSAuthenticationFailureHandler() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.security.web.authentication.AuthenticationFailureHandler
	 * #onAuthenticationFailure(javax.servlet.http.HttpServletRequest,
	 * javax.servlet.http.HttpServletResponse,
	 * org.springframework.security.core.AuthenticationException)
	 */
	@Override
	public void onAuthenticationFailure(final HttpServletRequest request,
			final HttpServletResponse response,
			final AuthenticationException exception) throws IOException,
			ServletException {

		log.info("authentication failure handler");
		if (defaultFailureUrl == null || defaultFailureUrl.isEmpty()) {
			throw new ServletException("null or missing redirect url");
		}

		request.setAttribute("login_error", exception.getMessage());

		request.setAttribute("host", request.getParameter("host"));
		request.setAttribute("port", request.getParameter("port"));
		request.setAttribute("zone", request.getParameter("zone"));
		request.setAttribute("resource", request.getParameter("resource"));
		request.setAttribute("user", request.getParameter("user"));
		request.setAttribute("password", request.getParameter("password"));

		log.debug("forward to error url: {}", defaultFailureUrl);

		request.getRequestDispatcher(defaultFailureUrl).forward(request,
				response);

	}

	public void setDefaultFailureUrl(final String defaultFailureUrl)
			throws JargonException {
		if (defaultFailureUrl == null || defaultFailureUrl.isEmpty()) {
			throw new JargonException("missing redirect url");
		}
		this.defaultFailureUrl = defaultFailureUrl;
	}

}
