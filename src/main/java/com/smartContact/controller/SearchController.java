package com.smartContact.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.smartContact.dao.ContactRepository;
import com.smartContact.dao.UserRepository;
import com.smartContact.entities.Contact;
import com.smartContact.entities.User;

//here restcontroller used because it does not simply return view
//it return message or object response body
@RestController
public class SearchController {
	
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ContactRepository contactRepository;
	
  //search handler
	@GetMapping("/search/{query}")
	public ResponseEntity<?> search(@PathVariable("query") String query,Principal  principal)
	{
		//get active user name
		User user = this.userRepository.getUserByName(principal.getName());
		//call query to  get all name which contained given query and  user
		List<Contact> contacts = this.contactRepository.findByNameContainingAndUser(query, user);
		//return all contact list as a json object
		return ResponseEntity.ok(contacts);
	}
}
