package com.kfyty.loveqq.framework.boot.mail.autoconfig;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Configuration;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnBean;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnMissingBean;
import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;

/**
 * 描述: mail 自动配置
 *
 * @author kfyty725
 * @date 2024/7/4 16:30
 * @email kfyty725@hotmail.com
 */
@Configuration
@ConditionalOnBean(MailProperties.class)
public class MailAutoConfiguration {

    @Bean
    public MailService mailService() {
        return new MailService();
    }

    @Bean
    @ConditionalOnMissingBean
    public Session mailSession(MailProperties mailProperties) {
        return Session.getDefaultInstance(mailProperties.getProperties(), new Authenticator() {

            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(mailProperties.getUsername(), mailProperties.getPassword());
            }
        });
    }
}
