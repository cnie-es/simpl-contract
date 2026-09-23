package eu.europa.ec.simpl.contracts.types;

public enum BusinessOperations {
    BP07_01("ISSUE_CONTRACT"),
    BP07_06("CONTRACT_TERMINATE"),
    BP07_09("CONTRACT_FINALIZE");

    private final String description;

    BusinessOperations(String businessOperation) {
        description = businessOperation;
    }

    public String description() {
        return description;
    }
}
