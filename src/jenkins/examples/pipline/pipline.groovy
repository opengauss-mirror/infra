def giteeComments = "| Check Name | Build Result | Build Details |\n| --- | --- | --- |\n"
def jobSuccess = true
pipeline {
    agent any
    environment {
        GITEE_TOKEN = ""
    }
    stages {
        stage('init'){
            steps {
                script {
                    try {
                        addGiteeMRComment comment: "openGauss pipeline is running. Please wait a moment..."
                    }
                    catch (exc) {
                        jobSuccess = false
                    }
                }
            }   
        }
        stage('build') {
            when {equals expected: true, actual: jobSuccess }
            steps {
              script {
                    def jobsList = ["build-arm", "build-x86"]
                    def parallelJobs2Run =  [:]
                    def parallelJobResults =  [:]
                    jobsList.each { job ->
                        def job_name = job
                        echo "Going to parallel for job ${job_name}"
                        parallelJobs2Run["${job_name}"] = { ->  
                               jobResult =  build job: "${job_name}",
                                parameters: [],
                                propagate: false,
                                wait: true
                                parallelJobResults["${job_name}"] = jobResult
                         }
                    }
                    parallel parallelJobs2Run
                    parallelJobResults.each{ name, jobResult ->
                        if (jobResult.result == "SUCCESS") {
                            resultIcon = ":white_check_mark: "
                            giteeComments += "|  ${name} | ${resultIcon}**${jobResult.result}** | [#${jobResult.number}](${jobResult.absoluteUrl}/console) |\n"
                        }
                        else if (jobResult.result == "ABORTED") {
                            resultIcon = ":heavy_minus_sign: "
                            giteeComments += "|  ${name} | ${resultIcon}**${jobResult.result}** | [#${jobResult.number}](${jobResult.absoluteUrl}/console) |\n"
                            jobSuccess = false
                        }
                        else {
                            resultIcon = ":x: "
                            giteeComments += "|  ${name} | ${resultIcon}**${jobResult.result}** | [#${jobResult.number}](${jobResult.absoluteUrl}/console) |\n"
                            jobSuccess = false
                        }
                    }
                    echo "Build jobSuccess: ${jobSuccess}"
                    if (!jobSuccess) {
                        currentBuild.result = 'FAILURE'
                    }
                }
            }
        }
    }

    post {
        always {
            addGiteeMRComment comment: giteeComments
        }
        success {
            echo '[INFO] openGauss pipeline run successfully.'
        }
        unsuccessful {
            echo '[ERROR] openGauss pipeline run failed.'
        }
    }
}

