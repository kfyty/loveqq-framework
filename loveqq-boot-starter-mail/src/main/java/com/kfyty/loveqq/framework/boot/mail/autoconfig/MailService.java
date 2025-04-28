package com.kfyty.loveqq.framework.boot.mail.autoconfig;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import lombok.SneakyThrows;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.util.List;

/**
 * 描述: 邮件服务
 *
 * @author kfyty725
 * @date 2024/7/4 16:34
 * @email kfyty725@hotmail.com
 */
public class MailService {
    @Autowired
    private MailProperties mailProperties;

    @Autowired
    private Session mailSession;

    /**
     * 发送邮件
     *
     * @param subject     主题
     * @param receiveUser 接受者
     * @param content     文本内容
     */
    public void send(String subject, String receiveUser, String content) {
        this.send(subject, receiveUser, content, "text/html;charset=utf-8");
    }

    /**
     * 发送邮件
     *
     * @param subject     主题
     * @param receiveUser 接受者
     * @param content     文本内容
     * @param contentType 内容类型
     */
    @SneakyThrows(MessagingException.class)
    public void send(String subject, String receiveUser, String content, String contentType) {
        MimeBodyPart text = new MimeBodyPart();
        text.setContent(content, contentType);

        MimeMultipart multipart = new MimeMultipart();
        multipart.addBodyPart(text);
        multipart.setSubType("mixed");

        this.send(subject, receiveUser, multipart);
    }

    /**
     * 发送邮件
     *
     * @param subject     主题
     * @param receiveUser 接受者
     * @param multipart   内容
     */
    @SneakyThrows({AddressException.class, MessagingException.class})
    public void send(String subject, String receiveUser, Multipart multipart) {
        MimeMessage mimeMessage = new MimeMessage(this.mailSession);
        mimeMessage.setFrom(new InternetAddress(this.mailProperties.getUsername()));
        mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(receiveUser));
        mimeMessage.setSubject(subject);
        mimeMessage.setContent(multipart);
        mimeMessage.saveChanges();
        this.send(mimeMessage);
    }

    /**
     * 发送邮件
     *
     * @param mimeMessage 完整消息体
     */
    @SneakyThrows({NoSuchProviderException.class, MessagingException.class})
    public void send(MimeMessage mimeMessage) {
        try (Transport transport = this.mailSession.getTransport()) {
            transport.connect(this.mailProperties.getHost(), this.mailProperties.getUsername(), this.mailProperties.getPassword());
            transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
        }
    }

    /**
     * 发送附件
     *
     * @param subject     主题
     * @param receiveUser 接收人
     * @param content     内容
     * @param files       附件
     */
    public void sendFiles(String subject, String receiveUser, String content, List<File> files) {
        MimeMultipart multipart = packageMultiPart(content, files);
        this.send(subject, receiveUser, multipart);
    }

    @SneakyThrows(MessagingException.class)
    public static MimeMultipart packageMultiPart(String content, List<File> files) {
        MimeMultipart multipart = new MimeMultipart();
        multipart.setSubType("mixed");

        for (File file : files) {
            MimeBodyPart bodyPart = new MimeBodyPart();
            bodyPart.setFileName(file.getName());
            bodyPart.setDataHandler(new DataHandler(new FileDataSource(file)));
            multipart.addBodyPart(bodyPart);
        }

        MimeBodyPart text = new MimeBodyPart();
        text.setContent(content, "text/html;charset=utf-8");
        multipart.addBodyPart(text);

        return multipart;
    }
}
