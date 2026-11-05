// Run on Richard's Windows PC with a downloaded Google Desktop OAuth client JSON.
// Owner completes Google's consent personally in the browser. No password requested.
// Refresh credentials go directly to root SSH stdin, never a local token file.
const fs=require('node:fs'),http=require('node:http'),crypto=require('node:crypto');
const {spawn}=require('node:child_process');
const OWNER='richraybrandt@gmail.com';
const SCOPE='openid email https://www.googleapis.com/auth/gmail.send';
async function post(url,data){
 const r=await fetch(url,{method:'POST',redirect:'error',headers:{'Content-Type':'application/x-www-form-urlencoded'},body:new URLSearchParams(data),signal:AbortSignal.timeout(25000)});
 if(!r.ok)throw Error('Google token exchange failed. Check Gmail API and consent configuration.');return r.json();
}
async function authorize(client){
 const verifier=crypto.randomBytes(32).toString('base64url'),state=crypto.randomBytes(32).toString('base64url');
 const challenge=crypto.createHash('sha256').update(verifier).digest('base64url');
 let redirect,finish;
 const received=new Promise((resolve,reject)=>finish={resolve,reject});
 const server=http.createServer((req,res)=>{
  const u=new URL(req.url,redirect);
  res.setHeader('Cache-Control','no-store');res.setHeader('Referrer-Policy','no-referrer');res.setHeader('Content-Type','text/plain; charset=utf-8');
  if(u.pathname!='/oauth/callback'){res.writeHead(404);res.end('Not found');return;}
  if(u.searchParams.get('state')!==state){res.writeHead(400);res.end('State mismatch. Close this page and restart setup.');return;}
  if(u.searchParams.has('error')){res.writeHead(400);res.end('Google authorization was not completed.');finish.reject(Error('Google authorization was declined.'));return;}
  const code=u.searchParams.get('code');if(!code){res.writeHead(400);res.end('Missing authorization code');return;}
  res.writeHead(200);res.end('Authorization received. Return to the Traveller setup terminal. You can close this tab.');finish.resolve(code);
 });
 await new Promise(resolve=>server.listen(0,'127.0.0.1',resolve));
 redirect='http://127.0.0.1:'+server.address().port+'/oauth/callback';
 const url=new URL('https://accounts.google.com/o/oauth2/v2/auth');
 url.search=new URLSearchParams({client_id:client.client_id,redirect_uri:redirect,response_type:'code',scope:SCOPE,access_type:'offline',prompt:'consent',state,code_challenge:challenge,code_challenge_method:'S256',login_hint:OWNER}).toString();
 const timer=setTimeout(()=>finish.reject(Error('Authorization timed out. Run setup again.')),10*60*1000);
 console.log('Opening Google authorization. Sign in as '+OWNER+' and review the send-email permission.');
 console.log('This setup will send one test email to your own inbox before enabling registrations.');
 console.log('If no browser opens, open this URL in your normal browser:\n'+url.toString());
 const browser=spawn('rundll32.exe',['url.dll,FileProtocolHandler',url.toString()],{stdio:'ignore',windowsHide:true});browser.on('error',()=>{});browser.unref();
 try{
  const code=await received;
  const tokens=await post('https://oauth2.googleapis.com/token',{grant_type:'authorization_code',client_id:client.client_id,client_secret:client.client_secret,redirect_uri:redirect,code,code_verifier:verifier});
  if(!tokens.refresh_token)throw Error('Google did not return offline access. Revoke the prior app grant and authorize again.');
  if(!(tokens.scope||'').split(' ').includes('https://www.googleapis.com/auth/gmail.send'))throw Error('Send-email permission was not granted.');
  const r=await fetch('https://openidconnect.googleapis.com/v1/userinfo',{headers:{Authorization:'Bearer '+tokens.access_token},redirect:'error',signal:AbortSignal.timeout(20000)});
  if(!r.ok)throw Error('Could not verify the authorized mailbox.');
  const identity=await r.json();if(identity.email?.toLowerCase()!==OWNER||identity.email_verified!==true)throw Error('Wrong mailbox authorized. Use '+OWNER+'.');
  return {enabled:false,provider:'gmail_api',username:OWNER,notificationTo:OWNER,clientId:client.client_id,clientSecret:client.client_secret,refreshToken:tokens.refresh_token};
 }finally{clearTimeout(timer);server.close();}
}
async function install(config){
 const args=['-i','C:/Users/Richard/.ssh/tomcathedral_20261020','-o','IdentitiesOnly=yes','-o','BatchMode=yes','-o','StrictHostKeyChecking=yes','root@147.182.181.185','python3 /opt/traveller-accounts/install-gmail.py'];
 const p=spawn('ssh.exe',args,{stdio:['pipe','pipe','pipe'],windowsHide:true});
 p.stdout.on('data',b=>process.stdout.write(b));
 // Installer errors are deliberately sanitized; no Google response bodies or tokens.
 p.stderr.on('data',b=>process.stderr.write(b));
 await new Promise((resolve,reject)=>{p.on('error',()=>reject(Error('Could not start secure SSH credential installation.')));p.on('close',code=>code===0?resolve():reject(Error('Secure server activation did not complete.')));p.stdin.end(JSON.stringify(config));});
}
(async()=>{
 const path=process.argv[2];if(!path)throw Error('Usage: node connect-gmail.cjs "C:\\path\\to\\downloaded-oauth-client.json"');
 const client=JSON.parse(fs.readFileSync(path,'utf8')).installed;
 if(!client?.client_id||!client?.client_secret)throw Error('Download a Desktop app OAuth client JSON from Google Cloud.');
 await install(await authorize(client));
})().catch(e=>{console.error(e.message);process.exitCode=1});
