import org.apache.jena.graph.Node
import org.apache.jena.query.{Query, QueryFactory}
import org.apache.jena.sparql.algebra.{Algebra, Op}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.jena.sparql.syntax.ElementPathBlock
import org.apache.jena.sparql.syntax.ElementVisitorBase
import org.apache.jena.sparql.syntax.ElementWalker
import QueryUtils.{Algo1, getTriplesFromPolicy, getTriplesFromQuery}
import org.apache.jena.sparql.core.TriplePath

import scala.collection.mutable.ListBuffer

object SparkRunner extends App {
  val conf = new SparkConf().setAppName("simpleSparkApp").setMaster("local")
  val sc = new SparkContext(conf)
  val rdd1 = sc.parallelize(Array(1, 2, 3, 4, 5)).map(x => x * 2)
  rdd1.foreach(println(_))


  val queryStr : String = """SELECT ?s {
                            |?s <http://purl.oclc.org/NET/ssnx/hasValue> ?o.
                            |?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?o1.
                            |filter (?o > 32)
                            |}""".stripMargin.replaceAll("\n", " ")

  val query : Query = QueryFactory.create(queryStr)


  println(queryStr)



  val op : Op = Algebra.compile(query)
  //println(op)

  /*
  op.visit(new OpVisitorBase() {
    override def visit(opVisitorBase: OpVisitorBase) : Unit {

    }
  })
  */

  val subjects = ListBuffer[TriplePath]()

  // This will walk through all parts of the query
  ElementWalker.walk(query.getQueryPattern, new ElementVisitorBase() { // ...when it's a block of triples...
    override def visit(el: ElementPathBlock): Unit = { // ...go through all the triples...
      val triples = el.patternElts
      while ( {
        triples.hasNext
      }) { // ...and grab the subject
        subjects.prepend(triples.next)
      }
    }
  })

  println("###############")

  //subjects.foreach(println(_))


  val queryP1Str = "PREFIX vcard: <http://www.w3.org/2001/vcard-rdf/3.0#>\nPREFIX tcl: <http://example.com>\nSELECT ?ad\nWHERE {\n\t?u\ta\ttcl:User.\n\t?u\tvcard:hasAddress\t?ad.\n}"
  val queryP2Str = "PREFIX geo: <https://www.w3.org/2003/01/geo/wgs84_pos#>\nPREFIX tcl: <http://example.com>\nSELECT ?u ?lat ?long\nWHERE {\n\t?c\ta\ttcl:Journey.\n\t?c\ttcl:user\t?u.\t\t\t\t\n\t?c\tgeo:latitude ?lat.\n\t?c\tgeo:longitude\t?long.\n}"
  val queryU1Str = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\nPREFIX tcl: <http://example.com>\nSELECT ?u ?age\nWHERE {\n    ?u\ta\ttcl:User.\n    ?u\tfoaf:age\t?age.\n}"
  val queryU2Str = "PREFIX geo: <https://www.w3.org/2003/01/geo/wgs84_pos#>\nPREFIX tcl: <http://example.com>\nSELECT ?c ?lat ?long\nWHERE {\n\t?c\ta\ttcl:Journey.\t\t\n\t?c\tgeo:latitude ?lat.\n\t?c\tgeo:longitude\t?long.\n}"

  val privacyPolicies : List[Query] = List(
    QueryFactory.create(queryP1Str),
    QueryFactory.create(queryP2Str)
  )

  val utilityPolicies : List[Query] = List(
    QueryFactory.create(queryU1Str),
    QueryFactory.create(queryU2Str)
  )




  val l = getTriplesFromQuery(privacyPolicies.head)

  l.foreach(println(_))

  println("***************************")

  val ope = Algo1(privacyPolicies.head, utilityPolicies)
  print(ope)


  val projVars = query.getProjectVars()
  val isIn = projVars.contains(subjects(0).getSubject)

  println("\n " + isIn)

}

