package utils

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
        }) {
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


  def checkSubject(triple : TriplePath, triplePatterns : ListBuffer[TriplePath]) : Boolean = {
    return triplePatterns.exists(
      pattern => pattern.getSubject == triple.getObject || (pattern.getSubject == triple.getSubject && pattern != triple))
  }


  def checkObject(triple : TriplePath, triplePatterns : ListBuffer[TriplePath]) : Boolean = {
    return triplePatterns.exists(
      pattern => pattern.getObject == triple.getSubject || (pattern.getObject == triple.getObject && pattern != triple))
  }



  def createDeleteTripleOperationStr(triple : TriplePath, privacyTriples : ListBuffer[TriplePath]) : String = {
    return "Delete " + triple.toString + " WHERE " + privacyTriples.toString().replace("ListBuffer", "")
  }


  def createDeleteSubjectOperationStr(triple : TriplePath, privacyTriples : ListBuffer[TriplePath]) : String = {
    "Delete " + triple.toString + " INSERT {[], " + triple.getPredicate + ", " + triple.getObject + "} WHERE " + privacyTriples.toString().replace("ListBuffer", "")
  }


  def createDeleteObjectOperationStr(triple : TriplePath, privacyTriples : ListBuffer[TriplePath]) : String = {
    "Delete " + triple.toString +  " INSERT {" + triple.getSubject + ", " + triple.getPredicate + ", []} WHERE " + privacyTriples.toString().replace("ListBuffer", "")
  }

}
