/*
 * Copyright 2016 Wojciech Zankowski.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.blazarquant.bfp.core.mail;

import com.blazarquant.bfp.core.mail.connection.MailConnection;
import com.blazarquant.bfp.core.mail.connection.MailConnectionCredentials;
import com.blazarquant.bfp.core.mail.connection.MailSession;
import com.blazarquant.bfp.core.mail.data.MailMessage;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;

/**
 * @author Wojciech Zankowski
 */
public class MailEngine {

    public static final String SENDER_MAIL = "no-reply@blazarquant.com";

    private final static Logger LOGGER = LoggerFactory.getLogger(MailEngine.class);

    private final LinkedBlockingQueue<MailMessage> mailMessageQueue = new LinkedBlockingQueue<>();
    private final ExecutorService mailEngineService = Executors.newSingleThreadExecutor(new ThreadFactory() {

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.setName("Mail-Engine-Service-Thread");
            return thread;
        }
    });

    private MailConnection mailConnection;
    private MailConnectionCredentials mailConnectionCredentials;
    private MailSynchronizationThread mailSynchronizationThread;
    private MailSession mailSession;

    @Inject
    public MailEngine(MailConnection mailConnection, MailConnectionCredentials mailConnectionCredentials) {
        this.mailConnection = mailConnection;
        this.mailConnectionCredentials = mailConnectionCredentials;
    }

    public void start() throws MessagingException {
        mailSession = mailConnection.connect(
                mailConnectionCredentials.getHost(),
                mailConnectionCredentials.getPort(),
                mailConnectionCredentials.getUsername(),
                mailConnectionCredentials.getPassword()
        );

        startMailSynchronizationThread();
    }

    // TODO Call somewhere
    public void stop() {
        if (mailSynchronizationThread != null) {
            mailSynchronizationThread.sendStop();
        }
    }

    public void sendMessage(String message, String subject, String userMail) {
        mailEngineService.submit(() -> {
            Transport transport = null;
            try {
                MimeMessage mimeMessage = new MimeMessage(mailSession.getSession());
                mimeMessage.setFrom(new InternetAddress(SENDER_MAIL));
                mimeMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(userMail));
                mimeMessage.setSubject(subject);

                MimeBodyPart bodyPart = new MimeBodyPart();
                bodyPart.setContent(message, "text/html");

                Multipart multipart = new MimeMultipart();
                multipart.addBodyPart(bodyPart);

                mimeMessage.setContent(multipart);
                mimeMessage.saveChanges();

                transport = mailSession.getTransport("smtp");
                if (!transport.isConnected()) {
                    transport.connect(
                            mailConnectionCredentials.getHost(),
                            mailConnectionCredentials.getPort(),
                            mailConnectionCredentials.getUsername(),
                            mailConnectionCredentials.getPassword()
                    );
                }
                transport.sendMessage(mimeMessage, new InternetAddress[]{new InternetAddress(userMail)});
            } catch (MessagingException e) {
                LOGGER.error("Failed to send mail message. Message saved to try again in the future.", e);
                mailMessageQueue.add(new MailMessage.Builder()
                        .recipient(userMail)
                        .subject(subject)
                        .text(message)
                        .build());

            } finally {
                if (transport != null) {
                    try {
                        transport.close();
                    } catch (MessagingException e) {
                        LOGGER.warn("Failed to close transport object.", e);
                    }
                }
            }
        });
    }

    public void startMailSynchronizationThread() {
        mailSynchronizationThread = new MailSynchronizationThread();
        mailSynchronizationThread.start();
    }

    private class MailSynchronizationThread extends Thread {

        public static final long DELAY = 10000;
        public static final String THREAD_NAME = "Mail-Synchronization-Thread";

        private boolean stop = false;

        public MailSynchronizationThread() {
            super(THREAD_NAME);
        }

        public void sendStop() {
            stop = true;
        }

        @Override
        public void run() {
            while (true) {
                if (stop) {
                    break;
                }
                try {
                    Thread.sleep(DELAY);

                    List<MailMessage> mailMessages = new ArrayList<>();
                    mailMessageQueue.drainTo(mailMessages);
                    mailMessages.forEach((mailMessage -> {
                        MailEngine.this.sendMessage(
                                mailMessage.getText(),
                                mailMessage.getSubject(),
                                mailMessage.getRecipient());
                    }));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

}
