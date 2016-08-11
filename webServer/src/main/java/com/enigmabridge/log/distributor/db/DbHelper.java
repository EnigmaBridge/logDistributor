package com.enigmabridge.log.distributor.db;

import com.enigmabridge.log.distributor.LogConstants;
import com.enigmabridge.log.distributor.db.dao.ClientDao;
import com.enigmabridge.log.distributor.db.dao.DomainDao;
import com.enigmabridge.log.distributor.db.dao.EBHostDao;
import com.enigmabridge.log.distributor.db.model.Client;
import com.enigmabridge.log.distributor.db.model.Domain;
import com.enigmabridge.log.distributor.db.model.EBHost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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



}
