package anatomization

import org.apache.jena.query.{Query, QueryExecutionFactory, QueryFactory, QuerySolution, ResultSet}
import org.apache.jena.rdf.model.Model

import scala.collection.mutable.ListBuffer


object Anatomization {

  def createAggregationQuery(predicate: String): Query = {
    val queryString =
      "SELECT ?sensitiveAttribute (COUNT(*) as ?count) " +
        "WHERE {?s ?p ?sensitiveAttribute . FILTER(?p = <" + predicate + ">). }" +
        "GROUP BY ?sensitiveAttribute"
    QueryFactory.create(queryString)
  }


  /** Utility method used to execute a query on top of a given model
    *
    * @param query the query to be executed
    * @param model the model on which the query will be executed
    * @return a list containing the solutions for the query
    */
  def execQuery(query: Query, model: Model): ListBuffer[QuerySolution] = {
    val solutionsList = ListBuffer[QuerySolution]()
    try {
      val qexec = QueryExecutionFactory.create(query, model)
      try {
        val results: ResultSet = qexec.execSelect

        while (results.hasNext) {
          solutionsList.append(results.next())
        }
      } finally if (qexec != null) qexec.close()
    }
    solutionsList
  }


  def createDeleteQueryString(predicate : String, groupID : Int) : String = {

    val URIInGroup = predicate + "/InGroup"
    val URIGroupId = s"Group${groupID}"

    s"""DELETE { ?s $predicate ?o }
       |INSERT { ?s $URIInGroup $URIGroupId }
       |WHERE { ?s $predicate ?o }""".stripMargin
  }


  def createInsertQueryString(predicate : String, groupID : Int, attributeID : Int, aggregationQueryResult : QuerySolution) : String = {
    val URIGroupID = s"Group${groupID}"
    val URIAttributeID = s"Attribute${attributeID}"

    s"""INSERT DATA {
    | $URIGroupID $predicate $URIAttributeID
    | $URIAttributeID http://anatomisation/value ${aggregationQueryResult.get("sensitiveAttribute")} .
    | $URIAttributeID http://anatomisation/cardinality " ${aggregationQueryResult.get("count")} .
    | }""".stripMargin
  }


  /**
    * Computes the list of operations to execute in order to apply the anatomisation algorithm
    * @param predicate the predicate which links a resource to a sensitive attribute
    * @param model the model we want to anonymize
    * @param groupID the starting group ID
    * @return a list of operations to execute in order to apply the anatomisation algorithm
    */
  def anatomisationAlgoUnitary(predicate : String, model : Model, groupID : Int) : ListBuffer[String] = {

    val ops = ListBuffer[String]()
    val aggQueryResults = execQuery(createAggregationQuery(predicate), model)

    var attributeID = 0
    ops.append(createDeleteQueryString(predicate, groupID))
    aggQueryResults.foreach(aqr => {
      ops.append(createInsertQueryString(predicate, groupID, attributeID, aqr))
      attributeID += 1
    })
    ops
  }


}