package com.example.tilminsmukkekone.presentation;

import com.example.tilminsmukkekone.application.LoveService;
import com.example.tilminsmukkekone.domain.classes.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Controller
public class LoveController {

    @Autowired
    private LoveService loveService;

    @GetMapping("/til-dig")
    public String showLovePage(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");

        long daysTogether = loveService.getDaysTogether(currentUser);
        long daysUntil = loveService.getDaysUntilNextAnniversary(currentUser);
        int relationshipYears = loveService.getRelationshipYears(currentUser);
        LocalDate nextAnniversaryDate = loveService.getNextAnniversaryDate(currentUser);

        model.addAttribute("daysTogether", daysTogether);
        model.addAttribute("daysUntil", daysUntil);
        model.addAttribute("relationshipYears", relationshipYears);
        model.addAttribute("nextAnniversaryDate", nextAnniversaryDate.toString());

        String randomCoupon = loveService.getRandomLoveCoupon();
        model.addAttribute("loveCoupon", randomCoupon);

        model.addAttribute("allCoupons", loveService.getAllLoveCoupons());

        return "til-dig";
    }

    @GetMapping("/api/love-coupon")
    @ResponseBody
    public Map<String, String> getRandomCoupon() {
        Map<String, String> response = new HashMap<>();
        response.put("coupon", loveService.getRandomLoveCoupon());
        return response;
    }
}