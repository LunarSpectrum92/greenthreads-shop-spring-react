package com.Konopka.eCommerce.repositories;

import com.Konopka.eCommerce.models.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface ClientRepository extends JpaRepository<Client, Integer> {

    Optional<Client> findByKeycloakId(String keycloakId);

    Optional<Client> findByAddressAddressId(int addressId);

}
