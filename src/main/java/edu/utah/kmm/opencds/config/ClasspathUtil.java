package edu.utah.kmm.opencds.config;

import org.apache.commons.lang3.StringUtils;
import org.opencds.config.api.dao.util.ResourceUtil;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Searches for all files within a specified class path using Spring's
 * PathMatchingResourcePatternResolver.  Also, dynamically constructs a
 * "knowledgeModules.xml" resource from individual "knowledgeModule.xml"
 * resources.
 */
public class ClasspathUtil implements ResourceUtil {

    private static final String KM_FRAGMENT_LOCATION = "edu/utah/kmm/dmd/opencds/config/";

    private final Map<String, Resource> resourceMap = new HashMap<>();

    private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    private int dupCount;

    public ClasspathUtil(Path configLocation) {
        String root = configLocation.toString();
        findAll("classpath*:%s/**", root);
        findAll("classpath*:META-INF/%s/**", root);
        this.resourceMap.put(root + "/knowledgeModules.xml", getKnowledgeModulesResource());
    }

    /**
     * Adds a resource to the resource map using the relative path as the key.  If an entry already exists in
     * the map, an underscore + unique # is appended to the file's name portion.
     *
     * @param key      The relative path of the resource.
     * @param resource The resource to add.
     */
    private void addResource(
            String key,
            Resource resource) {
        if (resourceMap.containsKey(key)) {
            int i = key.lastIndexOf(".");
            String dupStr = "_" + ++dupCount;
            key = i == -1 ? key + dupStr : key.substring(0, i) + dupStr + key.substring(i);
            addResource(key, resource);
        } else {
            resourceMap.put(key, resource);
        }
    }

    /**
     * Build a map of all file resources discovered in the specified path,
     * indexed by the path and file name.
     *
     * @param path The class path to be searched.
     * @param root The root folder for the resource path.
     */
    private void findAll(
            String path,
            String root) {
        Resource[] resources;
        path = String.format(path, root);

        try {
            resources = resolver.getResources(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (Resource resource : resources) {
            if (resource.isReadable()) {
                String resPath = getPath(resource);
                int i = resPath.lastIndexOf(root);
                resPath = resPath.substring(i);
                addResource(resPath, resource);
            }
        }

    }

    /**
     * Extracts the absolute path of the specified resource.
     *
     * @param resource The resource.
     * @return The absolute path of the resource.
     */
    private String getPath(Resource resource) {
        try {
            return resource.getURL().getPath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> findFiles(
            String path,
            boolean traverse) {
        List<String> files = new ArrayList<>();
        path = StringUtils.appendIfMissing(path, "/");

        for (String file : resourceMap.keySet()) {
            if (file.startsWith(path) && (traverse || !file.substring(path.length()).contains("/"))) {
                files.add(file);
            }
        }

        return files;
    }

    @Override
    public InputStream getResourceAsStream(String resource) {
        return getResourceAsStream(resourceMap.get(resource));
    }

    /**
     * Returns an input stream for the specified resource, handling null inputs
     * and IO exceptions.
     *
     * @param resource A resource.
     * @return An input stream for the resource (or nul if the resource was null).
     */
    private InputStream getResourceAsStream(Resource resource) {
        try {
            return resource == null ? null : resource.getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns an input stream resource that is a concatenation of
     * input streams for each knowledge module definition found in
     * the global class path.
     *
     * @return A concatenated input stream.
     */
    private Resource getKnowledgeModulesResource() {
        return getCompositeResource("classpath*:META-INF/**/knowledgeModule.xml", "knowledgeModules");
    }

    private Resource getCompositeResource(
            String pattern,
            String fragmentPrefix) {
        try {
            fragmentPrefix = KM_FRAGMENT_LOCATION + fragmentPrefix;
            List<Resource> resources = new ArrayList<>();
            resources.add(resolver.getResource(fragmentPrefix + "1.txt"));
            Resource[] kms = resolver.getResources(pattern);
            resources.addAll(Arrays.asList(kms));
            resources.add(resolver.getResource(fragmentPrefix + "2.txt"));
            List<InputStream> streams = resources.stream()
                    .map(this::getResourceAsStream)
                    .collect(Collectors.toList());
            InputStream is = new SequenceInputStream(Collections.enumeration(streams));
            return new InputStreamResource(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> listMatchingResources(
            String path,
            String startsWith,
            String endsWith) {
        List<String> matches = new ArrayList<>();
        startsWith = StringUtils.appendIfMissing(path, "/") + startsWith;

        for (String file : resourceMap.keySet()) {
            if (file.startsWith(startsWith) && file.endsWith(endsWith)) {
                matches.add(file);
            }
        }

        return matches;
    }
    
}
