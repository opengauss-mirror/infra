/*
 * 码云 Open API
 *
 * No description provided (generated by Swagger Codegen https://github.com/swagger-api/swagger-codegen)
 *
 * API version: 5.3.2
 * Generated by: Swagger Codegen (https://github.com/swagger-api/swagger-codegen.git)
 */

package gitee

// 获取一条私信
type UserMessage struct {
	Id int32 `json:"id,omitempty"`
	// 发送者
	Sender    *UserBasic `json:"sender,omitempty"`
	Unread    string     `json:"unread,omitempty"`
	Content   string     `json:"content,omitempty"`
	UpdatedAt string     `json:"updated_at,omitempty"`
	Url       string     `json:"url,omitempty"`
	HtmlUrl   string     `json:"html_url,omitempty"`
}
