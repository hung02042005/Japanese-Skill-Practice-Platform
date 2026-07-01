import org.springframework.mail.javamail.JavaMailSenderImpl;

public class TestSmtp {
    public static void main(String[] args) throws Exception {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);
        mailSender.setUsername("jlptelearningplatform@gmail.com");
        mailSender.setPassword("xejawuigzodzhblc");
        
        java.util.Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        
        System.out.println("Testing connection...");
        try {
            mailSender.testConnection();
            System.out.println("Success!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
