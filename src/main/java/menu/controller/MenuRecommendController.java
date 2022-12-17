package menu.controller;

import menu.model.*;
import menu.service.CoachService;
import menu.service.MenuCategoryService;
import menu.service.MenuService;
import menu.service.RecommendResultService;
import menu.view.InputView;
import menu.view.OutputView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author : jeeseob
 * @CreateAt : 2022/12/17
 */
public class MenuRecommendController {
    private final CoachService coachService;
    private final MenuService menuService;
    private final MenuCategoryService menuCategoryService;
    private final RecommendResultService recommendResultService;
    public MenuRecommendController() {
        this.coachService = new CoachService();
        this.menuService = new MenuService();
        this.menuCategoryService = new MenuCategoryService();
        this.recommendResultService = new RecommendResultService();
    }

    public void startRecommend() {
        OutputView.printStartMessage();
        saveCoaches();
        OutputView.printResultMessage();
        // TODO: 추천한 메뉴 계산
        calcRecommendResults();
        List<RecommendResult> recommendResults = recommendResultService.findAll();
        // TODO: 추천한 메뉴 출력
        OutputView.printFinishMessage();
    }

    private void calcRecommendResults() {
        calcRecommendResultsHaveMenuCategory();
        calcRecommendResultHaveCoachAndMenu();
    }

    public void calcRecommendResultHaveCoachAndMenu() {
        List<RecommendResult> recommendResults = recommendResultService.findAll();
        List<MenuCategory> menuCategories = recommendResults.stream().map(RecommendResult::getMenuCategory).collect(Collectors.toList());
        List<Coach> coaches = coachService.findAll();
        for (int i = 0; i < menuCategories.size(); i++) {
            for (Coach coach : coaches) {
                Menu menu = getMenuByCoach(menuCategories.get(i), coach);
                recommendResultService.update(coach, menu, i);
            }
        }
    }

    private Menu getMenuByCoach(MenuCategory menuCategory, Coach coach) {
        List<RecommendResult> recommendResults = recommendResultService.findAll();
        Menu randomMenu = menuService.getRandomMenuByMenuCategory(menuCategory);
        if (recommendResults.stream().map(recommendResult
                -> recommendResult.getCoachAndMenu().get(coach)).collect(Collectors.toList()).stream()
                .anyMatch(menu -> menu.equals(randomMenu))) {
            return getMenuByCoach(menuCategory, coach);
        }
        return randomMenu;
    }

    private void calcRecommendResultsHaveMenuCategory() {
        List<DayOfTheWeek> dayOfTheWeeks = Arrays.stream(DayOfTheWeek.values()).collect(Collectors.toList());
        for (DayOfTheWeek day : dayOfTheWeeks) {
            MenuCategory menuCategory = getRandomMenuCategory();
            recommendResultService.save(new RecommendResult(day, menuCategory));
        }
    }

    private MenuCategory getRandomMenuCategory() {
        List<RecommendResult> recommendResults = recommendResultService.findAll();
        MenuCategory menuCategory = menuCategoryService.getRandomMenuCategory();
        MenuCategory finalMenuCategory = menuCategory;
        if(recommendResults.stream().filter(recommendResult
                -> recommendResult.getMenuCategory().equals(finalMenuCategory)).count() >= 2) {
            menuCategory = getRandomMenuCategory(); // 카테고리가 2회 이상 겹치는 경우 다시 뽑는다.
        }
        return menuCategory;
    }

    public void saveCoaches() {
        List<String> coachNames = this.readCoaches();
        for (String coachName : coachNames) {
            OutputView.printInputHateMenuMessage(coachName);
            List<Menu> hateMenus = InputView.readHateMenus().stream().map(menuService::findByName).collect(Collectors.toList());
            coachService.addCoach(coachName, hateMenus);
        }
    }

    public List<String> readCoaches() {
        OutputView.printCoachNameMessage();
        List<String> coachNames = new ArrayList<>();
        try {
            coachNames = InputView.readCoachNames();
        } catch (IllegalArgumentException e) {
        System.out.println(e.getMessage());
        coachNames = readCoaches();
        }
        return coachNames;
    }
}
