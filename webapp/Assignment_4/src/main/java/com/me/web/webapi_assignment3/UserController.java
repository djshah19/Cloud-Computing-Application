package com.me.web.webapi_assignment3;

import com.me.web.dao.UserDao;
import com.me.web.pojo.User;

import com.me.web.service.AmazonSNSHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@RestController
public class UserController {

    @Autowired
    AmazonSNSHelper amazonSNS;

    @RequestMapping(value = "user/save", method = RequestMethod.POST)
    public String saveUser(HttpServletRequest req, UserDao userDao) throws Exception{
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        if(username != null && password != null && username.length() > 0 && password.length() > 0) {
            User user = new User();
            user.setUsername(username);
            String salt = BCrypt.gensalt();
            password = BCrypt.hashpw(password, salt);
            user.setPassword(password);
            int val = userDao.createUser(user);
            if(val==2) {
                return "{message:'User successfully registered'}";
            }
            else if(val == 1){
                return "{message:'Email ID incorrect'}";
            }
        }else{
            return "Username or password cannot be blank";
        }
        return "{message:'User already exist'}";
    }

    @RequestMapping(value = "user/{id}", method = RequestMethod.GET)
    public Object getUser(@PathVariable("id") String id, HttpServletRequest req, UserDao userDao) throws Exception{
        String headers = req.getHeader(HttpHeaders.AUTHORIZATION);
        if(headers != null) {
            String base64Credentials = headers.substring("Basic".length()).trim();
            byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
            String credentials = new String(credDecoded, StandardCharsets.UTF_8);
            final String[] values = credentials.split(":", 2);
            User user = userDao.verifyUser(values[0], values[1]);
            if (user != null && user.getUsername().equalsIgnoreCase("root@root.com")) {
                UUID uuid = UUID.fromString(id);
                User u = userDao.getUser(uuid);
                if (u != null) {
                    return u;
                } else {
                    return "User not found!";
                }
            } else {
                return "Unauthorized";
            }
        }
        else{
            return "Request header: Authorization not set";
        }
    }

    @RequestMapping(value = "user", method = RequestMethod.GET)
    public Object getUser(HttpServletRequest req, UserDao userDao) throws Exception{
        String headers = req.getHeader(HttpHeaders.AUTHORIZATION);
        if(headers != null) {
            String base64Credentials = headers.substring("Basic".length()).trim();
            byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
            String credentials = new String(credDecoded, StandardCharsets.UTF_8);
            final String[] values = credentials.split(":", 2);
            User user = userDao.verifyUser(values[0], values[1]);
            if (user != null && user.getUsername().equalsIgnoreCase("root@root.com")) {
                List<User> userList = userDao.getAllUser();
                if (userList != null) {
                    return userList;
                } else {
                    return null;
                }
            } else {
                return "Unauthorized";
            }
        }
        else{
            return "Request header: Authorization not set";
        }
    }

    @RequestMapping(value = "user/reset", method = RequestMethod.GET)
    public Object resetPassword(HttpServletRequest req, UserDao userDao) throws Exception{
            HashMap<String,Object> map = new HashMap<>();
            String username = req.getParameter("username");
            User user = userDao.getUser(username);
            if (user != null ) {
                amazonSNS.publish(username);
                map.put("Code", 200);
                map.put("Description", "Successfully complete!");
                return map;
            } else {
                return "Unauthorized";
            }
    }
}
