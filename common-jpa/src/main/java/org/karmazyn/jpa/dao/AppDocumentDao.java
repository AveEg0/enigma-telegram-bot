package org.karmazyn.jpa.dao;

import org.karmazyn.jpa.entity.AppDocument;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppDocumentDao extends JpaRepository<AppDocument, Long> {
}
