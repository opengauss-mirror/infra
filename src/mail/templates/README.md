# README
**NOTE**: Please read the guidance below before you starting changing any content inside of this folder.

## Introduce
This files inside of this folder are used for replacing the default mailman core email templates when the mailman suite
services starts up, therefore these files are strictly arranged by the design of mailman templates mechanism.

There are many different template substitutions provided by mailman core service, please refer [here](https://mailman.readthedocs.io/en/latest/src/mailman/rest/docs/templates.html#templated-texts)
to have a better understanding on this. In short, mailman templates are classified with different keys, here below are some of
these template keys:
- `domain:admin:notice:new-list`: Sent to the administrators of any newly created mailing list.
- `list:user:action:subscribe`: The message sent to subscribers when a subscription confirmation is required.
- `list:user:notice:welcome`: The notice sent to a member when they are subscribed to the mailing list.

## Templated texts
All the texts that Mailman uses to create or decorate messages can be associated with a URL. Mailman looks up templates by name and downloads it via that URL. The retrieved text supports placeholders which are filled in by Mailman. There are a common set of placeholders most templates support:

- `listname` - fully qualified list name (e.g. `ant@example.com`)
- `list_id` - the List-ID header (e.g. `ant.example.com`)
- `display_name` - the display name of the mailing list (e.g. `Ant`)
- `short_listname` - the local part of the list name (e.g. `ant`)
- `domain` - the domain name part of the list name (e.g. `example.com`)
- `descriptio`n` - the mailing list’s short description text
- `info` - the mailing list’s longer descriptive text
- `request_email` - the email address for the -request alias
- `owner_email` - the email address for the -owner alias
- `site_email` - the email address to reach the owners of the site
- `language` - the two letter language code for the list’s preferred language (e.g. `en`, `it`, `fr`)

Add `$` before the texts to call the variables in the template file.

## Customize templates for a mailing list

The templates support English and Chinese and so on, so there is no need to configure language. All mailing lists use common templates, which put in common directory under the domain directory by default. If you need to customize the templates for mailing lists you subscribe, just follow below.
1. Fork the repository at https://github.com/opensourceways/app-mailman.
2. If you want to customize templates for a mailing list, first find the domain of the mailing list under the templates directory, then find the mailing list under the domain directory that you want to customize.
3. Put the customized template file under the mailing list. You can write the file according to the common files. But the filename should follow the standard so that can resolve to a standard template name. e.g. if you want to customize the `list:user:notice:welcome` template, the file name must be `list-user-notice-welcome.txt`. See more about the template name, you can visit https://docs.mailman3.org/projects/mailman/en/latest/src/mailman/rest/docs/templates.html.
4. Create a pull request.Once the pull request be merged, the customised template will cover the common template. 
5. If you need to change the content of the customized template, just follow steps 1~4. 
6. If you do not need to use customize templates anymore, just remove the template under a mailing list directory and submit  a pull request.

## Folder Structure
To support templates configure for multiple communities, the structure of folder `templates` are arranged below:

```$xslt
mail
├───────templates
│       ├───────domain1
│       │       ├───────common  
│       │       │       ├───────list-admin-action-post.txt
│       │       │       ├───────list-user-action-subscribe.txt
│       │       │       └───────list-user-notice-welcome.txt
│       │       ├───────list1
│       │       └───────list2
│       │               └───────list-user-notice-welcome.txt  
│       └───────domain2 

```
Once the content in templates folder have been updated, we can update the mailman templates through recreating the `core-utils` pods in cluster.
