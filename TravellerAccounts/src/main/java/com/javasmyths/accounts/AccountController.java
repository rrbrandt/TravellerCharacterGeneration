package com.javasmyths.accounts;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.scheduling.annotation.Scheduled;
import javax.servlet.http.*;
import javax.mail.*;
import javax.mail.internet.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class AccountController {
 private final JsonNode db;
 private final JsonNode mail;
 private final GmailApiMailer mailer;
 private final SecureRandom random=new SecureRandom();
 private final Map<String,Long> throttle=new ConcurrentHashMap<>();
 public AccountController() throws Exception {
  String dir=System.getenv("CREDENTIALS_DIRECTORY");
  if(dir==null) throw new IllegalStateException("Account service credentials are required");
  ObjectMapper om=new ObjectMapper();
  db=om.readTree(Files.readAllBytes(Paths.get(dir,"database.json")));
  Path mp=Paths.get(dir,"mail.json");
  mail=Files.exists(mp)?om.readTree(Files.readAllBytes(mp)):om.createObjectNode();
  mailer=new GmailApiMailer(mail);
  try(Connection c=connect(); Statement s=c.createStatement()){s.executeQuery("SELECT version FROM schema_migrations WHERE version=1").close();}
 }
 Connection connect() throws SQLException {
  Connection c=DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/traveller?connectionTimeZone=UTC&forceConnectionTimeZoneToSession=true&connectTimeout=5000&socketTimeout=10000",db.get("username").asText(),db.get("password").asText());
  return c;
 }
 private boolean ready(){return mailer.ready();}
 private String token(){byte[] b=new byte[32];random.nextBytes(b);return Base64.getUrlEncoder().withoutPadding().encodeToString(b);}
 private byte[] hash(String s) throws Exception{return MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));}
 private String csrf(HttpServletRequest r){HttpSession s=r.getSession();String v=(String)s.getAttribute("csrf");if(v==null){v=token();s.setAttribute("csrf",v);}return v;}
 private boolean csrfValid(HttpServletRequest r,String value){HttpSession s=r.getSession(false);return s!=null&&value!=null&&value.equals(s.getAttribute("csrf"));}
 private String page(String mode,String message,HttpServletRequest r,HttpServletResponse res,Model m){
  res.setHeader("Cache-Control","no-store");res.setHeader("Referrer-Policy","no-referrer");res.setHeader("X-Robots-Tag","noindex, nofollow");
  m.addAttribute("mode",mode);m.addAttribute("message",message);m.addAttribute("mailReady",ready());m.addAttribute("csrf",csrf(r));return "account";
 }
 private String cookie(HttpServletRequest r){if(r.getCookies()!=null)for(Cookie c:r.getCookies())if(c.getName().equals("TRAVELLER_AUTH"))return c.getValue();return "";}
 private String loggedIn(HttpServletRequest r) throws Exception {
  String raw=cookie(r);if(!raw.matches("[A-Za-z0-9_-]{43}"))return null;
  try(Connection c=connect();PreparedStatement s=c.prepareStatement("SELECT a.email FROM auth_sessions s JOIN accounts a ON a.id=s.account_id WHERE s.session_hash=? AND s.revoked_at IS NULL AND s.expires_at>UTC_TIMESTAMP(6) AND a.status='active'")){
   s.setBytes(1,hash(raw));try(ResultSet rs=s.executeQuery()){return rs.next()?rs.getString(1):null;}
  }
 }
 @GetMapping({"/","/login","/register","/verify"})
 public String show(HttpServletRequest r,HttpServletResponse res,Model m) throws Exception {
  String mode=r.getServletPath().equals("/register")?"register":r.getServletPath().equals("/verify")?"verify":"login";
  String email=loggedIn(r);if(email!=null){m.addAttribute("email",email);mode="signedin";}
  return page(mode,null,r,res,m);
 }
 @PostMapping({"/register","/login"})
 public String requestLink(@RequestParam(defaultValue="") String email,@RequestParam(defaultValue="") String csrf,HttpServletRequest r,HttpServletResponse res,Model m) throws Exception {
  String mode=r.getServletPath().equals("/register")?"register":"login";
  if(!csrfValid(r,csrf)){res.setStatus(403);return page(mode,"This form expired. Please try again.",r,res,m);}
  email=email.trim();
  if(email.length()>254||!email.matches("[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9](?:[A-Za-z0-9.-]*[A-Za-z0-9])?\\.[A-Za-z]{2,}")){res.setStatus(400);return page(mode,"Enter a valid email address.",r,res,m);}
  if(mode.equals("register")&&!"yes".equals(r.getParameter("consent"))){res.setStatus(400);return page(mode,"Please confirm the account email notice.",r,res,m);}
  if(!ready()){res.setStatus(503);return page(mode,"Email sign-in is not enabled yet. No account was created and no email was sent. You can still use both tools from the portal.",r,res,m);}
  String key=email.toLowerCase(Locale.ROOT),rate=r.getRemoteAddr()+":"+key;
  long now=System.currentTimeMillis();
  synchronized(throttle){throttle.entrySet().removeIf(e->e.getValue()<now-60000);if(throttle.size()>10000||throttle.containsKey(rate)){res.setStatus(429);return page(mode,"Please wait a minute before requesting another link.",r,res,m);}throttle.put(rate,now);}
  String id=null,destination=null,status=null,raw=token();
  try(Connection c=connect()){
   if(mode.equals("register"))try(PreparedStatement s=c.prepareStatement("INSERT IGNORE INTO accounts(id,email,email_key) VALUES(?,?,?)")){s.setString(1,UUID.randomUUID().toString());s.setString(2,email);s.setString(3,key);s.executeUpdate();}
   try(PreparedStatement s=c.prepareStatement("SELECT id,email,status FROM accounts WHERE email_key=?")){s.setString(1,key);try(ResultSet rs=s.executeQuery()){if(rs.next()){id=rs.getString(1);destination=rs.getString(2);status=rs.getString(3);}}}
   if(id!=null&&!"disabled".equals(status)){
    try(PreparedStatement s=c.prepareStatement("INSERT INTO login_tokens(token_hash,account_id,purpose,expires_at) VALUES(?,?,?,UTC_TIMESTAMP(6)+INTERVAL 15 MINUTE)")){s.setBytes(1,hash(raw));s.setString(2,id);s.setString(3,"active".equals(status)?"sign_in":"verify_email");s.executeUpdate();}
    try{send(destination,"Your Traveller sign-in link","Open this link to verify your email and sign in to Traveller. It expires in 15 minutes and works once.\n\nhttps://traveller.doxylabs.net/account/verify#token="+raw+"\n\nIf you did not request this, ignore this message.");}
    catch(Exception ex){try(PreparedStatement s=c.prepareStatement("DELETE FROM login_tokens WHERE token_hash=?")){s.setBytes(1,hash(raw));s.executeUpdate();}res.setStatus(503);return page(mode,"Email could not be sent. Please try again later.",r,res,m);}
   }
  }
  return page(mode,"If this address is eligible, a sign-in link is on its way. Check your inbox and spam folder. Links expire in 15 minutes.",r,res,m);
 }
 @PostMapping("/verify")
 public String verify(@RequestParam(defaultValue="") String token,@RequestParam(defaultValue="") String csrf,HttpServletRequest r,HttpServletResponse res,Model m) throws Exception {
  if(!csrfValid(r,csrf)){res.setStatus(403);return page("verify","This form expired. Reopen your email link.",r,res,m);}
  if(!token.matches("[A-Za-z0-9_-]{43}")){res.setStatus(400);return page("login","This link is invalid. Request a new sign-in link.",r,res,m);}
  String account=null,newSession=token();
  try(Connection c=connect()){
   c.setAutoCommit(false);
   try{
    // Lock the account first to serialize concurrent first-verification notifications.
    try(PreparedStatement s=c.prepareStatement("SELECT a.id FROM accounts a JOIN login_tokens t ON t.account_id=a.id WHERE t.token_hash=? AND t.consumed_at IS NULL AND t.expires_at>UTC_TIMESTAMP(6) AND a.status<>'disabled' FOR UPDATE")){s.setBytes(1,hash(token));try(ResultSet rs=s.executeQuery()){if(rs.next())account=rs.getString(1);}}
    if(account==null){c.rollback();res.setStatus(400);return page("login","This link has expired or was already used. Request a new link.",r,res,m);}
    try(PreparedStatement s=c.prepareStatement("UPDATE login_tokens SET consumed_at=UTC_TIMESTAMP(6) WHERE token_hash=? AND consumed_at IS NULL AND expires_at>UTC_TIMESTAMP(6)")){s.setBytes(1,hash(token));if(s.executeUpdate()!=1)throw new SQLException("Token already consumed");}
    try(PreparedStatement s=c.prepareStatement("UPDATE accounts SET status='active',verified_at=UTC_TIMESTAMP(6) WHERE id=? AND status='pending'")){s.setString(1,account);if(s.executeUpdate()==1)try(PreparedStatement n=c.prepareStatement("INSERT IGNORE INTO registration_notifications(account_id,available_at) VALUES(?,UTC_TIMESTAMP(6))")){n.setString(1,account);n.executeUpdate();}}
    try(PreparedStatement s=c.prepareStatement("INSERT INTO auth_sessions(session_hash,account_id,last_seen_at,expires_at) VALUES(?,?,UTC_TIMESTAMP(6),UTC_TIMESTAMP(6)+INTERVAL 1 DAY)")){s.setBytes(1,hash(newSession));s.setString(2,account);s.executeUpdate();}
    c.commit();
   }catch(Exception ex){c.rollback();throw ex;}
  }
  r.changeSessionId();
  res.addHeader("Set-Cookie","TRAVELLER_AUTH="+newSession+"; Path=/; Max-Age=86400; Secure; HttpOnly; SameSite=Lax");
  return "redirect:/login";
 }
 @PostMapping("/logout")
 public String logout(@RequestParam(defaultValue="")String csrf,HttpServletRequest r,HttpServletResponse res,Model m)throws Exception{
  if(!csrfValid(r,csrf)){res.setStatus(403);return page("login","This form expired. Please try again.",r,res,m);}
  try(Connection c=connect();PreparedStatement s=c.prepareStatement("UPDATE auth_sessions SET revoked_at=UTC_TIMESTAMP(6) WHERE session_hash=?")){s.setBytes(1,hash(cookie(r)));s.executeUpdate();}
  r.getSession().invalidate();res.addHeader("Set-Cookie","TRAVELLER_AUTH=; Path=/; Max-Age=0; Secure; HttpOnly; SameSite=Lax");return "redirect:/login";
 }
 private void send(String to,String subject,String body)throws Exception{
  mailer.send(to,subject,body);
 }
 @Scheduled(fixedDelay=60000,initialDelay=60000)
 public void notifications(){
  if(!ready())return;
  try(Connection c=connect()){
   c.setAutoCommit(false);long id=0;String email=null,verified=null;
   try(Statement s=c.createStatement();ResultSet rs=s.executeQuery("SELECT n.id,a.email,a.verified_at FROM registration_notifications n JOIN accounts a ON a.id=n.account_id WHERE a.status='active' AND n.attempts<5 AND ((n.state IN ('pending','failed') AND n.available_at<=UTC_TIMESTAMP(6)) OR (n.state='sending' AND n.lease_until<UTC_TIMESTAMP(6))) ORDER BY n.id LIMIT 1 FOR UPDATE SKIP LOCKED")){if(rs.next()){id=rs.getLong(1);email=rs.getString(2);verified=rs.getString(3);}}
   if(id==0){c.rollback();return;}
   try(PreparedStatement s=c.prepareStatement("UPDATE registration_notifications SET state='sending',attempts=attempts+1,lease_until=UTC_TIMESTAMP(6)+INTERVAL 2 MINUTE WHERE id=?")){s.setLong(1,id);s.executeUpdate();}c.commit();c.setAutoCommit(true);
   try{send(mail.path("notificationTo").asText(),"New Traveller registration","A new Traveller account verified its email.\n\nEmail: "+email+"\nVerified (UTC): "+verified+"\nRegistration notification ID: "+id);
    try(PreparedStatement s=c.prepareStatement("UPDATE registration_notifications SET state='sent',sent_at=UTC_TIMESTAMP(6),lease_until=NULL,last_error_code=NULL WHERE id=?")){s.setLong(1,id);s.executeUpdate();}
   }catch(Exception ex){try(PreparedStatement s=c.prepareStatement("UPDATE registration_notifications SET state='failed',available_at=UTC_TIMESTAMP(6)+INTERVAL 5 MINUTE,lease_until=NULL,last_error_code='MAIL_SEND_FAILED' WHERE id=?")){s.setLong(1,id);s.executeUpdate();}}
  }catch(Exception ex){System.err.println("Registration notification worker could not complete; details withheld.");}
 }
 @GetMapping("/health") @ResponseBody
 public Map<String,Object> health()throws Exception{try(Connection c=connect();Statement s=c.createStatement()){s.executeQuery("SELECT 1").close();}Map<String,Object> m=new LinkedHashMap<>();m.put("ok",true);m.put("emailEnabled",ready());return m;}
}
