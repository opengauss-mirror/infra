import os
import sys
from mailmanclient.client import Client

# This file used to create the default domain, list and welcome templates for mail list
# the configuration are listed below:
MAILMAN_CORE_ENDPOINT = os.environ.get(
    "MAILMAN_CORE_ENDPOINT",
    'http://mailman-core-0.mail-suit-service.default.svc.cluster.local:8001/3.1')

MAILMAN_CORE_USER = os.environ.get("MAILMAN_CORE_USER", "restadmin")

MAILMAN_CORE_PASSWORD = os.environ.get("MAILMAN_CORE_PASSWORD", "restpass")

# configure used for http server for mailman core service
TEMPLATE_FOLDER_PATH = os.environ.get("TEMPLATE_FOLDER_PATH", "templates")

TEMPALTE_URI_PREFIX = 'https://gitee.com/opengauss/infra/raw/master/src/mail/templates'

def main():
    # pre-check before handling mailman core service
    if not os.path.exists(TEMPLATE_FOLDER_PATH):
        print("The template file folder 'TEMPLATE_FOLDER_PATH' must exits on local.")
        sys.exit(1)
    if not MAILMAN_CORE_PASSWORD:
        print("MAILMAN_CORE_PASSWORD required to login.")
        sys.exit(1)
    # connect mailman client
    client = Client(MAILMAN_CORE_ENDPOINT,
                    MAILMAN_CORE_USER,
                    MAILMAN_CORE_PASSWORD)
    domains = client.domains
    for domain in domains:
        # set domain templates
        common_path = os.path.join(TEMPLATE_FOLDER_PATH, domain.mail_host, 'common')
        common_templates = list(filter(lambda x: x.endswith('.txt'), os.listdir(common_path)))
        if common_templates:
            for template_file in common_templates:
                if os.path.splitext(template_file)[-1] != '.txt':
                    continue
                template_name = os.path.splitext(template_file)[0].replace('-', ':')
                uri = os.path.join(TEMPALTE_URI_PREFIX, domain.mail_host, 'common', template_file)
                try:
                    domain.set_template(template_name, uri)
                    print('set common template \r\n'
                          'template name: {} \r\n'
                          'template file: {} \r\n'
                          'uri: {} \r\n'.format(template_name, os.path.abspath(template_file), uri))
                except Exception as e:
                    print(e)
                    sys.exit(1)
        # set lists templates
        existing_lists = domain.lists
        list_dirs = os.listdir(os.path.join(TEMPLATE_FOLDER_PATH, domain.mail_host))
        for list_dir in list_dirs:
            if list_dir == 'common':
                continue
            if list_dir not in [x.list_name for x in existing_lists]:
                domain.create_list(list_dir)
                print('create list \r\n'
                      'domain: {} \r\n'
                      'list: {} \r\n'.format(domain.mail_host, list_dir))

        for maillist in domain.lists:
            try:
                list_text_dirs = os.listdir(os.path.join(TEMPLATE_FOLDER_PATH, domain.mail_host, maillist.list_name))
            except FileNotFoundError:
                continue
            list_text_dirs = list(filter(lambda x: x.endswith('.txt'), list_text_dirs))
            for list_template_file in list_text_dirs:
                if os.path.splitext(list_template_file)[-1] != '.txt':
                    continue
                template_name = os.path.splitext(list_template_file)[0].replace('-', ':')
                uri = os.path.join(TEMPALTE_URI_PREFIX, domain.mail_host, maillist.list_name, list_template_file)
                try:
                    maillist.set_template(template_name, uri)
                    print('set list template \r\n'
                          'list: {} \r\n'
                          'template name: {} \r\n'
                          'template file: {} \r\n'
                          'uri: {} \r\n'.format(maillist, template_name, os.path.abspath(list_template_file), uri))
                except Exception as e:
                    print(e)
                    sys.exit(1)
            templates = maillist.templates
            for template in templates:
                if (template.name.replace(':', '-') + '.txt') not in list_text_dirs:
                    maillist.set_template(template.name, '')
                    print('remove list template \r\n'
                          'list: {} \r\n'
                          'template name: {} \r\n'.format(maillist.list_name, template.name))


if __name__ == "__main__":
    main()
