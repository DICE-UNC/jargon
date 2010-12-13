package org.irods.jargon.idrop.desktop.systraygui.viscomponents;

import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.idrop.exceptions.IdropException;
import org.irods.jargon.part.policydriven.PolicyDrivenServiceListingEntry;

/**
 * @author Mike Conway - DICE (www.irods.org)
 */
public final class CollectionViewHolder {

    private final  CollectionAndDataObjectListingEntry collection;
    private final StagingViewManager stagingViewManager;

    public StagingViewManager getStagingViewManager() {
        return stagingViewManager;
    }

    /**
     * Default constructor with the contained series and a reference to the view manager.
     * @param series <code>Series</code> information
     * @param seriesViewManager (@link org.irods.jargon.idrop.desktop.systraygui.SeriesViewManager}
     * @throws IdropException
     */
    public CollectionViewHolder(final CollectionAndDataObjectListingEntry collection, final StagingViewManager stagingViewManager) throws IdropException {

        if (collection == null) {
            throw new IdropException("collection is null");
        }

        if (stagingViewManager == null) {
            throw new IdropException("stagingViewManager is null");
        }

        this.collection = collection;
        this.stagingViewManager = stagingViewManager;

    }

    /**
     * @return the series
     */
    public CollectionAndDataObjectListingEntry getCollection() {
        return collection;
    }
}
