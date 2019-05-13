import utils.QueryUtils.getTriplesFromQuery
import utils.Constants
import anonymization.Anonymization.{findCandidatesGeneral, findCandidatesUnitaryPolicy}
import org.apache.jena.query.{QueryExecutionFactory, QueryFactory, QuerySolution, ResultSet}
import org.apache.jena.rdf.model.{InfModel, Model, ModelFactory}
import org.apache.jena.reasoner.ReasonerRegistry


import anatomization.Anatomization._


object Main extends App {


  val l = getTriplesFromQuery(Constants.privacyPolicies.head)

  l.foreach(println(_))

  println("***************************")

  val candidates = findCandidatesUnitaryPolicy(Constants.privacyPolicies.head, Constants.utilityPoliciesExample)
  candidates.foreach(println(_))


  println("*************************** General")

  val generalCandidates = findCandidatesGeneral(Constants.privacyPolicies, Constants.utilityPoliciesExample)

  generalCandidates.foreach(l => l.foreach(println(_)))

  println("********************************")


  val model : Model = ModelFactory.createOntologyModel()

  val input = "./resources/graph.ttl"

  model.read(input, "TTL")

  val OWLReasoner = ReasonerRegistry.getOWLReasoner
  OWLReasoner.bindSchema(model)

  val infModel : InfModel = ModelFactory.createInfModel(OWLReasoner, model)

  val x = model.listSubjects()

  println("AAAAA")


/*
  val queryString = "PREFIX family: <http://example.org/family#> " +
    "PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
    "SELECT ?s ?o WHERE { ?s family:hasChild ?o .}"
*/

  val queryString = "PREFIX family: <http://example.org/family#> " +
    "PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
    "SELECT ?o (COUNT(*) as ?count) " +
    "WHERE {?s family:hasParent ?o}" +
    "GROUP BY ?o"


  val query = QueryFactory.create(queryString)
  try {
    val qexec = QueryExecutionFactory.create(query, infModel)
    try {
      val results : ResultSet = qexec.execSelect

      while (results.hasNext) {
        val soln : QuerySolution = results.nextSolution
        val x = soln.getResource("o")
        val r = soln.get("count")
        println(x + "  ---  " + r)
      }
    } finally if (qexec != null) qexec.close()
  }


  println("BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB")


  val queryTest = createSelectAllTriplesQuery("http://example.org/family#hasParent", "http://example.org/data#Mary")
  println(queryTest)

  val list = execQuery(queryTest, infModel)
  list.foreach(qs => println(qs))


  println("-----------------------AGGREGATION")

  val queryAgg = createAggregationQuery("http://example.org/family#hasParent")
  println(queryAgg)

  val listAgg = execQuery(queryAgg, infModel)
  listAgg.foreach(qs => println(qs.get("sensitiveAttribute") + " -- " + qs.get("count")))



}

