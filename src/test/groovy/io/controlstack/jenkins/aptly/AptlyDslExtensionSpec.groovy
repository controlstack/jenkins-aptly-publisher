package io.controlstack.jenkins.aptly

import groovy.json.*

import spock.lang.Specification

import hudson.model.*
import jenkins.model.Jenkins
import org.jvnet.hudson.test.JenkinsRule

import javaposse.jobdsl.dsl.DslScriptLoader
import javaposse.jobdsl.plugin.JenkinsJobManagement

import org.junit.Rule

class AptlyDslExtensionSpec extends Specification {
    @Rule
    JenkinsRule j = new JenkinsRule()

    def "Add aptly publisher" () {
        setup:

            def contents = """
            job("sample") {
                steps {
                    shell "mkdir -p deb"
                    shell "fpm -s dir -t deb -n test -v 0.0.1 deb"
                }
                publishers {
                    publishToAptly "sample"
                }
            }
            """

            def jobManagement = new JenkinsJobManagement(System.out, [:], new File('.'))
            def jobs = new DslScriptLoader(jobManagement).runScript(contents).jobs
        when:
            def myjob = Jenkins.instance.getItem("sample")
            def mypub = myjob.getPublishersList().first()

        then:
            jobs.size() == 1
            myjob.getPublishersList().size() == 1
            mypub.getClass() == AptlyPublisher.class
    }
}