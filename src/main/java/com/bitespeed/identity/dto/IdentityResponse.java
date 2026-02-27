package com.bitespeed.identity.dto;

import lombok.Data;

import java.util.List;

@Data
public class IdentityResponse {
    private Integer primaryContatctId;
    private List<String> emails;
    private List<String> phoneNumbers;
    private List<Integer> secondaryContactIds;
}
