package org.qualityannotate.core.config;

import io.quarkus.config.yaml.runtime.ApplicationYamlConfigSourceLoader;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.ConfigSourceProvider;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CustomApplicationYamlConfigSourceLoader {

    public static class CustomLoader extends ApplicationYamlConfigSourceLoader implements ConfigSourceProvider {
        @Override
        public List<ConfigSource> getConfigSources(final ClassLoader classLoader) {
            ArrayList<ConfigSource> configSources = new ArrayList<>(loadConfigSources(
                    Paths.get(System.getProperty("user.home"), ".config", "qualityannotate.yaml").toUri().toString(),
                    280,
                    classLoader));
            configSources.addAll(loadConfigSources(
                    Paths.get(System.getProperty("user.home"), ".config", "qualityannotate.yml").toUri().toString(),
                    280,
                    classLoader));
            return configSources;
        }

    }
}
