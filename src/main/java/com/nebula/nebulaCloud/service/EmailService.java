package com.nebula.nebulaCloud.service;

import com.nebula.nebulaCloud.model.Container;
import com.nebula.nebulaCloud.model.Instance;
import com.nebula.nebulaCloud.model.Plan;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Service responsible for sending emails.
 *
 * This service handles the business logic for email notifications,
 * including sending database credentials to users when instances are created
 * and plan purchase confirmations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${application.email.from}")
    private String fromEmail;

    @Value("${application.email.from-name}")
    private String fromName;

    /**
     * Sends database credentials to the user via email.
     *
     * This method is called after a new database instance is successfully created.
     * It sends an HTML email with all the connection details needed to access the database.
     *
     * @param userEmail The email address of the user who created the instance
     * @param instance The database instance that was created
     * @param plainPassword The original unencrypted password (not stored in DB)
     * @throws RuntimeException if the email fails to send
     */
    public void sendDatabaseCredentials(String userEmail, Instance instance, String plainPassword) {
        try {
            log.info("==================== EMAIL SENDING START ====================");
            log.info("Preparing to send database credentials email to: {}", userEmail);
            log.info("From: {} ({})", fromEmail, fromName);
            log.info("Subject: Your Database Instance is Ready - Nebula Cloud");

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(userEmail);
            helper.setSubject("Your Database Instance is Ready - Nebula Cloud");

            // Add headers to improve deliverability
            message.addHeader("X-Priority", "1");
            message.addHeader("X-MSMail-Priority", "High");
            message.addHeader("Importance", "High");
            message.addHeader("X-Mailer", "Nebula Cloud Platform");

            String htmlContent = buildCredentialsEmailHtml(instance, plainPassword);
            helper.setText(htmlContent, true);

            log.info("Attempting to send email via SMTP...");
            mailSender.send(message);
            log.info("✅ Email sent successfully to: {}", userEmail);
            log.info("==================== EMAIL SENDING END ====================");

        } catch (MessagingException e) {
            log.error("==================== EMAIL SENDING FAILED ====================");
            log.error("❌ MessagingException when sending to: {}", userEmail);
            log.error("Error message: {}", e.getMessage());
            log.error("Error type: {}", e.getClass().getName());
            log.error("Stack trace: ", e);
            throw new RuntimeException("Failed to send credentials email: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("==================== EMAIL SENDING FAILED ====================");
            log.error("❌ Unexpected error when sending to: {}", userEmail);
            log.error("Error message: {}", e.getMessage());
            log.error("Error type: {}", e.getClass().getName());
            log.error("Stack trace: ", e);
            throw new RuntimeException("Unexpected error sending email: " + e.getMessage(), e);
        }
    }

    /**
     * Builds the HTML content for the database credentials email.
     *
     * @param instance The database instance with credentials
     * @param plainPassword The original unencrypted password
     * @return HTML string with formatted credentials
     */
    private String buildCredentialsEmailHtml(Instance instance, String plainPassword) {
        Container container = instance.getContainer();
        String engine = container.getEngine().getName();
        String host = container.getIp();
        Integer port = container.getPort();
        String dbName = instance.getDatabaseName();
        String dbUser = instance.getUserDb().getDbUser();
        String dbPassword = plainPassword;

        // Build connection string based on database engine
        String connectionString = buildConnectionString(engine, host, port, dbName);

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        max-width: 600px;
                        margin: 0 auto;
                        padding: 20px;
                    }
                    .header {
                        background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                        color: white;
                        padding: 30px;
                        border-radius: 10px 10px 0 0;
                        text-align: center;
                    }
                    .content {
                        background: #f9f9f9;
                        padding: 30px;
                        border-radius: 0 0 10px 10px;
                    }
                    .credentials-box {
                        background: white;
                        border: 2px solid #667eea;
                        border-radius: 8px;
                        padding: 20px;
                        margin: 20px 0;
                    }
                    .credential-item {
                        margin: 10px 0;
                        padding: 10px;
                        background: #f0f0f0;
                        border-radius: 5px;
                    }
                    .credential-label {
                        font-weight: bold;
                        color: #667eea;
                        display: inline-block;
                        width: 150px;
                    }
                    .credential-value {
                        font-family: 'Courier New', monospace;
                        color: #333;
                    }
                    .warning {
                        background: #fff3cd;
                        border-left: 4px solid #ffc107;
                        padding: 15px;
                        margin: 20px 0;
                        border-radius: 5px;
                    }
                    .footer {
                        text-align: center;
                        margin-top: 30px;
                        padding-top: 20px;
                        border-top: 1px solid #ddd;
                        color: #666;
                        font-size: 12px;
                    }
                    code {
                        background: #f4f4f4;
                        padding: 2px 6px;
                        border-radius: 3px;
                        font-family: 'Courier New', monospace;
                    }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>🎉 Your Database is Ready!</h1>
                    <p>Your %s database instance has been successfully created</p>
                </div>
                
                <div class="content">
                    <h2>Database Connection Details</h2>
                    <p>Use the following credentials to connect to your database:</p>
                    
                    <div class="credentials-box">
                        <div class="credential-item">
                            <span class="credential-label">Database Engine:</span>
                            <span class="credential-value">%s</span>
                        </div>
                        <div class="credential-item">
                            <span class="credential-label">Host:</span>
                            <span class="credential-value">%s</span>
                        </div>
                        <div class="credential-item">
                            <span class="credential-label">Port:</span>
                            <span class="credential-value">%d</span>
                        </div>
                        <div class="credential-item">
                            <span class="credential-label">Database Name:</span>
                            <span class="credential-value">%s</span>
                        </div>
                        <div class="credential-item">
                            <span class="credential-label">Username:</span>
                            <span class="credential-value">%s</span>
                        </div>
                        <div class="credential-item">
                            <span class="credential-label">Password:</span>
                            <span class="credential-value">%s</span>
                        </div>
                    </div>
                    
                    <h3>Connection String</h3>
                    <div class="credential-item">
                        <code>%s</code>
                    </div>
                    
                    <div class="warning">
                        <strong>⚠️ Security Notice:</strong>
                        <ul>
                            <li>Keep these credentials secure and private</li>
                            <li>Do not share them with anyone</li>
                            <li>Consider changing the password after first login</li>
                            <li>Use environment variables in your applications</li>
                        </ul>
                    </div>
                    
                    <h3>Quick Start</h3>
                    <p>You can connect to your database using any database client or through your application code.</p>
                    
                    <p>Need help? Visit our documentation or contact support.</p>
                </div>
                
                <div class="footer">
                    <p>This is an automated message from Nebula Cloud</p>
                    <p>&copy; 2025 Nebula Cloud. All rights reserved.</p>
                </div>
            </body>
            </html>
            """.formatted(
                engine,
                engine,
                host,
                port,
                dbName,
                dbUser,
                dbPassword,
                connectionString
            );
    }

    /**
     * Builds a connection string based on the database engine.
     *
     * @param engine Database engine name (MySQL, PostgreSQL, etc.)
     * @param host Host/IP address
     * @param port Port number
     * @param dbName Database name
     * @return Formatted connection string
     */
    private String buildConnectionString(String engine, String host, Integer port, String dbName) {
        return switch (engine.toLowerCase()) {
            case "mysql" ->
                String.format("jdbc:mysql://%s:%d/%s", host, port, dbName);
            case "postgresql", "postgres" ->
                String.format("jdbc:postgresql://%s:%d/%s", host, port, dbName);
            case "mariadb" ->
                String.format("jdbc:mariadb://%s:%d/%s", host, port, dbName);
            case "mongodb" ->
                String.format("mongodb://%s:%d/%s", host, port, dbName);
            default ->
                String.format("%s://%s:%d/%s", engine, host, port, dbName);
        };
    }

    /**
     * Sends a plan purchase confirmation email to the user.
     *
     * This method is called after a payment is successfully approved.
     * It sends an HTML email with the plan details and next steps.
     *
     * @param userEmail The email address of the user who purchased the plan
     * @param planName The name of the plan purchased
     * @param amount The amount paid
     * @param maxInstances The maximum number of instances allowed in the plan
     * @throws RuntimeException if the email fails to send
     */
    public void sendPlanPurchaseConfirmation(String userEmail, String planName, java.math.BigDecimal amount, Integer maxInstances) {
        try {
            log.info("Preparing to send plan purchase confirmation email to: {}", userEmail);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(userEmail);
            helper.setSubject("✅ Plan Purchased Successfully - Nebula Cloud");

            String htmlContent = buildPlanPurchaseEmailHtml(planName, amount, maxInstances);
            helper.setText(htmlContent, true);

            mailSender.send(message);

            log.info("Plan purchase confirmation email sent successfully to: {}", userEmail);

        } catch (MessagingException e) {
            log.error("Failed to send plan purchase confirmation email to: {}. Error: {}", userEmail, e.getMessage());
            throw new RuntimeException("Failed to send plan purchase email: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error while sending plan purchase email to: {}. Error: {}", userEmail, e.getMessage());
            throw new RuntimeException("Unexpected error sending plan purchase email: " + e.getMessage(), e);
        }
    }

    /**
     * Builds the HTML content for the plan purchase confirmation email.
     *
     * @param planName The name of the plan
     * @param amount The amount paid
     * @param maxInstances The maximum number of instances
     * @return HTML string with formatted plan information
     */
    private String buildPlanPurchaseEmailHtml(String planName, java.math.BigDecimal amount, Integer maxInstances) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        max-width: 600px;
                        margin: 0 auto;
                        padding: 20px;
                    }
                    .header {
                        background: linear-gradient(135deg, #10b981 0%%, #059669 100%%);
                        color: white;
                        padding: 30px;
                        border-radius: 10px 10px 0 0;
                        text-align: center;
                    }
                    .content {
                        background: #f9f9f9;
                        padding: 30px;
                        border-radius: 0 0 10px 10px;
                    }
                    .plan-box {
                        background: white;
                        border: 2px solid #10b981;
                        border-radius: 8px;
                        padding: 25px;
                        margin: 20px 0;
                        text-align: center;
                    }
                    .plan-name {
                        font-size: 28px;
                        font-weight: bold;
                        color: #10b981;
                        margin-bottom: 10px;
                    }
                    .plan-price {
                        font-size: 24px;
                        font-weight: bold;
                        color: #333;
                        margin: 10px 0;
                    }
                    .plan-details {
                        background: #f0fdf4;
                        border-left: 4px solid #10b981;
                        padding: 15px;
                        margin: 20px 0;
                        border-radius: 5px;
                    }
                    .benefit-item {
                        margin: 10px 0;
                        padding: 8px 0;
                        border-bottom: 1px solid #d1fae5;
                    }
                    .benefit-item:last-child {
                        border-bottom: none;
                    }
                    .benefit-label {
                        font-weight: bold;
                        color: #10b981;
                    }
                    .next-steps {
                        background: #e0f2fe;
                        border-left: 4px solid #0284c7;
                        padding: 15px;
                        margin: 20px 0;
                        border-radius: 5px;
                    }
                    .next-steps h3 {
                        color: #0284c7;
                        margin-top: 0;
                    }
                    .cta-button {
                        display: inline-block;
                        background: #10b981;
                        color: white;
                        padding: 12px 30px;
                        text-decoration: none;
                        border-radius: 5px;
                        margin: 20px 0;
                        font-weight: bold;
                    }
                    .footer {
                        text-align: center;
                        margin-top: 30px;
                        padding-top: 20px;
                        border-top: 1px solid #ddd;
                        color: #666;
                        font-size: 12px;
                    }
                    .checkmark {
                        color: #10b981;
                        font-weight: bold;
                    }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>✅ Purchase Successful!</h1>
                    <p>Your plan has been activated and is ready to use</p>
                </div>
                
                <div class="content">
                    <p>Thank you for choosing Nebula Cloud! Your new plan is now active.</p>
                    
                    <div class="plan-box">
                        <div class="plan-name">%s Plan</div>
                        <div class="plan-price">$%.2f</div>
                    </div>
                    
                    <div class="plan-details">
                        <div class="benefit-item">
                            <span class="benefit-label"><span class="checkmark">✓</span> Maximum Instances:</span>
                            <span>%d database instances</span>
                        </div>
                        <div class="benefit-item">
                            <span class="benefit-label"><span class="checkmark">✓</span> Plan Status:</span>
                            <span>Active and ready to use</span>
                        </div>
                        <div class="benefit-item">
                            <span class="benefit-label"><span class="checkmark">✓</span> Full Support:</span>
                            <span>Access to all features</span>
                        </div>
                    </div>
                    
                    <div class="next-steps">
                        <h3>🚀 Next Steps</h3>
                        <ol>
                            <li>Log in to your Nebula Cloud dashboard</li>
                            <li>Navigate to "Database Instances" to create your first database</li>
                            <li>Choose your preferred database engine (MySQL, PostgreSQL, etc.)</li>
                            <li>Receive connection credentials via email</li>
                            <li>Start using your database!</li>
                        </ol>
                    </div>
                    
                    <div style="text-align: center;">
                        <a href="https://flat-white.crudzaso.com/dashboard" class="cta-button">Go to Dashboard</a>
                    </div>
                    
                    <h3>Questions or Need Help?</h3>
                </div>
                
                <div class="footer">
                    <p>This is an automated message from Nebula Cloud</p>
                    <p>&copy; 2025 Nebula Cloud. All rights reserved.</p>
                </div>
            </body>
            </html>
            """.formatted(planName, amount, maxInstances);
    }
}

