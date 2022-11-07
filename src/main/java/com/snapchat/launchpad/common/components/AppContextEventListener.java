package com.snapchat.launchpad.common.components;


import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.stereotype.Component;

@Component
public class AppContextEventListener {
    private static final Logger logger = LoggerFactory.getLogger(AppContextEventListener.class);

    @EventListener
    public void handleContextRefreshed(ContextRefreshedEvent event) {
        printActiveProperties(
                (ConfigurableEnvironment) event.getApplicationContext().getEnvironment());
    }

    private void printActiveProperties(ConfigurableEnvironment env) {
        System.out.println(
                "************************* ACTIVE APP PROPERTIES ******************************");
        System.out.println("Active Profiles: " + Arrays.toString(env.getActiveProfiles()));

        List<MapPropertySource> propertySources =
                env.getPropertySources().stream()
                        .filter(
                                it ->
                                        it instanceof MapPropertySource
                                                && it.getName().contains("application.yml"))
                        .map(it -> (MapPropertySource) it)
                        .collect(Collectors.toList());

        propertySources.stream()
                .map(propertySource -> propertySource.getSource().keySet())
                .flatMap(Collection::stream)
                .distinct()
                .sorted()
                .forEach(
                        key -> {
                            try {
                                System.out.println(key + "=" + env.getProperty(key));
                            } catch (Exception e) {
                                logger.warn("{} -> {}", key, e.getMessage());
                            }
                        });
        System.out.println(
                "******************************************************************************");
    }
}
