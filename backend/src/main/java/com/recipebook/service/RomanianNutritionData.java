package com.recipebook.service;

import com.recipebook.entity.IngredientCategory;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Hardcoded nutritional data (per 100g) for common Romanian ingredients
 * found in the imported recipe PDF. Values based on USDA standard data.
 */
public final class RomanianNutritionData {

    public record NutrientInfo(double calories, double protein, double carbs, double fat, IngredientCategory category) {}

    private static final Map<String, NutrientInfo> DATA = new LinkedHashMap<>();

    static {
        // === MEAT ===
        put("piept de pui",       165, 31, 0, 3.6, IngredientCategory.MEAT);
        put("piept de curcan",    135, 30, 0, 1.0, IngredientCategory.MEAT);
        put("pulpa de vita",      250, 26, 0, 15, IngredientCategory.MEAT);
        put("pulpa de vita tocata", 250, 26, 0, 15, IngredientCategory.MEAT);
        put("bacon",              541, 37, 1, 42, IngredientCategory.MEAT);
        put("bacon de curcan",    382, 28, 3, 28, IngredientCategory.MEAT);
        put("salam vegan",        200, 20, 8, 10, IngredientCategory.MEAT);
        put("sunca de pui",       110, 18, 2, 3, IngredientCategory.MEAT);

        // === FISH & SEAFOOD ===
        put("somon",              208, 20, 0, 13, IngredientCategory.MEAT);
        put("file de somon",      208, 20, 0, 13, IngredientCategory.MEAT);
        put("file de cod",        82, 18, 0, 0.7, IngredientCategory.MEAT);
        put("creveti",            99, 24, 0.2, 0.3, IngredientCategory.MEAT);
        put("creveti decorticati", 99, 24, 0.2, 0.3, IngredientCategory.MEAT);
        put("ton",                116, 26, 0, 0.8, IngredientCategory.MEAT);
        put("ghimbir fresh",      80, 1.8, 18, 0.8, IngredientCategory.SPICES);

        // === DAIRY ===
        put("iaurt grecesc 2%",   73, 10, 4, 2, IngredientCategory.DAIRY);
        put("iaurt grecesc",      73, 10, 4, 2, IngredientCategory.DAIRY);
        put("cottage cheese",     98, 11, 3.4, 4.3, IngredientCategory.DAIRY);
        put("cottage cheese light", 72, 12, 3, 1, IngredientCategory.DAIRY);
        put("mozarella light",    254, 28, 3, 14, IngredientCategory.DAIRY);
        put("mozzarella light",   254, 28, 3, 14, IngredientCategory.DAIRY);
        put("cascaval light",     250, 28, 2, 14, IngredientCategory.DAIRY);
        put("parmezan",           431, 38, 4, 29, IngredientCategory.DAIRY);
        put("branza feta",        264, 14, 4, 21, IngredientCategory.DAIRY);
        put("branza topita cheddar", 300, 18, 3, 24, IngredientCategory.DAIRY);
        put("cheddar",            403, 25, 1.3, 33, IngredientCategory.DAIRY);
        put("ricotta",            174, 11, 3, 13, IngredientCategory.DAIRY);
        put("crema de branza light", 150, 7, 5, 11, IngredientCategory.DAIRY);
        put("philadelphia light", 150, 7, 5, 11, IngredientCategory.DAIRY);
        put("lapte soia",         54, 3.3, 6, 1.8, IngredientCategory.BEVERAGES);
        put("lapte de soia",      54, 3.3, 6, 1.8, IngredientCategory.BEVERAGES);
        put("lapte de migdale",   15, 0.6, 0.3, 1.1, IngredientCategory.BEVERAGES);
        put("frisca light",       150, 2, 12, 10, IngredientCategory.DAIRY);
        put("smantana de gatit vegetala", 148, 0.5, 5, 14, IngredientCategory.DAIRY);
        put("smantana vegetala de gatit", 148, 0.5, 5, 14, IngredientCategory.DAIRY);
        put("budinca proteica",   60, 8, 5, 0.5, IngredientCategory.DAIRY);

        // === EGGS ===
        put("ou",                 155, 13, 1.1, 11, IngredientCategory.DAIRY);
        put("oua",                155, 13, 1.1, 11, IngredientCategory.DAIRY);
        put("albus",              52, 11, 0.7, 0.2, IngredientCategory.DAIRY);
        put("albus ou",           52, 11, 0.7, 0.2, IngredientCategory.DAIRY);

        // === GRAINS ===
        put("ovaz",               389, 17, 66, 7, IngredientCategory.GRAINS);
        put("orez",               130, 2.7, 28, 0.3, IngredientCategory.GRAINS);
        put("orez basmati",       130, 2.7, 28, 0.3, IngredientCategory.GRAINS);
        put("paste integrale",    348, 14, 64, 2.5, IngredientCategory.GRAINS);
        put("penne integrale",    348, 14, 64, 2.5, IngredientCategory.GRAINS);
        put("macaroane",          348, 14, 64, 2.5, IngredientCategory.GRAINS);
        put("faina",              364, 10, 76, 1, IngredientCategory.GRAINS);
        put("faina grau",         364, 10, 76, 1, IngredientCategory.GRAINS);
        put("faina alba",         364, 10, 76, 1, IngredientCategory.GRAINS);
        put("corn flakes",        357, 7, 84, 0.4, IngredientCategory.GRAINS);
        put("rondele de porumb",  387, 8, 81, 3, IngredientCategory.GRAINS);
        put("rondele porumb",     387, 8, 81, 3, IngredientCategory.GRAINS);
        put("tortilla integrala", 290, 8, 44, 9, IngredientCategory.GRAINS);
        put("tortilla din porumb", 218, 6, 44, 2, IngredientCategory.GRAINS);
        put("lipie graham",       280, 9, 50, 5, IngredientCategory.GRAINS);
        put("lipie din graham",   280, 9, 50, 5, IngredientCategory.GRAINS);
        put("toast integral",     247, 13, 41, 4, IngredientCategory.GRAINS);
        put("chifla brioche bun", 340, 9, 53, 10, IngredientCategory.GRAINS);
        put("praf de copt",       53, 0, 28, 0, IngredientCategory.GRAINS);
        put("amidon de porumb",   381, 0.3, 91, 0.1, IngredientCategory.GRAINS);
        put("amidon",             381, 0.3, 91, 0.1, IngredientCategory.GRAINS);
        put("foi de orez",        360, 1, 87, 0, IngredientCategory.GRAINS);
        put("foaie de orez",      360, 1, 87, 0, IngredientCategory.GRAINS);
        put("wrap integral",      290, 8, 44, 9, IngredientCategory.GRAINS);
        put("biscuiti lotus biscoff", 480, 5, 68, 22, IngredientCategory.GRAINS);

        // === VEGETABLES ===
        put("cartof",             77, 2, 17, 0.1, IngredientCategory.VEGETABLES);
        put("cartof dulce",       86, 1.6, 20, 0.1, IngredientCategory.VEGETABLES);
        put("ceapa",              40, 1.1, 9, 0.1, IngredientCategory.VEGETABLES);
        put("ceapa rosie",        40, 1.1, 9, 0.1, IngredientCategory.VEGETABLES);
        put("ceapa alba",         40, 1.1, 9, 0.1, IngredientCategory.VEGETABLES);
        put("ceapa verde",        32, 1.8, 7, 0.2, IngredientCategory.VEGETABLES);
        put("usturoi",            149, 6, 33, 0.5, IngredientCategory.VEGETABLES);
        put("ardei",              31, 1, 6, 0.3, IngredientCategory.VEGETABLES);
        put("ardei kapia",        31, 1, 6, 0.3, IngredientCategory.VEGETABLES);
        put("ardei gras rosu",    31, 1, 6, 0.3, IngredientCategory.VEGETABLES);
        put("ardei rosu",         31, 1, 6, 0.3, IngredientCategory.VEGETABLES);
        put("rosii",              18, 0.9, 3.9, 0.2, IngredientCategory.VEGETABLES);
        put("rosii cherry",       18, 0.9, 3.9, 0.2, IngredientCategory.VEGETABLES);
        put("rosii uscate",       258, 14, 56, 3, IngredientCategory.VEGETABLES);
        put("rosii decojite",     18, 0.9, 3.9, 0.2, IngredientCategory.VEGETABLES);
        put("castravete",         15, 0.7, 3.6, 0.1, IngredientCategory.VEGETABLES);
        put("castraveti murati",  11, 0.3, 2.3, 0.2, IngredientCategory.VEGETABLES);
        put("ciuperci",           22, 3.1, 3.3, 0.3, IngredientCategory.VEGETABLES);
        put("mix legume mexicane", 55, 2, 10, 0.5, IngredientCategory.VEGETABLES);
        put("mix legume",         55, 2, 10, 0.5, IngredientCategory.VEGETABLES);
        put("salata eisberg",     14, 0.9, 3, 0.1, IngredientCategory.VEGETABLES);
        put("salata iceberg",     14, 0.9, 3, 0.1, IngredientCategory.VEGETABLES);
        put("salata",             14, 0.9, 3, 0.1, IngredientCategory.VEGETABLES);
        put("baby spanac",        23, 2.9, 3.6, 0.4, IngredientCategory.VEGETABLES);
        put("conopida",           25, 1.9, 5, 0.3, IngredientCategory.VEGETABLES);
        put("varza",              25, 1.3, 6, 0.1, IngredientCategory.VEGETABLES);
        put("varza alba",         25, 1.3, 6, 0.1, IngredientCategory.VEGETABLES);
        put("varza roie",         31, 1.4, 7, 0.2, IngredientCategory.VEGETABLES);
        put("morcov",             41, 0.9, 10, 0.2, IngredientCategory.VEGETABLES);
        put("porumb",             86, 3.3, 19, 1.4, IngredientCategory.VEGETABLES);
        put("zucchini",           17, 1.2, 3.1, 0.3, IngredientCategory.VEGETABLES);
        put("tofu",               76, 8, 1.9, 4.8, IngredientCategory.VEGETABLES);

        // === FRUITS ===
        put("banana",             89, 1.1, 23, 0.3, IngredientCategory.FRUITS);
        put("avocado",            160, 2, 9, 15, IngredientCategory.FRUITS);
        put("capsuni",            32, 0.7, 8, 0.3, IngredientCategory.FRUITS);
        put("fructe de padure",   57, 1.2, 14, 0.3, IngredientCategory.FRUITS);
        put("afine",              57, 0.7, 14, 0.3, IngredientCategory.FRUITS);
        put("ananas",             50, 0.5, 13, 0.1, IngredientCategory.FRUITS);
        put("lime",               30, 0.7, 11, 0.2, IngredientCategory.FRUITS);
        put("masline",            115, 0.8, 6, 11, IngredientCategory.FRUITS);

        // === OILS & FATS ===
        put("ulei masline",       884, 0, 0, 100, IngredientCategory.OILS);
        put("ulei de masline",    884, 0, 0, 100, IngredientCategory.OILS);
        put("ulei",               884, 0, 0, 100, IngredientCategory.OILS);
        put("unt light",          450, 0.5, 0.5, 50, IngredientCategory.OILS);
        put("unt de arahide",     588, 25, 20, 50, IngredientCategory.OILS);
        put("peanut butter",      588, 25, 20, 50, IngredientCategory.OILS);
        put("unt arahide pudra",  450, 40, 25, 18, IngredientCategory.OILS);
        put("peanut butter pudra", 450, 40, 25, 18, IngredientCategory.OILS);

        // === SAUCES & CONDIMENTS ===
        put("sos de soya",        53, 8, 4.9, 0, IngredientCategory.SPICES);
        put("sos de soia",        53, 8, 4.9, 0, IngredientCategory.SPICES);
        put("sos soia less salt", 53, 8, 4.9, 0, IngredientCategory.SPICES);
        put("sos soya",           53, 8, 4.9, 0, IngredientCategory.SPICES);
        put("sos sriracha",       93, 2, 19, 0.9, IngredientCategory.SPICES);
        put("sriracha",           93, 2, 19, 0.9, IngredientCategory.SPICES);
        put("ketchup",            112, 1, 26, 0.4, IngredientCategory.SPICES);
        put("ketchup heinz 50%",  70, 1, 15, 0.2, IngredientCategory.SPICES);
        put("ketchup 50%",        70, 1, 15, 0.2, IngredientCategory.SPICES);
        put("ketchup -50%",       70, 1, 15, 0.2, IngredientCategory.SPICES);
        put("mustar",             66, 4, 6, 3, IngredientCategory.SPICES);
        put("maioneza light",     260, 0.5, 5, 26, IngredientCategory.SPICES);
        put("sweet chilli",       190, 0.5, 46, 0.2, IngredientCategory.SPICES);
        put("sos sweet chilli",   190, 0.5, 46, 0.2, IngredientCategory.SPICES);
        put("salsa",              36, 1, 7, 0.2, IngredientCategory.SPICES);
        put("sos salsa",          36, 1, 7, 0.2, IngredientCategory.SPICES);
        put("red hot sauce",      11, 0.5, 2, 0, IngredientCategory.SPICES);
        put("sos red hot",        11, 0.5, 2, 0, IngredientCategory.SPICES);
        put("barilla basilico",   53, 1.5, 7, 2, IngredientCategory.SPICES);
        put("sos basilico",       53, 1.5, 7, 2, IngredientCategory.SPICES);
        put("pesto rosu",         267, 4, 10, 23, IngredientCategory.SPICES);
        put("miere",              304, 0.3, 82, 0, IngredientCategory.SPICES);
        put("sirop agave",        310, 0, 76, 0, IngredientCategory.SPICES);
        put("sirop de artar",     260, 0, 67, 0, IngredientCategory.SPICES);
        put("stevia",             0, 0, 0, 0, IngredientCategory.SPICES);
        put("suc de portocale",   45, 0.7, 10, 0.2, IngredientCategory.BEVERAGES);
        put("suc de ananas",      53, 0.4, 13, 0.1, IngredientCategory.BEVERAGES);

        // === PROTEIN SUPPLEMENTS ===
        put("whey",               400, 80, 8, 5, IngredientCategory.OTHER);
        put("pudra proteica",     400, 80, 8, 5, IngredientCategory.OTHER);
        put("protein spread",     350, 20, 35, 15, IngredientCategory.OTHER);
        put("protein spread alb", 350, 20, 35, 15, IngredientCategory.OTHER);
    }

    private static void put(String name, double cal, double protein, double carbs, double fat, IngredientCategory category) {
        DATA.put(name.toLowerCase(), new NutrientInfo(cal, protein, carbs, fat, category));
    }

    public static Map<String, NutrientInfo> getAll() {
        return DATA;
    }

    /**
     * Fuzzy lookup: tries exact match first, then checks if any key is contained
     * in the input or vice versa.
     */
    public static NutrientInfo lookup(String ingredientName) {
        String key = ingredientName.toLowerCase().trim();
        // Exact match
        NutrientInfo info = DATA.get(key);
        if (info != null) return info;

        // Check if any known key is contained in the input
        for (Map.Entry<String, NutrientInfo> entry : DATA.entrySet()) {
            if (key.contains(entry.getKey()) || entry.getKey().contains(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private RomanianNutritionData() {}
}
