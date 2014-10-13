package littlepetstore

/**
 * Why does FreeUser fail to compile?
 */
object Sytax {

  object Thrice {
       def apply(x : Int) : Int = x *3
       def unapply(z : Int) : Option[Int] = if (z%3==0) Some(z/3) else None
  }

  val x = Thrice(3);

  trait User {
    def name:String;
    def id:Int;
  }

  //impl 1
  class FreeUser(val name: String, val id:Int) extends User {}
  object FreeUser {
    def unapply(user: FreeUser):Option[(String,Int)] = Some((user.name,user.id));
  }
  //impl 2
  class PremiumUser(val name: String, val id:Int) extends User {}
  object PremiumUser {
    def unapply(user: PremiumUser):Option[(String,Int)]= Some((user.name,user.id));
  }


  def hello(a:Int) = {
    1+1
  }
  object premiumCandidate {
    def unapply(user: FreeUser): Boolean = user.id> 100
  }

  val name = "ASDF"
  val user: User = new PremiumUser("jay",1);


  def main(args: Array[String]) {

    //now, we can use FreeUser / PremiumUser to do pattern matching against
    //a newly defined  user.  The user
    val xx = user match {
     // case u @ premiumCandidate => "hi, you could be premium!"
      case FreeUser(name) => "hi..." + name + " you are mediocre.  someday you can be premium.";
      case PremiumUser(name) => "Dear Premium User " + name + ". Hello . You are special !";
    }
    print(xx);

  }
}