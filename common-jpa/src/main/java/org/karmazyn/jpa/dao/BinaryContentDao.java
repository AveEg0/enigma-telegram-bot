package org.karmazyn.jpa.dao;

import org.karmazyn.jpa.entity.BinaryContent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BinaryContentDao extends JpaRepository<BinaryContent, Long> {
}
