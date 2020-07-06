package ch.aaap.assignment.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Canton {
  private String code;
  private String name;
}
