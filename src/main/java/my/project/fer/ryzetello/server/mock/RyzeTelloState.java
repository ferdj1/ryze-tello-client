package my.project.fer.ryzetello.server.mock;

public enum RyzeTelloState {
    OK("ok"),
    ERROR("error");

    private String value;

    RyzeTelloState(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
