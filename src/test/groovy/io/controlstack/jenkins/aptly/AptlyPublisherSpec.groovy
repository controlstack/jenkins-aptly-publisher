package io.controlstack.jenkins.aptly

import groovy.json.*

import spock.lang.Specification

import hudson.model.*
import hudson.tasks.Shell
import hudson.tasks.ArtifactArchiver

import org.jvnet.hudson.test.JenkinsRule

import org.junit.rules.TemporaryFolder
import org.junit.Rule

class AptlyPublisherSpec extends Specification {
    @Rule
    JenkinsRule j = new JenkinsRule()

    @Rule
    final TemporaryFolder aptlyDir = new TemporaryFolder()

    FreeStyleProject project
    AptlyWrapper aptly
    
    def setup() {
        

        JsonSlurper slurper = new JsonSlurper()
        def configTemplate = slurper.parseText(new File('src/test/resources/aptly.conf').text)

        configTemplate.rootDir = aptlyDir.root.getAbsolutePath()

        def config = aptlyDir.newFile('.aptly.conf')
        config.write JsonOutput.toJson(configTemplate)

        aptly = new AptlyWrapper()
        aptly.logger = System.out
        aptly.config = config.path

        aptly.createRepository name: "myrepo"
        aptly.publishRepository name: "myrepo"

    }

    def "Add aptly publisher" () {
        setup:
            project = j.createFreeStyleProject('myproject')

            def pub = new AptlyPublisher("myrepo")
            pub.aptlyConfigPath = aptly.config

            project.getBuildersList().add(new Shell("mkdir deb"))
            project.getBuildersList().add(new Shell("fpm -s dir -t deb -n test -v 0.0.1 deb"))
            project.getPublishersList().add(new ArtifactArchiver("*.deb"))
            project.getPublishersList().add(pub)

        when:
            FreeStyleBuild build = project.scheduleBuild2(0).get()

        then:
            build.getResult() == Result.SUCCESS
            def packages = aptly.getPackages("myrepo")
            packages.contains('test_0.0.1_amd64')
    }
}