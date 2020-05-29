/*
 * 码云 Open API
 *
 * No description provided (generated by Swagger Codegen https://github.com/swagger-api/swagger-codegen)
 *
 * API version: 5.3.2
 * Generated by: Swagger Codegen (https://github.com/swagger-api/swagger-codegen.git)
 */

package gitee

type ContentBasic struct {
	Name        string `json:"name,omitempty"`
	Path        string `json:"path,omitempty"`
	Size        string `json:"size,omitempty"`
	Sha         string `json:"sha,omitempty"`
	Type_       string `json:"type,omitempty"`
	Url         string `json:"url,omitempty"`
	HtmlUrl     string `json:"html_url,omitempty"`
	DownloadUrl string `json:"download_url,omitempty"`
	Links       string `json:"_links,omitempty"`
}
