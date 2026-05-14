package itesm.mx.application.dto;

import java.util.List;

public class GetReportePredictivoPlagasResponseDto {
    public String region;
    public String season;
    public String generatedAt;
    public long observationsAnalyzed;
    public String executiveSummary;
    public List<PrediccionPlagaItemDto> predictions;

    public GetReportePredictivoPlagasResponseDto() {
    }

    public GetReportePredictivoPlagasResponseDto(
            String region,
            String season,
            String generatedAt,
            long observationsAnalyzed,
            String executiveSummary,
            List<PrediccionPlagaItemDto> predictions
    ) {
        this.region = region;
        this.season = season;
        this.generatedAt = generatedAt;
        this.observationsAnalyzed = observationsAnalyzed;
        this.executiveSummary = executiveSummary;
        this.predictions = predictions;
    }
}
