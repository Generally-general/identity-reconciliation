package com.bitespeed.identity.service;

import com.bitespeed.identity.dto.IdentifyRequest;
import com.bitespeed.identity.dto.IdentityResponse;
import com.bitespeed.identity.entity.Contact;
import com.bitespeed.identity.repository.ContactRepository;
import com.bitespeed.identity.utils.LinkPrecedence;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Transactional
public class ContactService {
    private final ContactRepository contactRepository;

    public ContactService(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    public IdentityResponse identifyContact(IdentifyRequest request) {
        List<Contact> matches = contactRepository.findMatchingContacts(
                request.getEmail(),
                request.getPhoneNumber()
        );

        if(matches.isEmpty()) {
            Contact newContact = new Contact();
            newContact.setEmail(request.getEmail());
            newContact.setPhoneNumber(request.getPhoneNumber());
            newContact.setLinkPrecedence(LinkPrecedence.PRIMARY);
            newContact.setLinkedId(null);

            Contact saved = contactRepository.save(newContact);

            IdentityResponse.ContactDetails details = IdentityResponse.ContactDetails.builder()
                    .primaryContatctId(saved.getId())
                    .emails(saved.getEmail() != null ? List.of(saved.getEmail()) : Collections.emptyList())
                    .phoneNumbers(saved.getPhoneNumber() != null ? List.of(saved.getPhoneNumber()) : Collections.emptyList())
                    .secondaryContactIds(Collections.emptyList())
                    .build();

            return IdentityResponse.builder()
                    .contact(details)
                    .build();
        }

        Set<Long> fetchedPrimaryIds = new HashSet<>();
        Set<Contact> primaryCandidates = new HashSet<>();

        for(Contact contact : matches) {
            if(contact.getLinkPrecedence() == LinkPrecedence.PRIMARY) {
                primaryCandidates.add(contact);
            } else if(contact.getLinkedId() != null) {
                fetchedPrimaryIds.add(contact.getLinkedId());
            }
        }

        if(!fetchedPrimaryIds.isEmpty()) {
            List<Contact> fetchedPrimaries = contactRepository.findAllById(fetchedPrimaryIds);
            primaryCandidates.addAll(fetchedPrimaries);
        }

        Contact finalPrimary = primaryCandidates
                .stream()
                .min(Comparator.comparing(Contact::getCreatedAt))
                .orElseThrow();
    }
}
