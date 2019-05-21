package utils

import org.apache.jena.query.Query
import org.apache.jena.sparql.core.TriplePath
import org.apache.jena.sparql.syntax.{ElementPathBlock, ElementVisitorBase, ElementWalker}

import scala.collection.mutable.ListBuffer

object QueryUtils {


  /**
    * Gets the triples contained inside a query
    * @param query a SPARQL query
    * @return a list containing the triples inside the query
    */
  def getTriplesFromQuery(query: Query) : List[TriplePath] =  {
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

    return res.toList
  }


  /**
    * Gets the triples contained inside a policy (which is a list of queries)
    * @param policy a policy ie a list of SPARQL queries
    * @param acc an accumulator to store intermediary triples
    * @return a list containing all the triples inside the policy
    */
  def getTriplesFromPolicy(policy : List[Query], acc : List[TriplePath] = List[TriplePath]()) : List[TriplePath] =
    policy match {
      case q :: next => getTriplesFromPolicy(next, acc ++ getTriplesFromQuery(q))
      case Nil => acc
    }


}
