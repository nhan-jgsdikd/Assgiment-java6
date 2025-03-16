package java6.assgiment.DAO;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import java6.assgiment.Entity.Banner;


public interface BannerDAO extends JpaRepository<Banner, Long> {



}
