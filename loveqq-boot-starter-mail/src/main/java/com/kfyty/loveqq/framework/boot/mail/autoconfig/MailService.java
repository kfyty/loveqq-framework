package com.kfyty.loveqq.framework.boot.mail.autoconfig;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.NoSuchProviderException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.SneakyThrows;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

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
    public void send(String subject, String receiveUser, Multipart multipart) {
        this.send(subject, receiveUser, multipart, Message.RecipientType.TO);
    }

    /**
     * 发送邮件
     *
     * @param subject     主题
     * @param receiveUser 接受者
     * @param multipart   内容
     */
    @SneakyThrows({AddressException.class, MessagingException.class})
    public void send(String subject, String receiveUser, Multipart multipart, Message.RecipientType recipientType) {
        MimeMessage mimeMessage = new MimeMessage(this.mailSession);
        mimeMessage.setFrom(new InternetAddress(this.mailProperties.getUsername()));
        mimeMessage.setRecipient(recipientType, new InternetAddress(receiveUser));
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
        try (Transport transport = CommonUtil.empty(this.mailProperties.getProtocol()) ? this.mailSession.getTransport() : this.mailSession.getTransport(this.mailProperties.getProtocol())) {
            if (this.mailProperties.getPort() == null || this.mailProperties.getPort() < 0) {
                transport.connect(this.mailProperties.getHost(), this.mailProperties.getUsername(), this.mailProperties.getPassword());
            } else {
                transport.connect(this.mailProperties.getHost(), this.mailProperties.getPort(), this.mailProperties.getUsername(), this.mailProperties.getPassword());
            }
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
        List<DataSource> dataSources = files.stream().map(FileDataSource::new).collect(Collectors.toList());
        MimeMultipart multipart = packageMultiPart(content, dataSources);
        this.send(subject, receiveUser, multipart);
    }

    /**
     * 发送附件
     *
     * @param subject     主题
     * @param receiveUser 接收人
     * @param content     内容
     * @param contentType 附件内容
     * @param bytes       附件
     */
    public void sendBytes(String subject, String receiveUser, String content, String contentType, List<byte[]> bytes) {
        List<DataSource> dataSources = bytes.stream().map(e -> new ByteArrayDataSource(e, contentType)).collect(Collectors.toList());
        MimeMultipart multipart = packageMultiPart(content, dataSources);
        this.send(subject, receiveUser, multipart);
    }

    /**
     * 发送附件
     *
     * @param subject     主题
     * @param receiveUser 接收人
     * @param content     内容
     * @param dataSources 附件
     */
    public void sendDataSources(String subject, String receiveUser, String content, List<DataSource> dataSources) {
        MimeMultipart multipart = packageMultiPart(content, dataSources);
        this.send(subject, receiveUser, multipart);
    }

    /**
     * 打包附件
     *
     * @param content     邮件内容
     * @param dataSources 邮件附件数据源
     * @return {@link MimeMultipart}
     */
    @SneakyThrows(MessagingException.class)
    public static MimeMultipart packageMultiPart(String content, List<DataSource> dataSources) {
        MimeMultipart multipart = new MimeMultipart();
        multipart.setSubType("mixed");

        for (DataSource dataSource : dataSources) {
            MimeBodyPart bodyPart = new MimeBodyPart();
            bodyPart.setFileName(dataSource.getName());
            bodyPart.setDataHandler(new DataHandler(dataSource));
            multipart.addBodyPart(bodyPart);
        }

        MimeBodyPart text = new MimeBodyPart();
        text.setContent(content, "text/html;charset=utf-8");
        multipart.addBodyPart(text);

        return multipart;
    }
}
