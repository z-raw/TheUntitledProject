package theproject.config

import pureconfig.ConfigReader
import pureconfig.generic.derivation.default.*

final case class PostgresConfig(nThreads: Int, driver: String, url: String, user: String, password: String) derives ConfigReader

