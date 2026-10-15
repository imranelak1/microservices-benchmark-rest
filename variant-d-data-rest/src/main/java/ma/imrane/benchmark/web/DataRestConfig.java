package ma.imrane.benchmark.web;
import ma.imrane.benchmark.model.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortHandlerMethodArgumentResolver;
@Configuration
public class DataRestConfig implements RepositoryRestConfigurer {
    @Override public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config,CorsRegistry cors) {config.exposeIdsFor(Item.class,Category.class);config.setDefaultPageSize(50);config.setMaxPageSize(200);config.setReturnBodyOnCreate(true);config.setReturnBodyOnUpdate(true);}
    /** Même ordre stable que les variantes Jersey et MVC, y compris les collections automatiques. */
    @Bean static BeanPostProcessor defaultRepositorySort(){
        return new BeanPostProcessor(){
            @Override public Object postProcessAfterInitialization(Object bean,String name){
                if(bean instanceof SortHandlerMethodArgumentResolver resolver)resolver.setFallbackSort(Sort.by("id"));
                return bean;
            }
        };
    }
}
