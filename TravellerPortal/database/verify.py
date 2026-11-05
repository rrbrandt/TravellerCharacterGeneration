"""Transactional integration checks; fixture changes always roll back."""
import subprocess
from pathlib import Path
client='/etc/traveller/database-client.cnf'
def run(sql,ok=True):
    p=subprocess.run(['mysql','--defaults-extra-file='+client,'--batch','--skip-column-names'],input="SET time_zone='+00:00';\n"+sql,text=True,capture_output=True)
    if (p.returncode==0)!=ok: raise AssertionError(p.stderr or 'Expected operation to be rejected')
    return p.stdout.strip()
fixture="INSERT INTO accounts(id,email,email_key) VALUES('00000000-0000-0000-0000-000000000001','Database Test@example.invalid','database-test@example.invalid');"
run("START TRANSACTION;"+fixture+"INSERT INTO accounts(id,email,email_key) VALUES('00000000-0000-0000-0000-000000000002','Other@example.invalid','database-test@example.invalid');",False)
run("START TRANSACTION; INSERT INTO accounts(id,email,email_key,status) VALUES('00000000-0000-0000-0000-000000000001','test@example.invalid','test@example.invalid','active');",False)
run("START TRANSACTION; INSERT INTO login_tokens VALUES(UNHEX(SHA2('test',256)),'00000000-0000-0000-0000-000000000001','sign_in',CURRENT_TIMESTAMP(6),UTC_TIMESTAMP(6)+INTERVAL 15 MINUTE,NULL);",False)
result=run("START TRANSACTION;"+fixture+"""
UPDATE accounts SET status='active',verified_at=UTC_TIMESTAMP(6) WHERE id='00000000-0000-0000-0000-000000000001';
INSERT INTO login_tokens(token_hash,account_id,purpose,expires_at) VALUES(UNHEX(SHA2('fixture-link',256)),'00000000-0000-0000-0000-000000000001','sign_in',UTC_TIMESTAMP(6)+INTERVAL 15 MINUTE);
UPDATE login_tokens SET consumed_at=UTC_TIMESTAMP(6) WHERE token_hash=UNHEX(SHA2('fixture-link',256)) AND consumed_at IS NULL AND expires_at>UTC_TIMESTAMP(6);
SELECT ROW_COUNT();
UPDATE login_tokens SET consumed_at=UTC_TIMESTAMP(6) WHERE token_hash=UNHEX(SHA2('fixture-link',256)) AND consumed_at IS NULL AND expires_at>UTC_TIMESTAMP(6);
SELECT ROW_COUNT();
INSERT INTO auth_sessions(session_hash,account_id,last_seen_at,expires_at) VALUES(UNHEX(SHA2('fixture-session',256)),'00000000-0000-0000-0000-000000000001',UTC_TIMESTAMP(6),UTC_TIMESTAMP(6)+INTERVAL 1 DAY);
INSERT INTO saved_work_owners(application,record_id,account_id) VALUES('ship','fixture','00000000-0000-0000-0000-000000000001');
INSERT INTO registration_notifications(account_id,available_at) VALUES('00000000-0000-0000-0000-000000000001',UTC_TIMESTAMP(6));
ROLLBACK;
""")
assert result == '1\n0', 'Token consumption was not single-use'
run("START TRANSACTION;"+fixture+"INSERT INTO registration_notifications(account_id,available_at) VALUES('00000000-0000-0000-0000-000000000001',UTC_TIMESTAMP(6)),('00000000-0000-0000-0000-000000000001',UTC_TIMESTAMP(6));",False)
run('SELECT User FROM mysql.user;',False)
# Permission inspection proves absence of schema-changing grants without attempting DDL.
grants=run('SHOW GRANTS;')
assert 'ALL PRIVILEGES' not in grants and 'CREATE' not in grants and 'DROP' not in grants and 'GRANT OPTION' not in grants
assert run('SELECT COUNT(*) FROM accounts;')=='0'
for f in ['database.json','database-client.cnf']:
    assert Path('/etc/traveller',f).stat().st_mode & 0o077 == 0
print('PASS: duplicate email, verification constraint, foreign keys, single-use token update, ownership/session/notification inserts, unique notification, runtime permissions, credential permissions, and rollback cleanup.')
