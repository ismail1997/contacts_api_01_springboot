package io.ismail.contactapi.repository;

import io.ismail.contactapi.domain.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IContactRepository extends JpaRepository<Contact,String> {
    Optional<Contact> findById(String id);
}
