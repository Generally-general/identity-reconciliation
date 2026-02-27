package com.bitespeed.identity.controller;

import com.bitespeed.identity.dto.IdentifyRequest;
import com.bitespeed.identity.dto.IdentityResponse;
import com.bitespeed.identity.service.ContactService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IdentityController {
    private final ContactService contactService;

    public IdentityController(ContactService contactService) {
        this.contactService = contactService;
    }

    @PostMapping("/identify")
    public ResponseEntity<IdentityResponse> identify(@Valid @RequestBody IdentifyRequest request) {
        if(request.getEmail() == null && request.getPhoneNumber() == null) {
            return ResponseEntity.badRequest().build();
        }

        IdentityResponse response = contactService.identifyContact(request);
        return ResponseEntity.ok(response);
    }
}
