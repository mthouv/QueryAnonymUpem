package anonymization

import org.apache.jena.query.Query
import scala.collection.mutable.ListBuffer

import utils.QueryUtils.{getTriplesFromQuery, getTriplesFromPolicy, checkSubject, checkObject,
  createDeleteTripleOperationStr, createDeleteSubjectOperationStr, createDeleteObjectOperationStr}

object Anonymization {

  //TODO Maybe change return type instead of list of strings
  def findCandidatesUnitaryPolicy(unitaryPrivacy: Query, utilityPolicies: List[Query]) : List[String] = {
    val result = ListBuffer[String]()
    val unitaryPrivacyTriples = getTriplesFromQuery(unitaryPrivacy)
    val utilityPolicyTriples = getTriplesFromPolicy(utilityPolicies)
    val projectedVars = unitaryPrivacy.getProjectVars

    unitaryPrivacyTriples.foreach(triple => {
      if (!utilityPolicyTriples.contains(triple)) {

        result.prepend(createDeleteTripleOperationStr(triple, unitaryPrivacyTriples))

        if (checkSubject(triple, unitaryPrivacyTriples) || projectedVars.contains(triple.getSubject)) {
          result.prepend(createDeleteSubjectOperationStr(triple, unitaryPrivacyTriples))
        }

        if (triple.getObject.isURI && checkObject(triple, unitaryPrivacyTriples) || projectedVars.contains(triple.getObject)) {
          result.prepend(createDeleteObjectOperationStr(triple, unitaryPrivacyTriples))
        }
      }
    })

    result.toList
  }



  def findCandidatesGeneral(privacyPolicy : List[Query], utilityPolicy : List[Query]) :  List[List[String]] = {

    val result = ListBuffer[List[String]]()

    privacyPolicy.foreach(p => {
      val ops: List[String] = findCandidatesUnitaryPolicy(p, utilityPolicy)
      ops match {
        case _ :: _ => result.prepend(ops)
      }
    })

    result.toList
  }


}
