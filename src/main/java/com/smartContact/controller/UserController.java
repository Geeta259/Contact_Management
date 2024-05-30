package com.smartContact.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smartContact.dao.ContactRepository;
import com.smartContact.dao.UserRepository;
import com.smartContact.entities.Contact;
import com.smartContact.entities.User;
import com.smartContact.helper.Message;

import jakarta.servlet.http.HttpSession;

import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;




@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	//this method will common for all  handlers
	@ModelAttribute
	public void addCommonData(Model model,Principal principal)
	{
		//principal class used to get  current active user name
				String userName = principal.getName();
				System.out.println("USERNAME" + userName);
				
				//get login user details from username
				User user = userRepository.getUserByName(userName);
				System.out.println("USER " + user);
				
				//add this user into model so that can use in template
				model.addAttribute("user",user);
			
	}
	
	@RequestMapping("/index")
  public String dashboard(Model model,Principal principal)
  {
		model.addAttribute("title","User Dashboard");
	 return "normal/user_dashboard";
  }
	
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model)
	{
		model.addAttribute("title","Add Contact");
		model.addAttribute("contact",new Contact());
		return "normal/add_contact_form";
	}
	
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file,Principal principal,HttpSession session)
	{
		try
		{
			//save contact details into database
			String name = principal.getName();
			User user = this.userRepository.getUserByName(name);
			
			//set current active user in contact table
			contact.setUser(user);
			//set updated contacts add into user
			
			//processing and uploading file..
			if(file.isEmpty())
			{
				//if the file is empty then try message
				System.out.println("file is empty");
				//default image set
				contact.setImage("banner.jpg");
			}
			else
			{
				//file add into folder and update name to contact
				//get file name from form and set into contact class
				contact.setImage(file.getOriginalFilename());
				
				//get location where you have to save the file.
				//classpathresources first automatically get path upto static
				File saveFile = new ClassPathResource("static/images").getFile();
				
				//get absolute path of above file add separator and add file name
				Path path =  Paths.get(saveFile.getAbsolutePath()+ File.separator+ file.getOriginalFilename());
				
				//copy that file get input stream byte data copy from path 
				Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
					
			    System.out.println("file uploaded");
			}
			user.getContacts().add(contact);
			
			
			//file uploading
			
			this.userRepository.save(user);
			
		   System.out.println("DATA" + contact);
			System.out.println("Added to database");
			
			//message success sent
			
			session.setAttribute("message", new Message("Your contact is added! Add More..","success"));
			
		}catch(Exception e)
		{
			System.out.println("ERROR" + e.getMessage());
			//error message sent
			session.setAttribute("message", new Message("Something went wrong! Try Again...","danger"));
		}
		return "normal/add_contact_form";
	}
	
	//show contact handler
	//per page 5
	//current page = 0[page]
	@GetMapping("/show-contact/{page}")
	public String showContacts(@PathVariable("page") Integer page,Model m,Principal principal,HttpSession session)
	{
		m.addAttribute("title","Show User Contacts");
		
		//contact list send
		//get current active user name
		String userName = principal.getName();
		//get user details from user name
		User user = this.userRepository.getUserByName(userName);
		
		//create an object of pageable with current page no and no of data per page
		Pageable  pageable =  PageRequest.of(page, 5);
		
		//get contacts details by user id
		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(),pageable);
		
		//add all contacts into model
		m.addAttribute("contacts",contacts);
		m.addAttribute("currentPage",page);
		m.addAttribute("totalPages",contacts.getTotalPages());
		
		//return this view
		return "normal/show_contacts";
	}
	
	
	//showing particular contact details
	@RequestMapping("/{cid}/contact")
	public String showContactDetail(@PathVariable("cid")Integer cid,Model m,Principal principal)
	{
		try
		{
			Optional<Contact> contactOptional = this.contactRepository.findById(cid);
			
			Contact  contact = contactOptional.get();
			
			//get current login user name
			String userName = principal.getName();
		
			//from username get user
			User user = this.userRepository.getUserByName(userName);
			
			//if login user equals contact user id 	then add contact into model
			if(user.getId()== contact.getUser().getId())
			m.addAttribute("contact",contact);
			
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
		return "normal/contact_detail";
	}
	
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid")Integer cid,Principal principal,HttpSession session)
	{
		try
		{
			//get contact from cid which get from url
			
			Contact contact = this.contactRepository.findById(cid).get();
			
			//check valid contact or not
			
			//get current login user name
			String userName = principal.getName();

			//from username get user details
			User user = this.userRepository.getUserByName(userName);
		
			//if login user equals contact user id 	then contact is valid
			if(user.getId()== contact.getUser().getId())
			{
				//get contacts of active and remove current selected contact
				user.getContacts().remove(contact);
				//then save user after remove its contact
				this.userRepository.save(user);
				
				//set message into session through Message class object
				session.setAttribute("message", new Message("contact deleted successfully","success"));

			}
		  }catch(Exception e)
		{
			//if any exception occur
			System.out.println(e);
			//set error message on session
			session.setAttribute("message", new Message("something went wrong..","danger"));
			
		}
		//redirect url to show_contact template when this url fired.
		
		return "redirect:/user/show-contact/0";
	}
	
	//open update form handler
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid,Model m)
	{
		m.addAttribute("title","Contact Update");
		Contact contact = this.contactRepository.findById(cid).get();
		m.addAttribute("contact",contact);
		return "normal/update_form";
	}
	
	//update contact action handler
	@PostMapping("/process-update")
	public String updateHandler(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file ,Model m,HttpSession session,Principal principal)
	{
		try {
			
			//old contact details through id
			Contact oldContact = this.contactRepository.findById(contact.getCid()).get();
			if(!file.isEmpty())
			{
				//if new file is not empty
				
				//delete old photo
				//get path of file
				File deleteFile = new ClassPathResource("static/images").getFile();
				//get old image of contact 
				File file1 = new File(deleteFile,oldContact.getImage());
				//remove that file
				file1.delete();
				
				//update new photo
				//get location where you have to save the file.
				//classpathresources first automatically get path upto static
				
				File saveFile = new ClassPathResource("static/images").getFile();
				
				//get absolute path of above file add separator and add file name
				Path path =  Paths.get(saveFile.getAbsolutePath()+ File.separator+ file.getOriginalFilename());
		

				//copy that file get input stream byte data copy from path 
				Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
			
				//set new image into contact
				contact.setImage(file.getOriginalFilename());
				
			}
			else
			{
				//set old image 
				contact.setImage(oldContact.getImage());
			}
			
			//get current user
			User user = this.userRepository.getUserByName(principal.getName());
			//set active user for this contact 
			contact.setUser(user);
			//update contact
			this.contactRepository.save(contact);
			
			//set message on session
			session.setAttribute("message", new Message("Your contact is updated","success"));
		}catch(Exception e)
		{
			//set error message on session
			session.setAttribute("message", new Message("Something went wrong..","danger"));

			e.printStackTrace();
		}
		//reidrect into contact details  page
		return "redirect:/user/"+contact.getCid()+"/contact";
	}

	//show profile of active user
	@GetMapping("/profile")
	public String yourProfile(Model  model)
	{
		model.addAttribute("title","Profile  Page");
		return "normal/Profile";
	}
	
	//update profile of active user
			@PostMapping("/update-profile")
			public String updateProfile(Model  model)
			{
				model.addAttribute("title","Update Admin Profile");
				System.out.println("run this");
				return "normal/update_profile";
			}
			
			@PostMapping("/process-update-profile")
			public String processUpdateProfile(@ModelAttribute User user,@RequestParam("imageUrl") MultipartFile file ,Model m,HttpSession session,Principal principal)
			{
				try {
					
					//old contact details through id
					User oldUser  = this.userRepository.findById(user.getId()).get();
					
					if(!file.isEmpty())
					{
						//if new file is not empty
						
						//delete old photo
						//get path of file
						/*
						File deleteFile = new ClassPathResource("static/image").getFile();
						//get old image of contact 
						File file1 = new File(deleteFile,oldUser.getImage());
						//remove that file
						file1.delete();
						*/
						
						//update new photo
						//get location where you have to save the file.
						//classpathresources first automatically get path upto static
						
						File saveFile = new ClassPathResource("static/images").getFile();
						
						//get absolute path of above file add separator and add file name
						Path path =  Paths.get(saveFile.getAbsolutePath()+ File.separator+ file.getOriginalFilename());
				

						//copy that file get input stream byte data copy from path 
						Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
					
						//set new image into contact
						user.setImage(file.getOriginalFilename());
						
					}
					else
					{
						//set old image 
						user.setImage(oldUser.getImage());
					}
					
					
					//update contact
					this.userRepository.save(user);
					
					//set message on session
					session.setAttribute("message", new Message("Your Profile is updated","success"));
				}catch(Exception e)
				{
					//set error message on session
					session.setAttribute("message", new Message("Something went wrong..","danger"));

					e.printStackTrace();
				}
				
				return "normal/Profile";
			}

	//open setting handler
	@GetMapping("/settings")
	public String openSetting()
	{
		return "normal/settings";
	}
	
	//change password handler
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword, @RequestParam("newPassword") String newPassword,Principal principal,HttpSession session)
	{
		//System.out.println("OLD Password " + oldPassword);
		//System.out.println("New Password " + newPassword);
		String userName = principal.getName();
		User currentUser = this.userRepository.getUserByName(userName);
		
		if(this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword()))
		{
			//if old password given by  user and old password in database matches
			//change password
			//set new password in coded form
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			
			//save into database
			this.userRepository.save(currentUser);
			
			session.setAttribute("message", new Message("Your password is successfully changed..","success"));
			return "redirect:/user/index";
		}
		else
		{
			//error
			session.setAttribute("message", new Message("Please enter correct old password","danger"));
			return "redirect:/user/settings";
		}
		
	}
}
