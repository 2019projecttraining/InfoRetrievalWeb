package ir.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ir.models.Patent;

@Repository
public interface PatentRepositoty extends JpaRepository<Patent, String>{}
