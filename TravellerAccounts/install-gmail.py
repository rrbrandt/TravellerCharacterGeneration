"""Root-only installer. Read OAuth credentials from SSH stdin, never arguments/logs.
Checks the authorized mailbox, sends ONE setup test to the owner, then activates.
"""
import base64,json,os,subprocess,sys,time,urllib.parse,urllib.request
from email.message import EmailMessage
from pathlib import Path
OWNER='richraybrandt@gmail.com'
def request(url,data=None,access=None):
 headers={}
 if access: headers['Authorization']='Bearer '+access
 if isinstance(data,dict):
  data=json.dumps(data).encode();headers['Content-Type']='application/json'
 elif isinstance(data,str):
  data=data.encode();headers['Content-Type']='application/x-www-form-urlencoded'
 with urllib.request.urlopen(urllib.request.Request(url,data=data,headers=headers),timeout=25) as r:
  return json.load(r)
def main():
 os.umask(0o077)
 if os.geteuid()!=0: raise RuntimeError('Run through root SSH')
 d=json.loads(sys.stdin.read(32768))
 assert d.get('username')==OWNER and d.get('notificationTo')==OWNER
 assert d.get('provider')=='gmail_api'
 for key in ['clientId','clientSecret','refreshToken']:
  assert isinstance(d.get(key),str) and 0<len(d[key])<8192
 access=request('https://oauth2.googleapis.com/token',urllib.parse.urlencode(dict(grant_type='refresh_token',client_id=d['clientId'],client_secret=d['clientSecret'],refresh_token=d['refreshToken'])))['access_token']
 identity=request('https://openidconnect.googleapis.com/v1/userinfo',access=access)
 assert identity.get('email','').lower()==OWNER and identity.get('email_verified') is True
 msg=EmailMessage();msg['From']=OWNER;msg['To']=OWNER;msg['Subject']='Traveller email setup test'
 msg.set_content('Traveller on DoxyLabs can now send email using the Gmail HTTPS API.\n\nThis is the setup test. Registration verification links and new-registration notifications will use this connection.\n')
 result=request('https://gmail.googleapis.com/gmail/v1/users/me/messages/send',dict(raw=base64.urlsafe_b64encode(msg.as_bytes()).decode().rstrip('=')),access)
 assert result.get('id')
 p=Path('/etc/traveller/mail.json');old=p.read_bytes()
 backup=Path('/etc/traveller/mail-before-api-'+str(int(time.time()))+'.json');backup.write_bytes(old);backup.chmod(0o600)
 d['enabled']=True
 temp=p.with_suffix('.new');temp.write_text(json.dumps(d,indent=2)+'\n');temp.chmod(0o600);temp.replace(p)
 try:
  subprocess.run(['systemctl','restart','traveller-accounts'],check=True,capture_output=True)
  for _ in range(45):
   time.sleep(1)
   try:
    health=request('http://127.0.0.1:8085/account/health')
    if health.get('ok') and health.get('emailEnabled'):
     print('Gmail API setup test accepted; account email delivery enabled. Check the owner inbox.');return
   except Exception: pass
  raise RuntimeError('Account service did not become ready')
 except Exception:
  p.write_bytes(old);p.chmod(0o600)
  subprocess.run(['systemctl','restart','traveller-accounts'],capture_output=True)
  raise
if __name__=='__main__':
 try: main()
 except Exception:
  print('Gmail activation failed. Previous mail configuration retained; inspect API access, consent and Gmail API enablement. No credentials were printed.',file=sys.stderr)
  sys.exit(1)
