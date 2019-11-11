package com.eztier.testxmlfs2
package patients.domain

object Codecs {
  implicit val mapToEthnicity: Map[String, String] => Ethnicity =
    m => Ethnicity(m.get("ethnicity1"), m.get("ethnicity2"))

}
