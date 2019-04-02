package anonymization

import org.apache.jena.query.Query
import org.apache.jena.sparql.core.{TriplePath, Var}

import scala.collection.mutable.ListBuffer
import utils.QueryUtils.{checkObject, checkSubject, createDeleteObjectOperationStr, createDeleteSubjectOperationStr, createDeleteTripleOperationStr, getTriplesFromPolicy, getTriplesFromQuery}

object Anonymization {

  import scala.collection.JavaConverters._

  /*
  def findCandidatesUnitaryPolicy2(unitaryPrivacy: Query, utilityPolicies: List[Query]) : List[String] = {
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
  */

  def findCandidatesUnitaryPolicy(unitaryPrivacy: Query, utilityPolicies: List[Query]) : List[String] = {
    val unitaryPrivacyTriples = getTriplesFromQuery(unitaryPrivacy)
    val utilityPolicyTriples = getTriplesFromPolicy(utilityPolicies)
    val projectedVars = unitaryPrivacy.getProjectVars

    findCandidatesUnitaryPolicyAux(unitaryPrivacyTriples, utilityPolicyTriples, projectedVars.asScala.toList, List[String]())
  }


  def findCandidatesUnitaryPolicyAux(privacyTriples: List[TriplePath], utilityTriples: List[TriplePath], projectedVars : List[Var], acc : List[String]) : List[String] = {

    privacyTriples match {
      case triple :: next => {

        val possibleOperations = findPossibleOperations(triple, privacyTriples, utilityTriples, projectedVars)

        possibleOperations match {
          case None => findCandidatesUnitaryPolicyAux(next, utilityTriples, projectedVars, acc)
          case Some(ops) => findCandidatesUnitaryPolicyAux(next, utilityTriples, projectedVars, ops ::: acc)
        }
      }
      case Nil => acc
    }
  }



  def findPossibleOperations(triple: TriplePath, privacyTriples : List[TriplePath], utilityTriples: List[TriplePath], projectedVars : List[Var]) : Option[List[String]] = {

    if (utilityTriples.contains(triple)) {
      None
    }
    else {
      val isSubjectDeletionPossible = checkSubject(triple, privacyTriples) || projectedVars.contains(triple.getSubject)
      val isObjectDeletionPossible = triple.getObject.isURI && checkObject(triple, privacyTriples) || projectedVars.contains(triple.getObject)

      val ops =
        (isSubjectDeletionPossible, isObjectDeletionPossible) match {
          case (true, true) => List[String](createDeleteSubjectOperationStr(triple, privacyTriples), createDeleteObjectOperationStr(triple, privacyTriples))
          case (true, false) => List[String](createDeleteSubjectOperationStr(triple, privacyTriples))
          case (false, true) => List[String](createDeleteObjectOperationStr(triple, privacyTriples))
          case _ => List()
        }
      return Some(createDeleteTripleOperationStr(triple, privacyTriples) :: ops)
    }
  }

  

  def findCandidatesGeneral(privacyPolicy : List[Query], utilityPolicy : List[Query]) :  List[List[String]] = {
    def findCandidatesGeneralAux(privacyPolicy : List[Query], utilityPolicy : List[Query],  acc : List[List[String]]) : List[List[String]] = {
      privacyPolicy match {
        case  policy :: next => {
          val ops : List[String] = findCandidatesUnitaryPolicy(policy, utilityPolicy)
          if (ops.isEmpty)
            findCandidatesGeneralAux(next, utilityPolicy, acc)
          else
            findCandidatesGeneralAux(next, utilityPolicy, ops :: acc)
        }
        case Nil => acc
      }
    }

    findCandidatesGeneralAux(privacyPolicy, utilityPolicy, List[List[String]]())
  }
}
