package org.irods.jargon.spring.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;

/**
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class IRODSWebSecurityExpressionHandler extends
		DefaultWebSecurityExpressionHandler {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	public IRODSWebSecurityExpressionHandler() {
		log.debug("web security expression handler constructed");
	}

	@Override
	public EvaluationContext createEvaluationContext(
			final Authentication authentication, final FilterInvocation fi) {
		log.debug("...creating evaluation context for auth {}",
				authentication.toString());
		return super.createEvaluationContext(authentication, fi);
	}

	@Override
	public ExpressionParser getExpressionParser() {

		log.debug("...getting expression parser");
		return super.getExpressionParser();
	}

	@Override
	public void setRoleHierarchy(final RoleHierarchy roleHierarchy) {

		log.debug("...setting role hierarchy {}", roleHierarchy.toString());
		super.setRoleHierarchy(roleHierarchy);
	}

}
