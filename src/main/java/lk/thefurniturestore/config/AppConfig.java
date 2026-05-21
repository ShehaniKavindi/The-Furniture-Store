package lk.thefurniturestore.config;

import org.glassfish.jersey.server.ResourceConfig;

public class AppConfig extends ResourceConfig {
    public AppConfig() {
        packages("lk.thefurniturestore.controller");
        packages("lk.thefurniturestore.middleware");
        register(org.glassfish.jersey.media.multipart.MultiPartFeature.class);
    }
}
