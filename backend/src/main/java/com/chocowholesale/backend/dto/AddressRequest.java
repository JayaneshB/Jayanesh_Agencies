package com.chocowholesale.backend.dto;

public record AddressRequest(
    String line1, String line2, String city,
    String state, String pincode, String country, Boolean isDefault
) {}
