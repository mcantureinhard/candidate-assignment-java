package ch.aaap.assignment.model;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PoliticalCommunity {
  private String number;
  private String name;
  private String shortName;
  private LocalDate lastUpdate;
  private Canton canton;
  private District district;
}
