package com.authentication.AuthProject.controller;

import com.authentication.AuthProject.dto.request.LoginRequest;
import com.authentication.AuthProject.dto.request.SignupRequest;
import com.authentication.AuthProject.service.AuthService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }



    @RequestMapping("/")
    @ResponseBody
    public String greet() {
        return "Project is Running";
    }



    @GetMapping("/signup")
    public String signUpPage() {
        return "signup";
    }



    @PostMapping("/signup")
    public String signup(@ModelAttribute SignupRequest request, RedirectAttributes redirectAttributes) {

        //ModelAt
        //change the error name
        //replace var keyword


        //Check for the success message
        var error = service.signup(request);

        if (error.isPresent()) {
            redirectAttributes.addFlashAttribute("error", error.get());
            return "redirect:/signup";
        }

        redirectAttributes.addFlashAttribute("message", "Signup successful. Please login.");
        return "redirect:/login";
    }



    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }



    @PostMapping("/login")
    public String login(@ModelAttribute LoginRequest request, RedirectAttributes redirectAttributes) {
        var userId = service.login(request);

        if (userId.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", service.getLoginError(request).orElse("Login failed."));
            return "redirect:/login";
        }

        return "redirect:/home/" + userId.get();
    }



    @GetMapping("/home")
    public String homeRedirect() {
        return "redirect:/login";
    }



    @GetMapping("/home/{id}")
    public String homePage(@PathVariable Long id, Model model) {
        model.addAttribute("userId", id);
        return "home";
    }
}
