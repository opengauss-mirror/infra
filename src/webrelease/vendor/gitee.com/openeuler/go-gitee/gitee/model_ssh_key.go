/*
 * 码云 Open API
 *
 * No description provided (generated by Swagger Codegen https://github.com/swagger-api/swagger-codegen)
 *
 * API version: 5.3.2
 * Generated by: Swagger Codegen (https://github.com/swagger-api/swagger-codegen.git)
 */

package gitee

// 获取一个公钥
type SshKey struct {
	Id        string `json:"id,omitempty"`
	Key       string `json:"key,omitempty"`
	Url       string `json:"url,omitempty"`
	Title     string `json:"title,omitempty"`
	CreatedAt string `json:"created_at,omitempty"`
}
