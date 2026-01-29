package com.commontools.server.repository;

import com.commontools.server.model.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
    List<Contact> findByEmail(String email);
    List<Contact> findByEmailSent(Boolean emailSent);
    List<Contact> findAllByOrderByCreatedAtDesc();
}
