/*
 * 码云 Open API
 *
 * No description provided (generated by Swagger Codegen https://github.com/swagger-api/swagger-codegen)
 *
 * API version: 5.3.2
 * Generated by: Swagger Codegen (https://github.com/swagger-api/swagger-codegen.git)
 */

package gitee

// basic information
type BasicInfo struct {
	Label string     `json:"label,omitempty"`
	Ref   string     `json:"ref,omitempty"`
	Sha   string     `json:"sha,omitempty"`
	User  *UserBasic `json:"user,omitempty"`
	Repo  *Project   `json:"repo,omitempty"`
}
