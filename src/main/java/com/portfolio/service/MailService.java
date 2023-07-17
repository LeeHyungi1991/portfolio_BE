package com.portfolio.service;

import com.portfolio.util.SmtpUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.internet.AddressException;
import java.util.List;

@Service
public class MailService {
    @Value("${mail.smtp.host}")
    private String host;
    @Value("${mail.smtp.port}")
    private String port;
    @Value("${mail.smtp.username}")
    private String username;
    @Value("${mail.smtp.password}")
    private String password;

    public void sendMail(String[] recipients, String subject, String contents, List<MultipartFile> files) throws Exception {
        try {
            SmtpUtil.sendMail(host, port, username, password, recipients, subject, contents, files);
        } catch (AddressException e) {
            throw new Exception("이메일 주소가 존재하지 않습니다.");
        }
    }
}
