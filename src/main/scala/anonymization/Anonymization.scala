package anonymization

import org.apache.jena.query.Query
import org.apache.jena.sparql.core.{TriplePath, Var}

import scala.collection.mutable.ListBuffer
import utils.QueryUtils.{checkObject, checkSubject, createDeleteObjectOperationStr, createDeleteSubjectOperationStr, createDeleteTripleOperationStr, getTriplesFromPolicy, getTriplesFromQuery}

object Anonymization {

  import scala.collection.JavaConverters._


  /**
    * Checks if the subject ot a triple appears as the subject or object of another triple
    * @param triple the triple we are checking
    * @param triplePatterns the list of triples with which we will check our initial triple
    * @return true if the subject of the original triple appears as the subject or object of another triple in triplePatterns,
    *         false otherwise
    */
  def checkSubject(triple : TriplePath, triplePatterns : List[TriplePath]) : Boolean = {
    return triplePatterns.exists(
      pattern => pattern.getSubject == triple.getObject || (pattern.getSubject == triple.getSubject && pattern != triple))
  }

  /**
    * Checks if the object ot a triple appears as the subject or object of another triple
    * @param triple the triple we are checking
    * @param triplePatterns the list of triples with which we will check our initial triple
    * @return true if the object of the original triple appears as the subject or object of another triple in triplePatterns,
    *         false otherwise
    */
  def checkObject(triple : TriplePath, triplePatterns : List[TriplePath]) : Boolean = {
    return triplePatterns.exists(
      pattern => pattern.getObject == triple.getSubject || (pattern.getObject == triple.getObject && pattern != triple))
  }


  /**
    * Find the candidate anonymization operations for a unitary privacy policy
    * @param unitaryPrivacy the unitary privacy policy
    * @param utilityPolicies the utility polices
    * @return a list of possible anonymization operations (their string representation)
    */
  def findCandidatesUnitaryPolicy(unitaryPrivacy: Query, utilityPolicies: List[Query]) : List[String] = {
    val unitaryPrivacyTriples = getTriplesFromQuery(unitaryPrivacy)
    val utilityPolicyTriples = getTriplesFromPolicy(utilityPolicies)
    val projectedVars = unitaryPrivacy.getProjectVars

    findCandidatesUnitaryPolicyAux(unitaryPrivacyTriples, utilityPolicyTriples, projectedVars.asScala.toList, List[String]())
  }

  /**
    * Auxilary recursive function used to compute the candidate anonymization operations for a unitary privacy policy
    * @param privacyTriples the triples of a privacy policy
    * @param utilityTriples the triples of a utility policy
    * @param projectedVars the projected vars of the privacy policy
    * @param acc an accumulator to store the intermediary results
    * @return a list of possible anonymization operations (their string representation)
    */
  def findCandidatesUnitaryPolicyAux(privacyTriples: List[TriplePath], utilityTriples: List[TriplePath], projectedVars : List[Var],
                                     acc : List[String]) : List[String] = {
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

  /**
    * Computes the possible anonymization operations for a triple considering a list of privacy triples, a list of utility
    * triples et the projected vars of a privacy policy
    * @param triple the triple to anonymize
    * @param privacyTriples the triples of a privacy policy
    * @param utilityTriples the triples of utility policy
    * @param projectedVars the projected vars of a privacy policy
    * @return a list of possible anonymization operations (their string representation)
    */
  def findPossibleOperations(triple: TriplePath, privacyTriples : List[TriplePath], utilityTriples: List[TriplePath],
                             projectedVars : List[Var]) : Option[List[String]] = {

    if (utilityTriples.contains(triple)) None
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


  /**
    * Creates the String representation of a triple deletion query
    * @param triple the triple to be deleted
    * @param privacyTriples the triples of the privacy policy
    * @return the string representation of a deletion query
    */
  def createDeleteTripleOperationStr(triple : TriplePath, privacyTriples : List[TriplePath]) : String = {
    return "Delete " + triple.toString + " WHERE " + privacyTriples.toString().replace("ListBuffer", "")
  }


  /**
    * Creates the String representation of a subject deletion query
    * @param triple the triple whose subject will be deleted (replaced by a blank node)
    * @param privacyTriples the triples of the privacy policy
    * @return the string representation of a subject deletion query
    */
  def createDeleteSubjectOperationStr(triple : TriplePath, privacyTriples : List[TriplePath]) : String = {
    "Delete " + triple.toString + " INSERT {[], " + triple.getPredicate + ", " + triple.getObject + "} WHERE " + privacyTriples.toString().replace("ListBuffer", "")
  }


  /**
    * Creates the String representation of an object deletion query
    * @param triple the triple whose object will be deleted (replaced by a blank node)
    * @param privacyTriples the triples of the privacy policy
    * @return the string representation of an object deletion query
    */
  def createDeleteObjectOperationStr(triple : TriplePath, privacyTriples : List[TriplePath]) : String = {
    "Delete " + triple.toString +  " INSERT {" + triple.getSubject + ", " + triple.getPredicate + ", []} WHERE " + privacyTriples.toString().replace("ListBuffer", "")
  }


  /**
    * General version of findCandidatesUnitaryPolicy, finds the candidate anonymization operations for a
    * privacy policy containing one or multiple queries
    * @param privacyPolicy the privacy policy
    * @param utilityPolicy the utility policy
    * @return a list of possible anonymization operations (their string representation)
    */
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
