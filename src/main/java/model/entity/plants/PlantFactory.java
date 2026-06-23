package model.entity.plants;

import model.enums.PlantType;
import model.user.CollectionState;

public final class PlantFactory {
    private PlantFactory() {
    }

    public static PlantInstance create(PlantDefinition definition, int level) {
        if (definition == null) {
            throw new IllegalArgumentException("Plant definition cannot be null");
        }

        int finalLevel = Math.max(1, level);
        PlantStats stats = UpgradeResolver.resolveStats(definition, finalLevel);
        PlantInstance instance = new PlantInstance(definition, stats, finalLevel);

        instance.putRuntimeState("plantType", definition.getType());
        instance.putRuntimeState("plantName", definition.getName());
        instance.putRuntimeState("plantKey", definition.getPlantKey());
        instance.putRuntimeState("category", definition.getCategoryEnum());
        instance.putRuntimeState("tags", definition.getTagEnums());
        instance.putRuntimeState("baseAbilityKind",
                definition.getBaseAbility() == null ? null : definition.getBaseAbility().getKind());
        instance.putRuntimeState("baseAbilityId",
                definition.getBaseAbility() == null ? null : definition.getBaseAbility().getResolvedBehaviorId());
        instance.putRuntimeState("baseAbilityParams",
                definition.getBaseAbility() == null ? null : definition.getBaseAbility().getParams());
        instance.putRuntimeState("plantFoodKind",
                definition.getPlantFoodEffect() == null ? null : definition.getPlantFoodEffect().getKind());
        instance.putRuntimeState("plantFoodId",
                definition.getPlantFoodEffect() == null ? null : definition.getPlantFoodEffect().getResolvedBehaviorId());
        instance.putRuntimeState("plantFoodParams",
                definition.getPlantFoodEffect() == null ? null : definition.getPlantFoodEffect().getParams());
        instance.putRuntimeState("resolvedLevel", finalLevel);

        return instance;
    }

    public static PlantInstance createByType(PlantType type, int level) {
        return create(PlantLibrary.getByType(type), level);
    }

    public static PlantInstance createByName(String name, int level) {
        return create(PlantLibrary.getByName(name), level);
    }

    public static PlantInstance createFromCollection(PlantType type, CollectionState collectionState) {
        int level = collectionState == null ? 1 : collectionState.getPlantLevel(type);
        return createByType(type, level);
    }

    public static PlantInstance createFromCollection(String name, CollectionState collectionState) {
        PlantType type = PlantType.fromName(name);
        int level = collectionState == null ? 1 : collectionState.getPlantLevel(type);
        return createByType(type, level);
    }
}
