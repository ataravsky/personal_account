package network.network.controller;

import network.network.model.Role;
import network.network.model.UserAccount;
import network.network.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class MainController {
    @Autowired
    private UserAccountRepository userAccountRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/")
    public String mainController() {

        return "redirect:/user";
    }

    @GetMapping("/first")
    public String first() {
        if (!userAccountRepository.findAll().isEmpty()) {
            return "redirect:/user";
        }
        return "first";
    }

    @PostMapping("/first")
    public String addUserFirst(UserAccount user, Model model,
                          @RequestParam Map<String, String> form
    ) {

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
        return "first";
    }
}
