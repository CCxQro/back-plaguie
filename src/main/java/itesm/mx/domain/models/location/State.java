package itesm.mx.domain.models.location;

public class State {
    private Long stateId;
    private String name;

    public State() {}

    public State(Long stateId, String name) {
        this.stateId = stateId;
        this.name = name;
    }

    public Long getStateId() {
        return stateId;
    }

    public void setStateId(Long stateId) {
        this.stateId = stateId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
