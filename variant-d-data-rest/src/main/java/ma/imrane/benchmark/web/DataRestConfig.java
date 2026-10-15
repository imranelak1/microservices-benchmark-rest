package ma.imrane.benchmark.web;
import ma.imrane.benchmark.model.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
@Configuration
public class DataRestConfig implements RepositoryRestConfigurer {
    @Override public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config,CorsRegistry cors) {config.exposeIdsFor(Item.class,Category.class);config.setDefaultPageSize(50);config.setMaxPageSize(200);config.setReturnBodyOnCreate(true);config.setReturnBodyOnUpdate(true);}
}
