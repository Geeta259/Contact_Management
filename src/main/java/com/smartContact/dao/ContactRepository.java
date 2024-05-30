package com.smartContact.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smartContact.entities.Contact;
import com.smartContact.entities.User;

public interface ContactRepository extends JpaRepository<Contact,Integer>{
   
	//get all contacts of current user through jpa query
	@Query("from Contact as c where c.user.id =:userId")
	public Page<Contact> findContactsByUser(@Param("userId") int userId,Pageable pageable);
	
	/*
	 * A page is a sublist of a list of object.
	 * pageable object has two information currentpage and no of contact per page
	 * */
	
	//get all contact which contained given name and user
	//jpa query used here
	public List<Contact> findByNameContainingAndUser(String name,User user);
	
}
