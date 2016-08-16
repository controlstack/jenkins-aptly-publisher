package io.controlstack.jenkins.aptly

import spock.lang.*
import groovy.json.*
// import java.nio.file.Paths

import io.controlstack.jenkins.aptly.exceptions.*

import org.junit.Rule
import org.junit.rules.TemporaryFolder


class AptlyWrapperSpec extends Specification {
    @Rule final TemporaryFolder aptlyDir = new TemporaryFolder()
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
    }

    def cleanup() {

    }

    def "Create new aptly repository with distribution" () {
        when: "Repository is created"
            boolean success = aptly.createRepository name: "b", distribution: "wheezy"

        then: "Success is returned"
            success == true
    }

    def "Create new aptly repository with default distribution" () {
        when: "Repository is created"
            boolean success = aptly.createRepository name: "name"

        then: "Success is returned"
            success == true
    }

    def "Create repository with empty name fails" () {
        when: "Empty repository is created"
            aptly.createRepository name: ""

        then: "RunCmdException is thrown"
            thrown(IllegalArgumentException)

    }

    def "Create repository without name fails" () {
        when:
            aptly.createRepository distribution: "wheezy"

        then:
            thrown(IllegalArgumentException)
    }

    def "Aptly fails to create the same repository twice" () {
        when: "Repository 'a' is created two times"
            aptly.createRepository name: "a"
            aptly.createRepository name: "a"            
        then: "RunCmdException is thrown"
            thrown(RunCmdException)
    }

    def "List repositories" (){
        given: "Two repositories 'zz', 'aa' are created"
            aptly.createRepository name: "zz"
            aptly.createRepository name: "zd"
        
        when: "List of repositories is retrieved"
            def repositories = aptly.repositories

        then: "List of repositories has 'zz', 'aa'"
            repositories.sort() == [ "zz", "zd" ].sort()
    }

    def "Add package to repository" () {
        given: "Package exists in 'a' repository"
            aptly.createRepository name: "a"
            def pkg = new File('src/test/resources/test_1.0_amd64.deb')
            aptly.addPackage("a", pkg.path)

        when: "Package list retrieved"
            def packages = aptly.getPackages("a")
        
        then: "Added package exists"
            packages.contains("test_1.0_amd64")
    }

    def "Add invalid package fails" () {
        given: "Repository 'a' exist"
            aptly.createRepository name: "a"

        when: "Invalid package is added"
            def pkg = new File('src/test/aptly.conf')
            aptly.addPackage('a', pkg.path)
        
        then:
            thrown(RunCmdException)
    }

    def "Publish repository" () {
        given:
            aptly.createRepository name: "a"
            aptly.createRepository name: "b", distribution: "wheezy"
            aptly.createRepository name: "c", distribution: "lewl"

        when:
            aptly.publishRepository name: "a"
            aptly.publishRepository name: "b"
            def repositories = aptly.getPublishedRepositories()

        then:
            repositories.sort() == ["a", "b"].sort()
    }

    def "Publish same distribution fails" () {
        given:
            aptly.createRepository name: "a", distribution: "wheezy"
            aptly.createRepository name: "b", distribution: "wheezy"
        
        when:
            aptly.publishRepository name: "a"
            aptly.publishRepository name: "b"

        then:
            thrown(RunCmdException)
    }
            

    def "Update unpublished repository fails" () {
        given:
            aptly.createRepository name: "a"
            aptly.createRepository  name: "b"
        
        when:
            aptly.publishRepository name: "a", update: true
        
        then:
            thrown(RunCmdException)
    }

    def "Update published repository" () {
        given:
            aptly.createRepository name: "a"
            aptly.publishRepository name: "a"
        
        when:
            aptly.publishRepository name: "a", update: true
        
        then:
            def repositories = aptly.getPublishedRepositories()
            repositories.contains("a")
    }

    def "Add existing package to repository" () {
    }
}