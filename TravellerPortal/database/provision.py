"""Run on DoxyLabs as root. Refuses an existing database or application account.
Credentials remain on the server, root-readable only. No app wiring is enabled.
"""
import json, os, subprocess
from pathlib import Path
os.umask(0o077)
def mysql(sql, defaults=None):
    args=['mysql']
    if defaults: args.append('--defaults-extra-file='+str(defaults))
    args += ['--batch','--raw','--skip-column-names']
    p=subprocess.run(args,input=sql,text=True,capture_output=True)
    if p.returncode: raise RuntimeError('MySQL operation failed: '+p.stderr)
    return p.stdout.strip()
assert not mysql("SELECT SCHEMA_NAME FROM information_schema.SCHEMATA WHERE SCHEMA_NAME='traveller'")
assert mysql("SELECT COUNT(*) FROM mysql.user WHERE User='traveller_app'")=='0'
private=Path('/etc/traveller')
private.mkdir(mode=0o700,exist_ok=True)
config=private/'database.json'
client=private/'database-client.cnf'
assert not config.exists() and not client.exists()
mysql(Path('/var/tmp/V001__accounts.sql').read_text())
# MySQL generates the credential and returns it only to this process.
row=mysql("CREATE USER 'traveller_app'@'localhost' IDENTIFIED BY RANDOM PASSWORD WITH MAX_USER_CONNECTIONS 10;")
password=row.split('\t')[2]
for table in ['accounts','login_tokens','auth_sessions','saved_work_owners','registration_notifications']:
    mysql(f'GRANT SELECT, INSERT, UPDATE, DELETE ON traveller.{table} TO traveller_app@localhost')
mysql('GRANT SELECT ON traveller.schema_migrations TO traveller_app@localhost')
config.write_text(json.dumps(dict(host='127.0.0.1',port=3306,database='traveller',username='traveller_app',password=password),indent=2)+'\n')
escaped=password.replace('\\','\\\\').replace('"','\\"')
client.write_text('[client]\nuser=traveller_app\npassword="'+escaped+'"\nhost=127.0.0.1\nport=3306\ndatabase=traveller\n')
config.chmod(0o600); client.chmod(0o600)
print('Created traveller database and localhost-only runtime account; credentials saved root-only under /etc/traveller.')
print(mysql('SELECT DATABASE(), CURRENT_USER(); SHOW TABLES;',client))
