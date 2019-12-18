package com.eztier
package testhl7

import ca.uhn.hl7v2.{DefaultHapiContext, HL7Exception}
import ca.uhn.hl7v2.model.v231.segment.{PID, PV1}
import ca.uhn.hl7v2.parser.CanonicalModelClassFactory
import ca.uhn.hl7v2.validation.impl.NoValidation
import cats.data.{Validated}

import cats.implicits._

object Data {
  val msg0: String =
    "MSH|^~\\&|SendingApp|SendingFac|ReceivingApp|ReceivingFac|20120226102502||ORU^R01|Q161522306T164850327|P|2.3\r" +
      "PID|1||000168674|000168674|GUNN^BEBE||19821201|F||||||||M|||890-12-3456|||N||||||||N\r" +
      "PV1|1|I||EL|||00976^PHYSICIAN^DAVID^G|976^PHYSICIAN^DAVID^G|01055^PHYSICIAN^RUTH^K~02807^PHYSICIAN^ERIC^LEE~07019^GI^ASSOCIATES~01255^PHYSICIAN^ADAM^I~02084^PHYSICIAN^SAYED~01116^PHYSICIAN^NURUDEEN^A~01434^PHYSICIAN^DONNA^K~02991^PHYSICIAN^NICOLE|MED||||7|||00976^PHYSICIAN^DAVID^G||^^^Chart ID^Vis|||||||||||||||||||||||||20120127204900\r" +
      "ORC|RE|||||||||||00976^PHYSICIAN^DAVID^G\r" +
      "OBR|1|88855701^STDOM|88855701|4083023^PT|||20120226095400|||||||20120226101300|Blood|01255||||000002012057000145||20120226102500||LA|F||1^^^20120226040000^^R~^^^^^R|||||||||20120226040000\r" +
      "OBX|1|NM|PT Patient^PT||22.5|second(s)|11.7-14.9|H|||F|||20120226102500||1^SYSTEMA^SYSTEMB\r" +
      "OBX|2|NM|PT (INR)^INR||1.94||||||F|||20120226102500||1^SYSTEM^SYSTEM\r" +
      "NTE|1||This is our override value using the setter\r" +
      "NTE|2||the range is 2.5 - 3.5.\r" +
      "NTE|3\r"+
      "NTE|4||Studies published in NEJM show that patients treated long-term with low intensity warfarin therapy for prevention of recurrent\r" +
      "NTE|5||venous thromboembolism (with a target INR of 1.5 - 2.0) had a superior outcome.  These results were seen in patients after a median\r"
}

case class PatientVisitFromTo(postalCode: String, facility: String)

object HapiTest {
  val hapiContext = new DefaultHapiContext()
  hapiContext.setModelClassFactory(new CanonicalModelClassFactory("2.3.1"))
  hapiContext.setValidationContext(new NoValidation)
  val p = hapiContext.getPipeParser()

  val hpiMsgMaybe = Either.catchOnly[HL7Exception](p.parse(Data.msg0))

  val hpiMsgMaybe2 = Validated.catchOnly[HL7Exception](p.parse(Data.msg0))

  val cartesianPostalAndFacility = for {
    x <- Either.catchOnly[HL7Exception](p.parse(Data.msg0))
    pid = x.get("PID").asInstanceOf[PID]
    pv1 = x.get("PV1").asInstanceOf[PV1]
    y = pid.getPatientAddress.map(_.getZipOrPostalCode.getValueOrEmpty)
    z = pv1.getAssignedPatientLocation.getFacility.getUniversalID.getValueOrEmpty
  } yield (y.toList, List(z)).tupled

  val trainingData: List[PatientVisitFromTo] = cartesianPostalAndFacility match {
    case Right(l) =>
      l.map(a => PatientVisitFromTo(a._1, a._2))
    case Left(_) => List()
  }
}
