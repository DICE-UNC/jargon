/**
 *
 */
package org.irods.jargon.datautils.indexer;

import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.datautils.indexer.NodeVisitLogEntry.VisitTypeEnum;
import org.irods.jargon.datautils.visitor.HierComposite;
import org.irods.jargon.datautils.visitor.HierLeaf;

/**
 * Test visitor instrumented for unit tests
 *
 * @author conwaymc
 *
 */
public class TestIndexVisitor extends AbstractIndexerVisitor {

	private NodeVisitLog nodeVisitLog = new NodeVisitLog();

	/**
	 * @param irodsAccessObjectFactory
	 * @param irodsAccount
	 * @throws JargonException
	 */
	public TestIndexVisitor(final IRODSAccessObjectFactory irodsAccessObjectFactory, final IRODSAccount irodsAccount)
			throws JargonException {
		super(irodsAccessObjectFactory, irodsAccount);
	}

	public NodeVisitLog getNodeVisitLog() {
		return nodeVisitLog;
	}

	public void setNodeVisitLog(final NodeVisitLog nodeVisitLog) {
		this.nodeVisitLog = nodeVisitLog;
	}

	@Override
	public boolean visitEnterWithMetadata(final HierComposite node, final MetadataRollup metadataRollup) {
		NodeVisitLogEntry entry = new NodeVisitLogEntry();
		entry.setNodeAbsolutePath(node.getAbsolutePath());
		entry.setNodeName(node.getName());
		entry.setResultOfVisit(true);
		entry.setVisitType(VisitTypeEnum.ENTER);
		List<MetaDataAndDomainData> myMeta = metadataRollup.getMetadata().peek();
		for (MetaDataAndDomainData meta : myMeta) {
			try {
				entry.getMetadataThisLevel().add(meta.clone());
			} catch (CloneNotSupportedException e) {
				throw new JargonRuntimeException("cannot clone", e);
			}
		}
		nodeVisitLog.add(entry);
		return true;

	}

	@Override
	public boolean visitLeaveWithMetadata(final HierComposite node, final MetadataRollup metadataRollup,
			final boolean visitorEntered) {
		NodeVisitLogEntry entry = new NodeVisitLogEntry();
		entry.setNodeAbsolutePath(node.getAbsolutePath());
		entry.setNodeName(node.getName());
		entry.setResultOfVisit(visitorEntered);
		entry.setVisitType(VisitTypeEnum.LEAVE);
		List<MetaDataAndDomainData> myMeta = metadataRollup.getMetadata().peek();
		for (MetaDataAndDomainData meta : myMeta) {
			try {
				entry.getMetadataThisLevel().add(meta.clone());
			} catch (CloneNotSupportedException e) {
				throw new JargonRuntimeException("cannot clone", e);
			}
		}
		nodeVisitLog.add(entry);
		return visitorEntered;
	}

	@Override
	public boolean visitWithMetadata(final HierLeaf hierLeaf, final MetadataRollup metadataRollup) {
		NodeVisitLogEntry entry = new NodeVisitLogEntry();
		entry.setNodeAbsolutePath(hierLeaf.getAbsolutePath());
		entry.setNodeName(hierLeaf.getName());
		entry.setResultOfVisit(true);
		entry.setVisitType(VisitTypeEnum.VISIT_LEAF);
		List<MetaDataAndDomainData> myMeta = metadataRollup.getMetadata().peek();
		for (MetaDataAndDomainData meta : myMeta) {
			try {
				entry.getMetadataThisLevel().add(meta.clone());
			} catch (CloneNotSupportedException e) {
				throw new JargonRuntimeException("cannot clone", e);
			}
		}
		nodeVisitLog.add(entry);
		return true;
	}

}
