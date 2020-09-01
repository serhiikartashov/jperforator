package com.example.consumer.controller;

import com.example.consumer.entity.User;
import com.example.consumer.entity.UserCreateResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("users")
public class SyncAPI {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostMapping
    public UserCreateResponse create(@RequestBody User request){
        String id = UUID.randomUUID().toString();
        jdbcTemplate.update("INSERT INTO users (id, name, email) VALUES (?, ?, ?)",
                id, request.getName(), request.getEmail());
        return new UserCreateResponse(id);
    }

    @GetMapping("/{id}")
    public User get(@PathVariable String id){
        return jdbcTemplate.queryForObject("select id, name, email from users where id = ?", new Object[]{id},
                (rs, rowNum) -> new User(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("email")));
    }
}
