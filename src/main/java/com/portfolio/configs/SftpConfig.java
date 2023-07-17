package com.portfolio.configs;

import com.portfolio.util.SftpUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SftpConfig {
    @Value("${spring.sftp.host}")
    private String host;

    @Value("${spring.sftp.username}")
    private String username;

    @Value("${spring.sftp.port}")
    private int port = 22;

    @Value("${spring.sftp.password}")
    private String password;

    @Value("${spring.sftp.root}")
    private String root;

    @Value("${spring.sftp.privateKey}")
    private String privateKey;

    @Value("${spring.sftp.back.conf.root}")
    private String backConfRoot;

    @Bean
    public SftpUtil sftpUtil() {
        return new SftpUtil(host, username, password, backConfRoot + privateKey, port, root);
    }
}
