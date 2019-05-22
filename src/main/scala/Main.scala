import utils.QueryUtils.getTriplesFromQuery
import utils.Constants
import anonymization.Anonymization.{findCandidatesGeneral, findCandidatesUnitaryPolicy}
import org.apache.jena.query.{QueryExecutionFactory, QueryFactory, QuerySolution, ResultSet}
import org.apache.jena.rdf.model.{InfModel, Model, ModelFactory}
import org.apache.jena.reasoner.ReasonerRegistry


import anatomization.Anatomization._


object Main extends App {

  println("***********Query-based Unitary privacy policy****************\n")

  val candidates = findCandidatesUnitaryPolicy(Constants.privacyPolicies.head, Constants.utilityPoliciesExample)
  candidates.foreach(println(_))


  println("\n***********General algorithm with both policies****************")

  val generalCandidates = findCandidatesGeneral(Constants.privacyPolicies, Constants.utilityPoliciesExample)

  generalCandidates.foreach(l => l.foreach(println(_)))

  println("\n\n***********Anatomisation approach****************")
  println("***********Predicate is <http://example.org/family#hasParent>****************\n")



  val model : Model = ModelFactory.createOntologyModel()

  val input = "./resources/graph.ttl"

  model.read(input, "TTL")

  val OWLReasoner = ReasonerRegistry.getOWLReasoner
  OWLReasoner.bindSchema(model)

  val infModel : InfModel = ModelFactory.createInfModel(OWLReasoner, model)

  val ll = anatomisationAlgoUnitary("http://example.org/family#hasChild", infModel, 0)

  ll.foreach(x => println(x+ "\n"))
}

