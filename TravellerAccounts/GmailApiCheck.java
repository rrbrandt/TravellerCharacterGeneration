package com.javasmyths.accounts;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.*;
public class GmailApiCheck {
 static void check(boolean condition,String reason){if(!condition)throw new AssertionError(reason);}
 static ObjectNode config(){ObjectNode c=new ObjectMapper().createObjectNode();c.put("enabled",true).put("provider","gmail_api").put("username","richraybrandt@gmail.com").put("notificationTo","richraybrandt@gmail.com").put("clientId","test-client").put("clientSecret","secret&value").put("refreshToken","test-refresh");return c;}
 public static void main(String[] args)throws Exception{
  ObjectMapper mapper=new ObjectMapper();ObjectNode c=config();List<String> calls=new ArrayList<>();
  GmailApiMailer m=new GmailApiMailer(c,(url,type,auth,body)->{
   calls.add(url);check(url.startsWith("https://"),"HTTPS only");
   if(url.equals("https://oauth2.googleapis.com/token")){
    check(auth==null&&type.equals("application/x-www-form-urlencoded"),"Token request headers");check(body.contains("client_secret=secret%26value"),"Form encoding");return new GmailApiMailer.Result(200,"{\"access_token\":\"test-access\"}");
   }
   check(url.equals("https://gmail.googleapis.com/gmail/v1/users/me/messages/send"),"Send-only endpoint");check(auth.equals("Bearer test-access"),"Bearer header");
   String raw=mapper.readTree(body).get("raw").asText();check(!raw.contains("="),"Unpadded URL-safe base64");
   MimeMessage msg=new MimeMessage(Session.getInstance(new Properties()),new ByteArrayInputStream(Base64.getUrlDecoder().decode(raw)));
   check(msg.getSubject().equals("Traveller test · UTF-8"),"Unicode subject");check(((InternetAddress)msg.getFrom()[0]).getAddress().equals("richraybrandt@gmail.com"),"Sender");check(msg.getAllRecipients().length==1,"Single recipient");check(msg.getContent().toString().contains("test body"),"Message body");return new GmailApiMailer.Result(200,"{\"id\":\"message-test\"}");
  });
  check(m.ready(),"Configured mail ready");m.send("test@example.invalid","Traveller test · UTF-8","test body");check(calls.size()==2,"Token refresh and message send");
  for(String field:new String[]{"clientId","clientSecret","refreshToken","username","notificationTo"}){ObjectNode missing=config();missing.remove(field);check(!new GmailApiMailer(missing).ready(),"Missing "+field+" fails closed");}
  c=config();c.put("enabled",false);check(!new GmailApiMailer(c).ready(),"Disabled fails closed");
  c=config();c.put("provider","smtp");check(!new GmailApiMailer(c).ready(),"SMTP configuration not activated");
  c=config();c.put("username","wrong@example.invalid");check(!new GmailApiMailer(c).ready(),"Only approved sender");
  for(int stage:new int[]{0,1}){
   final int failingStage=stage;final int[] count={0};
   GmailApiMailer failure=new GmailApiMailer(config(),(url,type,auth,body)->{if(count[0]++==failingStage)return new GmailApiMailer.Result(403,"PRIVATE_TOKEN_RESPONSE");return new GmailApiMailer.Result(200,"{\"access_token\":\"test-access\"}");});
   try{failure.send("test@example.invalid","test","body");throw new AssertionError("Failure accepted");}catch(IllegalStateException e){check(!e.getMessage().contains("PRIVATE"),"Sanitized failure");check(count[0]==stage+1,"No send after refresh rejection");}
  }
  System.out.println("PASS: Gmail HTTPS refresh/send, MIME encoding, approved sender, missing credential checks, disabled state, and sanitized API failures. No network requests or emails sent.");
 }
}
