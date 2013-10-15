/*
   Copyright 2013 Airspeed Consulting

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package ca.airspeed.demo.testingemail;

import static org.springframework.util.Assert.notNull;

import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

/**
 * Hello world!
 * 
 */
public class EmailService {
	@Autowired
	private JavaMailSender mailSender;
	
	@Autowired
	private InternetAddress senderAddress;

	public void sendSimpleEmail(InternetAddress to, String subject,
			String textBody) throws MessagingException {
		notNull(mailSender, String.format(
				"Check your configuration, I need an instance of %s.",
				JavaMailSender.class.getCanonicalName()));
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true);
		helper.setTo(to);
		helper.setFrom(senderAddress);
		helper.setSubject(subject);
		helper.setText(textBody);
		mailSender.send(message);
	}
	
	public void sendWithAttachments(InternetAddress to, String subject,
			String textBody, List<FileSystemResource> fileList) throws MessagingException {
		notNull(mailSender, String.format(
				"Check your configuration, I need an instance of %s.",
				JavaMailSender.class.getCanonicalName()));
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true);
		helper.setTo(to);
		helper.setFrom(senderAddress);
		helper.setSubject(subject);
		helper.setText(textBody);
		for (FileSystemResource resource: fileList) {
			helper.addAttachment(resource.getFilename(), resource.getFile());
		}
		mailSender.send(message);
	}
}
