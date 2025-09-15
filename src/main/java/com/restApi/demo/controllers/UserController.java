package com.restApi.demo.controllers;

import com.restApi.demo.domain.DomainManager;
import com.restApi.demo.domain.User;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@CrossOrigin(
  origins = { "http://localhost:5173", "http://127.0.0.1:5173" },
  methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS },
  allowedHeaders = "*"
)
public class UserController {

  @Data
  public static class CreateOrUpdateUser {
    private String username;
    private String email;
  }

  private final DomainManager domainManager;

  public UserController(DomainManager domainManager){
    this.domainManager = domainManager;
  }

  @PostMapping
  public User create(@RequestBody CreateOrUpdateUser request) {
    return domainManager.createUser(request.getUsername(), request.getEmail());
  }

  @GetMapping
  public List<User> list(){
    return domainManager.listUsers();
  }

  @GetMapping("{id}")
  public User get(@PathVariable UUID id){
    return domainManager.getUser(id).get();
  }

  @PutMapping("{id}")
  public User update(@PathVariable UUID id, @RequestBody CreateOrUpdateUser request) {
    return domainManager.updateUser(id, request.getUsername(), request.getEmail());
  }

  @DeleteMapping("{id}")
  public void delete(@PathVariable UUID id){ 
    domainManager.deleteUser(id);
  }
}