object Main {
  private def printZooAnimals(animals: List[String]): Unit = println(s"The zoo has ${animals.mkString(",")}")

  def main(arguments: Array[String]): Unit = printZooAnimals(Seq("kangaroos", "giraffes"))
}
