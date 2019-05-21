package utils

import org.apache.jena.query.{Query, QueryFactory}

object Constants {

  val queryP1Str =
    "PREFIX vcard: <http://www.w3.org/2001/vcard-rdf/3.0#> " +
      "PREFIX tcl: <http://example.com> "+
      "SELECT ?ad " +
      "WHERE { " +
        "?u a tcl:User. " +
        "?u vcard:hasAddress ?ad.}"


  val queryP2Str =
    "PREFIX geo: <https://www.w3.org/2003/01/geo/wgs84_pos#> "+
    "PREFIX tcl: <http://example.com> " +
    "SELECT ?u ?lat ?long " +
    "WHERE { "+
        "?c a tcl:Journey. " +
        "?c tcl:user ?u. " +
        "?c geo:latitude ?lat. " +
        "?c geo:longitude ?long.}"


  val queryU1Str =
    "PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
    "PREFIX tcl: <http://example.com> " +
    "SELECT ?u ?age " +
    "WHERE { " +
        "?u a tcl:User. " +
        "?u foaf:age ?age.}"


  val queryU2Str =
    "PREFIX geo: <https://www.w3.org/2003/01/geo/wgs84_pos#> " +
    "PREFIX tcl: <http://example.com> " +
    "SELECT ?c ?lat ?long " +
    "WHERE { " +
        "?c a tcl:Journey." +
        "?c geo:latitude ?lat. " +
        "?c geo:longitude ?long.}"


  val privacyPolicies : List[Query] = List(
    QueryFactory.create(Constants.queryP1Str),
    QueryFactory.create(Constants.queryP2Str)
  )


  val utilityPoliciesExample : List[Query] = List(
    QueryFactory.create(Constants.queryU1Str),
    QueryFactory.create(Constants.queryU2Str)
  )

}
