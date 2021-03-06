/*
 * 码云 Open API
 *
 * No description provided (generated by Swagger Codegen https://github.com/swagger-api/swagger-codegen)
 *
 * API version: 5.3.2
 * Generated by: Swagger Codegen (https://github.com/swagger-api/swagger-codegen.git)
 */

package gitee

type NewFileParam struct {
	// 用户授权码
	AccessToken string `json:"access_token,omitempty"`
	// 文件内容, 要用 base64 编码
	Content string `json:"content,omitempty"`
	// 提交信息
	Message string `json:"message,omitempty"`
	// 分支名称。默认为仓库对默认分支
	Branch string `json:"branch,omitempty"`
	// Committer的名字，默认为当前用户的名字
	CommitterName string `json:"committer[name],omitempty"`
	// Committer的邮箱，默认为当前用户的邮箱
	CommitterEmail string `json:"committer[email],omitempty"`
	// Author的名字，默认为当前用户的名字
	AuthorName string `json:"author[name],omitempty"`
	// Author的邮箱，默认为当前用户的邮箱
	AuthorEmail string `json:"author[email],omitempty"`
}
