package edu.utah.kmm.opencds.config;

import org.opencds.config.api.ConfigData;
import org.opencds.config.api.KnowledgeRepository;
import org.opencds.config.api.KnowledgeRepositoryService;
import org.opencds.config.api.cache.CacheService;
import org.opencds.config.api.dao.FileDao;
import org.opencds.config.api.dao.file.ClasspathResourceDaoImpl;
import org.opencds.config.api.dao.util.ResourceUtil;
import org.opencds.config.api.strategy.AbstractConfigStrategy;
import org.opencds.config.api.strategy.ConfigCapability;
import org.opencds.config.file.dao.*;
import org.opencds.config.service.*;
import org.opencds.plugin.PluginDataCache;
import org.opencds.plugin.support.PluginDataCacheImpl;

import java.util.Collections;
import java.util.HashSet;

/**
 * This is an alternative to OpenCDS' class path strategy.  It does not require packaging
 * the configuration into a JAR.  Rather, it uses a custom resource utility class to locate
 * the configuration files in a specified class path.
 */
public class ClasspathConfigStrategy extends AbstractConfigStrategy {

    private static final String CLASSPATH_SEPARATOR = "/";

    public ClasspathConfigStrategy() {
        super(new HashSet<>(Collections.singletonList(ConfigCapability.READ_ONCE)), "CLASSPATH2");
    }

    @Override
    public KnowledgeRepository getKnowledgeRepository(
            ConfigData configData,
            CacheService cacheService) {
        ResourceUtil classpathUtil = new ClasspathUtil(configData.getConfigLocation());
        String path = configData.getConfigLocation().toString() + CLASSPATH_SEPARATOR;

        ConceptDeterminationMethodServiceImpl cdmService = new ConceptDeterminationMethodServiceImpl(new ConceptDeterminationMethodFileDao(classpathUtil,
                path + CDMS), cacheService);

        ExecutionEngineServiceImpl eeService = new ExecutionEngineServiceImpl(new ExecutionEngineFileDao(classpathUtil,
                path + EXECUTION_ENGINES), cacheService);

        FileDao ppFileDao = new ClasspathResourceDaoImpl(classpathUtil,
                path + PLUGIN_DIR + CLASSPATH_SEPARATOR + PACKAGES);
        PluginPackageServiceImpl ppService = new PluginPackageServiceImpl(new PluginPackageFileDao(classpathUtil,
                path + PLUGIN_DIR), ppFileDao, cacheService);

        FileDao sdFileDao = new ClasspathResourceDaoImpl(classpathUtil,
                path + SUPPORTING_DATA_DIR + CLASSPATH_SEPARATOR + PACKAGES);
        SupportingDataPackageServiceImpl sdpService = new SupportingDataPackageServiceImpl(sdFileDao, cacheService);
        SupportingDataServiceImpl sdService = new SupportingDataServiceImpl(new SupportingDataFileDao(classpathUtil,
                path + SUPPORTING_DATA_DIR), sdpService, ppService, cacheService);

        FileDao kpFileDao = new ClasspathResourceDaoImpl(classpathUtil,
                path + KNOWLEDGE_PACKAGE_DIR);
        KnowledgePackageServiceImpl kpService = new KnowledgePackageServiceImpl(configData, eeService, kpFileDao, cacheService);
        KnowledgeModuleServiceImpl kmService = new KnowledgeModuleServiceImpl(new KnowledgeModuleFileDao(classpathUtil,
                path + KNOWLEDGE_MODULES), kpService, sdService, cacheService);

        SemanticSignifierServiceImpl ssService = new SemanticSignifierServiceImpl(new SemanticSignifierFileDao(classpathUtil,
                path + SEMANTIC_SIGNIFIERS), cacheService);

        ConceptServiceImpl conceptService = new ConceptServiceImpl(cdmService, kmService, cacheService);

        PluginDataCache pluginDataCache = new PluginDataCacheImpl();

        return new KnowledgeRepositoryService(cdmService, conceptService, eeService, kmService, kpService, ppService,
                ssService, sdService, sdpService, cacheService, pluginDataCache);
    }

}

