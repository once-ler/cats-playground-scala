package com.eztier.testfs2cassandra
package domain

case class Extracted(
  id: String = "",
  docId: String = "",
  filePath: String,
  content: String
)

case class DocumentMetadata
(
  id: String = "",
  docId: String = "",
  filePath: String,
  investigator: String,
  studyTeamMembers: String,
  company: String,
  status: String,
  display: String,
  display_long: String
)

case class DocumentExtracted
(
  id: String,
  docId: String,
  filePath: String,
  content: String,
  investigator: String,
  studyTeamMembers: String,
  company: String,
  status: String,
  display: String,
  display_long: String
)
