
def addOrRemoveLables(String OWNER, String PROJECT, String PR_NUMBER, String ACCESS_TOKEN, ArrayList ADD_LABELS, ArrayList REMOVE_LABLES) {
  try {
    println "going to update pr $OWNER, $PROJECT, $PR_NUMBER,$ADD_LABELS,$REMOVE_LABLES"
    //删除PR的标签
    REMOVE_LABLES.eachWithIndex { v, index ->
      def requestUrl = "https://gitee.com/api/v5/repos/" + OWNER + "/" + PROJECT + "/pulls/" + PR_NUMBER + "/labels/" + v
	  println requestUrl

	  def auth_token = "Authorization: Bearer " + ACCESS_TOKEN
	  def header = "Content-Type: application/json"

	  final String response = sh(script: "curl -X DELETE $requestUrl -H '$auth_token' -H '$header'", returnStdout: true).trim()
	  echo response
      //unset response or will raise exception
      response = null
    }

    //添加新的标签
    def requestUrl = "https://gitee.com/api/v5/repos/" + OWNER + "/" + PROJECT + "/pulls/" + PR_NUMBER + "/labels"
    def prNameStr = ADD_LABELS.join(',')
    def updatedLabels = """
        ["$prNameStr"]
      """
    println requestUrl
    def auth_token = "Authorization: Bearer " + ACCESS_TOKEN
	def header = "Content-Type: application/json"
	final String response = sh(script: "curl -X POST $requestUrl -H '$auth_token' -H '$header' -d '$updatedLabels'", returnStdout: true).trim()
	echo response
    //unset response or will raise exception
    response = null
  }catch(Exception ex){
    println "failed to update pr $OWNER, $PROJECT, $PR_NUMBER,$ADD_LABELS,$REMOVE_LABLES, $ex"
  }
}


    post {
        always {
            addGiteeMRComment comment: giteeComments
        }
        success {
            echo '[INFO] openGauss pipeline run successfully.'
            script {
                retry(3) {
          	        withCredentials([string(credentialsId: 'gitee_token_id', variable: 'GITEE_TOKEN')]){
                            addOrRemoveLables(env.giteeTargetNamespace, env.giteeTargetRepoName, env.giteePullRequestIid,
                                  "${GITEE_TOKEN}", ["ci-pipeline-success"], ["ci-pipeline-running", "ci-pipeline-failed"])
			        }
                }
	    }
        }
        unsuccessful {
            echo '[ERROR] openGauss pipeline run failed.'
            script {
                retry(3) {
		        withCredentials([string(credentialsId: 'gitee_token_id', variable: 'GITEE_TOKEN')]){
                            addOrRemoveLables(env.giteeTargetNamespace, env.giteeTargetRepoName, env.giteePullRequestIid,
                                  "${GITEE_TOKEN}", ["ci-pipeline-failed"], ["ci-pipeline-running", "ci-pipeline-success"])
			        }
                }
	    }
        }
    }
