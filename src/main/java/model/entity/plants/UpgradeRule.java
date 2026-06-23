package model.entity.plants;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import model.enums.PlantFlag;
import model.enums.PlantStatType;
import model.enums.SpecialUpgradeType;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UpgradeRule {

    public enum UpgradeKind {
        STAT,
        FLAG,
        SPECIAL,
        FAMILY,
        BEHAVIOR,
        UNKNOWN;

        public static UpgradeKind fromRaw(String raw) {
            if (raw == null || raw.trim().isEmpty()) {
                return UNKNOWN;
            }
            return switch (normalize(raw)) {
                case "stat" -> STAT;
                case "flag" -> FLAG;
                case "special" -> SPECIAL;
                case "family" -> FAMILY;
                case "behavior" -> BEHAVIOR;
                default -> UNKNOWN;
            };
        }
    }

    public enum UpgradeOperation {
        ADD,
        SET,
        MULTIPLY,
        SUBTRACT,
        UNKNOWN;

        public static UpgradeOperation fromRaw(String raw) {
            if (raw == null || raw.trim().isEmpty()) {
                return UNKNOWN;
            }
            return switch (normalize(raw)) {
                case "add", "+" -> ADD;
                case "set", "=" -> SET;
                case "multiply", "mul", "*" -> MULTIPLY;
                case "subtract", "sub", "-" -> SUBTRACT;
                default -> UNKNOWN;
            };
        }
    }

    private int level;

    @JsonAlias({"kind", "type"})
    private String kind;

    @JsonAlias({"stat", "statType"})
    private String stat;

    @JsonAlias({"operation", "op"})
    private String operation;

    private Double value;

    private String flag;
    private String special;
    private String family;
    private String behaviorId;

    @JsonAlias({"raw", "text", "description"})
    private String raw;

    private String note;

    @JsonAlias({"params", "parameters"})
    private Map<String, Object> params = new LinkedHashMap<>();

    public UpgradeRule() {
    }

    public static UpgradeRule stat(int level, PlantStatType stat, UpgradeOperation op, double value) {
        UpgradeRule rule = new UpgradeRule();
        rule.setLevel(level);
        rule.setKind(UpgradeKind.STAT.name());
        rule.setStat(stat == null ? null : stat.name());
        rule.setOperation(op == null ? null : op.name());
        rule.setValue(value);
        return rule;
    }

    public static UpgradeRule flag(int level, PlantFlag flag) {
        UpgradeRule rule = new UpgradeRule();
        rule.setLevel(level);
        rule.setKind(UpgradeKind.FLAG.name());
        rule.setFlag(flag == null ? null : flag.name());
        return rule;
    }

    public static UpgradeRule special(int level, SpecialUpgradeType specialType) {
        UpgradeRule rule = new UpgradeRule();
        rule.setLevel(level);
        rule.setKind(UpgradeKind.SPECIAL.name());
        rule.setSpecial(specialType == null ? null : specialType.name());
        return rule;
    }

    public static UpgradeRule family(int level, String family, String behaviorId) {
        UpgradeRule rule = new UpgradeRule();
        rule.setLevel(level);
        rule.setKind(UpgradeKind.FAMILY.name());
        rule.setFamily(family);
        rule.setBehaviorId(behaviorId);
        return rule;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public UpgradeKind getKindEnum() {
        return UpgradeKind.fromRaw(kind);
    }

    public String getStat() {
        return stat;
    }

    public void setStat(String stat) {
        this.stat = stat;
    }

    public PlantStatType getStatEnum() {
        return PlantStatType.fromRaw(stat);
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public UpgradeOperation getOperationEnum() {
        return UpgradeOperation.fromRaw(operation);
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public double getValueOrDefault(double defaultValue) {
        return value == null ? defaultValue : value;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public PlantFlag getFlagEnum() {
        return PlantFlag.fromRaw(flag);
    }

    public String getSpecial() {
        return special;
    }

    public void setSpecial(String special) {
        this.special = special;
    }

    public SpecialUpgradeType getSpecialEnum() {
        return SpecialUpgradeType.fromRaw(special);
    }

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public String getBehaviorId() {
        return behaviorId;
    }

    public void setBehaviorId(String behaviorId) {
        this.behaviorId = behaviorId;
    }

    public String getRaw() {
        return raw;
    }

    public void setRaw(String raw) {
        this.raw = raw;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params == null ? new LinkedHashMap<>() : new LinkedHashMap<>(params);
    }

    public Object getParam(String key) {
        return params.get(key);
    }

    public boolean hasParam(String key) {
        return params.containsKey(key);
    }

    private static String normalize(String raw) {
        return raw.trim()
                .toLowerCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');
    }
}