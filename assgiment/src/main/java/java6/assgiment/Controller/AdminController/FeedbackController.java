package java6.assgiment.Controller.AdminController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java6.assgiment.DAO.CommentDAO;
import java6.assgiment.DAO.ContactDAO;
import java6.assgiment.Entity.Contact;
import java6.assgiment.Entity.Comment;


import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/contact")
public class FeedbackController {

    @Autowired
    private ContactDAO contactDAO;

    @Autowired
    private CommentDAO commentRepository;

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
            return ResponseEntity.ok("Phản hồi đã được lưu thành công!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi lưu phản hồi: " + e.getMessage());
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
            return ResponseEntity.ok("Phản hồi bình luận đã được lưu thành công!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi lưu phản hồi bình luận: " + e.getMessage());
        }
    }
}