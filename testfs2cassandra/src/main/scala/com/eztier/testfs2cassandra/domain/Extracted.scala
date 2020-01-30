package com.eztier.testfs2cassandra
package domain

import java.time.Instant

case class Extracted(
  domain: Option[String] = None,
  root_type: Option[String] = None,
  root_id: Option[String] = None,
  doc_id: Option[String] = None,
  doc_file_path: Option[String] = None,
  content: Option[String] = None,
  metadata: Option[Map[String, String]] = None
)

/*
  Solr uses DateTimeFormatter.ISO_INSTANT "YYYY-MM-DDThh:mm:ssZ".
  No time zone can be specified.
  The String representations of dates is always expressed in Coordinated Universal Time (UTC).
 */
case class DocumentMetadata
(
  id: Option[String] = None, // domain:root_type:root_id:doc_id
  domain: Option[String] = None,
  root_type: Option[String] = None,
  root_id: Option[String] = None,
  root_owner: Option[String] = None,
  root_associates: Option[List[String]] = None,
  root_company: Option[String],
  root_status: Option[String],
  root_display: Option[String],
  root_display_long: Option[String],
  doc_id: Option[String] = None,
  doc_other_id: Option[String] = None,
  doc_file_path: Option[String] = None,
  doc_object_path: Option[String] = None,
  doc_category: Option[String] = None,
  doc_name: Option[String] = None,
  doc_date_created: Option[String] = None,
  doc_year_created: Option[Int] = None
)

case class DocumentExtracted
(
  id: Option[String] = None, // domain:root_type:root_id:doc_id
  domain: Option[String] = None,
  root_id: Option[String] = None,
  root_type: Option[String] = None,
  root_owner: Option[String] = None,
  root_associates: Option[List[String]] = None,
  root_company: Option[String] = None,
  root_status: Option[String] = None,
  root_display: Option[String] = None,
  root_display_long: Option[String] = None,
  doc_id: Option[String] = None,
  doc_other_id: Option[String] = None,
  doc_file_path: Option[String] = None,
  doc_object_path: Option[String] = None,
  doc_category: Option[String] = None,
  doc_name: Option[String] = None,
  doc_date_created: Option[String] = None,
  doc_year_created: Option[Int] = None,
  content: Option[String] = None,
  metadata: Option[Map[String, String]] = None
)

case class Document
(
  doc_id: Option[String] = None,
  doc_other_id: Option[String] = None,
  doc_xml: Option[String] = None
)
