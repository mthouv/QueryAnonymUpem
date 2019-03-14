import org.apache.jena.graph.Node
import org.apache.jena.query.{Query, QueryFactory}
import org.apache.jena.sparql.algebra.{Algebra, Op, OpVisitorBase}
import org.apache.jena.sparql.core.TriplePath
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.jena.sparql.syntax.ElementPathBlock
import org.apache.jena.sparql.syntax.ElementVisitorBase
import org.apache.jena.sparql.syntax.ElementWalker

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
  //query.setPrefix("xsd", "<http://www.w3.org/2001/XMLSchema#>")

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

  println("###############")

  // This will walk through all parts of the query
  ElementWalker.walk(query.getQueryPattern, new ElementVisitorBase() { // ...when it's a block of triples...
    override def visit(el: ElementPathBlock): Unit = { // ...go through all the triples...
      val triples = el.patternElts
      while ( {
        triples.hasNext
      }) { // ...and grab the subject
        val triple : TriplePath = triples.next

        val sub : Node = triple.getSubject
        val predicate : Node = triple.getPredicate
        val obj : Node = triple.getObject


        println(sub + " " + predicate + " " + obj)
        //subjects.prepend(triples.next)
      }
    }
  })
  //subjects.foreach(println(_))
}
