package com.smartContact.service;



import java.util.Properties;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;


@Service
public class EmailService {
	public boolean sendEmail(String subject,String message,String to)
	{
		boolean f = false;
		String from = "yourmail@gmail.com";
		
		//variable for gmail host
		String host="smtp.gmail.com";
		
		//get the system properties
		Properties properties = System.getProperties();
		
		
		//System.out.println("PROPERTIES " + properties);
		
		//setting important information to properties object
		
		//host  set
		properties.put("mail.smtp.host", "smtp.gmail.com");
		properties.put("mail.smtp.socketFactory.port", "465");
		properties.put("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory");
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.port", "465");
		 properties.put("mail.smtp.ssl.trust", "*");
		
		//step 1: to get the session object
		Session session = Session.getInstance(properties,new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication("youremail@gmail.com","yourpassword");
			}
		});
		
		session.setDebug(true);
		
		//step 2 : compose message
		MimeMessage msg = new MimeMessage(session);
		try
		{
			//from email
			msg.setFrom(from);
			
			//adding recipient to message
			msg.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			
			//adding subject to message
			msg.setSubject(subject);
			
			//adding message  text to message
			msg.setContent(message,"text/html");
			
			//send
			
			//step3 : send message using transport class
			
			Transport.send(msg);
			
			System.out.println("Sent success ...");;
			
			f=true;
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return f;
		
	}
}
