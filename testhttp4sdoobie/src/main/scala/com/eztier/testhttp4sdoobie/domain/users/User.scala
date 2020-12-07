package com.eztier.testhttp4sdoobie.domain
package users

case class Geolocation
(
  lat: String,
  lng: String
)

case class Company
(
  name: String,
  catchPhrase: String,
  bs: String
)

case class Address
(
  street: String,
  suite: String,
  city: String,
  zipcode: String,
  geo: Geolocation
)

case class User
(
  id: Int,
  name: String,
  username: String,
  email: String,
  address: Address,
  phone: String,
  website: String,
  company: Company
)
