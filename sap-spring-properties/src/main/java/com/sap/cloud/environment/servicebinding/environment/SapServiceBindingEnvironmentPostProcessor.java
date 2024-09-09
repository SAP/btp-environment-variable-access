package com.sap.cloud.environment.servicebinding.environment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;

import com.sap.cloud.environment.servicebinding.SapServiceBindingsProperties;

/* An {@link EnvironmentPostProcessor} that binds service binding properties to {@link SapServiceBindingsProperties}. */
@Order
public class SapServiceBindingEnvironmentPostProcessor implements EnvironmentPostProcessor
{
    private static final Logger log = LoggerFactory.getLogger(SapServiceBindingEnvironmentPostProcessor.class);

    @Override
    public void postProcessEnvironment( ConfigurableEnvironment environment, SpringApplication application )
    {
        Bindable<SapServiceBindingsProperties> bindable = Bindable.of(SapServiceBindingsProperties.class);

        BindResult<?> bindResult = Binder.get(environment).bind("cds", bindable);
        if( bindResult.isBound() ) {
            SapServiceBindingsPropertiesAccessor
                .setServiceBindingsProperties(((SapServiceBindingsProperties) bindResult.get()).getServiceBindings());
        } else if( bindable.getValue() != null ) {
            SapServiceBindingsPropertiesAccessor
                .setServiceBindingsProperties(bindable.getValue().get().getServiceBindings());
        } else {
            log.info("Could not bind any service from the properties.");
        }
    }
}
