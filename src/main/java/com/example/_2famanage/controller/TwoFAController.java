package com.example._2famanage.controller;

import com.example._2famanage.model.TwoFAAccount;
import com.example._2famanage.service.AccountStore;
import com.example._2famanage.service.TOTPService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
public class TwoFAController {

    private final TOTPService totpService;
    private final AccountStore accountStore;

    public TwoFAController(TOTPService totpService, AccountStore accountStore) {
        this.totpService = totpService;
        this.accountStore = accountStore;
    }

    /**
     * 主页 - 显示所有账号及验证码
     */
    @GetMapping("/")
    public String index(Model model) {
        List<TwoFAAccount> accounts = accountStore.getAll();
        List<Map<String, Object>> codes = new ArrayList<>();
        for (TwoFAAccount account : accounts) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", account.getName());
            item.put("code", totpService.generateCode(account.getSecret()));
            codes.add(item);
        }
        model.addAttribute("codes", codes);
        model.addAttribute("remainingSeconds", totpService.getRemainingSeconds());
        return "index";
    }

    /**
     * API: 获取所有验证码（JSON，供前端 AJAX 轮询）
     */
    @GetMapping("/api/codes")
    @ResponseBody
    public Map<String, Object> getCodes() {
        List<TwoFAAccount> accounts = accountStore.getAll();
        List<Map<String, Object>> codes = new ArrayList<>();
        for (TwoFAAccount account : accounts) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", account.getName());
            item.put("code", totpService.generateCode(account.getSecret()));
            codes.add(item);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("codes", codes);
        result.put("remainingSeconds", totpService.getRemainingSeconds());
        return result;
    }

    /**
     * 添加新账号
     */
    @PostMapping("/add")
    @ResponseBody
    public Map<String, Object> addAccount(@RequestParam String name, @RequestParam String secret) {
        secret = secret.replaceAll("\\s+", "").toUpperCase();
        accountStore.add(new TwoFAAccount(name, secret));
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        return result;
    }

    /**
     * 删除账号
     */
    @PostMapping("/delete")
    @ResponseBody
    public Map<String, Object> deleteAccount(@RequestParam int index) {
        accountStore.remove(index);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        return result;
    }
}
