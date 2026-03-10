package com.recipebook.service;

import com.recipebook.dto.UserNutritionTargetResponse;
import com.recipebook.entity.DayType;
import com.recipebook.entity.User;
import com.recipebook.entity.UserNutritionTarget;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class NutritionTargetService {

    private static final Logger LOG = Logger.getLogger(NutritionTargetService.class);

    @Inject
    DayTypeService dayTypeService;

    /** Backward-compatible: returns DEFAULT target. */
    public UserNutritionTargetResponse getTarget(Long userId) {
        return getTarget(userId, DayType.DEFAULT);
    }

    /** Get target for a specific day type, falling back to DEFAULT if not found. */
    public UserNutritionTargetResponse getTarget(Long userId, DayType dayType) {
        LOG.debugf("getTarget userId=%d, dayType=%s", userId, dayType);
        UserNutritionTarget target = UserNutritionTarget.find(
                "user.id = ?1 and dayType = ?2", userId, dayType).firstResult();
        if (target == null && dayType != DayType.DEFAULT) {
            // Fallback to DEFAULT
            target = UserNutritionTarget.find(
                    "user.id = ?1 and dayType = ?2", userId, DayType.DEFAULT).firstResult();
        }
        if (target == null) {
            return new UserNutritionTargetResponse(2000, 150.0, 250.0, 65.0, dayType.name());
        }
        return new UserNutritionTargetResponse(
                target.dailyCalories, target.dailyProtein, target.dailyCarbs, target.dailyFat,
                target.dayType.name());
    }

    /** Resolve the correct target for a specific date (checks day type assignment). */
    public UserNutritionTargetResponse getTargetForDate(Long userId, LocalDate date) {
        DayType dayType = dayTypeService.getDayType(userId, date);
        return getTarget(userId, dayType);
    }

    /** Backward-compatible: update DEFAULT target. */
    public UserNutritionTargetResponse updateTarget(Long userId, int cal, double pro, double carbs, double fat) {
        return updateTarget(userId, DayType.DEFAULT, cal, pro, carbs, fat);
    }

    /** Update target for a specific day type. */
    public UserNutritionTargetResponse updateTarget(Long userId, DayType dayType, int cal, double pro, double carbs, double fat) {
        LOG.debugf("updateTarget userId=%d, dayType=%s, cal=%d, pro=%.1f, carbs=%.1f, fat=%.1f",
                userId, dayType, cal, pro, carbs, fat);
        UserNutritionTarget target = UserNutritionTarget.find(
                "user.id = ?1 and dayType = ?2", userId, dayType).firstResult();
        if (target == null) {
            LOG.infof("Creating nutrition target for userId=%d, dayType=%s", userId, dayType);
            User user = User.findById(userId);
            target = new UserNutritionTarget();
            target.user = user;
            target.dayType = dayType;
        }
        target.dailyCalories = cal;
        target.dailyProtein = pro;
        target.dailyCarbs = carbs;
        target.dailyFat = fat;
        target.persist();
        return new UserNutritionTargetResponse(
                target.dailyCalories, target.dailyProtein, target.dailyCarbs, target.dailyFat,
                target.dayType.name());
    }

    /** Get all targets (DEFAULT, TRAINING, REST) for settings UI. */
    public List<UserNutritionTargetResponse> getAllTargets(Long userId) {
        List<UserNutritionTarget> targets = UserNutritionTarget.find("user.id", userId).list();
        List<UserNutritionTargetResponse> result = new ArrayList<>();
        for (UserNutritionTarget t : targets) {
            result.add(new UserNutritionTargetResponse(
                    t.dailyCalories, t.dailyProtein, t.dailyCarbs, t.dailyFat, t.dayType.name()));
        }
        return result;
    }

    public void createDefaultTarget(User user) {
        UserNutritionTarget target = new UserNutritionTarget();
        target.user = user;
        target.dayType = DayType.DEFAULT;
        target.persist();
    }
}
