package com.eztier.testxmlfs2.openstreetmap
package domain

object Codecs {
  implicit val openMapPlaceToDomainPlace: domain.OpenStreetPlace => DomainPlace =
    o => DomainPlace(s"${o.HouseNumber} ${o.Cycleway}", o.City, o.State, o.Postcode)

  implicit val domainPlaceToOpenMapPlace: DomainPlace => domain.OpenStreetPlace =
    d => OpenStreetPlace(
      "",
      d.AddressLine1.split(' ').headOption.getOrElse(""),
      d.AddressLine1.split(' ').drop(1).mkString(" "),
      "",
      d.City,
      "",
      d.State,
      d.ZipCode,
      "United States of America",
      "us"
    )

  implicit val openMapNeighborhoodToDomainPlace: OpenStreetNeighborhood => DomainPlace =
    o => DomainPlace("", "", "", "", o.DisplayName)


  /*
  implicit val openStreetPlaceSemigroup: Semigroup[OpenStreetPlace] = new Semigroup[OpenStreetPlace] {
    def combine(a: OpenStreetPlace, b: OpenStreetPlace): OpenStreetPlace = ???
  }

  implicit val semigroupPlace: Semigroup[DomainPlace] =
    Semigroup[OpenStreetPlace].imap(openMapPlaceToDomainPlace)(domainPlaceToOpenMapPlace)
  */
}
