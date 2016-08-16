package io.controlstack.jenkins.aptly

import io.controlstack.jenkins.aptly.exceptions.RunCmdException

class AptlyWrapper {

    private final String executable

    String config
    private PrintStream logger = System.out

    AptlyWrapper() {
        this.executable = "aptly"
    }

    AptlyWrapper(String cmd) {
        this.executable = cmd
    }

    public boolean createRepository(LinkedHashMap opts=[:]) {
        String name = opts.name
        opts.remove "name"

        if (!name) {
            throw new IllegalArgumentException("createRepository failed: No repository name specified")
        }

        opts.config = opts.config ?: config
        opts.distribution = opts.distribution ?: name

        def p = new Proc(opts)
        p.run("repo create", name)

        logger.println p.output()

        if (p.exitCode != 0) {
            throw new RunCmdException("createRepository failed: ${p.stderr}")
        }
        true
    }

    public boolean publishRepository(LinkedHashMap opts) {
        String name = opts.name
        opts.remove "name"

        String update = opts.update
        opts.remove "update"

        opts.config = opts.config ?: config
        opts.architectures = opts.architectures ?: "amd64,all"

        def p = new Proc(opts)

        def cmd = update ? "publish update" : "publish repo"
        p.run(cmd, name)

        logger.println p.output()

        if (p.exitCode != 0) {
            throw new RunCmdException("publishRepository failed: ${p.stderr}")
        }
        true
    }

    public boolean addPackage(String repository, String path) {
        def p = new Proc(config: config)
        p.run "repo add --force-replace=true", repository, path
        logger.println p.output()

        if (p.exitCode != 0) {
            throw new RunCmdException("addPackage failed: ${p.stderr}")
        }
        true
    }

    public ArrayList<String> getRepositories() {
        def p = new Proc(config: config)
        p.run("repo list")
        logger.println p.output()

        String out = p.stdout.toString()
        (out =~ / .*\* \[(.*)\].*/).collect{ it[1]}
    }

    public ArrayList<String> getPublishedRepositories() {
        def p = new Proc(config: config)
        p.run("publish list")

        String out = p.stdout.toString()
        logger.println out

        ( out =~ /  \*.* publishes \{.*\[(.*)\]\}/).collect{ it[1] }
    }

    public ArrayList<String> getPackages(String repository) {
        def p = new Proc(config: config, with_packages: true)
        p.run("repo show", repository)

        String out = p.stdout.toString()
        logger.println out
        (out =~ /  (.*)$/).collect{ it[1] }
    }

    public void setLogger(PrintStream value) {
        this.logger = value
    }

    public void setConfig(String value) {
        this.config = value
    }

    class Proc {
        int exitCode
        StringBuilder stdout
        StringBuilder stderr

        private HashMap opts

        Proc (HashMap opts) {
            this.opts = opts
            this.stdout = new StringBuilder()
            this.stderr = new StringBuilder()
            this.exitCode = 0
        }

        public void run(String cmd, String... params) {
            String cmdStr = "${executable} ${cmd} ${opts()} ${params.join(' ')}"

            Process proc = cmdStr.execute()
            proc.consumeProcessOutput(this.stdout, this.stderr)
            proc.waitFor()
            exitCode = proc.exitValue()    
        }

        public String output() {
            String retval
            this.stdout.toString().split("\n").each { line ->
                retval += "aptly: stdout >> ${line}\n"
            }

            this.stderr.toString().split("\n").each { line ->
                retval += "aptly: stderr >> ${line}\n"
            }
            retval
        }

        private String opts() {
            def ret = []
            opts.keySet().each { key ->
                def k = key.replaceAll("_", "-")
                if (opts[key]) {
                    ret << "-${k}=${opts[key]}"
                } 
            }
            ret.join(" ")
        }
    }
}