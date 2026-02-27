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


        List<Contact> contactsToUpdate = new ArrayList<>();

        for(Contact primary : primaryCandidates) {
            if(!primary.getId().equals(finalPrimary.getId())) {
                List<Contact> secondaries = contactRepository.findByLinkedId(primary.getId());

                for (Contact secondary : secondaries) {
                    secondary.setLinkedId(finalPrimary.getId());
                    contactsToUpdate.add(secondary);
                }

                primary.setLinkPrecedence(LinkPrecedence.SECONDARY);
                primary.setLinkedId(finalPrimary.getId());
                contactsToUpdate.add(primary);
            }
        }

        if(!contactsToUpdate.isEmpty()) {
            contactRepository.saveAll(contactsToUpdate);
        }

        List<Contact> cluster = contactRepository.findByLinkedId(finalPrimary.getId());

        Set<String> existingEmails = new HashSet<>();
        Set<String> existingPhones = new HashSet<>();

        existingEmails.add(finalPrimary.getEmail());
        existingPhones.add(finalPrimary.getPhoneNumber());

        for(Contact contact : cluster) {
            if(contact.getEmail() != null) {
                existingEmails.add(contact.getEmail());
            }
            if(contact.getPhoneNumber() != null) {
                existingPhones.add(contact.getPhoneNumber());
            }
        }

        boolean isNewEmail = request.getEmail() != null
                && !existingEmails.contains(request.getEmail());
        boolean isNewPhone = request.getPhoneNumber() != null
                && !existingPhones.contains(request.getPhoneNumber());

        if(isNewEmail || isNewPhone) {
            Contact newSecondary = new Contact();

            newSecondary.setEmail(request.getEmail());
            newSecondary.setPhoneNumber(request.getPhoneNumber());
            newSecondary.setLinkPrecedence(LinkPrecedence.SECONDARY);
            newSecondary.setLinkedId(finalPrimary.getId());

            contactRepository.save(newSecondary);
        }

        List<Contact> finalSecondaries =
                contactRepository.findByLinkedId(finalPrimary.getId());

        LinkedHashSet<String> emails = new LinkedHashSet<>();
        LinkedHashSet<String> phones = new LinkedHashSet<>();

        if(finalPrimary.getEmail() != null) {
            emails.add(finalPrimary.getEmail());
        }

        if(finalPrimary.getPhoneNumber() != null) {
            phones.add(finalPrimary.getPhoneNumber());
        }

        for(Contact contact : finalSecondaries) {
            if(!contact.getId().equals(finalPrimary.getId()) && contact.getEmail() != null) {
                emails.add(contact.getEmail());
            }
            if(!contact.getId().equals(finalPrimary.getId()) && contact.getPhoneNumber() != null) {
                phones.add(contact.getPhoneNumber());
            }
        }

        List<Long> secondaryIds = finalSecondaries
                .stream()
                .map(Contact::getId)
                .toList();

        IdentityResponse.ContactDetails details =
                IdentityResponse.ContactDetails.builder()
                        .primaryContatctId(finalPrimary.getId())
                        .emails(new ArrayList<>(emails))
                        .phoneNumbers(new ArrayList<>(phones))
                        .secondaryContactIds(secondaryIds)
                        .build();

        return IdentityResponse.builder()
                .contact(details)
                .build();
    }
}
