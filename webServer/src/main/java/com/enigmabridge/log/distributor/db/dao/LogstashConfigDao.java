package com.enigmabridge.log.distributor.db.dao;

import com.enigmabridge.log.distributor.db.model.LogstashConfig;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by dusanklinec on 04.08.16.
 */
public interface LogstashConfigDao extends CrudRepository<LogstashConfig, Integer> {
}
