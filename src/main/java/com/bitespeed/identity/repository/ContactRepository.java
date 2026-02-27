package com.bitespeed.identity.repository;

import com.bitespeed.identity.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {

    @Query("""
        SELECT c FROM Contact c
        WHERE 
            (:email IS NOT NULL AND c.email = :email)
                OR 
            (:phoneNumber IS NOT NULL AND c.phoneNumber = :phoneNumber)
    """)
    List<Contact> findMatchingContacts(
            @Param("email") String email,
            @Param("phoneNumber") String phoneNumber
    );

    List<Contact> findByLinkedId(Long linkedId);
}
