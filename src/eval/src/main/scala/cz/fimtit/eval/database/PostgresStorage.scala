package cz.fimtit.eval.database

import java.sql.DriverManager
import cz.filmtit.core.model.TranslationPair


/**
 * @author Joachim Daiber
 *
 */

abstract class PostgresStorage {

  //Load the driver:
  classOf[org.postgresql.Driver]

  //Initialize connection
  val connection = DriverManager.getConnection("jdbc:postgresql://localhost/filmtit", "postgres", "postgres")

  var tableName = "sentences"

  def createTable(translationPairs: TraversableOnce[TranslationPair]) {

    connection.createStatement().execute("DROP TABLE IF EXISTS %s; CREATE TABLE %s (sentence VARCHAR(10000));".format(tableName, tableName))

    val insertStatement = connection.prepareStatement("INSERT INTO %s (sentence) VALUES (?)".format(tableName))

    println("Reading sentences...")
    translationPairs foreach { translationPair => {
      insertStatement.setString(1, translationPair.source)
      insertStatement.execute()
    }
    }

  }
}









