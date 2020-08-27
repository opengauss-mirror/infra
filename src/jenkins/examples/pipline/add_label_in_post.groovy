
function delete_labels() {
  echo "Delete labels"
  curl -X DELETE "https://gitee.com/api/v5/repos/opengauss/openGauss-server/pulls/$1/labels/$3" -H "Authorization: Bearer $2" -H 'Content-Type: application/json'
}


delete_labels ${giteePullRequestIid} ${token} ci-pipeline-success
delete_labels ${giteePullRequestIid} ${token} ci-pipeline-failed


curl -X POST "https://gitee.com/api/v5/repos/opengauss/openGauss-server/pulls/${giteePullRequestIid}/labels" \
-H "Authorization: Bearer ${token}" \
-H "Content-Type: application/json" \
-d '["ci-pipeline-running"]'


echo "Post build task success success success"
function delete_labels() {
  echo "Delete labels"
  curl -X DELETE "https://gitee.com/api/v5/repos/opengauss/openGauss-server/pulls/$1/labels/$3" -H "Authorization: Bearer $2" -H 'Content-Type: application/json' > /dev/null
}

delete_labels ${giteePullRequestIid} ${token} ci-pipeline-running

curl -X POST "https://gitee.com/api/v5/repos/opengauss/openGauss-server/pulls/${giteePullRequestIid}/labels" \
-H "Authorization: Bearer ${token}" \
-H "Content-Type: application/json" \
-d '["ci-pipeline-success"]' > /dev/null

