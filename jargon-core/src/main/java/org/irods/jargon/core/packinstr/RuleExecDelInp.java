/**
 * 
 */
package org.irods.jargon.core.packinstr;

import org.irods.jargon.core.exception.JargonException;

import edu.sdsc.grid.io.irods.Tag;

/**
 * Packing instruction to delete a rule from the delayed execution queue
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class RuleExecDelInp extends AbstractIRODSPackingInstruction {

	public static final String PI_TAG = "RULE_EXEC_DEL_INP_PI";
	public static final int RULE_PURGE_API_NBR = 624;

	public static final String RULE_EXEC_ID = "ruleExecId";

	private final String ruleExecId;

	/**
	 * Instance method for delete of a rule in the delayed rule execution queue
	 * 
	 * @param ruleExecId
	 *            <code>String</code> with the id of a rule in the delayed rule
	 *            execution queue
	 * @return
	 */
	public static RuleExecDelInp instanceForDeleteRule(final String ruleExecId) {
		return new RuleExecDelInp(ruleExecId);
	}

	private RuleExecDelInp(final String ruleExecId) {
		super();

		if (ruleExecId == null || ruleExecId.isEmpty()) {
			throw new IllegalArgumentException("null or empty ruleExecId");
		}

		this.ruleExecId = ruleExecId;
		this.setApiNumber(RULE_PURGE_API_NBR);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.packinstr.AbstractIRODSPackingInstruction#getTagValue
	 * ()
	 */
	@Override
	public Tag getTagValue() throws JargonException {
		Tag message = new Tag(PI_TAG);
		message.addTag(RULE_EXEC_ID, ruleExecId);
		return message;
	}

}
