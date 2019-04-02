package utils

import org.apache.jena.query.{Query, QueryFactory}

object Constants {

  val queryP1Str = "PREFIX vcard: <http://www.w3.org/2001/vcard-rdf/3.0#>\nPREFIX tcl: <http://example.com>\nSELECT ?ad\nWHERE {\n\t?u\ta\ttcl:User.\n\t?u\tvcard:hasAddress\t?ad.\n}"

  val queryP2Str = "PREFIX geo: <https://www.w3.org/2003/01/geo/wgs84_pos#>\nPREFIX tcl: <http://example.com>\nSELECT ?u ?lat ?long\nWHERE {\n\t?c\ta\ttcl:Journey.\n\t?c\ttcl:user\t?u.\t\t\t\t\n\t?c\tgeo:latitude ?lat.\n\t?c\tgeo:longitude\t?long.\n}"

  val queryU1Str = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\nPREFIX tcl: <http://example.com>\nSELECT ?u ?age\nWHERE {\n    ?u\ta\ttcl:User.\n    ?u\tfoaf:age\t?age.\n}"

  val queryU2Str = "PREFIX geo: <https://www.w3.org/2003/01/geo/wgs84_pos#>\nPREFIX tcl: <http://example.com>\nSELECT ?c ?lat ?long\nWHERE {\n\t?c\ta\ttcl:Journey.\t\t\n\t?c\tgeo:latitude ?lat.\n\t?c\tgeo:longitude\t?long.\n}"


  val privacyPolicies : List[Query] = List(
    QueryFactory.create(Constants.queryP1Str),
    QueryFactory.create(Constants.queryP2Str)
  )


  val utilityPoliciesExample : List[Query] = List(
    QueryFactory.create(Constants.queryU1Str),
    QueryFactory.create(Constants.queryU2Str)
  )

}
