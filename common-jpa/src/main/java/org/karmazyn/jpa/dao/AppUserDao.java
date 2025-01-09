package org.karmazyn.jpa.dao;

import org.karmazyn.jpa.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserDao extends JpaRepository<AppUser, Long> {
    AppUser findAppUsersByTelegramUserId(long telegramUserId);
}
