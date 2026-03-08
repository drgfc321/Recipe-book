package com.recipebook.service;

import com.recipebook.dto.UserNutritionTargetResponse;
import com.recipebook.entity.User;
import com.recipebook.entity.UserNutritionTarget;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

@ApplicationScoped
public class NutritionTargetService {

    private static final Logger LOG = Logger.getLogger(NutritionTargetService.class);

    public UserNutritionTargetResponse getTarget(Long userId) {
        LOG.debugf("getTarget userId=%d", userId);
        UserNutritionTarget target = UserNutritionTarget.find("user.id", userId).firstResult();
        if (target == null) {
            return new UserNutritionTargetResponse(2000, 150.0, 250.0, 65.0);
        }
        return new UserNutritionTargetResponse(target.dailyCalories, target.dailyProtein, target.dailyCarbs, target.dailyFat);
    }

    public UserNutritionTargetResponse updateTarget(Long userId, int cal, double pro, double carbs, double fat) {
        LOG.debugf("updateTarget userId=%d, cal=%d, pro=%.1f, carbs=%.1f, fat=%.1f", userId, cal, pro, carbs, fat);
        UserNutritionTarget target = UserNutritionTarget.find("user.id", userId).firstResult();
        if (target == null) {
            LOG.infof("Creating first nutrition target for userId=%d", userId);
            User user = User.findById(userId);
            target = new UserNutritionTarget();
            target.user = user;
        }
        target.dailyCalories = cal;
        target.dailyProtein = pro;
        target.dailyCarbs = carbs;
        target.dailyFat = fat;
        target.persist();
        return new UserNutritionTargetResponse(target.dailyCalories, target.dailyProtein, target.dailyCarbs, target.dailyFat);
    }

    public void createDefaultTarget(User user) {
        UserNutritionTarget target = new UserNutritionTarget();
        target.user = user;
        target.persist();
    }
}
