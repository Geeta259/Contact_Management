package com.smartContact.controller;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


import com.smartContact.dao.UserRepository;
import com.smartContact.entities.User;
import com.smartContact.service.EmailService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ForgotController {
	
	Random random;
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder bcrypt;

   //email id form open handler
	@RequestMapping("/forgot")
	public String openEmailForm()
	{
		return "forgot_email";
	}
	
	@PostMapping("/send-otp")
	public String sendOTP(@RequestParam("email") String email,HttpSession session)
	{
		random  = new Random();
		System.out.println("Email " + email);
		
		//generating otp of 4 digit
		int otp  = 1000 + random.nextInt(9000);
		System.out.println("OTP " + otp);
	
		String subject = "OTP From Smart Contact Manager";
		String message = ""
				+"<div style='border:1px solid #e2e2e2; padding:20px'>"
				+"<h1>"
				+"OTP  is "
				+"<b>"+otp
				+"</n>"
				+"</h1>"
				+"</div>";
		
	    String to = email;
	    
		boolean flag = this.emailService.sendEmail(subject,message,to);
		
		if(flag)
		{
			//set otp and registered email in  session 
			session.setAttribute("myotp", otp);
			session.setAttribute("email", email);
			return "verify_otp";
		}
		else
		{
			session.setAttribute("message","Please entered correct email id !!");
			return "forgot_email";
		}
		
	}
	
	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp") int otp,HttpSession session){
		int myotp = (int)session.getAttribute("myotp");
		String email = (String)session.getAttribute("email");
		if(myotp==otp)
		{
			//verify registered email is registered or not
			User user = this.userRepository.getUserByName(email);
			System.out.println(user);
			if(user==null)
			{
				//send error message
				session.setAttribute("message","No user exist with this email");
				return "forgot_email";
			}
			else
			{
				//send change password form
				
			}
			//if generated otp and entered otp are matched
			return  "password_change";
		}
		else {
			session.setAttribute("message", "You have entered wrong otp..!!");
			return "verify_otp";
		}
	}
	
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newpassword") String newpassword,HttpSession session)
	{
		String email = (String)session.getAttribute("email");
		User user = this.userRepository.getUserByName(email);
		user.setPassword(this.bcrypt.encode(newpassword));
		this.userRepository.save(user);
		return "redirect:/signin?changepass=done";
	}
}
