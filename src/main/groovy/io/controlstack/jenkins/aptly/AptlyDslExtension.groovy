package io.controlstack.jenkins.aptly

import hudson.Extension
import javaposse.jobdsl.dsl.helpers.publisher.PublisherContext
import javaposse.jobdsl.plugin.DslExtensionMethod
import javaposse.jobdsl.plugin.ContextExtensionPoint

// import java.util.logging.Logger

@Extension(optional=true)
class AptlyDslExtension extends ContextExtensionPoint {

    @DslExtensionMethod(context = PublisherContext.class)
    public Object aptly(String repository) {
        return new AptlyPublisher(repository)
    }

}