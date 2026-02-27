package com.bitespeed.identity.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
public class IdentityResponse {
    private ContactDetails contact;

    @Data
    @Builder
    public static class ContactDetails {
        private Long primaryContatctId;
        private List<String> emails;
        private List<String> phoneNumbers;
        private List<Integer> secondaryContactIds;
    }
}
