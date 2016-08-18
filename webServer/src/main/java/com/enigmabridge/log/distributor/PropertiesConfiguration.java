package com.enigmabridge.log.distributor;

import com.enigmabridge.log.distributor.api.ApiConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toMap;

/**
 * Override properties file location / configuration file.
 *
 * http://stackoverflow.com/questions/25855795/spring-boot-and-multiple-external-configuration-files
 * Created by dusanklinec on 01.08.16.
 */
@Configuration
class PropertiesConfiguration {
    private final static Logger LOG = LoggerFactory.getLogger(PropertiesConfiguration.class);
    private final static String[] PROPERTIES_FILENAMES = {"application.properties"};
    private static final String CONFIG_FILE_NAME = "appConfig.yml";

    @Value("${properties.location:}")
    private String propertiesLocation;

    @Value("${config.location:}")
    private String configLocation;

    @Bean(name = "properties-config")
    public Map<String, Properties> myProperties() {
        return stream(PROPERTIES_FILENAMES)
                .collect(toMap(filename -> filename, this::loadProperties));
    }

    @Bean(name = ApiConfig.YAML_CONFIG)
    @DependsOn(value = "properties-config")
    public PropertySourcesPlaceholderConfigurer configFileLoad() {
        final PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
        final YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();

        final Resource[] possiblePropertiesResources = {
                new PathResource(getCustomConfigPath()),
                new PathResource("/etc/logdist/" + CONFIG_FILE_NAME),
                new PathResource("config/" + CONFIG_FILE_NAME),
                new PathResource(CONFIG_FILE_NAME),
                new ClassPathResource(CONFIG_FILE_NAME)
        };
        final Optional<Resource> resource =
                stream(possiblePropertiesResources)
                .filter(Resource::exists)
                .reduce((previous, current) -> current);

        if (!resource.isPresent()){
            return propertySourcesPlaceholderConfigurer;
        }

        // Log which file was actually used.
        try {
            LOG.info("Using config file: {}", resource.get().getFile());
        } catch (Exception e) {
            LOG.error("Could not get file info", e);
        }

        yaml.setResources(resource.get());
        propertySourcesPlaceholderConfigurer.setProperties(yaml.getObject());
        return propertySourcesPlaceholderConfigurer;
    }

    private Properties loadProperties(final String filename) {
        final Resource[] possiblePropertiesResources = {
                new ClassPathResource(filename),
                new PathResource("config/" + filename),
                new PathResource(filename),
                new PathResource(getCustomPath(filename))
        };
        final Resource resource = stream(possiblePropertiesResources)
                .filter(Resource::exists)
                .reduce((previous, current) -> current)
                .get();
        final Properties properties = new Properties();

        try {
            properties.load(resource.getInputStream());
        } catch(final IOException exception) {
            throw new RuntimeException(exception);
        }

        LOG.info("Using {} as user resource", resource);

        return properties;
    }

    private String getCustomPath(final String filename) {
        if (propertiesLocation == null){
            return filename;
        }
        return propertiesLocation.endsWith(".properties") ? propertiesLocation : propertiesLocation + filename;
    }

    private String getCustomConfigPath() {
        if (configLocation == null){
            return CONFIG_FILE_NAME;
        }
        return configLocation.endsWith(".yml") ? configLocation : configLocation + "/" + CONFIG_FILE_NAME;
    }

}