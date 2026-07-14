package com.example._2famanage.service;

import com.example._2famanage.model.TwoFAAccount;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 账号数据持久化存储，使用 JSON 文件
 */
@Service
public class AccountStore {

    private static final Logger log = LoggerFactory.getLogger(AccountStore.class);

    @Value("${account.store.path:}")
    private String configuredPath;

    private String storePath;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<TwoFAAccount> accounts = new ArrayList<>();

    @PostConstruct
    public void init() {
        // 确定存储路径：优先使用配置路径，否则使用 JAR 包同级目录
        if (configuredPath != null && !configuredPath.isEmpty()) {
            storePath = configuredPath;
        } else {
            File jarDir = new ApplicationHome(AccountStore.class).getDir();
            storePath = new File(jarDir, "2fa-accounts.json").getAbsolutePath();
        }

        // 确保存储目录存在
        File storeFile = new File(storePath);
        File parentDir = storeFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        log.info("数据文件路径: {}", storeFile.getAbsolutePath());

        loadFromFile();
        // 首次启动，没有数据时添加示例账号
        if (accounts.isEmpty()) {
            accounts.add(new TwoFAAccount("示例账号", "JBSWY3DPEHPK3PXP"));
            saveToFile();
            log.info("首次启动，已添加示例账号");
        }
    }

    public List<TwoFAAccount> getAll() {
        return accounts;
    }

    public void add(TwoFAAccount account) {
        accounts.add(account);
        saveToFile();
    }

    public void remove(int index) {
        if (index >= 0 && index < accounts.size()) {
            accounts.remove(index);
            saveToFile();
        }
    }

    private void loadFromFile() {
        File file = new File(storePath);
        if (!file.exists()) {
            log.info("数据文件不存在，将创建新文件: {}", file.getAbsolutePath());
            return;
        }
        try {
            List<TwoFAAccount> loaded = objectMapper.readValue(file, new TypeReference<List<TwoFAAccount>>() {});
            accounts.clear();
            accounts.addAll(loaded);
            log.info("已从文件加载 {} 个账号", accounts.size());
        } catch (IOException e) {
            log.error("加载数据文件失败: {}", e.getMessage());
        }
    }

    private void saveToFile() {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(storePath), accounts);
        } catch (IOException e) {
            log.error("保存数据文件失败: {}", e.getMessage());
        }
    }
}
