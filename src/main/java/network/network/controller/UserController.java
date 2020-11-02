package network.network.controller;

import network.network.model.Role;
import network.network.model.UserAccount;
import network.network.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class UserController {
    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/add")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public String formAddUser() {
        return "addUser";
    }

    @PostMapping("/add")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public String addUser(UserAccount user, Model model,
                          @RequestParam Map<String, String> form
                          ) {

        UserAccount checkUser = userAccountRepository.findByUsername(user.getUsername());
        if (checkUser != null) {
            model.addAttribute("message", "Пользователь существует!");
            return "addUser";
        }

        Set<String> roles = Arrays.stream(Role.values())
                .map(Role::name)
                .collect(Collectors.toSet());

        for (String key : form.keySet()) {
            System.out.println(key);
            if (roles.contains(key)) {
                user.getRoles().add(Role.valueOf(key));
            }
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setDate(LocalDate.now());
        userAccountRepository.save(user);

        model.addAttribute("success", "Пользователь успешно добавлен!");
        return "addUser";
    }

    @GetMapping("/user")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
    public String userList(@RequestParam(required = false, defaultValue = "") String username,
                           @RequestParam(required = false, defaultValue = "") Role role,
                           Model model,
                           @RequestParam(value = "page", required = false, defaultValue = "0") Integer page) {
        Page<UserAccount> user = userAccountRepository.findAll(PageRequest.of(page, 3, Sort.Direction.DESC, "id"));


        List<Role> list = Arrays.asList(Role.values());

        if (list.contains(role)) {

            user = userAccountRepository.test(role, username, PageRequest.of(page, 3, Sort.Direction.DESC, "id"));
            String rolestr = role.toString();
            model.addAttribute("role", rolestr);
        } else if (username != "" && username != null) {
            System.out.println(2);
            user = userAccountRepository.test(username, PageRequest.of(page, 3, Sort.Direction.DESC, "id"));
        }

        if (role == null) {
            model.addAttribute("role", "");
        }

        model.addAttribute("username", username);
        model.addAttribute("user", user.getContent());
        model.addAttribute("numPage", user);
        model.addAttribute("numbers", IntStream.range(0, user.getTotalPages()).toArray());
        return "users";
    }


    @GetMapping("/user/{id}/edit")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public String user(@PathVariable("id") UserAccount user, Model model) {
        List<Role> list = Arrays.asList(Role.values());
        Map<Role, Boolean> map = new HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            if (user.getRoles().contains(list.get(i))) {
                map.put(list.get(i), true);
            } else {
                map.put(list.get(i), false);
            }
        }


        model.addAttribute("maps", map);
        model.addAttribute("user", user);
        return "userEdit";
    }

    @PostMapping("/user/{id}/edit")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public String editUser(@PathVariable("id") UserAccount user,
                           @RequestParam("username") String username,
                           @RequestParam("lastName") String lastName,
                           @RequestParam("firstName") String firstName,
                           @RequestParam("status") String status,
                           @RequestParam("role") String role,
                           Model model
    ) {
            String oldUsername = user.getUsername();
        System.out.println(oldUsername);

            user.getRoles().clear();
            user.getRoles().add(Role.valueOf(role));
            user.setStatus(status);
            user.setFirstName(firstName);
            user.setUsername(username);
        System.out.println(user.getUsername());
            user.setLastName(lastName);

        List<Role> list = Arrays.asList(Role.values());
        Map<Role, Boolean> map = new HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            if (user.getRoles().contains(list.get(i))) {
                map.put(list.get(i), true);
            } else {
                map.put(list.get(i), false);
            }
        }
            UserAccount us = userAccountRepository.findByUsername(user.getUsername());
            if (us != null
                   && !oldUsername.equalsIgnoreCase(user.getUsername())) {
                model.addAttribute("message", "Такое имя пользователя уже занято!");
                model.addAttribute("maps", map);
                model.addAttribute("user", user);
                return "userEdit";
            }

                userAccountRepository.save(user);




            model.addAttribute("maps", map);
            model.addAttribute("user", user);
            model.addAttribute("success", "Пользователь успешно обновлен!");
            return "userEdit";

    }

    @GetMapping("/user/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
    public String userId(@PathVariable("id") UserAccount user,
                          Model model) {
        model.addAttribute("user", user);
        return "userId";
    }

    @PostMapping("/user/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public String userIdActive(@PathVariable("id") UserAccount user,
                          @RequestParam(name = "status", defaultValue ="INACTIVE") String st,
                          Model model) {
        user.setStatus(st);
        userAccountRepository.save(user);
        model.addAttribute("user", user);
        return "userId";
    }


    @GetMapping("/user/{id}/edit/password")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public String editPasswordGet(@PathVariable("id") UserAccount user, Model model) {
        model.addAttribute("user", user);
        return "password";
    }

    @PostMapping("/user/{id}/edit/password")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public String editPasswordPost(@PathVariable("id") UserAccount user,
                           @RequestParam("password") String password,
                           @RequestParam("new1") String new1,
                           @RequestParam("new2") String new2,
                           Model model
    ) {
        if (BCrypt.checkpw(password, user.getPassword())) {
        } else {
            model.addAttribute("message", "Старый пароль неверный");
            model.addAttribute("user", user);
            return "password";
        }

        if (!new1.equals(new2)) {
            model.addAttribute("message", "Новые пароли не совпадают");
            model.addAttribute("user", user);
            return "password";
        }

        user.setPassword(passwordEncoder.encode(new1));
        userAccountRepository.save(user);
        model.addAttribute("success", "Новый пароль успешно установлен");
        model.addAttribute("user", user);
        return "password";
    }
}
