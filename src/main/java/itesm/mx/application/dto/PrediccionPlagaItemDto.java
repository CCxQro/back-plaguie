package itesm.mx.application.dto;

public class PrediccionPlagaItemDto {
    public String plagueName;
    public Integer probability;
    public String estimatedPeriod;
    public String riskLevel;
    public String affectedHost;
    public String justification;
    public String suggestedProduct;

    public PrediccionPlagaItemDto() {
    }

    public PrediccionPlagaItemDto(
            String plagueName,
            Integer probability,
            String estimatedPeriod,
            String riskLevel,
            String affectedHost,
            String justification,
            String suggestedProduct
    ) {
        this.plagueName = plagueName;
        this.probability = probability;
        this.estimatedPeriod = estimatedPeriod;
        this.riskLevel = riskLevel;
        this.affectedHost = affectedHost;
        this.justification = justification;
        this.suggestedProduct = suggestedProduct;
    }
}
