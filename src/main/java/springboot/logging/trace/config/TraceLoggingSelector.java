package springboot.logging.trace.config;

import java.util.Map;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import springboot.logging.trace.filter.TraceLoggingFilter;

/**
 * trace logging selector.
 * @author leo
 */
public class TraceLoggingSelector implements ImportBeanDefinitionRegistrar /*ImportSelector*/ {
    /**
     * 注册
     * @param importingClassMetadata {@link AnnotationMetadata}
     * @param registry {@link BeanDefinitionRegistry}
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        Map<String, Object> annotationAttributes = importingClassMetadata.getAnnotationAttributes(EnableLoggingTrace.class.getName());
        if (annotationAttributes == null){
            return;
        }
        if (!(Boolean) annotationAttributes.get("enable")){
            return;
        }
        AbstractBeanDefinition beanDefinition =BeanDefinitionBuilder.genericBeanDefinition(TraceLoggingFilter.class)
                .addPropertyValue("headers",annotationAttributes.get("headers")).getBeanDefinition();
        BeanDefinitionReaderUtils.registerWithGeneratedName(beanDefinition, registry);
    }


    /* @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        Map<String, Object> annotationAttributes = importingClassMetadata.getAnnotationAttributes(EnableLoggingTrace.class.getName());

        if (!Boolean.class.cast(annotationAttributes.getOrDefault("enable","false"))){
            return new String [0];
        }
        return new String[]{TraceLoggingFilter.class.getName()};
    }*/
}
