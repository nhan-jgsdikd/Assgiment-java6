package java6.assgiment.DAO;

import org.springframework.data.jpa.repository.JpaRepository;

import java6.assgiment.Entity.Contact;



public interface ContactDAO extends JpaRepository<Contact, Long> {

}