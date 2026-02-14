package com.commontools.server.service;

import com.commontools.server.dto.ContactRequest;
import com.commontools.server.model.Contact;
import com.commontools.server.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Deprecated: contact submissions are replaced by feedbacks.
 * This service now delegates to FeedbackService to persist submissions into feedbacks table.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Deprecated
public class ContactService {
    private final ContactRepository contactRepository;
    private final EmailService emailService;
    private final FeedbackService feedbackService;

    @Transactional
    public com.commontools.server.model.Feedback createContact(ContactRequest request) {
        // Delegate to FeedbackService to store into feedbacks
        return feedbackService.createFeedbackFromContact(request);
    }

    // Deprecated passthroughs (read-only access to legacy contacts table)
    public Contact getContactById(Long id) {
        return contactRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("联系记录不存在"));
    }

    public List<Contact> getAllContacts() {
        return contactRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Contact> getContactsByEmail(String email) {
        return contactRepository.findByEmail(email);
    }
}
