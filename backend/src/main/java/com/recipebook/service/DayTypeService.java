package com.recipebook.service;

import com.recipebook.entity.DayType;
import com.recipebook.entity.User;
import com.recipebook.entity.UserDayType;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class DayTypeService {

    private static final Logger LOG = Logger.getLogger(DayTypeService.class);

    public DayType getDayType(Long userId, LocalDate date) {
        UserDayType entry = UserDayType.find("user.id = ?1 and date = ?2", userId, date).firstResult();
        if (entry == null) {
            return DayType.DEFAULT;
        }
        return entry.dayType;
    }

    public DayType setDayType(Long userId, LocalDate date, DayType dayType) {
        LOG.debugf("setDayType userId=%d, date=%s, dayType=%s", userId, date, dayType);
        UserDayType entry = UserDayType.find("user.id = ?1 and date = ?2", userId, date).firstResult();

        if (dayType == DayType.DEFAULT) {
            // No need to store DEFAULT — just delete any existing row
            if (entry != null) {
                entry.delete();
            }
            return DayType.DEFAULT;
        }

        if (entry == null) {
            entry = new UserDayType();
            entry.user = User.findById(userId);
            entry.date = date;
        }
        entry.dayType = dayType;
        entry.persist();
        return entry.dayType;
    }

    public Map<LocalDate, DayType> getDayTypesForWeek(Long userId, LocalDate weekStart) {
        LocalDate weekEnd = weekStart.plusDays(6);
        List<UserDayType> entries = UserDayType.find(
                "user.id = ?1 and date >= ?2 and date <= ?3", userId, weekStart, weekEnd).list();

        Map<LocalDate, DayType> result = new HashMap<>();
        for (UserDayType entry : entries) {
            result.put(entry.date, entry.dayType);
        }
        return result;
    }
}
