/*
 * 码云 Open API
 *
 * No description provided (generated by Swagger Codegen https://github.com/swagger-api/swagger-codegen)
 *
 * API version: 5.3.2
 * Generated by: Swagger Codegen (https://github.com/swagger-api/swagger-codegen.git)
 */

package gitee

// 更新一个仓库WebHook
type Hook struct {
	Id                  string `json:"id,omitempty"`
	Url                 string `json:"url,omitempty"`
	CreatedAt           string `json:"created_at,omitempty"`
	Password            string `json:"password,omitempty"`
	ProjectId           string `json:"project_id,omitempty"`
	Result              string `json:"result,omitempty"`
	ResultCode          string `json:"result_code,omitempty"`
	PushEvents          string `json:"push_events,omitempty"`
	TagPushEvents       string `json:"tag_push_events,omitempty"`
	IssuesEvents        string `json:"issues_events,omitempty"`
	NoteEvents          string `json:"note_events,omitempty"`
	MergeRequestsEvents string `json:"merge_requests_events,omitempty"`
}