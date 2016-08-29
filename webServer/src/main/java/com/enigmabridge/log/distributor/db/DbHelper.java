package com.enigmabridge.log.distributor.db;

import com.enigmabridge.log.distributor.LogConstants;
import com.enigmabridge.log.distributor.db.dao.ApiKey2ClientDao;
import com.enigmabridge.log.distributor.db.dao.ClientDao;
import com.enigmabridge.log.distributor.db.dao.DomainDao;
import com.enigmabridge.log.distributor.db.dao.EBHostDao;
import com.enigmabridge.log.distributor.db.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * DB Helper.
 *
 * Created by dusanklinec on 11.08.16.
 */
@Component
@Repository
@Transactional
public class DbHelper {

    @Autowired
    private DomainDao domainDao;

    @Autowired
    private ClientDao clientDao;

    @Autowired
    private EBHostDao hostDao;

    @Autowired
    private ApiKey2ClientDao apikey2ClientDao;

    @Autowired
    private EntityManager entityManager;

    private int batchSize = 100;

    @Transactional
    public Domain getDomain(String domain){
        return getDomain(domain, true);
    }

    /**
     * Loads Domain record for the particular domain string.
     * If domain parameter is null, default domain name is used.
     *
     * @param domain domain ID to load
     * @param createIfNotPresent if domain record does not exist with given ID, should new record be created?
     * @return domain db record with given ID. Existing or newly created. May be null if createIfNotPresent == false.
     */
    @Transactional
    public Domain getDomain(String domain, boolean createIfNotPresent){
        if (domain == null || domain.isEmpty()){
            domain = LogConstants.DEFAULT_DOMAIN;
        }

        domain = domain.toLowerCase();

        final Domain domain1 = domainDao.findByDomain(domain);
        if (domain1 != null || !createIfNotPresent){
            return domain1;
        }

        final Domain domain2 = new Domain(domain);
        domainDao.save(domain2);

        return domain2;
    }

    @Transactional
    public Client findByClientIdAndDomain(String clientId, Domain domain){
        return clientDao.findByClientIdAndDomain(clientId, domain);
    }

    @Transactional
    public Client findByClientIdAndDomain(String clientId, String domain){
        final Domain d = getDomain(domain, false);
        if (d == null){
            return null;
        }

        return clientDao.findByClientIdAndDomain(clientId, d);
    }

    @Transactional
    public List<Client> deleteClientsByClientIdAndDomain(String clientId, String domain){
        final Domain d = getDomain(domain, false);
        if (d == null){
            return Collections.emptyList();
        }

        return clientDao.deleteByClientIdAndDomain(clientId, d);
    }

    public Iterable<Domain> findAllDomains() {
        return domainDao.findAll();
    }

    public Iterable<EBHost> findAllHosts() {
        return hostDao.findAll();
    }

    public Iterable<Client> findAllClients() {
        return clientDao.findAll();
    }

    public Iterable<ApiKey2Client> findAllApiMappings() {
        return apikey2ClientDao.findAll();
    }

    /**
     * Stores collection of entities in a bulk
     * http://frightanic.com/software-development/jpa-batch-inserts/
     *
     * @param entities collection of entities to store
     * @return list of persisted entities
     */
    @Transactional
    public <T extends DBID> Collection<T> bulkSave(Collection<T> entities) {
        return bulkSave(entities, batchSize);
    }

    @Transactional
    public <T extends DBID> Collection<T> bulkSave(Collection<T> entities, int batchSize) {
        final List<T> savedEntities = new ArrayList<T>(entities.size());
        int i = 0;
        for (T t : entities) {
            T newEntity = t;
            if (t.getId() == null) {
                entityManager.persist(t);

            } else {
                entityManager.merge(t);
            }

            savedEntities.add(newEntity);
            i++;
            if (i % batchSize == 0) {
                // Flush a batch of inserts and release memory.
                entityManager.flush();
                entityManager.clear();
            }
        }

        entityManager.flush();
        entityManager.clear();
        return savedEntities;
    }

    private <T extends DBID> T persistOrMerge(T t) {
        if (t.getId() == null) {
            entityManager.persist(t);
            return t;
        } else {
            return entityManager.merge(t);
        }
    }

}
