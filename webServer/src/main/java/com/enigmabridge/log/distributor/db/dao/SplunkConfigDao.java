package com.enigmabridge.log.distributor.db.dao;

import com.enigmabridge.log.distributor.db.model.SplunkConfig;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by dusanklinec on 04.08.16.
 */
public interface SplunkConfigDao extends CrudRepository<SplunkConfig, Integer> {
}
