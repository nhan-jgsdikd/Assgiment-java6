package java6.assgiment.Controller.PageController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java6.assgiment.DAO.ContactDAO;
import java6.assgiment.Entity.Contact;

import java.util.logging.Logger;

@Controller
public class ContactController {

    private static final Logger logger = Logger.getLogger(ContactController.class.getName());

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private ContactDAO contactDAO;

    @GetMapping("/contact")
    public String showContactForm(Model model) {
        model.addAttribute("contact", new Contact());
        return "Dashboard/Contact";
    }

    @PostMapping("/contact/submit")
    public String submitContactForm(@ModelAttribute("contact") Contact contact, Model model) {
        try {
            contactDAO.save(contact);
            logger.info("Đã lưu liên hệ vào database: " + contact.getEmail());
    
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("animehisone@gmail.com");
            message.setTo(contact.getEmail());
            message.setSubject("Xác nhận tự động: Tin nhắn của bạn đã được ghi nhận");
            message.setText(
                "Kính gửi " + contact.getUsername() + ",\n\n" +
                "Cảm ơn bạn đã liên hệ với chúng tôi. Đây là hệ thống trả lời tự động để xác nhận rằng tin nhắn của bạn đã được hệ thống ghi nhận thành công.\n\n" +
                "Thông tin bạn gửi:\n" +
                "- Tên đăng nhập: " + contact.getUsername() + "\n" +
                "- Số điện thoại: " + contact.getPhone() + "\n" +
                "- Nội dung: " + contact.getMessage() + "\n\n" +
                "Chúng tôi sẽ xem xét và phản hồi bạn trong thời gian sớm nhất. Nếu cần hỗ trợ ngay lập tức, vui lòng gọi hotline: +84 971 305 479.\n\n" +
                "Trân trọng,\n" +
                "Hệ thống tự động - Team Nhóm 7 - JAVA6\n" +
                "[Email này được gửi tự động, vui lòng không trả lời trực tiếp.]"
            );
            logger.info("Chuẩn bị gửi email tới: " + contact.getEmail());
            mailSender.send(message);
            logger.info("Email đã gửi thành công tới: " + contact.getEmail());
    
            model.addAttribute("successMessage", "Tin nhắn của bạn đã được gửi thành công!");
        } catch (Exception e) {
            logger.severe("Lỗi khi gửi tin nhắn: " + e.getMessage());
            model.addAttribute("errorMessage", "Có lỗi xảy ra khi gửi tin nhắn: " + e.getMessage());
        }
        return "Dashboard/Contact";
    }
}