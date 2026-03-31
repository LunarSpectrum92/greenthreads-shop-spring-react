package com.Konopka.eCommerce.DTO;

import jakarta.validation.constraints.Pattern;

public record AddressDTO(
    int addressId,
    String country,
    String city,
    String street,
    String houseNumber,
    String flatNumber,
    String postalCode
) {}