package com.eztier.testfs2cassandra
package domain

case class Extracted(
  id: String = "",
  filePath: String,
  content: String
)
