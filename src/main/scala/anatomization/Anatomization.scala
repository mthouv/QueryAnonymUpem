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


  def createSelectAllTriplesQuery(predicate: String, sensibleAttribute: String): Query = {
    val queryString =
      "SELECT * " +
      "WHERE { ?s ?p ?o ." +
      "FILTER(?p = <" + predicate + ">) ." +
      "FILTER(?o =  <" + sensibleAttribute + ">) }"
    QueryFactory.create(queryString)
  }


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

    /*
    val deleteClause = "DELETE { " + subject + " " + predicate + " " + obj + "}"
    val insertClause = "INSERT { " + subject + " " + URIInGroup + " " + URIGroupId + "}"
    val whereClause = "WHERE { " + subject + " " + predicate + " " + obj + "}"

    deleteClause + "\n" + insertClause + "\n" + whereClause
    */

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


  def anatomisationAlgoUnitary(predicate : String, model : Model, groupID : Int) : ListBuffer[String] = {

    val ops = ListBuffer[String]()
    val aggQueryResults = execQuery(createAggregationQuery(predicate), model)

    var attributeID = 0
    ops.append(createDeleteQueryString(predicate, groupID))
    aggQueryResults.foreach(aqr => {
      //val allTriplesQuery = createSelectAllTriplesQuery(predicate, aqr.get("sensitiveAttribute").toString)
      //val triplesResults = execQuery(allTriplesQuery, model)
      ops.append(createInsertQueryString(predicate, groupID, attributeID, aqr))
      attributeID += 1
    })
    ops
  }


}