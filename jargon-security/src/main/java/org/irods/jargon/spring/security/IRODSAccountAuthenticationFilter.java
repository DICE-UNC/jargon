package org.irods.jargon.spring.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Web authentication filter for Spring Security enabled IRODS Account
 * authorization
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class IRODSAccountAuthenticationFilter extends
		UsernamePasswordAuthenticationFilter {

	public IRODSAccountAuthenticationFilter() {
		super();
	}

	private Logger log = LoggerFactory.getLogger(this.getClass());

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
	 * javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */

	@Override
	public Authentication attemptAuthentication(
			final HttpServletRequest request, final HttpServletResponse response)
			throws AuthenticationException {

		log.info("authentication filter invocation, url={}",
				request.getRequestURI());

		log.debug("request params:{}", request.getParameterMap());

		log.info("requires auth value:{}",
				this.requiresAuthentication(request, response));

		if (getAuthenticationManager() == null) {
			String msg = "null irodsAccountAuthenticationManager";
			log.error(msg);
			throw new JargonRuntimeException(msg);
		}

		if (!(getAuthenticationManager() instanceof IRODSAccountAuthenticationManager)) {
			String msg = "account manager is not an instance of irodsAccountAuthenticationManager";
			log.error(msg);
			throw new JargonRuntimeException(msg);
		}

		// build IRODSAccount from data in request
		String host = request.getParameter("host");
		String portParm = request.getParameter("port");
		String zone = request.getParameter("zone");
		String resource = request.getParameter("resource");
		String user = request.getParameter("user");
		String password = request.getParameter("password");

		StringBuilder b = new StringBuilder();
		b.append(zone);
		b.append('/');
		b.append("home");
		b.append('/');
		b.append(user);

		int port = 0;

		if (host == null || host.isEmpty()) {
			String msg = "host parameter is missing";
			log.error(msg);
			throw new BadCredentialsException(msg);
		}

		if (portParm == null || portParm.isEmpty()) {
			String msg = "port parameter is missing";
			log.error(msg);
			throw new BadCredentialsException(msg);
		}

		try {
			port = Integer.parseInt(portParm);
		} catch (NumberFormatException nfe) {
			String msg = "port is not a valid number:" + portParm;
			log.error(msg);
			throw new BadCredentialsException(msg);
		}

		if (zone == null || zone.isEmpty()) {
			String msg = "zone parameter is missing";
			log.error(msg);
			throw new BadCredentialsException(msg);
		}

		if (resource == null || resource.isEmpty()) {
			String msg = "resource parameter is missing";
			log.error(msg);
			throw new BadCredentialsException(msg);
		}

		if (user == null || user.isEmpty()) {
			String msg = "user parameter is missing";
			log.error(msg);
			throw new BadCredentialsException(msg);
		}

		if (password == null || password.isEmpty()) {
			String msg = "password parameter is missing";
			log.error(msg);
			throw new BadCredentialsException(msg);
		}

		IRODSAccount loginIRODSAccount = null;

		try {
			loginIRODSAccount = IRODSAccount.instance(host, port, user,
					password, b.toString(), zone, resource);
		} catch (JargonException e) {
			String msg = "invalid credentials, cannot create IRODSAccount with given parameters";
			log.error(msg, e);
			throw new BadCredentialsException(msg, e);
		}

		Authentication token = this.getAuthenticationManager().authenticate(
				new IRODSAuthenticationToken(loginIRODSAccount));
		log.info("authentication successful for:{}", loginIRODSAccount);

		log.debug("setting login up in SecurityContextHolder");
		SecurityContextHolder.getContext().setAuthentication(token);

		return token;

	}

}
