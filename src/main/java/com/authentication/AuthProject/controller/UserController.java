package com.authentication.AuthProject.controller;

import com.authentication.AuthProject.dto.ChangePasswordRequest;
import com.authentication.AuthProject.dto.UpdateProfileRequest;
import com.authentication.AuthProject.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    //id should be unique and unpredictable
    //use try catch
    //http codes to be used


    @GetMapping("/profile/{id}")
    public String profile(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        var user = service.getUser(id);

        if (user.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "User not found.");
            return "redirect:/login";
        }

        model.addAttribute("user", user.get());
        return "profile";
    }



    @GetMapping("/edit-profile/{id}")
    public String editProfilePage(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        var user = service.getUser(id);

        if (user.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "User not found.");
            return "redirect:/login";
        }

        model.addAttribute("user", user.get());
        return "edit-profile";
    }



    @PutMapping("/edit-profile/{id}")
    public String updateProfile(@PathVariable Long id,
                                @ModelAttribute UpdateProfileRequest request,
                                RedirectAttributes redirectAttributes) {
        var error = service.updateUser(id, request);

        if (error.isPresent()) {
            redirectAttributes.addFlashAttribute("error", error.get());
            return "redirect:/edit-profile/" + id;
        }

        redirectAttributes.addFlashAttribute("message", "Profile updated successfully.");
        return "redirect:/profile/" + id;
    }



    @GetMapping("/change-password/{id}")
    public String changePasswordPage(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        var user = service.getUser(id);

        if (user.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "User not found.");
            return "redirect:/login";
        }

        model.addAttribute("userId", id);
        return "change-password";
    }


    @PutMapping("/change-password/{id}")
    public String changePassword(@PathVariable Long id,
                                   @ModelAttribute ChangePasswordRequest request,
                                   RedirectAttributes redirectAttributes) {
        var error = service.changePassword(id, request);

        if (error.isPresent()) {
            redirectAttributes.addFlashAttribute("error", error.get());
            return "redirect:/change-password/" + id;
        }

        redirectAttributes.addFlashAttribute("message", "Password updated successfully.");
        return "redirect:/home/" + id;
    }
}
