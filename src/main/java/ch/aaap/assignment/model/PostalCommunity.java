package ch.aaap.assignment.model;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PostalCommunity {
  private String zipCode;
  private String zipCodeAddition;
  private String name;
  private List<PoliticalCommunity> politicalCommunities;
}
