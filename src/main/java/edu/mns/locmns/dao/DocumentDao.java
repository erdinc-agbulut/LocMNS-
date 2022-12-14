package edu.mns.locmns.dao;

import edu.mns.locmns.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentDao extends JpaRepository<Document, Integer> {
}
