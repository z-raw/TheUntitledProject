package theproject.core

import cats.effect.IO
import cats.effect.kernel.Resource
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import doobie.util.transactor.Transactor
import org.testcontainers.containers.PostgreSQLContainer

import scala.concurrent.ExecutionContext

trait DoobieSpec {

  val initScript: String // to be implemented by child Specs that interact with DB

  val postgres: Resource[IO, PostgreSQLContainer[Nothing]] = Resource.make {
    IO {
      val container: PostgreSQLContainer[Nothing] = new PostgreSQLContainer("postgres").withInitScript(initScript)
      container.start()
      container
    }
  } { container =>
    IO(container.stop())
  }
  
  val transactor : Resource[IO, Transactor[IO]] = for {
    container <- postgres
    ec <- ExecutionContexts.fixedThreadPool[IO](1)
    xa <- HikariTransactor.newHikariTransactor[IO](
      driverClassName = container.getDriverClassName,
      url = container.getJdbcUrl,
      user = container.getUsername,
      pass = container.getPassword,
      connectEC = ec
    )
  } yield xa
}
