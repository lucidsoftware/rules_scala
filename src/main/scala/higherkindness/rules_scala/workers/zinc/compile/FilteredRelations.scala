package sbt.internal.inc

/**
 * Zinc's libraryClassName is a map from a library jar to class names. Problem is it's not deterministic. You only get a
 * single class in the map and it's not always the same class. Until that bug in Zinc is fixed, we're setting the
 * libraryClassName to an empty libraryClassName because empty is better than non-deterministic.
 *
 * This class is in the sbt.internal.inc package to get access to the MRelationsNameHashing class, which is private to
 * that package. Super duper hacky, but I wasn't able to find a better way to change the libraryClassName for the
 * relation.
 *
 * TODO: fix this bug in Zinc
 */
object FilteredRelations {
  def getFilteredRelations(relations: Relations): Relations = {
    val emptyRelations = Relations.empty

    relations
      .asInstanceOf[MRelationsNameHashing]
      .copy(
        libraryClassName = emptyRelations.libraryClassName,
      )
  }
}
