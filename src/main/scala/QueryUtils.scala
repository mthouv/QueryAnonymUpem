import org.apache.jena.query.Query
import org.apache.jena.sparql.core.TriplePath
import org.apache.jena.sparql.syntax.{ElementPathBlock, ElementVisitorBase, ElementWalker}

import scala.collection.mutable.ListBuffer

object QueryUtils {


  def getTriplesFromQuery(query: Query) : ListBuffer[TriplePath] =  {
    val res = ListBuffer[TriplePath]()
    ElementWalker.walk(query.getQueryPattern, new ElementVisitorBase() { // ...when it's a block of triples...
      override def visit(el: ElementPathBlock): Unit = { // ...go through all the triples...
        val triples = el.patternElts
        while ( {
          triples.hasNext
        }) { // ...and grab the subject
          res.prepend(triples.next)
        }
      }
    })

    return res
  }


  def getTriplesFromPolicy(policy : List[Query], acc : ListBuffer[TriplePath] = ListBuffer[TriplePath]()) : ListBuffer[TriplePath] =
    policy match {
      case q :: next => getTriplesFromPolicy(next, acc ++ getTriplesFromQuery(q))
      case Nil => acc
    }



  def presentInUtility(privacyTriple : TriplePath, utilityTriples : ListBuffer[TriplePath]) : Boolean = {
    return utilityTriples.contains(privacyTriple)
  }




  def checkSubject(triple : TriplePath, triplePatterns : ListBuffer[TriplePath]) : Boolean = {
    return triplePatterns.exists(
      pattern => pattern.getSubject == triple.getObject || (pattern.getSubject == triple.getSubject && pattern != triple))
  }


  def checkObject(triple : TriplePath, triplePatterns : ListBuffer[TriplePath]) : Boolean = {
    return triplePatterns.exists(
      pattern => pattern.getObject == triple.getSubject && (pattern.getObject == triple.getObject && pattern != triple))
  }



  //TODO Maybe change return type instead of list of strings
  def Algo1(unitaryPrivacy: Query, utilityPolicies: List[Query]) : ListBuffer[String] = {
    val result = ListBuffer[String]()
    val unitaryPrivacyTriples = getTriplesFromQuery(unitaryPrivacy)
    val utilityPolicyTriples = getTriplesFromPolicy(utilityPolicies)
    val projectedVars = unitaryPrivacy.getProjectVars

    unitaryPrivacyTriples.foreach(triple => {
      if (!presentInUtility(triple, utilityPolicyTriples)) {
        result.prepend("Delete " + triple.toString + " WHERE " + unitaryPrivacyTriples.toString().replace("ListBuffer", ""))

        if (checkSubject(triple, unitaryPrivacyTriples) || projectedVars.contains(triple.getSubject)) {
          result.prepend("Delete " + triple.toString + " INSERT {[], " + triple.getPredicate + ", " + triple.getObject + "} WHERE " + unitaryPrivacyTriples.toString().replace("ListBuffer", ""))
        }

        if (checkObject(triple, unitaryPrivacyTriples) || projectedVars.contains(triple.getObject)) {
          result.prepend("Delete " + triple.toString +  " INSERT {" + triple.getSubject + ", " + triple.getPredicate + ", []} WHERE " + unitaryPrivacyTriples.toString().replace("ListBuffer", ""))
        }
      }
    })

    result
  }

}
