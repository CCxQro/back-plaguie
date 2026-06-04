package itesm.mx.domain.models.parcela;

import itesm.mx.domain.models.alerta.Alerta;
import java.time.LocalDateTime;

public class ParcelaSuggestion {
    private Long suggestionId;
    private Parcela parcela;
    private Alerta alerta;
    private String message;
    private LocalDateTime createdAt;

    public ParcelaSuggestion() {}

    public ParcelaSuggestion(Long suggestionId, Parcela parcela, Alerta alerta, String message, LocalDateTime createdAt) {
        this.suggestionId = suggestionId;
        this.parcela = parcela;
        this.alerta = alerta;
        this.message = message;
        this.createdAt = createdAt;
    }

    public Long getSuggestionId() { return suggestionId; }
    public void setSuggestionId(Long suggestionId) { this.suggestionId = suggestionId; }

    public Parcela getParcela() { return parcela; }
    public void setParcela(Parcela parcela) { this.parcela = parcela; }

    public Alerta getAlerta() { return alerta; }
    public void setAlerta(Alerta alerta) { this.alerta = alerta; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
