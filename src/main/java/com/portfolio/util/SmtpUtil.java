package com.portfolio.util;

import com.sun.istack.ByteArrayDataSource;
import org.springframework.web.multipart.MultipartFile;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileTypeMap;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.List;
import java.util.Properties;

public class SmtpUtil {
    public static void sendMail(String host, String port, String username, String password, String[] recipients, String subject, String contents, List<MultipartFile> files) throws Exception {
        // SMTP 서버 설정 정보 세팅
        Properties props = System.getProperties();
        // smtp 서버
        props.put("mail.smtp.host", host);
        // smtp 포트
        props.put("mail.smtp.port", port);
        // smtp 인증 여부
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory"); //SSL Factory Class
        props.put("mail.smtp.auth", "true"); //Enabling SMTP Authentication

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                return new javax.mail.PasswordAuthentication(username, password);
            }
        });
        session.setDebug(false);

        MimeMessage message = new MimeMessage(session);

        message.addHeader("Content-type", "text/HTML; charset=UTF-8");
        message.addHeader("format", "flowed");
        message.addHeader("Content-Transfer-Encoding", "8bit");
        message.setFrom(new InternetAddress(username));
        InternetAddress[] addresses = new InternetAddress[recipients.length];
        for (int i = 0; i < recipients.length; i++) {
            addresses[i] = new InternetAddress(recipients[i], false);
        }
        message.addRecipients(Message.RecipientType.TO, addresses);
        message.setSubject(subject, "UTF-8");

        // 메일 내용
        Multipart multipart = new MimeMultipart();
        BodyPart messageBodyPart;
        if (files != null && files.size() > 0) {
            for (MultipartFile file : files) {
                messageBodyPart = new MimeBodyPart();
                String fileName = file.getOriginalFilename();
                String mimeType = FileTypeMap.getDefaultFileTypeMap().getContentType(fileName);
                DataSource dataSource = new ByteArrayDataSource(file.getBytes(), mimeType);
                messageBodyPart.setDataHandler(new DataHandler(dataSource));
                messageBodyPart.setFileName(fileName);
                multipart.addBodyPart(messageBodyPart);
            }
        }
        StringBuilder stringBuilder  = new StringBuilder();
        stringBuilder.append(contents).append("<br><br>");
        stringBuilder.append("<div style=\"margin-top:5vh; display: flex; justify-items: left; justify-contents: left; text-align: left;\">");
        stringBuilder.append("<div style=\"width: 60% !important; display: flex;\">");
        stringBuilder.append("<img style=\"width: 30% !important; border-radius: 50%;\" src='https://fileserver.tranquil-worker.com/documents/tranquil-icon.png'>");
        stringBuilder.append("<div style=\"display: grid; justify-content: center; margin-left: 5%;\">");
        stringBuilder.append("<h4>본 메일은 차분한 워커의 포트폴리오에서<br> 발신된 메일입니다. :)</h4>");
        stringBuilder.append("<h4>다시 포트폴리오 페이지로 돌아가시려면<br>아래의 링크를 눌러주세요.<br> 감사합니다!</h4>");
        stringBuilder.append("<p style=\"margin-top: 5px;\"><button style=\"border: 1px solid #dee2e6;\"><a style=\"color: black !important; text-decoration: none;\" href=\"https://portfolio.tranquil-worker.com/mail\">다시 메일 테스트 페이지로 이동하기</a></button></p>");
        stringBuilder.append("</div>");
        stringBuilder.append("</div>");
        messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(stringBuilder.toString(), "text/html; charset=UTF-8");
        multipart.addBodyPart(messageBodyPart);
        message.setContent(multipart);
        Transport.send(message);
    }
}
