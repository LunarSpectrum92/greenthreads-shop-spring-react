package com.Konopka.eCommerce.services;

import com.Konopka.eCommerce.models.Address;
import com.Konopka.eCommerce.models.Client;
import com.Konopka.eCommerce.repositories.AddressRepository;
import com.Konopka.eCommerce.repositories.ClientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Optional;


@Service
public class AddressService {

    private final AddressRepository addressRepository;
    private final ClientRepository clientRepository;

    public AddressService(AddressRepository addressRepository, ClientRepository clientRepository) {
        this.addressRepository = addressRepository;
        this.clientRepository = clientRepository;
    }


    public ResponseEntity<Address> updateAddress(Address address, Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_Admin"));

        String keycloakId = authentication.getName();

        if (!isAdmin) {
            Optional<Client> clientOpt = clientRepository.findByKeycloakId(keycloakId);

            if (clientOpt.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }

            Client currentClient = clientOpt.get();

            if (currentClient.getAddress() == null ||
                    currentClient.getAddress().getAddressId() != address.getAddressId()) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        }

        if (!addressRepository.existsById(address.getAddressId())) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(addressRepository.save(address), HttpStatus.OK);
    }


}
