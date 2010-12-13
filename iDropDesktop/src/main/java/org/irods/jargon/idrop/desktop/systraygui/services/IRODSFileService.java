package org.irods.jargon.idrop.desktop.systraygui.services;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.ResourceAO;
import org.irods.jargon.core.pub.RuleProcessingAO;
import org.irods.jargon.core.pub.domain.Collection;
import org.irods.jargon.core.pub.domain.Resource;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.query.AVUQueryElement;
import org.irods.jargon.core.query.AVUQueryOperatorEnum;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.irods.jargon.core.rule.IRODSRuleExecResult;
import org.irods.jargon.idrop.exceptions.IdropException;
import org.slf4j.LoggerFactory;

/**
 * Delegate class that interacts with iRODS based on GUI interactions
 * FIXME: this needs to be re-worked for connection handling (cache, or per-service scheme) right now it
 * just opens connections per invocation.  This was a shortcut to get a demo done and needs to be corrected.
 * @author Mike Conway - DICE (www.irods.org)
 */
public class IRODSFileService {

    public static org.slf4j.Logger log = LoggerFactory.getLogger(IRODSFileService.class);
    private final IRODSAccount irodsAccount;
    private final IRODSFileSystem irodsFileSystem;

    private IRODSFileService() {
        irodsAccount = null;
        irodsFileSystem = null;
        // not to be invoked, thus private
    }

    public IRODSFileService(final IRODSAccount irodsAccount, final IRODSFileSystem irodsFileSystem) throws IdropException {
        if (irodsAccount == null) {
            throw new IdropException("null irodsAccount");
        }

        if (irodsFileSystem == null) {
            throw new IdropException("null irodsFileSystem");
        }

        this.irodsAccount = irodsAccount;
        this.irodsFileSystem = irodsFileSystem;

    }

