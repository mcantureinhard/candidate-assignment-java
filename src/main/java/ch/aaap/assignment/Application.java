package ch.aaap.assignment;

import ch.aaap.assignment.model.Canton;
import ch.aaap.assignment.model.District;
import ch.aaap.assignment.model.Model;
import ch.aaap.assignment.model.PoliticalCommunity;
import ch.aaap.assignment.model.PostalCommunity;
import ch.aaap.assignment.raw.CSVPoliticalCommunity;
import ch.aaap.assignment.raw.CSVPostalCommunity;
import ch.aaap.assignment.raw.CSVUtil;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.util.Pair;

public class Application {

  private Model model;

  public Application() {
    initModel();
  }

  public static void main(String[] args) {
    new Application();
  }

  /** Reads the CSVs and initializes a in memory model */
  private void initModel() {
    Set<CSVPoliticalCommunity> politicalCommunities = CSVUtil.getPoliticalCommunities();
    Set<CSVPostalCommunity> postalCommunities = CSVUtil.getPostalCommunities();

    Set<District> districts = new HashSet<>();
    Set<Canton> cantons = new HashSet<>();
    Set<PoliticalCommunity> politicalCommunitiesSet = new HashSet<>();
    Set<PostalCommunity> postalCommunitiesSet = new HashSet<>();
    for (CSVPoliticalCommunity politicalCommunity : politicalCommunities) {
      cantons.add(Canton.builder().name(politicalCommunity.getCantonName())
          .code(politicalCommunity.getCantonCode()).build());
      districts.add(District.builder().name(politicalCommunity.getDistrictName())
          .number(politicalCommunity.getDistrictNumber()).build());
    }

    politicalCommunities.forEach(pc -> {
      politicalCommunitiesSet.add(
          PoliticalCommunity.builder()
              .name(pc.getName())
              .number(pc.getNumber())
              .shortName(pc.getShortName())
              .lastUpdate(pc.getLastUpdate())
              .canton(cantons.stream().filter(c -> c.getCode().equals(pc.getCantonCode())).findFirst().orElse(null))
              .district(districts.stream().filter(d -> d.getNumber().equals(pc.getDistrictNumber())).findFirst().orElse(null))
              .build()
      );
    });

    // Interesting fact that we can have postal communities that are part of multiple political communities.
    // Had to change my model after I realized
    postalCommunities
        .stream()
        .collect(Collectors.groupingBy(
            po -> new Pair(po.getZipCode(), po.getZipCodeAddition())
        )).forEach((zipPair, pcList) -> postalCommunitiesSet.add(
            PostalCommunity.builder()
                .name(pcList.get(0).getName())
                .zipCode(pcList.get(0).getZipCode())
                .zipCodeAddition(pcList.get(0).getZipCodeAddition())
                .politicalCommunities(
                    politicalCommunitiesSet
                        .stream()
                        .filter(pc -> pcList.stream().map(
                            CSVPostalCommunity::getPoliticalCommunityNumber).collect(Collectors.toList()).contains(pc.getNumber())
                        )
                    .collect(Collectors.toList())
                )
                .build()
        ));

    model = Model.builder()
        .cantons(cantons)
        .districts(districts)
        .politicalCommunities(politicalCommunitiesSet)
        .postalCommunities(postalCommunitiesSet)
        .build();
  }

  /**
   * @return model
   */

  public Model getModel() {
    return model;
  }

  /**
   * @param cantonCode of a canton (e.g. ZH)
   * @return amount of political communities in given canton
   */
  public long getAmountOfPoliticalCommunitiesInCanton(String cantonCode) {
    Optional<Canton> canton = model.getCantons().stream().filter(c -> c.getCode().equals(cantonCode)).findFirst();
    if (canton.isEmpty()) {
      throw new IllegalArgumentException("Invalid Canton Code");
    }
    return model.getPoliticalCommunities().stream().filter(pl -> pl.getCanton().equals(canton.get())).count();
  }

  /**
   * @param cantonCode of a canton (e.g. ZH)
   * @return amount of districts in given canton
   */
  public long getAmountOfDistrictsInCanton(String cantonCode) {
    Optional<Canton> canton = model.getCantons().stream().filter(c -> c.getCode().equals(cantonCode)).findFirst();
    if (canton.isEmpty()) {
      throw new IllegalArgumentException("Invalid Canton Code");
    }
    return model.getPoliticalCommunities()
        .stream()
        .filter(pl -> pl.getCanton().equals(canton.get()))
        .map(PoliticalCommunity::getDistrict)
        .distinct()
        .count();
  }

  /**
   * @param districtNumber of a district (e.g. 101)
   * @return amount of districts in given canton
   */
  public long getAmountOfPoliticalCommunitiesInDistict(String districtNumber) {
    Optional<District> district = model.getDistricts().stream().filter(d -> d.getNumber().equals(districtNumber)).findFirst();
    if (district.isEmpty()) {
      throw new IllegalArgumentException("Invalid District Number");
    }
    return model.getPoliticalCommunities().stream()
        .filter(pc -> pc.getDistrict().equals(district.get())).count();
  }

  /**
   * @param zipCode 4 digit zip code
   * @return district that belongs to specified zip code
   */
  public Set<String> getDistrictsForZipCode(String zipCode) {
    return model.getPostalCommunities()
        .stream()
        .filter(po -> po.getZipCode().equals(zipCode))
        .map(PostalCommunity::getPoliticalCommunities)
        .flatMap(Collection::stream)
        .map(pc -> pc.getDistrict().getName())
        .collect(Collectors.toSet());
  }

  /**
   * @param postalCommunityName name
   * @return lastUpdate of the political community by a given postal community name
   */
  public LocalDate getLastUpdateOfPoliticalCommunityByPostalCommunityName(
      String postalCommunityName) {
    return model.getPostalCommunities()
        .stream()
        .filter(po -> po.getName().equals(postalCommunityName))
        .map(PostalCommunity::getPoliticalCommunities)
        .flatMap(Collection::stream)
        .map(PoliticalCommunity::getLastUpdate)
        .findFirst()
        .orElse(null);
  }

  /**
   * https://de.wikipedia.org/wiki/Kanton_(Schweiz)
   *
   * @return amount of canton
   */
  public long getAmountOfCantons() {
    return model.getCantons().size();
  }

  /**
   * https://de.wikipedia.org/wiki/Kommunanz
   *
   * @return amount of political communities without postal communities
   */
  public long getAmountOfPoliticalCommunityWithoutPostalCommunities() {
    return model.getPoliticalCommunities()
        .stream()
        .filter(pc -> !model.getPostalCommunities()
            .stream()
            .map(PostalCommunity::getPoliticalCommunities)
            .flatMap(Collection::stream)
            .collect(Collectors.toSet())
            .contains(pc))
        .count();
  }
}
