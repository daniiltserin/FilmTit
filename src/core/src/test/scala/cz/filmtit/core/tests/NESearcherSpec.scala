package cz.filmtit.core.tests
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.Spec
import cz.filmtit.share.{Language, TranslationPair}
import cz.filmtit.core.search.postgres.impl.NEStorage
import cz.filmtit.core.model.data.AnnotatedChunk
import cz.filmtit.core.Utils.chunkFromString
import cz.filmtit.core.{Configuration, Factory}
import java.io.File
import TestUtil.createTMWithDummyContent

/**
 * Test specification for [[cz.filmtit.core.model.TranslationPairSearcher]].
 *
 * @author Joachim Daiber
 */

@RunWith(classOf[JUnitRunner])
class NESearcherSpec extends Spec {

  val configuration = new Configuration(new File("configuration.xml"))
  val memory = createTMWithDummyContent(configuration)

  describe("A NE searcher") {
    it("should be able to restore the NE in the chunk") {

      val candidates = memory.firstBest("Thomas rode to Alabama", Language.EN, null)

      /* Since we found the results via NE matches, the corresponding NE
         annotations must be restorable from the database. */
      assert(
        candidates.exists(_.getChunkL1.asInstanceOf[AnnotatedChunk].toAnnotatedString() contains "<Person>" )
      )
    }

    it("should queryable by multiple threads at the same time") {

      //Query the same TM from 100 threads in parallel:
      (1 to 100).par foreach { _ =>
        memory.firstBest("Thomas rode to Alabama", Language.EN, null)
      }
    }

  }


}
