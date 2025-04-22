package java6.assgiment.Controller.AdminController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java6.assgiment.DAO.CommentDAO;
import java6.assgiment.DAO.ContactDAO;
import java6.assgiment.Entity.Contact;
import java6.assgiment.Entity.Comment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

@Controller
@RequestMapping("/admin/contact")
public class FeedbackController {

    private static final Logger logger = Logger.getLogger(FeedbackController.class.getName());

    @Autowired
    private ContactDAO contactDAO;

    @Autowired
    private CommentDAO commentRepository;

    @Autowired
    private JavaMailSender mailSender;

    @GetMapping
    public String showFeedbackList(Model model) {
        List<Contact> contacts = contactDAO.findAll();
        List<Comment> comments = commentRepository.findAll();
        model.addAttribute("contacts", contacts);
        model.addAttribute("comments", comments);
        return "Admin/Feedback";
    }

    @PostMapping("/reply/{id}")
    @ResponseBody
    public ResponseEntity<String> replyContact(@PathVariable Long id, @RequestBody String response) {
        try {
            Contact contact = contactDAO.findById(id).orElseThrow(() -> new IllegalArgumentException("Contact not found"));
            contact.setResponse(response);
            contact.setRespondedAt(LocalDateTime.now());
            contactDAO.save(contact);
            logger.info("Đã lưu phản hồi cho liên hệ ID: " + id);

            // Send email to the customer
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("animehisone@gmail.com");
            message.setTo(contact.getEmail());
            message.setSubject("Phản hồi từ Team Nhóm 7 - JAVA6");
            message.setText(
                "Kính gửi " + contact.getUsername() + ",\n\n" +
                "Cảm ơn bạn đã liên hệ với chúng tôi. Dưới đây là phản hồi từ đội ngũ quản trị:\n\n" +
                "Nội dung phản hồi: " + response + "\n\n" +
                "Thông tin bạn đã gửi trước đó:\n" +
                "- Tên đăng nhập: " + contact.getUsername() + "\n" +
                "- Số điện thoại: " + contact.getPhone() + "\n" +
                "- Nội dung: " + contact.getMessage() + "\n\n" +
                "Nếu bạn cần thêm hỗ trợ, vui lòng trả lời email này hoặc gọi hotline: +84 971 305 479.\n\n" +
                "Trân trọng,\n" +
                "Team Nhóm 7 - JAVA6"
            );
            logger.info("Chuẩn bị gửi email phản hồi tới: " + contact.getEmail());
            mailSender.send(message);
            logger.info("Email phản hồi đã gửi thành công tới: " + contact.getEmail());

            return ResponseEntity.ok("Phản hồi đã được lưu và gửi email thành công!");
        } catch (Exception e) {
            logger.severe("Lỗi khi xử lý phản hồi liên hệ ID " + id + ": " + e.getMessage());
            return ResponseEntity.badRequest().body("Lỗi khi lưu hoặc gửi email phản hồi: " + e.getMessage());
        }
    }

    @PostMapping("/reply-comment/{id}")
    @ResponseBody
    public ResponseEntity<String> replyComment(@PathVariable Long id, @RequestBody String response) {
        try {
            Comment comment = commentRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Comment not found"));
            comment.setResponse(response);
            comment.setRespondedAt(LocalDateTime.now());
            commentRepository.save(comment);
            logger.info("Đã lưu phản hồi cho bình luận ID: " + id);

            // Note: Email sending for comments is skipped unless Comment entity has an email field.
            // If Comment has an email field (e.g., comment.getEmail()), uncomment and adjust the following:
            /*
            if (comment.getEmail() != null && !comment.getEmail().isEmpty()) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom("animehisone@gmail.com");
                message.setTo(comment.getEmail());
                message.setSubject("Phản hồi bình luận từ Team Nhóm 7 - JAVA6");
                message.setText(
                    "Kính gửi bạn,\n\n" +
                    "Cảm ơn bạn đã để lại bình luận. Dưới đây là phản hồi từ đội ngũ quản trị:\n\n" +
                    "Nội dung phản hồi: " + response + "\n\n" +
                    "Bình luận của bạn: " + comment.getContent() + "\n\n" +
                    "Nếu bạn cần thêm hỗ trợ, vui lòng trả lời email này hoặc gọi hotline: +84 971 305 479.\n\n" +
                    "Trân trọng,\n" +
                    "Team Nhóm 7 - JAVA6"
                );
                logger.info("Chuẩn bị gửi email phản hồi bình luận tới: " + comment.getEmail());
                mailSender.send(message);
                logger.info("Email phản hồi bình luận đã gửi thành công tới: " + comment.getEmail());
            }
            */

            return ResponseEntity.ok("Phản hồi bình luận đã được lưu thành công!");
        } catch (Exception e) {
            logger.severe("Lỗi khi xử lý phản hồi bình luận ID " + id + ": " + e.getMessage());
            return ResponseEntity.badRequest().body("Lỗi khi lưu phản hồi bình luận: " + e.getMessage());
        }
    }
}