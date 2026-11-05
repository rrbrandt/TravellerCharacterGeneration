package com.javasmyths.accounts;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

/** Send-only Gmail API transport. No SMTP, inbox access, redirects or secret logging. */
public final class GmailApiMailer {
 interface Transport { Result post(String url,String type,String authorization,String body) throws Exception; }
 static final class Result { final int status;final String body;Result(int status,String body){this.status=status;this.body=body;} }
 private final JsonNode config;
 private final Transport transport;
 private final ObjectMapper mapper=new ObjectMapper();
 public GmailApiMailer(JsonNode config){this(config,defaultTransport());}
 GmailApiMailer(JsonNode config,Transport transport){this.config=config;this.transport=transport;}
 private static Transport defaultTransport(){
  HttpClient client=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).followRedirects(HttpClient.Redirect.NEVER).build();
  return (url,type,auth,body)->{
   HttpRequest.Builder builder=HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(20)).header("Content-Type",type).POST(HttpRequest.BodyPublishers.ofString(body));
   if(auth!=null)builder.header("Authorization",auth);
   HttpResponse<String> response=client.send(builder.build(),HttpResponse.BodyHandlers.ofString());
   return new Result(response.statusCode(),response.body());
  };
 }
 public boolean ready(){
  if(!config.path("enabled").asBoolean(false)||!config.path("provider").asText().equals("gmail_api"))return false;
  for(String key:new String[]{"username","notificationTo","clientId","clientSecret","refreshToken"})if(config.path(key).asText().isBlank())return false;
  return config.path("username").asText().equalsIgnoreCase("richraybrandt@gmail.com");
 }
 private String encode(String value){return URLEncoder.encode(value,StandardCharsets.UTF_8);}
 public void send(String to,String subject,String body)throws Exception{
  if(!ready())throw new IllegalStateException("Email is not configured");
  Result token=transport.post("https://oauth2.googleapis.com/token","application/x-www-form-urlencoded",null,
   "grant_type=refresh_token&client_id="+encode(config.path("clientId").asText())+"&client_secret="+encode(config.path("clientSecret").asText())+"&refresh_token="+encode(config.path("refreshToken").asText()));
  if(token.status!=200)throw new IllegalStateException("Gmail authorization failed");
  String access=mapper.readTree(token.body).path("access_token").asText();
  if(access.isBlank())throw new IllegalStateException("Gmail access token missing");
  MimeMessage message=new MimeMessage(Session.getInstance(new Properties()));
  message.setFrom(new InternetAddress(config.path("username").asText(),"Traveller · DoxyLabs"));
  InternetAddress[] recipients=InternetAddress.parse(to,true);
  if(recipients.length!=1)throw new IllegalArgumentException("Exactly one recipient is required");
  recipients[0].validate();message.setRecipients(Message.RecipientType.TO,recipients);
  message.setSubject(subject,"UTF-8");message.setText(body,"UTF-8");message.saveChanges();
  ByteArrayOutputStream raw=new ByteArrayOutputStream();message.writeTo(raw);
  String payload=mapper.writeValueAsString(Collections.singletonMap("raw",Base64.getUrlEncoder().withoutPadding().encodeToString(raw.toByteArray())));
  Result result=transport.post("https://gmail.googleapis.com/gmail/v1/users/me/messages/send","application/json","Bearer "+access,payload);
  if(result.status!=200||mapper.readTree(result.body).path("id").asText().isBlank())throw new IllegalStateException("Gmail message was not accepted");
 }
}
