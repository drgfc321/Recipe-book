package com.recipebook.service;

import com.recipebook.dto.UserNutritionTargetResponse;
import com.recipebook.entity.User;
import com.recipebook.entity.UserNutritionTarget;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class NutritionTargetService {

    public UserNutritionTargetResponse getTarget(Long userId) {
        UserNutritionTarget target = UserNutritionTarget.find("user.id", userId).firstResult();
        if (target == null) {
            return new UserNutritionTargetResponse(2000, 150.0, 250.0, 65.0);
        }
        return new UserNutritionTargetResponse(target.dailyCalories, target.dailyProtein, target.dailyCarbs, target.dailyFat);
    }

    public UserNutritionTargetResponse updateTarget(Long userId, int cal, double pro, double carbs, double fat) {
        UserNutritionTarget target = UserNutritionTarget.find("user.id", userId).firstResult();
        if (target == null) {
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
