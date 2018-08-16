## Indexing/visitor notes


Packages are included in data utils (visitor and indexer) to support the Hierachical Visitor pattern:

http://wiki.c2.com/?HierarchicalVisitorPattern

One may subclass the AbstractIrodsVisitorComponent and thus implement logic that will apply a process at each collection and leaf node. This is really useful for things like 
file format recognition, administrative tasks, and creating indexes.

```Java

public abstract class AbstractIrodsVisitorComponent extends AbstractJargonService implements HierVisitor {

	/**
	 * @param irodsAccessObjectFactory
	 *            {@link IRODSAccessObjectFactory}
	 * @param irodsAccount
	 *            {@link IRODSAccount}
	 */
	public AbstractIrodsVisitorComponent(final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount) {
		super(irodsAccessObjectFactory, irodsAccount);
	}

	public AbstractIrodsVisitorComponent() {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.irods.jargon.datautils.visitor.HierVisitor#visitEnter(org.irods.jargon.
	 * datautils.visitor.HierComposite)
	 */
	@Override
	public abstract boolean visitEnter(HierComposite node);

	@Override
	public abstract boolean visitLeave(HierComposite node, boolean visitorEntered);

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.datautils.visitor.HierVisitor#visit(org.irods.jargon.
	 * datautils.visitor.HierLeaf)
	 */
	@Override
	public abstract boolean visit(HierLeaf node);

	@Override
	public abstract void launch(final String startingCollectionPath);

}



```

This pattern is used in the indexer package to create a visitor implementation that can support basic indexing operations. The AbstractIndexerVisitor component is
a subclass of the AbstractIrodsVisitorComponent with the addition of metadata gathering. This specialized visitor will maintain a stack of AVU from the current location up the 
heirarchy to the initial parent folder. This means that at each visit event all parent metadata is available. These can be accessed using the 'withMetadata' variants
of the visitor pattern, such as

```Java

/**
	 * Indicate whether the visitor should enter a collection and process its
	 * children. This processes the collection before any children are processed.
	 * <p>
	 * Alternately, the implementation can wait until all children are processed in
	 * the visitLeaveWithMetadata() method
	 *
	 * To be extended by the indexer, this will call visitEnter with the
	 * already-obtained metadata values rolled up to the parent
	 *
	 * @param node
	 *            {@link HierComposite} with the parent node to enter
	 * @param metadataRollup
	 *            {@link MetadataRollup} with the metadata from the root down to the
	 *            current node
	 * @return {@code boolean} with a return of <code>true</code> if the visitor
	 *         should enter the collection
	 */
	public abstract boolean visitEnterWithMetadata(HierComposite node, MetadataRollup metadataRollup);


```


The indexing code can make use of an optional IndexerFilter, such as the default ConfigurableIndexerFilter that respects AVU do-not-index flags. In this 
first implementation an AVU with the values of 

```

DONOTINDEX | indexer name or blank | irods:indexing

```

Will cause the indexer to short-circuit for child collections and objects. The AVU value of blank will stop all indexing. The AVU value otherwise is string matched
to the name of the indexer, and if they match, this causes the indexer to short circuit. Later implementations will have more richness of control and optimizations for
supporting multiple indexers at once!















