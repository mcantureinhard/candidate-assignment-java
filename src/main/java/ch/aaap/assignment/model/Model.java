package ch.aaap.assignment.model;

import java.util.Set;
import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class Model {
  private Set<PoliticalCommunity> politicalCommunities;
  private Set<PostalCommunity> postalCommunities;
  private Set<Canton> cantons;
  private Set<District> districts;
}
