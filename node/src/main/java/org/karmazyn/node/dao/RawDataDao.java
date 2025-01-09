package org.karmazyn.node.dao;

import org.karmazyn.node.entity.RawData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RawDataDao extends JpaRepository<RawData, Long> {
}
