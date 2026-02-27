package com.bitespeed.identity.service;

import com.bitespeed.identity.dto.IdentifyRequest;
import com.bitespeed.identity.dto.IdentityResponse;
import com.bitespeed.identity.repository.ContactRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@Transactional
public class ContactService {
    private final ContactRepository contactRepository;

    public ContactService(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    public IdentityResponse identifyContact(IdentifyRequest request) {
        IdentityResponse.ContactDetails details = IdentityResponse.ContactDetails.builder()
                .primaryContatctId(1)
                .emails(List.of("placeholder@test.com"))
                .phoneNumbers(List.of("12345"))
                .secondaryContactIds(Collections.emptyList())
                .build();

        IdentityResponse response = new IdentityResponse();
        response.setContact(details);
        return response;
    }
}
