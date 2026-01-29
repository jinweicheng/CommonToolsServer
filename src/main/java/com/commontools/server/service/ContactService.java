package com.commontools.server.service;

import com.commontools.server.dto.ContactRequest;
import com.commontools.server.model.Contact;
import com.commontools.server.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContactService {
    private final ContactRepository contactRepository;
    private final EmailService emailService;

    @Transactional
    public Contact createContact(ContactRequest request) {
        // 创建联系记录
        Contact contact = new Contact();
        contact.setName(request.getName());
        contact.setEmail(request.getEmail());
        contact.setSubject(request.getSubject());
        contact.setMessage(request.getMessage());
        contact.setEmailSent(false);

        // 先保存到数据库
        Contact savedContact = contactRepository.save(contact);
        log.info("联系表单记录已保存 - ID: {}, 邮箱: {}", savedContact.getId(), savedContact.getEmail());

        // 尝试发送邮件
        try {
            // 发送邮件给管理员
            emailService.sendContactEmailToAdmin(
                    request.getName(),
                    request.getEmail(),
                    request.getSubject(),
                    request.getMessage()
            );

            // 发送确认邮件给用户
            emailService.sendContactConfirmation(
                    request.getEmail(),
                    request.getName(),
                    request.getSubject()
            );

            // 更新邮件发送状态
            savedContact.setEmailSent(true);
            savedContact.setErrorMessage(null);
            savedContact = contactRepository.save(savedContact);
            log.info("联系表单邮件发送成功 - ID: {}, 邮箱: {}", savedContact.getId(), savedContact.getEmail());
        } catch (Exception e) {
            // 记录错误但不影响数据保存
            log.error("联系表单邮件发送失败 - ID: {}, 邮箱: {}, 错误: {}", 
                    savedContact.getId(), savedContact.getEmail(), e.getMessage(), e);
            savedContact.setEmailSent(false);
            savedContact.setErrorMessage(e.getMessage());
            savedContact = contactRepository.save(savedContact);
        }

        return savedContact;
    }

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
