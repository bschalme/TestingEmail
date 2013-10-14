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

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;
import javax.mail.internet.MimeMultipart;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.util.ReflectionTestUtils;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;

/**
 * Unit test for EmailService.
 */
public class EmailServiceTest {
	private Wiser wiser;

	@Before
	public void setUp() throws Exception {
		wiser = new Wiser();
		wiser.setPort(2525);
		wiser.start();
	}

	@After
	public void tearDown() throws Exception {
		wiser.stop();
		wiser = null;
	}

	/**
	 * When we send out an email with just a text body, et expect to get a
	 * Multipart email having only a plain text body.
	 */
	@Test
	public void testSimpleEmail() throws Exception {
		// Given:
		EmailService service = makeALocalMailer();

		InternetAddress expectedTo = new InternetAddress(
				"Indiana.Jones@domain.com", "Indiana Jones");
		String expectedSubject = "This is a Test Email";
		String expectedTextBody = "This is a simple test.";

		// When:
		service.sendSimpleEmail(expectedTo, expectedSubject, expectedTextBody);

		// Then:
		List<WiserMessage> messages = wiser.getMessages();
		assertEquals("Number of messages sent;", 1, messages.size());

		WiserMessage message = messages.get(0);
		assertNotNull("No message was actually sent.", message);
		MimeMessage mimeMessage = message.getMimeMessage();
		Address[] toRecipients = mimeMessage.getRecipients(RecipientType.TO);
		assertEquals("Number of To: Recipient;", 1, toRecipients.length);
		Address toRecipient = toRecipients[0];
		assertEquals("To: Recipient;", expectedTo, toRecipient);

		InternetAddress expectedFrom = new InternetAddress("admin@domain.com",
				"Domain Admin");
		Address[] fromArr = mimeMessage.getFrom();
		assertEquals("From: email addresses;", 1, fromArr.length);
		assertEquals("Email From: address,", expectedFrom, fromArr[0]);

		assertEquals("Subject;", expectedSubject, mimeMessage.getSubject());

		assertNotNull("The date of the email cannot be null.",
				mimeMessage.getSentDate());

		MimeMultipart body = ((MimeMultipart) mimeMessage.getContent());
		assertEquals("Number of MIME Parts in the body;", 1, body.getCount());
		MimeMultipart textPart = ((MimeMultipart) body.getBodyPart(0)
				.getContent());
		assertEquals("Number of MIME parts in the text body;", 1,
				textPart.getCount());
		MimeBodyPart plainTextPart = ((MimeBodyPart) textPart.getBodyPart(0));
		assertTrue("Expected the plain text content to be text/plain.",
				plainTextPart.isMimeType("text/plain"));
		assertEquals("Text Body;", expectedTextBody, plainTextPart.getContent());
	}

	@Test
	public void testWithAttachment() throws Exception {
		// Given:
		EmailService service = makeALocalMailer();

		InternetAddress expectedTo = new InternetAddress(
				"Indiana.Jones@domain.com", "Indiana Jones");
		String expectedSubject = "This is a Test Email";
		String expectedTextBody = "This is a test with a PDF attachment.";
		List<FileSystemResource> filesToAttach = new ArrayList<FileSystemResource>();
		filesToAttach.add(new FileSystemResource(this.getClass()
				.getClassLoader().getResource("HelloWorld.pdf").getFile()));

		// When:
		service.sendWithAttachments(expectedTo, expectedSubject,
				expectedTextBody, filesToAttach);

		// Then:
		List<WiserMessage> messages = wiser.getMessages();
		assertEquals("Number of messages sent;", 1, messages.size());
		WiserMessage message = messages.get(0);
		MimeMessage mimeMessage = message.getMimeMessage();

		assertEquals("Subject;", expectedSubject, mimeMessage.getSubject());

		MimeMultipart body = ((MimeMultipart) mimeMessage.getContent());
		assertEquals("Number of MIME Parts in the body;", 2, body.getCount());

		MimeBodyPart attachment = ((MimeBodyPart) body.getBodyPart(1));
		assertTrue("Attachment MIME Type should be application/pdf",
				attachment.isMimeType("application/pdf"));
		assertEquals("Attachment filename;", "HelloWorld.pdf",
				attachment.getFileName());
		assertTrue("No content found in the attachment.", isNotBlank(attachment
				.getContent().toString()));
	}

	private EmailService makeALocalMailer() throws Exception {
		EmailService service = new EmailService();
		JavaMailSenderImpl sender = new JavaMailSenderImpl();
		sender.setHost("localhost");
		sender.setPort(2525);
		ReflectionTestUtils.setField(service, "mailSender", sender);
		ReflectionTestUtils.setField(service, "senderAddress",
				new InternetAddress("admin@domain.com", "Domain Admin"));
		return service;
	}
}
