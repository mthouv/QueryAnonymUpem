import utils.QueryUtils.getTriplesFromQuery
import utils.Constants
import anonymization.Anonymization.{findCandidatesUnitaryPolicy, findCandidatesGeneral}


object Main extends App {


  val l = getTriplesFromQuery(Constants.privacyPolicies.head)

  l.foreach(println(_))

  println("***************************")

  val candidates = findCandidatesUnitaryPolicy(Constants.privacyPolicies.head, Constants.utilityPoliciesExample)
  candidates.foreach(println(_))


  println("*************************** General")

  val generalCandidates = findCandidatesGeneral(Constants.privacyPolicies, Constants.utilityPoliciesExample)

  generalCandidates.foreach(l => l.foreach(println(_)))
}

