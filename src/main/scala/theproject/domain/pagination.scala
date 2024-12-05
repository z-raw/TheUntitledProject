package theproject.domain

object pagination {

  final case class Pagination(limit: Int, offset: Int)
  
  object Pagination {
    
    val defaultPageLimit = 20
    
    def apply(maybeLimit: Option[Int], maybeOffset: Option[Int]): Pagination = 
      new Pagination(maybeLimit.getOrElse(defaultPageLimit), maybeOffset.getOrElse(0))
    
    def default: Pagination = new Pagination(defaultPageLimit, 0)
   
  }
}
