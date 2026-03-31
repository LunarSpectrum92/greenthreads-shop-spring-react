package com.Konopka.eCommerce.DTO;


import com.Konopka.eCommerce.models.Address;

public record ClientRequest(
        String phone,
        Address address
) {
}