    public List<CollectionAndDataObjectListingEntry> getCollectionsUnderParentCollection(final String parentCollectionAbsolutePath) throws IdropException {

        if (parentCollectionAbsolutePath == null || parentCollectionAbsolutePath.isEmpty()) {
            throw new IdropException("null parentCollectionAbsolutePath");
        }

        try {
            CollectionAndDataObjectListAndSearchAO collectionAO = irodsFileSystem.getIRODSAccessObjectFactory().getCollectionAndDataObjectListAndSearchAO(irodsAccount);
            return collectionAO.listCollectionsUnderPath(parentCollectionAbsolutePath, 0);
        } catch (JargonException ex) {
            Logger.getLogger(IRODSFileService.class.getName()).log(Level.SEVERE, null, ex);
            throw new IdropException("exception getting collections under: {}" + parentCollectionAbsolutePath, ex);
        } finally {
            try {
                irodsFileSystem.close(irodsAccount);
            } catch (JargonException ex) {
                Logger.getLogger(IRODSFileService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public IRODSFile getIRODSFileForPath(final String irodsFilePath) throws IdropException {

        if (irodsFilePath == null || irodsFilePath.isEmpty()) {
            throw new IdropException("null or empty irodsFilePath");
        }

        try {
            return irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(irodsFilePath);
        } catch (JargonException ex) {
            Logger.getLogger(IRODSFileService.class.getName()).log(Level.SEVERE, null, ex);
            throw new IdropException("exception getting collections under: {}" + irodsFilePath, ex);
        } finally {
            try {
                irodsFileSystem.close(irodsAccount);
            } catch (JargonException ex) {
                Logger.getLogger(IRODSFileService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public List<CollectionAndDataObjectListingEntry> getFilesAndCollectionsUnderParentCollection(final String parentCollectionAbsolutePath) throws IdropException {

        if (parentCollectionAbsolutePath == null || parentCollectionAbsolutePath.isEmpty()) {
            throw new IdropException("null parentCollectionAbsolutePath");
        }

        try {
            CollectionAndDataObjectListAndSearchAO collectionAO = irodsFileSystem.getIRODSAccessObjectFactory().getCollectionAndDataObjectListAndSearchAO(irodsAccount);
            return collectionAO.listDataObjectsAndCollectionsUnderPath(parentCollectionAbsolutePath);
        } catch (JargonException ex) {
            Logger.getLogger(IRODSFileService.class.getName()).log(Level.SEVERE, null, ex);
            throw new IdropException("exception getting collections under: {}" + parentCollectionAbsolutePath, ex);
        } finally {
            try {
                irodsFileSystem.close(irodsAccount);
            } catch (JargonException ex) {
                Logger.getLogger(IRODSFileService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public Collection getParentCollection(final String parentCollectionAbsolutePath) throws IdropException {

        if (parentCollectionAbsolutePath == null || parentCollectionAbsolutePath.isEmpty()) {
            throw new IdropException("null parentCollectionAbsolutePath");
        }

        try {
            CollectionAO collectionAO = irodsFileSystem.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
            return collectionAO.findByAbsolutePath(parentCollectionAbsolutePath);
        } catch (JargonException ex) {
            Logger.getLogger(IRODSFileService.class.getName()).log(Level.SEVERE, null, ex);
            throw new IdropException("exception getting collections under: {}" + parentCollectionAbsolutePath, ex);
        } finally {
            try {
                irodsFileSystem.close(irodsAccount);
            } catch (JargonException ex) {
                Logger.getLogger(IRODSFileService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public String getStringFromSourcePaths(final List<String> sourcePaths) throws IdropException {
        if (sourcePaths == null || sourcePaths.isEmpty()) {
            throw new IdropException("sourcePaths is null or empty");
        }

        StringBuilder sb = new StringBuilder();
        for (String path : sourcePaths) {
            sb.append(path);
        }

        return sb.toString();

    }

    public IRODSRuleExecResult runIRODSRule(final String irodsRule) throws IdropException {
        log.info("executing rule: {}", irodsRule);

        try {
            final RuleProcessingAO ruleProcessingAO = irodsFileSystem.getIRODSAccessObjectFactory().getRuleProcessingAO(irodsAccount);
            return ruleProcessingAO.executeRule(irodsRule);
        } catch (JargonException ex) {
            Logger.getLogger(IRODSFileService.class.getName()).log(Level.SEVERE, null, ex);
            throw new IdropException("exception processing rule", ex);
        } finally {
            try {
                irodsFileSystem.close(irodsAccount);
            } catch (JargonException ex) {
                Logger.getLogger(IRODSFileService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public List<Resource> getResources() throws IdropException {
        log.info("getting resources");

        IRODSFileSystem irodsFileSystem = null;
        try {
            irodsFileSystem = IRODSFileSystem.instance();
            final ResourceAO resourceAO = irodsFileSystem.getIRODSAccessObjectFactory().getResourceAO(irodsAccount);
            return resourceAO.findAll();
        } catch (JargonException ex) {
            Logger.getLogger(IRODSFileService.class.getName()).log(Level.SEVERE, null, ex);
            throw new IdropException("exception processing rule", ex);
        } finally {
            try {
                irodsFileSystem.close(irodsAccount);
            } catch (JargonException ex) {
                Logger.getLogger(IRODSFileService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Method will return a listing of collections with a given metadata value, in this case the marker attribute
     * for the result of a virus scan.
     * @param parentCollectionAbsolutePath <code>String</code> with the absolute path to the parent collection.
     * @return <code>List<MetaDataAndDomainData></code> with the results of the query.
     * @throws IdropException
     */
    public List<MetaDataAndDomainData> getVirusStatusForParentCollection(final String parentCollectionAbsolutePath) throws IdropException {
        return getProcessingResultMetadataForCollection(parentCollectionAbsolutePath, "PolicyDrivenService:PolicyProcessingResultAttribute:VirusScan");
    }

    /**
     * Method will return a listing of the marker values for a data object for fixity check status
     * @param parentCollectionAbsolutePath <code>String</code> with the absolute path to the parent collection.
     * @return <code>MetaDataAndDomainData</code> with the results of the query or null.
     * @throws IdropException
     */
    public MetaDataAndDomainData getFixityStatusForDataObject(final String parentCollectionAbsolutePath, final String dataObjectName) throws IdropException {
        List<MetaDataAndDomainData> metaDataList = getProcessingResultMetadataForDataObject(parentCollectionAbsolutePath, dataObjectName, "CHECKSUM%");
        if (metaDataList.size() > 0) {
            return metaDataList.get(0);
        } else {
            return null;
        }
    }

    /**
     * Method will return a listing of the marker values for a data object for virus scan status
     * @param parentCollectionAbsolutePath <code>String</code> with the absolute path to the parent collection.
     * @return <code>MetaDataAndDomainData</code> with the results of the query or null.
     * @throws IdropException
     */
    public MetaDataAndDomainData getVirusStatusForDataObject(final String parentCollectionAbsolutePath, final String dataObjectName) throws IdropException {
        List<MetaDataAndDomainData> metaDataList = getProcessingResultMetadataForDataObject(parentCollectionAbsolutePath, dataObjectName, "VIRUS_SCAN%");
        if (metaDataList.size() > 0) {
            return metaDataList.get(0);
        } else {
            return null;
        }
    }

    /**
     * Get a list of the AVU metadata for the given collection
     * @param irodsAbsolutePath <code>String</code> that is the absolute iRODS path to the collection.
     * @return <code>List<MetaDataAndDomainData><code> with the query results.
     * @throws IdropException
     */
    public List<MetaDataAndDomainData> getMetadataForCollection(final String irodsAbsolutePath) throws IdropException {
        if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
            throw new IdropException("null or empty irodsAbsolutePath");
        }

        log.info("getting metadata for collection:{}", irodsAbsolutePath);

        try {
            final CollectionAO collectionAO = irodsFileSystem.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
            return collectionAO.findMetadataValuesForCollection(irodsAbsolutePath, 0);
        } catch (Exception ex) {
            Logger.getLogger(IRODSFileService.class.getName()).log(Level.SEVERE, null, ex);
            throw new IdropException("exception processing rule", ex);
        } finally {
            try {
                irodsFileSystem.close(irodsAccount);
            } catch (JargonException ex) {
                Logger.getLogger(IRODSFileService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    /**
     * Get a list of the AVU metadata for the given collection
     * @param irodsAbsolutePath <code>String</code> that is the absolute iRODS path to the collection.
     * @return <code>List<MetaDataAndDomainData><code> with the query results.
     * @throws IdropException
     */
    public List<MetaDataAndDomainData> getMetadataForDataObject(final String irodsAbsolutePath, final String fileName) throws IdropException {
        if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
            throw new IdropException("null or empty irodsAbsolutePath");
        }

        if (fileName == null || fileName.isEmpty()) {
            throw new IdropException("null or empty fileName");
        }

        log.info("getting metadata for data object:{}", irodsAbsolutePath);
        log.info("file name:{}", fileName);

        try {
            final DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
            return dataObjectAO.findMetadataValuesForDataObject(irodsAbsolutePath, fileName);
        } catch (Exception ex) {
            Logger.getLogger(IRODSFileService.class.getName()).log(Level.SEVERE, null, ex);
            throw new IdropException("exception processing rule", ex);
        } finally {
            try {
                irodsFileSystem.close(irodsAccount);
            } catch (JargonException ex) {
                Logger.getLogger(IRODSFileService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

      /**
     * Method will return a listing of collections with a given metadata value, in this case the marker attribute
     * for the result of a fixity check.
     * @param parentCollectionAbsolutePath <code>String</code> with the absolute path to the parent collection.
     * @return <code>List<MetaDataAndDomainData></code> with the results of the query.
     * @throws IdropException
     */
    public List<MetaDataAndDomainData> getFixityStatusForParentCollection(final String parentCollectionAbsolutePath) throws IdropException {
        return getProcessingResultMetadataForCollection(parentCollectionAbsolutePath, "PolicyDrivenService:PolicyProcessingResultAttribute:FixityCheck");
    }

    public List<MetaDataAndDomainData> getProcessingResultMetadataForCollection(final String parentCollectionAbsolutePath, final String markerAttribute) throws IdropException {
        if (parentCollectionAbsolutePath == null || parentCollectionAbsolutePath.isEmpty()) {
            throw new IdropException("null or empty parentCollectionAbsolutePath");
        }

        if (markerAttribute == null || markerAttribute.isEmpty()) {
            throw new IdropException("null or empty markerAttribute");
        }

        List<AVUQueryElement> queryElements = new ArrayList<AVUQueryElement>();
        try {
            queryElements.add(AVUQueryElement.instanceForValueQuery(AVUQueryElement.AVUQueryPart.ATTRIBUTE, AVUQueryOperatorEnum.EQUAL, markerAttribute));
        } catch (JargonQueryException ex) {
            Logger.getLogger(IRODSFileService.class.getName()).log(Level.SEVERE, null, ex);
            throw new IdropException(ex);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(RodsGenQueryEnum.COL_COLL_NAME.getName());
        sb.append(" LIKE ");
        sb.append("'");
        sb.append(parentCollectionAbsolutePath);
        sb.append("%");
        sb.append("'");

        try {
            final CollectionAO collectionAO = irodsFileSystem.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
            return collectionAO.findMetadataValuesByMetadataQueryWithAdditionalWhere(queryElements, sb.toString());
        } catch (JargonException ex) {
            Logger.getLogger(IRODSFileService.class.getName()).log(Level.SEVERE, null, ex);
            throw new IdropException("exception processing rule", ex);
        } catch (JargonQueryException ex) {
            Logger.getLogger(IRODSFileService.class.getName()).log(Level.SEVERE, null, ex);
            throw new IdropException("query exception processing rule", ex);
        } finally {
            try {
                irodsFileSystem.close(irodsAccount);
            } catch (JargonException ex) {
                Logger.getLogger(IRODSFileService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public List<MetaDataAndDomainData> getProcessingResultMetadataForDataObject(final String parentCollectionAbsolutePath, final String dataObjectName, final String markerAttribute) throws IdropException {

        if (parentCollectionAbsolutePath == null || parentCollectionAbsolutePath.isEmpty()) {
            throw new IdropException("null or empty parentCollectionAbsolutePath");
        }

        if (dataObjectName == null || dataObjectName.isEmpty()) {
            throw new IdropException("null or empty dataObjectName");
        }

        if (markerAttribute == null || markerAttribute.isEmpty()) {
            throw new IdropException("null or empty markerAttribute");
        }

        List<AVUQueryElement> queryElements = new ArrayList<AVUQueryElement>();
        try {
            queryElements.add(AVUQueryElement.instanceForValueQuery(AVUQueryElement.AVUQueryPart.ATTRIBUTE, AVUQueryOperatorEnum.LIKE, markerAttribute));
        } catch (JargonQueryException ex) {
            Logger.getLogger(IRODSFileService.class.getName()).log(Level.SEVERE, null, ex);
            throw new IdropException(ex);
        }

        try {
            final DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
            return dataObjectAO.findMetadataValuesForDataObjectUsingAVUQuery(queryElements, parentCollectionAbsolutePath, dataObjectName);
        } catch (JargonException ex) {
            Logger.getLogger(IRODSFileService.class.getName()).log(Level.SEVERE, null, ex);
            throw new IdropException("exception processing rule", ex);
        } catch (JargonQueryException ex) {
            Logger.getLogger(IRODSFileService.class.getName()).log(Level.SEVERE, null, ex);
            throw new IdropException("query exception processing rule", ex);
        } finally {
            try {
                irodsFileSystem.close(irodsAccount);
            } catch (JargonException ex) {
                Logger.getLogger(IRODSFileService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * For a given data object, get a list of the resources for that object
     * @param irodsCollectionAbsolutePath <code>String</code> with the absolute path to the collection that holds the data object.
     * @param dataObjectName <code>String</code> with the name of the data object.
     * @return <code>List<Resource></code>
     * @throws IdropException
     */
    public List<Resource> getResourcesForDataObject(final String irodsCollectionAbsolutePath, final String dataObjectName) throws IdropException {
        try {
            DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
            return dataObjectAO.getResourcesForDataObject(irodsCollectionAbsolutePath, dataObjectName);
        } catch (JargonException ex) {
            Logger.getLogger(IRODSFileService.class.getName()).log(Level.SEVERE, null, ex);
            throw new IdropException("exception getting resources for a data object", ex);
        } finally {
            try {
                irodsFileSystem.close(irodsAccount);
            } catch (JargonException ex) {
                Logger.getLogger(IRODSFileService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public boolean createNewFolder(final String newFolderAbsolutePath) throws IdropException {

        log.info("createNewFolder");
        if (newFolderAbsolutePath == null || newFolderAbsolutePath.isEmpty()) {
            throw new IdropException("null or empty newFolderAbsolutePath");
        }

        boolean createSuccessful = false;

        try {
            IRODSFile newDirectory = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(newFolderAbsolutePath);
            createSuccessful = newDirectory.mkdirs();
        } catch (JargonException ex) {
            Logger.getLogger(IRODSFileService.class.getName()).log(Level.SEVERE, null, ex);
            throw new IdropException("exception creating new dir", ex);
        } finally {
            try {
                irodsFileSystem.close(irodsAccount);
            } catch (JargonException ex) {
                Logger.getLogger(IRODSFileService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return createSuccessful;
    }

    public void deleteFileOrFolderNoForce(final String deleteFileAbsolutePath) throws IdropException {

        log.info("deleteFileOrFolderNoForce");

        if (deleteFileAbsolutePath == null || deleteFileAbsolutePath.isEmpty()) {
            throw new IdropException("null or empty deleteFileAbsolutePath");
        }

        log.info("delete path:{}", deleteFileAbsolutePath);


        try {
            IRODSFile deleteFileOrDir = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(deleteFileAbsolutePath);
            deleteFileOrDir.delete();
        } catch (JargonException ex) {
            Logger.getLogger(IRODSFileService.class.getName()).log(Level.SEVERE, null, ex);
            throw new IdropException("exception deleting dir:" + deleteFileAbsolutePath, ex);
        } finally {
            try {
                irodsFileSystem.close(irodsAccount);
            } catch (JargonException ex) {
                Logger.getLogger(IRODSFileService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void moveCollectionUnderneathNewParent(final String currentAbsolutePath, final String newAbsolutePath) throws IdropException {

        log.info("renameFileOrFolder");

        if (currentAbsolutePath == null || currentAbsolutePath.isEmpty()) {
            throw new IdropException("null or empty currentAbsolutePath");
        }

        if (newAbsolutePath == null || newAbsolutePath.isEmpty()) {
            throw new IdropException("null or empty newAbsolutePath");
        }

        log.info("currentAbsolutePath:{}", currentAbsolutePath);
        log.info("newAbsolutePath:{}", newAbsolutePath);

        try {
            DataTransferOperations dataTransferOperations = irodsFileSystem.getIRODSAccessObjectFactory().getDataTransferOperations(irodsAccount);
            dataTransferOperations.moveTheSourceCollectionUnderneathTheTargetCollectionUsingSourceParentCollectionName(currentAbsolutePath, newAbsolutePath);
        } catch (JargonException ex) {
            Logger.getLogger(IRODSFileService.class.getName()).log(Level.SEVERE, null, ex);
            throw new IdropException("exception renaming file", ex);
        } finally {
            try {
                irodsFileSystem.close(irodsAccount);
            } catch (JargonException ex) {
                Logger.getLogger(IRODSFileService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public String renameIRODSFileOrDirectory(final String irodsCurrentAbsolutePath, final String newFileOrCollectionName) throws IdropException {

        if (irodsCurrentAbsolutePath == null || irodsCurrentAbsolutePath.isEmpty()) {
            throw new IdropException("null or empty irodsCurrentAbsolutePath");
        }

        if (newFileOrCollectionName == null || newFileOrCollectionName.isEmpty()) {
            throw new IdropException("null or empty newFileOrCollectionName");
        }

        log.info("rename of IRODSFileOrDirectory, current absPath:{}", irodsCurrentAbsolutePath);
        log.info("newFileOrCollectionName:{}", newFileOrCollectionName);

        String newPath = "";

        try {

            IRODSFileFactory irodsFileFactory = irodsFileSystem.getIRODSFileFactory(irodsAccount);
            IRODSFile sourceFile = irodsFileFactory.instanceIRODSFile(irodsCurrentAbsolutePath);
            StringBuilder newPathSb = new StringBuilder();
            newPathSb.append(sourceFile.getParent());
            newPathSb.append("/");
            newPathSb.append(newFileOrCollectionName);

            newPath = newPathSb.toString();

            DataTransferOperations dataTransferOperations = irodsFileSystem.getIRODSAccessObjectFactory().getDataTransferOperations(irodsAccount);
            dataTransferOperations.move(irodsCurrentAbsolutePath, newPath);
            log.info("move completed");
        } catch (JargonException ex) {
            Logger.getLogger(IRODSFileService.class.getName()).log(Level.SEVERE, null, ex);
            throw new IdropException("exception moving file", ex);
        } finally {
            try {
                irodsFileSystem.close(irodsAccount);
            } catch (JargonException ex) {
                Logger.getLogger(IRODSFileService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return newPath;
    }

    public void moveIRODSFileUnderneathNewParent(final String currentAbsolutePath, final String newAbsolutePath) throws IdropException {

        log.info("moveIRODSFileUnderneathNewParent");

        if (currentAbsolutePath == null || currentAbsolutePath.isEmpty()) {
            throw new IdropException("null or empty currentAbsolutePath");
        }

        if (newAbsolutePath == null || newAbsolutePath.isEmpty()) {
            throw new IdropException("null or empty newAbsolutePath");
        }

        log.info("currentAbsolutePath:{}", currentAbsolutePath);
        log.info("newAbsolutePath:{}", newAbsolutePath);

        try {
            DataTransferOperations dataTransferOperations = irodsFileSystem.getIRODSAccessObjectFactory().getDataTransferOperations(irodsAccount);
            dataTransferOperations.move(currentAbsolutePath, newAbsolutePath);
        } catch (JargonException ex) {
            Logger.getLogger(IRODSFileService.class.getName()).log(Level.SEVERE, null, ex);
            throw new IdropException("exception moving file", ex);
        } finally {
            try {
                irodsFileSystem.close(irodsAccount);
            } catch (JargonException ex) {
                Logger.getLogger(IRODSFileService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
