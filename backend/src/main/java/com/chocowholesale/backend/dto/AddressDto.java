package com.chocowholesale.backend.dto;

import com.chocowholesale.backend.entity.Address;
import java.util.UUID;

public record AddressDto(
    UUID id, String line1, String line2, String city,
    String state, String pincode, String country, Boolean isDefault
) {
    public static AddressDto from(Address a) {
        return new AddressDto(a.getId(), a.getLine1(), a.getLine2(), a.getCity(),
                a.getState(), a.getPincode(), a.getCountry(), a.getIsDefault());
    }
}
