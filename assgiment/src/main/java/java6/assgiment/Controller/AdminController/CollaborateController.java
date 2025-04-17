package java6.assgiment.Controller.AdminController;

import java6.assgiment.DAO.WorkEventDAO;
import java6.assgiment.Entity.User;
import java6.assgiment.Entity.WorkEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/Collaborate")
public class CollaborateController {

    private static final Logger logger = LoggerFactory.getLogger(CollaborateController.class);

    @Autowired
    private WorkEventDAO workEventDAO;

    @Autowired
    private HttpSession session;

    /**
     * Render the Collaborate page with upcoming and past events.
     */
    @GetMapping
    public String showSchedule(Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            logger.info("User not logged in, redirecting to login");
            return "redirect:/login";
        }

        LocalDateTime now = LocalDateTime.now();
        model.addAttribute("user", loggedInUser);
        model.addAttribute("upcomingEvents", workEventDAO.findByIsDeletedFalseAndStartDateAfter(now));
        model.addAttribute("pastEvents", workEventDAO.findByIsDeletedFalseAndStartDateBefore(now));
        logger.debug("Loaded {} upcoming and {} past events", 
            workEventDAO.findByIsDeletedFalseAndStartDateAfter(now).size(),
            workEventDAO.findByIsDeletedFalseAndStartDateBefore(now).size());
        return "Admin/Collaborate";
    }

    /**
     * Provide events for FullCalendar in JSON format.
     */
    @GetMapping("/events")
    @ResponseBody
    public List<Map<String, Object>> getCalendarEvents() {
        try {
            List<Map<String, Object>> events = workEventDAO.findByIsDeletedFalse().stream()
                .filter(event -> event.getStartDate() != null)
                .map(event -> {
                    try {
                        return Map.<String, Object>of(
                            "id", event.getId().toString(),
                            "title", event.getTitle() != null ? event.getTitle() : "",
                            "start", event.getStartDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                            "location", event.getLocation() != null ? event.getLocation() : "",
                            "description", event.getDescription() != null ? event.getDescription() : "",
                            "status", event.getStatus() != null ? event.getStatus().name() : "PENDING",
                            "className", switch (event.getStatus()) {
                                case PENDING -> "fc-event-pending";
                                case COMPLETED -> "fc-event-completed";
                                case CANCELLED -> "fc-event-cancelled";
                            }
                        );
                    } catch (Exception e) {
                        logger.error("Error mapping event ID {}: {}", event.getId(), e.getMessage());
                        return null;
                    }
                })
                .filter(map -> map != null)
                .collect(Collectors.toList());

            logger.debug("Returning {} events for FullCalendar", events.size());
            return events;
        } catch (Exception e) {
            logger.error("Error fetching calendar events: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * Save or update an event.
     */
    @PostMapping("/save")
    public String saveEvent(@Valid @ModelAttribute WorkEvent event, BindingResult result, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            logger.info("User not logged in, redirecting to login");
            return "redirect:/login";
        }

        if (result.hasErrors()) {
            model.addAttribute("errorMessage", "Vui lòng kiểm tra lại dữ liệu nhập vào.");
            LocalDateTime now = LocalDateTime.now();
            model.addAttribute("user", loggedInUser);
            model.addAttribute("upcomingEvents", workEventDAO.findByIsDeletedFalseAndStartDateAfter(now));
            model.addAttribute("pastEvents", workEventDAO.findByIsDeletedFalseAndStartDateBefore(now));
            logger.warn("Validation errors in event form: {}", result.getAllErrors());
            return "Admin/Collaborate";
        }

        try {
            if (event.getId() == null) {
                event.setIsDeleted(false);
                event.setStatus(WorkEvent.EventStatus.PENDING);
                logger.info("Creating new event: {}", event.getTitle());
            } else {
                Optional<WorkEvent> existing = workEventDAO.findById(event.getId());
                if (existing.isPresent()) {
                    WorkEvent updated = existing.get();
                    updated.setTitle(event.getTitle());
                    updated.setStartDate(event.getStartDate());
                    updated.setLocation(event.getLocation());
                    updated.setDescription(event.getDescription());
                    updated.setStatus(event.getStatus());
                    event = updated;
                    logger.info("Updating event ID {}: {}", event.getId(), event.getTitle());
                } else {
                    model.addAttribute("errorMessage", "Sự kiện không tồn tại.");
                    logger.warn("Event ID {} not found for update", event.getId());
                    return "Admin/Collaborate";
                }
            }
            workEventDAO.save(event);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi khi lưu sự kiện: " + e.getMessage());
            LocalDateTime now = LocalDateTime.now();
            model.addAttribute("user", loggedInUser);
            model.addAttribute("upcomingEvents", workEventDAO.findByIsDeletedFalseAndStartDateAfter(now));
            model.addAttribute("pastEvents", workEventDAO.findByIsDeletedFalseAndStartDateBefore(now));
            logger.error("Error saving event: {}", e.getMessage());
            return "Admin/Collaborate";
        }

        return "redirect:/Collaborate";
    }

    /**
     * Fetch an event by ID for editing.
     */
    @GetMapping("/get/{id}")
    @ResponseBody
    public ResponseEntity<WorkEvent> getEvent(@PathVariable Long id) {
        Optional<WorkEvent> event = workEventDAO.findById(id);
        if (event.isPresent() && !event.get().getIsDeleted()) {
            logger.debug("Fetched event ID {}", id);
            return ResponseEntity.ok(event.get());
        }
        logger.warn("Event ID {} not found or deleted", id);
        return ResponseEntity.notFound().build();
    }

    /**
     * Soft-delete an event by ID.
     */
    @DeleteMapping("/delete/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        Optional<WorkEvent> event = workEventDAO.findById(id);
        if (event.isPresent() && !event.get().getIsDeleted()) {
            WorkEvent toDelete = event.get();
            toDelete.setIsDeleted(true);
            workEventDAO.save(toDelete);
            logger.info("Soft-deleted event ID {}", id);
            return ResponseEntity.ok().build();
        }
        logger.warn("Event ID {} not found or already deleted", id);
        return ResponseEntity.notFound().build();
    }
}