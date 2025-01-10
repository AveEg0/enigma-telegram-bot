package org.karmazyn.jpa.dao;

import org.karmazyn.jpa.entity.AppPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppPhotoDao extends JpaRepository<AppPhoto, Long> {
}
