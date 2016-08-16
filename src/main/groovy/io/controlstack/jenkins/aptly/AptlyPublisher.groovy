package io.controlstack.jenkins.aptly

import hudson.Launcher
import hudson.Extension
import hudson.FilePath

import hudson.model.BuildListener
import hudson.model.TaskListener
import hudson.model.AbstractBuild
import hudson.model.Run

import hudson.tasks.Recorder
import hudson.tasks.Publisher
import hudson.tasks.BuildStepMonitor
import hudson.tasks.BuildStepDescriptor

import org.kohsuke.stapler.DataBoundConstructor
import org.kohsuke.stapler.DataBoundSetter

import org.apache.commons.io.FilenameUtils

import javax.annotation.Nonnull

class AptlyPublisher extends Recorder implements Serializable {
    private static final long serialVersionUID = 1L;

    private String repository
    private String aptlyConfigPath
    private PrintStream log

    @DataBoundConstructor
    AptlyPublisher(String repository) {
        this.repository = repository
        // this.log        = System.out
    }

    public void perform(@Nonnull Run<?, ?> build, @Nonnull FilePath workspace, @Nonnull Launcher launcher, @Nonnull TaskListener listener) {    
        AptlyWrapper aptly = new AptlyWrapper()
        
        if (this.aptlyConfigPath) {
            aptly.config = this.aptlyConfigPath
        }

        List<Run.Artifact> artifacts = build.getArtifacts()
        System.out.println("WE HAVE ${artifacts}")

        artifacts?.each { artifact ->
            System.out.println("ARTIFACT: ${artifact.getFile().getAbsolutePath()}")
            String path = artifact.getFile().getAbsolutePath()
            String ext = FilenameUtils.getExtension(path)

            if (ext == "deb") {
                aptly.addPackage(this.repository, path)
            }
        }
        
        aptly.publishRepository(name: this.repository, update: true)
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        perform(build, build.getWorkspace(), launcher, (TaskListener)listener)
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        public DescriptorImpl() {
            load()
        }

        @Override
        public String getDisplayName() {
            return "Publish To Aptly"
        }

        @Override
        public boolean isApplicable(Class type) {
            true
        }
    }

    @DataBoundSetter
    void setRepository(String value) {
        this.repository = value
    }

    String getRepository() {
        this.repository
    }

    @DataBoundSetter
    void setAptlyConfigPath(String value) {
        this.aptlyConfigPath = value
    }

    String getAptlyConfigPath() {
        this.aptlyConfigPath
    }
}