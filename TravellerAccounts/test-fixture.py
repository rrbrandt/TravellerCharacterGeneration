import hashlib,json,secrets,subprocess,sys,uuid
from pathlib import Path
p=Path('/var/tmp/traveller-account-test.json')
def sql(s):
 r=subprocess.run(['mysql','--batch','--skip-column-names','traveller'],input=s,text=True,capture_output=True)
 if r.returncode: raise RuntimeError('Fixture query failed')
 return r.stdout.strip()
if sys.argv[1]=='seed':
 assert not p.exists()
 d=dict(id=str(uuid.uuid4()),token=secrets.token_urlsafe(32),expired=secrets.token_urlsafe(32))
 d['email']='deployment-'+d['id']+'@example.invalid'
 sql("INSERT INTO accounts(id,email,email_key) VALUES('{id}','{email}','{email}');".format(**d))
 for key,minutes in [('token',15),('expired',-15)]:
  h=hashlib.sha256(d[key].encode()).hexdigest()
  sql("INSERT INTO login_tokens(token_hash,account_id,purpose,expires_at) VALUES(UNHEX('%s'),'%s','verify_email',UTC_TIMESTAMP(6)+INTERVAL %d MINUTE)"%(h,d['id'],minutes))
 p.write_text(json.dumps(d));p.chmod(0o600)
 print(json.dumps(d))
elif sys.argv[1]=='check':
 d=json.loads(p.read_text());id=d['id']
 assert sql("SELECT status FROM accounts WHERE id='%s'"%id)=='active'
 assert sql("SELECT COUNT(*) FROM registration_notifications WHERE account_id='%s'"%id)=='1'
 assert sql("SELECT COUNT(*) FROM auth_sessions WHERE account_id='%s' AND revoked_at IS NOT NULL"%id)=='1'
 print('PASS: activated account, exactly one registration notice queued, session revoked on logout')
elif sys.argv[1]=='cleanup':
 d=json.loads(p.read_text())
 sql("DELETE FROM accounts WHERE id='{id}' AND email='{email}'".format(**d));p.unlink()
 print('Removed exact deployment fixture and its token/session/notification records')
