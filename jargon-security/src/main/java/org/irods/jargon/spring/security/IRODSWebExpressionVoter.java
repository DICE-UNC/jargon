/**
 * 
 */
package org.irods.jargon.spring.security;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.expression.WebExpressionVoter;
import org.springframework.security.web.access.expression.WebSecurityExpressionHandler;

/**
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class IRODSWebExpressionVoter extends WebExpressionVoter {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * 
	 */
	public IRODSWebExpressionVoter() {
		super();
		log.info("loading web expression voter");
	}

	@Override
	public void setExpressionHandler(
			final WebSecurityExpressionHandler expressionHandler) {
		log.debug("expression handler set {}", expressionHandler.toString());
		super.setExpressionHandler(expressionHandler);
	}

	@Override
	public boolean supports(final Class<?> clazz) {
		return super.supports(clazz);
	}

	@Override
	public boolean supports(final ConfigAttribute attribute) {
		return super.supports(attribute);
	}

	@Override
	public int vote(final Authentication authentication, final Object object,
			final Collection<ConfigAttribute> attributes) {

		log.debug("voting for authentication {}", authentication);
		log.debug("on object {}", object.toString());
		log.debug("with attributes {}", attributes.toString());

		int vote = super.vote(authentication, object, attributes);

		log.debug("my vote is {}", vote);
		return vote;
	}

}
