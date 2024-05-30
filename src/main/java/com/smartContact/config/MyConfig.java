package com.smartContact.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class MyConfig {
  @Bean
  public UserDetailsService getUserDetailService()
  {
	  return new UserDetailServiceImpl();
  }
  
  @Bean
  public BCryptPasswordEncoder passwordEncoder()
  {
	  return new BCryptPasswordEncoder();
  }
  
  @Bean
  public DaoAuthenticationProvider authenticationProvider()
  {
	  DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
	  daoAuthenticationProvider.setUserDetailsService(this.getUserDetailService());
	  daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
	  
	  return daoAuthenticationProvider;
  }
  
  
  //configure method
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception
  {
	  httpSecurity.authorizeRequests()
	  .requestMatchers("/admin/**")
	  .hasRole("ADMIN")
	  .requestMatchers("/user/**")
	  .hasRole("USER")
	  .requestMatchers("/**")
	  .permitAll()
	  .anyRequest()
	  .authenticated()
	  .and()
	  .formLogin()
	  .loginPage("/signin")
	  .loginProcessingUrl("/dologin")
	  .defaultSuccessUrl("/user/index")
	  .and()
	  .csrf().disable();
	  
	  return httpSecurity.build();
	  
  }
}
